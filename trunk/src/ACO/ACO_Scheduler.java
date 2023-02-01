package ACO;

import PSO.PSODatacenterBroker;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import utils.Constants;
import utils.DatacenterCreator;
import utils.GenerateMatrices;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;

public class ACO_Scheduler
{
    private static List<Cloudlet> cloudletList = new LinkedList<>();
    private static List<Vm> vmList;
    private static Datacenter[] datacenter;
    //private static Datacenter datacenter;
    private static ACO ACOSchedularInstance;
    private static double mapping[];
    private static double[][] commMatrix;
    private static double[][] execMatrix;

    public static void main(String[] args)
    {
        Log.printLine("Starting ACO Scheduler...");

//        new GenerateMatrices();
//        commMatrix = GenerateMatrices.getCommMatrix();
//        execMatrix = GenerateMatrices.getExecMatrix();
//        ACOSchedularInstance = new ACO();
//        mapping = ACOSchedularInstance.run();
        GenerateMatrices GM = new GenerateMatrices(vmList);
        commMatrix = GM.getcommMatrix();
        execMatrix = GM.getexecMatrix();

        try {
            String filePath = "cloudlets1.txt";
            int num_user = 1;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenter
            //datacenter = DatacenterCreator.createDatacenter("DataCenter_"+1, Constants.NO_OF_VMS);
            datacenter = new Datacenter[Constants.NO_OF_DATA_CENTERS];
            //管理-数据中心
            datacenter[0] = DatacenterCreator.createDatacenter("Datacenter_manage",1,1);
            //设计-数据中心
            for (int i = 1; i < 3; i++) {
                datacenter[i] = DatacenterCreator.createDatacenter("Datacenter_" + i,1,2);
            }
            //施工-数据中心
            for (int i = 3; i < 6; i++) {
                datacenter[i] = DatacenterCreator.createDatacenter("Datacenter_" + i,1,3);
            }

            //Third step: Create Broker
            ACODatacenterBroker broker = createBroker("Broker_0");
            int brokerId = broker.getId();

            //Fourth step: Create VMs and Cloudlets and send them to broker
            vmList = createVM(brokerId, Constants.NO_OF_VMS);
//            cloudletList = createCloudlet(brokerId, Constants.NO_OF_TASKS, 0);
            //createTasks(brokerId,filePath,Constants.NO_OF_TASKS);
            cloudletList = createCloudlet(brokerId, Constants.NO_OF_TASKS, 0);

            broker.submitVmList(vmList);
//            broker.setMapping(mapping);
            broker.submitCloudletList(cloudletList);

            broker.RunACO(5, 2);
            // Fifth step: Starts the simulation
            CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

//            printCloudletList(newList);
            PrintResults(newList);
            Log.printLine(ACO_Scheduler.class.getName() + " finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }

    }
    private static List<Vm> createVM(int userId, int vms) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> list = new LinkedList<Vm>();

        //VM Parameters1  适用于计算密集-计算大 （0.4个）
        long size1 = 10000; //image size (MB)
        int ram1 = 1024; //vm memory (MB)
        int mips1 = 1500;
        long bw1 = 1000;// VM带宽（mbps）
        int pesNumber1 = 2; //number of cpus
        String vmm1 = "Xen"; //VMM name

        //VM Parameters2  适用于数据密集-内存大（0.4个）
        long size2 = 20000; //image size (MB)
        int ram2 = 2048; //vm memory (MB)
        int mips2 = 1000;
        long bw2 = 1000;// VM带宽（mbps）
        int pesNumber2 = 1; //number of cpus
        String vmm2 = "Xen"; //VMM name

        //VM Parameters1  跨数据中心--带宽大 (0.2个)
        long size3 = 10000; //image size (MB)
        int ram3 = 1024; //vm memory (MB)
        int mips3 = 1000;
        long bw3 = 2000;// VM带宽（mbps）
        int pesNumber3 = 1; //number of cpus
        String vmm3 = "Xen"; //VMM name


        //create VMs
        Vm[] vm = new Vm[vms];

        for (int i = 0; i < vms*0.4; i++) {
            //CloudletSchedulerDynamicWorkload:动态调整分配的时间，提高整体性能和效率。考虑到进度和截止任务时间
            vm[i] = new Vm(i, userId, mips1, pesNumber1, ram1, bw1, size1, vmm1, new CloudletSchedulerDynamicWorkload(mips1,pesNumber1));
            list.add(vm[i]);
        }
        for (int i = (int)(vms*0.4); i < (int)(vms*0.8); i++) {
            vm[i] = new Vm(i, userId, mips2, pesNumber2, ram2, bw2, size2, vmm2, new CloudletSchedulerTimeShared());
            list.add(vm[i]);
        }
        for (int i = (int)(vms*0.8); i < vms; i++) {
            vm[i] = new Vm(i, userId, mips3, pesNumber3, ram3, bw3, size3, vmm3, new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }

        return list;
    }
    private static List<Vm> createVMq(int userId, int vms) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> list = new LinkedList<Vm>();

        //VM Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        int mips = 500;
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create VMs
        Vm[] vm = new Vm[vms];

        for (int i = 0; i < vms; i++) {
            vm[i] = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }

        return list;
    }

    protected static void createTasks(int brokerId,String filePath, int taskNum) {
        try
        {
            @SuppressWarnings("resource")
            BufferedReader br= new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            String data = null;
            int index = 0;

            //cloudlet properties.
            int pesNumber = 1;
            long fileSize = 1000;
            long outputSize = 1000;
            UtilizationModel utilizationModel = new UtilizationModelFull();

            while ((data = br.readLine()) != null)
            {
                System.out.println(data);
                String[] taskLength=data.split("\t");//tasklength[i]是任务执行的耗费（指令数量）
                for(int j=0;j<20;j++){
                    Cloudlet task=new Cloudlet(index+j, (long) Double.parseDouble(taskLength[j]), pesNumber, fileSize,
                            outputSize, utilizationModel, utilizationModel,
                            utilizationModel);
                    task.setUserId(brokerId);
                    cloudletList.add(task);
                    if(cloudletList.size()==taskNum)
                    {
                        br.close();
                        return;
                    }
                }
                //20 cloudlets each line in the file cloudlets.txt.
                index+=20;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    private static List<Cloudlet> createCloudlet(int userId,int cloudlets,int idShift) {
        LinkedList<Cloudlet> letList = new LinkedList<Cloudlet>();

        //cloudlet1 parameters ：数据密集任务参数
        long fileSize1 = 500;
        long outputSize1 = 500;
        int pesNumber1 = 1;

        //cloudlet2 parameters ：计算密集性任务参数
        long fileSize2 = 200;
        long outputSize2 = 200;
        int pesNumber2 = 2;

        //cloudlet3 parameters ：跨数据中心任务参数
        long fileSize3 = 300;
        long outputSize3 = 300;
        int pesNumber3 = 1;
        int magnification = 3;

        UtilizationModel utilizationModel = new UtilizationModelFull();
        Cloudlet[] cloudlet = new Cloudlet[cloudlets];
        //在这里创建多种任务-数据密集任务
        for (int i = 0; i < cloudlets*0.6 ; i++) {
            Random rd = new Random();
            int dcId = rd.nextInt(Constants.NO_OF_VMS);
            long length1 = (long) ((commMatrix[i][dcId] + execMatrix[i][dcId]));
            //long length = (long) (1e3*execMatrix[i][dcId]);
            cloudlet[i] = new Cloudlet(idShift + i, length1, pesNumber1, fileSize1, outputSize1, utilizationModel, utilizationModel, utilizationModel);
            cloudlet[i].setUserId(userId);
            letList.add(cloudlet[i]);
        }
        for (int i = (int)((int)cloudlets*0.6); i < cloudlets*0.9; i++) {
            Random rd = new Random();
            int dcId = rd.nextInt(Constants.NO_OF_VMS);
            long length2 = (long) ((commMatrix[i][dcId] + execMatrix[i][dcId]));
            //long length = (long) (1e3*execMatrix[i][dcId]);
            cloudlet[i] = new Cloudlet(idShift + i, length2, pesNumber2, fileSize2, outputSize2, utilizationModel, utilizationModel, utilizationModel);
            cloudlet[i].setUserId(userId);
            letList.add(cloudlet[i]);
        }
        for (int i = (int)((int)cloudlets*0.9); i < cloudlets; i++) {
            Random rd = new Random();
            int dcId = rd.nextInt(Constants.NO_OF_VMS);
            long length3 = (long) ((commMatrix[i][dcId] + execMatrix[i][dcId]));
            //long length = (long) (1e3*execMatrix[i][dcId]);
            cloudlet[i] = new Cloudlet(idShift + i, length3, pesNumber3, fileSize3, outputSize3, utilizationModel, utilizationModel, utilizationModel);
            cloudlet[i].setUserId(userId);
            letList.add(cloudlet[i]);
        }

        return letList;
    }

    private static ACODatacenterBroker createBroker(String name) throws Exception {
        return new ACODatacenterBroker(name);
    }

    private static double PrintResults(List<Cloudlet> list)
    {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("================ Execution Result ==================");
        Log.printLine("No."+indent +"Cloudlet ID" + indent + "STATUS" + indent
                + "Data center ID" + indent + "VM ID" + indent+"VM mips"+ indent +"CloudletLength"+indent+ "Time"
                + indent + "Start Time" + indent + "Finish Time");
        double mxFinishTime = 0;
        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++)
        {
            cloudlet = list.get(i);
            Log.print(i+1+indent+indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus()== Cloudlet.SUCCESS)
            {
                Log.print("SUCCESS");

                Log.printLine(indent +indent + indent + cloudlet.getResourceId()
                        + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent + getVmById(cloudlet.getVmId()).getMips()
                        + indent + indent + cloudlet.getCloudletLength()
                        + indent + indent+ indent + indent
                        + dft.format(cloudlet.getActualCPUTime()) + indent
                        + indent + dft.format(cloudlet.getExecStartTime())
                        + indent + indent
                        + dft.format(cloudlet.getFinishTime()));
            }
            mxFinishTime = Math.max(mxFinishTime, cloudlet.getFinishTime());
        }
        Log.printLine("================ Execution Result Ends here ==================");
        Log.printLine(mxFinishTime);
        return mxFinishTime;
    }

    public static Vm getVmById(int vmId)
    {
        for(Vm v:vmList)
        {
            if(v.getId()==vmId)
                return v;
        }
        return null;
    }
}
