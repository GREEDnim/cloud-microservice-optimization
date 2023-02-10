package MOPSO;

import net.sourceforge.jswarm_pso.FitnessFunction;
import org.cloudbus.cloudsim.Log;
import utils.Calculator;
import utils.Constants;
import utils.GenerateMatrices;
import utils.VmType;
import org.cloudbus.cloudsim.lists.VmList;




public class SchedulerFitnessFunction extends FitnessFunction {
    private static double[][]

            execMatrix, commMatrix;
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
        return alpha * loadBalance(position)*100+ (1 - alpha) * calcMakespan(position) ;
        //return calcMakespan(position);
        //return calcTotalTime(position);
    }

    //任务完成总时间
    public double calcTotalTime(double[] position) {
        double totalCost = 0;
        for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
            int dcId = (int) position[i];
            totalCost += execMatrix[i][dcId] + commMatrix[i][dcId];
        }
        return totalCost;
    }
    //虚拟机负载

    /**
     * 计算当前虚拟机负载情况 ： 已利用的容量： L/（pe*mips）+bw
     * 计算虚拟机负载（时间）的标准差，目标是每个虚拟机的标准差越小，每个虚拟机的执行时间越均衡
     * @param position
     * @return
     */
    public double loadBalance(double[] position) {
        double[] vmSerTime = new double[Constants.NO_OF_VMS];
        double[] vmLoad = new double[Constants.NO_OF_VMS];
        double[] vmAbility = new double[Constants.NO_OF_VMS];
        double vmTotalTime = 0.0;

        for (int i = 0; i < Constants.NO_OF_VMS; i++) {
            if(i<10){
                vmAbility[i] = 2000;
            }
            else if(i<20 && i>=10){
                vmAbility[i] = 1000;
            }
            else if(i<Constants.NO_OF_VMS && i>=20){
                vmAbility[i] = 500;
            }
            for (int j = 0; j < Constants.NO_OF_TASKS; j++) {
                if (i == (int)position[j]){
                    vmLoad[i] += execMatrix[j][i];
                }
            }
        }
        for (int i = 0; i < Constants.NO_OF_VMS; i++) {
            vmSerTime[i] = Calculator.div(vmLoad[i],vmAbility[i]);
            vmTotalTime += vmSerTime[i];
        }
        double avgSerTime = Calculator.div(vmTotalTime,Constants.NO_OF_VMS);
        double sum = 0.00000;
        for (double num : vmSerTime) {
            sum += (num - avgSerTime) * (num - avgSerTime);
        }
        //loadLevel = Math.sqrt(sum / vmSerTime.length);
        return  Calculator.div(sum,Constants.NO_OF_VMS)+0.001;

    }

    public double[] vmSerTime(double[] position) {
        double[] vmSerTime = new double[Constants.NO_OF_VMS];
        double[] vmLoad = new double[Constants.NO_OF_VMS];
        double[] vmAbility = new double[Constants.NO_OF_VMS];
        for (int i = 0; i < Constants.NO_OF_VMS; i++) {
            if(i<10){
                vmAbility[i] = 2000;
            }
            else if(i<20 && i>=10){
                vmAbility[i] = 1000;
            }
            else if(i<Constants.NO_OF_VMS && i>=20){
                vmAbility[i] = 500;
            }
            for (int j = 0; j < Constants.NO_OF_TASKS; j++) {
                if ( i== (int)position[j]){
                    vmLoad[i] += execMatrix[j][i];
                }
            }
        }
        for (int i = 0; i < Constants.NO_OF_VMS; i++) {
            vmSerTime[i] = Calculator.div(vmLoad[i],vmAbility[i]) ;
        }
        return  vmSerTime;
    }


    public double[] vmSerTime1(double[] position) {
        double[] vmSerTime = new double[Constants.NO_OF_VMS];
        double[] vmLoad = new double[Constants.NO_OF_VMS];
        double[] vmAbility = new double[Constants.NO_OF_VMS];

        //计算虚拟机消耗
        for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
            int dcId = (int)position[i];
            if(vmSerTime[dcId] != 0) --vmSerTime[dcId];
            vmLoad[i] += execMatrix[i][dcId];
        }
        //计算虚拟机容量
        for (int i = 0; i < Constants.NO_OF_VMS; i++) {
            if(i<10){
                vmAbility[i] = 2000;
            }
            else if(i<20 && i>=10){
                vmAbility[i] = 1000;
            }
            else if(i<Constants.NO_OF_VMS && i>=20){
                vmAbility[i] = 500;
            }
        }
        //计算虚拟机服务时间
        for (int i = 0; i < Constants.NO_OF_VMS; i++) {
            vmSerTime[i] = Calculator.div(vmLoad[i],vmAbility[i]) ;
        }
        return  vmSerTime;
    }

    public double loadBalance1(double[] position) {
        double[] vmSerTime = new double[Constants.NO_OF_VMS];
        double[] vmLoad = new double[Constants.NO_OF_VMS];
        double[] vmAbility = new double[Constants.NO_OF_VMS];
        double vmTotalTime = 0.0;

        //计算虚拟机消耗
        for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
            int dcId = (int)position[i];
            if(vmSerTime[dcId] != 0) --vmSerTime[dcId];
            vmLoad[i] += execMatrix[i][dcId];
        }
        //计算虚拟机容量
        for (int i = 0; i < Constants.NO_OF_VMS; i++) {
            if(i<10){
                vmAbility[i] = 2000;
            }
            else if(i<20 && i>=10){
                vmAbility[i] = 1000;
            }
            else if(i<Constants.NO_OF_VMS && i>=20){
                vmAbility[i] = 500;
            }
        }
        //计算虚拟机服务时间
        for (int i = 0; i < Constants.NO_OF_VMS; i++) {
            vmSerTime[i] = Calculator.div(vmLoad[i],vmAbility[i]);
            vmTotalTime += vmSerTime[i] ;
        }
        double avgSerTime = Calculator.div(vmTotalTime,Constants.NO_OF_VMS);
        double sum = 0.00000;
        for (double num : vmSerTime) {
            sum += (num - avgSerTime) * (num - avgSerTime);
        }
        //loadLevel = Math.sqrt(sum / vmSerTime.length);
        return  Calculator.div(sum,Constants.NO_OF_VMS)+0.001;
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
        double LoadCost = 0.0;

        for(int i =0;i<Constants.NO_OF_TASKS;i++)
        {
            int dcId = (int) position[i];
            if(dcId>=0 && dcId<Constants.NO_OF_VMS*0.4){
                LoadCost += execMatrix[i][dcId]*VmType.Type1.cost;
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
}
