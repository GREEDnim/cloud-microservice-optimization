package MOPSO;

import net.sourceforge.jswarm_pso.Particle;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import utils.ChaosStrategy;
import utils.Constants;

import java.util.List;
import java.util.Random;

public class SchedulerParticle extends Particle {


    /** 粒子活跃判断计数*/
    int count;



    public SchedulerParticle() {
        super(Constants.NO_OF_TASKS);
        double[] position = new double[Constants.NO_OF_TASKS];
        double[] velocity = new double[Constants.NO_OF_TASKS];

        for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
            Random randObj = new Random();
            position[i] = randObj.nextInt(Constants.NO_OF_VMS);
            velocity[i] = Math.random();
        }
        setPosition(position);
        setVelocity(velocity);
        count=0;
    }

    public int Position(int i){
        int result = 0;
        for (int j = 0; j < Constants.NO_OF_TASKS; j++) {
            result = (int) getPosition()[j];
        }
        return result;
    }


    @Override
    public String toString() {
        //double mapping[] = MOPSO_Scheduler.mapping;
        double[][] commMatrix = MOPSO_Scheduler.commMatrix;
        double[][] execMatrix = MOPSO_Scheduler.execMatrix;
        String output = "";
        for (int i = 0; i < Constants.NO_OF_VMS; i++) {
            String tasks = "";
            double totalcloudletLength = 0.0;
            int no_of_tasks = 0;
            for (int j = 0; j < Constants.NO_OF_TASKS; j++) {
                if (i == (int) getPosition()[j]) {
                    long length1 = (long)(10*(commMatrix[j][i])+ 1e3*(execMatrix[j][i]));
                    totalcloudletLength += length1;
                    tasks += (tasks.isEmpty() ? "" : " ") + j;
                    ++no_of_tasks;
                }
            }

            if (tasks.isEmpty()) {output += "There is no tasks associated to VM " + i + "\n";}
            else {
                output += "There are " + no_of_tasks + " tasks associated to VM " + i + " totalcloudletLength " + totalcloudletLength + " and they are " + tasks + "\n";
            }
            if((i+1)%3==0){
                output += "\n"+ "================ 分割线 =================="+"\n";
            }
        }
        return output;
    }

    /**
     * 启动混沌扰动策略--
     */
    @Override
    public void InitMutation()
    {
        //变异策略部分。
        double temp = this.getFitness()-this.getBestFitness();
        double[] Position = MOPSO.Position;
        double[] Velocity = MOPSO.Velocity;

        //(4/Constants.NO_OF_TASKS)  -(1/Constants.NO_OF_TASKS)
        if(Math.abs(temp)<0.04 )
        {
            count++;
            //(int)(1000/Constants.NO_OF_TASKS) + (int)(1000/Constants.NO_OF_TASKS)
            //900----4--
            //1000--8
            if(count>=8)//粒子连续10次判断都不活跃 进入变异环节
            {
                count=0;
                //启动变异策略
                System.out.println("go particle mutation!-粒子超出边界"+10+"次-启动变异策略");
                ChaosStrategy instance = ChaosStrategy.getInstance();
                instance.CalChaos();

                double[] new_vel = new double[Constants.NO_OF_TASKS];
                double[] new_pos = new double[Constants.NO_OF_TASKS];
                for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
                    //混沌映射方式生成变异粒子的速度和位置
                    //new_pos[i] = instance.PLM(1,instance.getChaosValue())*Constants.NO_OF_VMS;
                    new_pos[i] = instance.PTent(instance.getChaosValue())*Constants.NO_OF_VMS;
                    new_vel[i] = instance.LM(1,instance.getChaosValue());
                    //new_pos[i] = Position[i];
                    //new_vel[i] = Velocity[i];
                }
                setPosition(new_pos);
                setVelocity(new_vel);
            }
        }
    }

    /**
     * 启动混沌扰动策略--基于Logistic映射产生-Tent映射
     */
    public void MutationStrategy()
    {
        //变异策略部分。
        double temp = this.getFitness()-this.getBestFitness();
        //for (int i=0;i<Position.length;i++){
        //System.out.println("粒子"+ i +"Position："+ Position[i]);
        //}
        if(Math.abs(temp)<0.005)
        {
            count++;
            if(count>=10)//粒子连续5次判断都不活跃 进入变异环节
            {
                count=0;
                //启动变异策略
                System.out.println("go particle mutation!-粒子超出边界-启动变异策略");

                double[] new_vel = new double[Constants.NO_OF_TASKS];
                double[] new_pos = new double[Constants.NO_OF_TASKS];
                for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
                    //混沌映射方式生成变异粒子的速度和位置
                    //new_pos[i] = instance.PLM(1,instance.getChaosValue())*Constants.NO_OF_VMS;
                    //new_pos[i] = Position[i];
                    //new_vel[i] = Velocity[i];
                }
                setPosition(new_pos);
                setVelocity(new_vel);
            }
        }
    }
}
