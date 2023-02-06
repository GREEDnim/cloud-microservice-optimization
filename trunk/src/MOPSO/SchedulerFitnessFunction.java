package MOPSO;

import net.sourceforge.jswarm_pso.FitnessFunction;
import utils.Constants;
import utils.GenerateMatrices;
import utils.VmType;


public class SchedulerFitnessFunction extends FitnessFunction {
    private static double[][] execMatrix, commMatrix;
    SchedulerParticle  Particle;

    SchedulerFitnessFunction() {
        super(false);
        commMatrix = GenerateMatrices.getCommMatrix();
        execMatrix = GenerateMatrices.getExecMatrix();
    }

    /**
     * 多目标加权
     * @param position : Particle's position
     * @return
     */
    @Override
    public double evaluate(double[] position) {
        double alpha = 0.3;
        return alpha * calcTotalTime(position) + (1 - alpha) * calcMakespan(position) ;
        //return calcMakespan(position);
        //return calcTotalTime(position);
    }

    //任务完成成本
    public double calcTotalTime(double[] position) {
        double totalCost = 0;
        for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
            int dcId = (int) position[i];
            totalCost += execMatrix[i][dcId] + commMatrix[i][dcId];
        }
        return totalCost;
    }
    //虚拟机负载
    public double loadBlancing2(double[] position) {
        double vmTotalAbility = 0;
        double vmLoad = 0;
        String output = "";

        for (int i = 0; i < Constants.NO_OF_VMS; i++) {
            String tasks = "";
            int no_of_tasks = 0;
            for (int j = 0; j < Constants.NO_OF_TASKS; j++) {
                if (i == Particle.Position(i)) {
                    tasks += (tasks.isEmpty() ? "" : " ") + j;
                    ++no_of_tasks;
                }
            }
            if (!tasks.isEmpty()) output += "There are " + no_of_tasks + " tasks associated to VM " + i + " and they are " + tasks + "\n";;

        }

        for (int i = 0; i < Constants.NO_OF_VMS; i++) {
            int dcId = (int) position[i];
            vmLoad += execMatrix[i][dcId] + commMatrix[i][dcId];
        }

        for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
            int dcId = (int) position[i];
            vmLoad += execMatrix[i][dcId] + commMatrix[i][dcId];
        }
        return vmLoad/vmTotalAbility;
    }

    /**
     * 计算每次成本-估计虚拟机类型
     * @param position
     * @return
     */
    public double calcCostPerTime(double[] position){
        double totalCost = 0;
        for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
            int vmId = (int) position[i];
            //totalCost += execMatrix[i][vmId]*VmType.type[vmId][Constants.cost];
            totalCost += execMatrix[i][vmId];
        }
        return totalCost/10000.0;
    }

    //最大完成时间
    public double calcMakespan(double[] position) {
        double makespan = 0;
        double[] dcWorkingTime = new double[Constants.NO_OF_VMS];

        for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
            int dcId = (int) position[i];
            if(dcWorkingTime[dcId] != 0) --dcWorkingTime[dcId];
            dcWorkingTime[dcId] += execMatrix[i][dcId] + commMatrix[i][dcId];
            makespan = Math.max(makespan, dcWorkingTime[dcId]);
        }
        return makespan;
    }

    public  double calcEC(double[] position)
    {
        double Consumption = 0.0;
        for(int i =0;i<Constants.NO_OF_TASKS;i++)
        {
            int dcId = (int) position[i];
            Consumption += execMatrix[i][dcId];

        }
        return Consumption;
    }
    public  double calcLoadCost(double[] position) {
        double utilization = 0.0;
        for(int i =0;i<Constants.NO_OF_TASKS;i++)
        {
            int dcId = (int) position[i];
            utilization += execMatrix[i][dcId];

        }
        return utilization;
    }
}
