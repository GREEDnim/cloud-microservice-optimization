package MOPSO;


import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import utils.Constants;
import utils.DatacenterCreator;
import utils.GenerateMatrices;
import utils.VmType;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;

public class MOPSO_Scheduler {


    private static List<Cloudlet> cloudletList = new LinkedList<>();

    private static List<Vm> vmList;

    private static Datacenter[] datacenter;
    //private static Datacenter datacenter;
    private static MOPSO SAWPSOSchedularInstance;

    private static double mapping[];

    private static double[][] commMatrix;
    private static double[][] execMatrix;



    private static List<Vm> createVM(int userId, int vms) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> list = new LinkedList<Vm>();

        //VM Parameters1  适用于计算密集 10个
        long size1 = 10000; //image size (MB)
        int ram1 = 512; //vm memory (MB)
        int mips1 = 1500;
        long bw1 = 2000;// VM带宽（mbps）
        int pesNumber1 = 2; //number of cpus
        String vmm1 = "Xen"; //VMM name

        //VM Parameters1  适用于数据密集 14个
        long size2 = 20000; //image size (MB)
        int ram2 = 1024; //vm memory (MB)
        int mips2 = 1000;
        long bw2 = 1000;// VM带宽（mbps）
        int pesNumber2 = 1; //number of cpus
        String vmm2 = "Xen"; //VMM name

        //create VMs
        Vm[] vm = new Vm[vms];

        for (int i = 0; i < 8; i++) {
            //CloudletSchedulerDynamicWorkload:动态调整分配的时间，提高整体性能和效率。考虑到进度和截止任务时间
            vm[i] = new Vm(i, userId, mips1, pesNumber1, ram1, bw1, size1, vmm1, new CloudletSchedulerDynamicWorkload(mips1,pesNumber1));
            list.add(vm[i]);
        }
        for (int i = 8; i < 16; i++) {
            vm[i] = new Vm(i, userId, mips2, pesNumber2, ram2, bw2, size2, vmm2, new CloudletSchedulerTimeShared());
            list.add(vm[i]);
        }
        for (int i = 16; i < vms; i++) {
            vm[i] = new Vm(i, userId, mips2, pesNumber2, ram2, bw2, size2, vmm2, new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }

        return list;
    }

    private static List<Vm> createVMs(int userId, int vms,int type) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> list = new LinkedList<Vm>();

        long size; //image size (MB)
        int ram ; //vm memory (MB)
        int mips ;
        long bw ;// VM带宽（mbps）
        int pesNumber; //number of cpus
        String vmm = "Xen"; //VMM name

        //create VMs
        Vm[] vm = new Vm[vms];
        if(type == 1){
            size = VmType.Type1.size;
            ram = VmType.Type1.ram;
            mips = VmType.Type1.mips;
            bw = VmType.Type1.bw;
            pesNumber = VmType.Type1.pesNumber;

            for (int i = 0; i < vms; i++) {
                //CloudletSchedulerDynamicWorkload:动态调整分配的时间，提高整体性能和效率。考虑到进度和截止任务时间
                vm[i] = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerDynamicWorkload(mips,pesNumber));
                list.add(vm[i]);
            }
        }
        else if(type == 2){
            size = VmType.Type2.size;
            ram = VmType.Type2.ram;
            mips = VmType.Type2.mips;
            bw = VmType.Type2.bw;
            pesNumber = VmType.Type2.pesNumber;

            for (int i = 0; i < vms; i++) {
                //CloudletSchedulerDynamicWorkload:动态调整分配的时间，提高整体性能和效率。考虑到进度和截止任务时间
                vm[i] = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerDynamicWorkload(mips,pesNumber));
                list.add(vm[i]);
            }
        }
        else if(type == 3){
            size = VmType.Type3.size;
            ram = VmType.Type3.ram;
            mips = VmType.Type3.mips;
            bw = VmType.Type3.bw;
            pesNumber = VmType.Type3.pesNumber;

            for (int i = 0; i < vms; i++) {
                //CloudletSchedulerDynamicWorkload:动态调整分配的时间，提高整体性能和效率。考虑到进度和截止任务时间
                vm[i] = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerDynamicWorkload(mips,pesNumber));
                list.add(vm[i]);
            }
        }
        else if(type == 4){
            size = VmType.Type4.size;
            ram = VmType.Type4.ram;
            mips = VmType.Type4.mips;
            bw = VmType.Type4.bw;
            pesNumber = VmType.Type4.pesNumber;

            for (int i = 0; i < vms; i++) {
                //CloudletSchedulerDynamicWorkload:动态调整分配的时间，提高整体性能和效率。考虑到进度和截止任务时间
                vm[i] = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerDynamicWorkload(mips,pesNumber));
                list.add(vm[i]);
            }
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
            List<Cloudlet> cloudletList = new LinkedList<>();

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
                            outputSize, utilizationModel, utilizationModel, utilizationModel);
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
        for (int i = 0; i < 600; i++) {
            int dcId = (int) (mapping[i]);
            long length1 = (long) ((commMatrix[i][dcId] + execMatrix[i][dcId]));
            //long length = (long) (1e3*execMatrix[i][dcId]);
            cloudlet[i] = new Cloudlet(idShift + i, length1, pesNumber1, fileSize1, outputSize1, utilizationModel, utilizationModel, utilizationModel);
            cloudlet[i].setUserId(userId);
            letList.add(cloudlet[i]);
        }
        for (int i = 600; i < 900; i++) {
            int dcId = (int) (mapping[i]);
            long length2 = (long) ((commMatrix[i][dcId] + execMatrix[i][dcId]));
            //long length = (long) (1e3*execMatrix[i][dcId]);
            cloudlet[i] = new Cloudlet(idShift + i, length2, pesNumber2, fileSize2, outputSize2, utilizationModel, utilizationModel, utilizationModel);
            cloudlet[i].setUserId(userId);
            letList.add(cloudlet[i]);
        }
        for (int i = 1000; i < cloudlets; i++) {
            int dcId = (int) (mapping[i]);
            long length3 = (long) ((commMatrix[i][dcId]*magnification + execMatrix[i][dcId]));
            //long length = (long) (1e3*execMatrix[i][dcId]);
            cloudlet[i] = new Cloudlet(idShift + i, length3, pesNumber3, fileSize3, outputSize3, utilizationModel, utilizationModel, utilizationModel);
            cloudlet[i].setUserId(userId);
            letList.add(cloudlet[i]);
        }

        return letList;
    }

    public static void main(String[] args) {
        Log.printLine("Starting MOPSO Scheduler...");

        new GenerateMatrices();
        commMatrix = GenerateMatrices.getCommMatrix();
        execMatrix = GenerateMatrices.getExecMatrix();
        SAWPSOSchedularInstance = new MOPSO();
        mapping = SAWPSOSchedularInstance.run();


        try {
            String filePath = "cloudlets1.txt";
            int num_user = 1;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events-false

            CloudSim.init(num_user, calendar, trace_flag);

            //Second step: Create Datacenters  创建数据中心--分别创建多个不同的数据中心
            //(String name,int PeNum,int mips,int hostId,int ram,long storage, int bw)
/*            datacenter = new Datacenter[Constants.NO_OF_DATA_CENTERS];
            for (int i = 0; i < Constants.NO_OF_DATA_CENTERS; i++) {
                datacenter[i] = DatacenterCreator.createDatacenter("Datacenter_" + i);
            }*/
            //datacenter = DatacenterCreator.createDatacenter("DataCenter_"+1,Constants.NO_OF_VMS);

            MOPSODatacenterBroker broker = createBroker("Broker_0");
            int brokerId = broker.getId();

            //Fourth step: Create VMs and Cloudlets and send them to broker
            vmList = createVM(brokerId, Constants.NO_OF_VMS);
            datacenter = new Datacenter[Constants.NO_OF_DATA_CENTERS];
            for(int i=0; i<6; i++){
                int peNum;
                datacenter[0] = DatacenterCreator.createDatacenter("DataCenter_"+1,1);
            }
            //绑定虚拟机到数据中心
/*            for(Vm v :vmList){

            }*/


            //cloudletList = createCloudlet(brokerId, Constants.NO_OF_TASKS, 0);
            //createTasks(brokerId,filePath,Constants.NO_OF_TASKS);
            cloudletList = createCloudlet(brokerId,Constants.NO_OF_TASKS, 0);


            // mapping our dcIds to cloudsim dcIds
            // 将虚拟机id映射到cloudSim
            HashSet<Integer> dcIds = new HashSet<>();
            HashMap<Integer, Integer> hm = new HashMap<>();
            for (Vm dc : vmList) {
                if (!dcIds.contains(dc.getId()))
                    dcIds.add(dc.getId());
            }

            Iterator<Integer> it = dcIds.iterator();
            for (int i = 0; i < mapping.length; i++) {
                if (hm.containsKey((int) mapping[i])) continue;
                hm.put((int) mapping[i], it.next());
            }

            for (int i = 0; i < mapping.length; i++)
                mapping[i] = hm.containsKey((int) mapping[i]) ? hm.get((int) mapping[i]) : mapping[i];
            for (int i = 0; i < mapping.length; i++)
                mapping[i] = hm.containsKey((int) mapping[i]) ? hm.get((int) mapping[i]) : mapping[i];

            broker.submitVmList(vmList);
            broker.setMapping(mapping);
            broker.submitCloudletList(cloudletList);

            //绑定任务到虚拟机？？
            //broker.bindCloudletToVm(1,1);


            // Fifth step: Starts the simulation
            CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            //printCloudletList(newList);
            PrintResults(newList);
            Log.printLine(MOPSO_Scheduler.class.getName() + " finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static MOPSODatacenterBroker createBroker(String name) throws Exception {
        return new MOPSODatacenterBroker(name);
    }

    /**
     * Prints the Cloudlet objects
     * @param list list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" +
                indent + "Data center ID" +
                indent + "VM ID" +
                indent + indent + "Time" +
                indent + "Start Time" +
                indent + "Finish Time");

        double mxFinishTime = 0;
        DecimalFormat dft = new DecimalFormat("###.##");
        dft.setMinimumIntegerDigits(2);
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + dft.format(cloudlet.getCloudletId()) + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");
                Log.printLine(indent + indent + dft.format(cloudlet.getResourceId()) +
                        indent + indent + indent + dft.format(cloudlet.getVmId()) +
                        indent + indent + dft.format(cloudlet.getActualCPUTime()) +
                        indent + indent + dft.format(cloudlet.getExecStartTime()) +
                        indent + indent + indent + dft.format(cloudlet.getFinishTime()));
            }
            mxFinishTime = Math.max(mxFinishTime, cloudlet.getFinishTime());
        }
        Log.printLine(mxFinishTime);
        SAWPSOSchedularInstance.printBestFitness();
    }

    private static double PrintResults(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("================ Execution Result ==================");
        Log.printLine("No."+indent +"Cloudlet ID"+ indent +"UserId"+ indent +"VMUserId"+ indent + "STATUS" + indent
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
                Log.print(indent  + cloudlet.getUserId() + indent + indent);
                Log.print(indent  + getVmById(cloudlet.getVmId()).getUserId()+ indent + indent);
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
        Log.printLine("mxFinishTime:"+mxFinishTime);
        return mxFinishTime;
    }

    public static Vm getVmById(int vmId) {
        for(Vm v:vmList)
        {
            if(v.getId()==vmId)
                return v;
        }
        return null;
    }
}