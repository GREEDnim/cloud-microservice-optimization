package PSO;

import net.sourceforge.jswarm_pso.FitnessFunction;
import utils.Calculator;
import utils.Constants;
import utils.GenerateMatrices;
import utils.VmType;

public class SchedulerFitnessFunction extends FitnessFunction{
    private static double[][] execMatrix, commMatrix;

    SchedulerFitnessFunction() {
        super(false);
        commMatrix = GenerateMatrices.getCommMatrix();
        execMatrix = GenerateMatrices.getExecMatrix();
    }

    @Override
    public double evaluate(double[] position) {
        double alpha = 0.0;
        return alpha*loadBalance(position)*100 + (1 - alpha) * calcMakespan(position);
        //return calcMakespan(position);
    }

    public double calcTotalTime(double[] position) {
        double totalCost = 0;
        for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
            int dcId = (int) position[i];
            totalCost += execMatrix[i][dcId] + commMatrix[i][dcId];
        }

        return totalCost;
    }

    //每个虚拟机最大完成时间
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

    /**
     * 计算加载代价
     * @param position
     * @return
     */
    public  double calcLoadCost(double[] position) {
        double LoadCost = 0.0;
        for(int i =0;i<Constants.NO_OF_TASKS;i++)
        {
            int dcId = (int) position[i];
            if(dcId>=0 && dcId<Constants.NO_OF_VMS*0.4){
                LoadCost += execMatrix[i][dcId]* VmType.Type1.cost;
            }
            else if(dcId>=Constants.NO_OF_VMS*0.4 && dcId<Constants.NO_OF_VMS*0.8){
                LoadCost += execMatrix[i][dcId]*VmType.Type2.cost;
            }
            else if(dcId>=Constants.NO_OF_VMS*0.8 && dcId<Constants.NO_OF_VMS){
                LoadCost += execMatrix[i][dcId]*VmType.Type3.cost;
            }

        }
        return LoadCost;
    }


    public double loadBalance(double[] position) {
        double[] vmSerTime = new double[Constants.NO_OF_VMS];
        double vmTotalTime = 0.0;

        for (int i = 0; i < Constants.NO_OF_VMS; i++) {
            for (int j = 0; j < Constants.NO_OF_TASKS; j++) {
                if (i == (int)position[j]){
                    vmSerTime[i] += execMatrix[j][i];
                }
            }
        }
        for (int i = 0; i < Constants.NO_OF_VMS; i++) {
            vmTotalTime += vmSerTime[i];
        }
        double avgSerTime = Calculator.div(vmTotalTime,Constants.NO_OF_VMS);
        double sum = 0.00000;
        for (double num : vmSerTime) {
            sum += (num - avgSerTime) * (num - avgSerTime);
        }
        //loadLevel = Math.sqrt(sum / vmSerTime.length);
        return  Calculator.div(sum,Constants.NO_OF_VMS);
    }

    public double[] vmSerTime(double[] position) {
        double[] vmSerTime = new double[Constants.NO_OF_VMS];
        for (int i = 0; i < Constants.NO_OF_VMS; i++) {
            for (int j = 0; j < Constants.NO_OF_TASKS; j++) {
                if (i == (int)position[j]){
                    vmSerTime[i] += execMatrix[j][i];
                }
            }
        }
        return  vmSerTime;
    }

}
