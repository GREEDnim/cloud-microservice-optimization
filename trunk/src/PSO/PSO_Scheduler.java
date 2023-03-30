package PSO;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.network.datacenter.NetworkCloudlet;
import utils.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;

public class PSO_Scheduler {


    private static List<Cloudlet> cloudletList = new LinkedList<>();
    private static List<Vm> vmList;
    private static Datacenter[] datacenter;
    //private static Datacenter datacenter;
    private static PSO PSOSchedularInstance;
    private static double mapping[];
    private static double[][] commMatrix;
    private static double[][] execMatrix;

    /**
     * 创建用于存放虚拟机的容器。该列表稍后被传递给代理
     * @param userId
     * @param vms
     * @return
     */
    private static List<Vm> createVM(int userId, int vms) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> list = new LinkedList<Vm>();
        //List<Host> hostLis = DatacenterCreator.hostLists;

        //VM Parameters1  适用于计算密集-计算大 （10个）  第一类-高性能
        long size1 = 10000; //image size (MB)
        int ram1 = 1024; //vm memory (MB)
        int mips1 = 2000;//处理时长           *************
        long bw1 = 1500;// VM带宽（mbps）
        int pesNumber1 = 1; //number of cpus
        String vmm1 = "Xen"; //VMM name

        //VM Parameters3  跨数据中心--带宽大 (10个)      第二类-中性能
        long size2 = 15000; //image size (MB)
        int ram2 = 1024; //vm memory (MB)
        int mips2 = 1000;//处理时长           *********
        long bw2 = 2000;// VM带宽（mbps）
        int pesNumber2 = 1; //number of cpus
        String vmm2 = "Xen"; //VMM name

        //VM Parameters3  适用于数据密集-内存大（10个）   第三类-低性能
        long size3 = 20000; //image size (MB)
        int ram3 = 2048; //vm memory (MB)
        int mips3 = 500;//处理时长           *****
        long bw3 = 1000;// VM带宽（mbps）
        int pesNumber3 = 1; //number of cpus
        String vmm3 = "Xen"; //VMM name

        //create VMs
        Vm[] vm = new Vm[vms];
        //CloudletSchedulerDynamicWorkload:动态调整分配的时间，提高整体性能和效率。考虑到进度和截止任务时间
        //new CloudletSchedulerTimeShared():空间共享，所有虚拟机共享相同的cpu和内存，每个cloudlet到达后立即执行
        //CloudletSchedulerSpaceShared()：以分时方式调度cloudlets，特定时间为每个任务分配一定量的CPU和内存资源，循环方式执行
        for (int i = 0; i < vms; i+=3) {
            //vm[i] = new Vm(i, userId, mips1, pesNumber1, ram1, bw1, size1, vmm1, new CloudletSchedulerDynamicWorkload(mips1, pesNumber1));
            vm[i] = new Vm(i, userId, mips1, pesNumber1, ram1, bw1, size1, vmm1, new CloudletSchedulerSpaceShared());
            //vm[i] = new Vm(i, userId, mips1, pesNumber1, ram1, bw1, size1, vmm1, new NetworkCloudletSpaceSharedScheduler());
            //vm[i] = new Vm(i, userId, mips1, pesNumber1, ram1, bw1, size1, vmm1, new CloudletSchedulerTimeShared());

            vm[i+1] = new Vm(i+1, userId, mips2, pesNumber2, ram2, bw2, size2, vmm2, new CloudletSchedulerSpaceShared());
            //vm[i] = new Vm(i, userId, mips2, pesNumber2, ram2, bw2, size2, vmm2, new NetworkCloudletSpaceSharedScheduler());
            //vm[i] = new Vm(i, userId, mips2, pesNumber2, ram2, bw2, size2, vmm2, new CloudletSchedulerDynamicWorkload(mips2, pesNumber2));
            //vm[i+1] = new Vm(i+1, userId, mips2, pesNumber2, ram2, bw2, size2, vmm2, new CloudletSchedulerTimeShared());

            vm[i+2] = new Vm(i+2, userId, mips3, pesNumber3, ram3, bw3, size3, vmm3, new CloudletSchedulerSpaceShared());
            //vm[i] = new Vm(i, userId, mips3, pesNumber3, ram3, bw3, size3, vmm3, new CloudletSchedulerDynamicWorkload(mips3, pesNumber3));
            //vm[i+2] = new Vm(i+2, userId, mips3, pesNumber3, ram3, bw3, size3, vmm3, new CloudletSchedulerTimeShared());

            list.add(vm[i]);
            list.add(vm[i+1]);
            list.add(vm[i+2]);
        }
        return list;
    }


    private static List<Cloudlet> createTasks(int brokerId, String filePath, int cloudlets) {

        LinkedList<Cloudlet> letList = new LinkedList<Cloudlet>();
        try {
            @SuppressWarnings("resource")
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            String data = null;
            int index = 0;

            //cloudlet1 parameters ：数据型任务 轻量
            long fileSize1 = 500;
            long outputSize1 = 500;
            int pesNumber1 = 1;

            //cloudlet2 parameters ：网络型任务 中负载
            long fileSize2 = 350;
            long outputSize2 = 350;
            int pesNumber2 = 1;
            int magnification = 2;
            long memory2 = 800;

            //cloudlet3 parameters ：计算型任务 高负载
            long fileSize3 = 200;
            long outputSize3 = 200;
            int pesNumber3 = 1;


            //UtilizationModel utilizationModel = new UtilizationModelFull();
            UtilizationModelStochastic utilizationModel = new UtilizationModelStochastic();

            while ((data = br.readLine()) != null) {
                System.out.println(data);
                String[] taskLength = data.split("\t");//tasklength[i]是任务执行的耗费（指令数量）
                for (int j = 0; j < 20; j++) {

                    if(Double.parseDouble(taskLength[j]) < 3000) {
                        Cloudlet task = new Cloudlet(index + j, (long) Double.parseDouble(taskLength[j]), pesNumber1, fileSize1,
                                outputSize1, utilizationModel, utilizationModel, utilizationModel);
                        task.setUserId(brokerId);
                        letList.add(task);
                    }
                    if ( Double.parseDouble(taskLength[j]) >= 3000 && Double.parseDouble(taskLength[j])<=6000) {
                        Cloudlet task = new Cloudlet(index + j, (long) Double.parseDouble(taskLength[j]), pesNumber2, fileSize2,
                                outputSize2, utilizationModel, utilizationModel, utilizationModel);
                        task.setUserId(brokerId);
                        letList.add(task);
                    }
                    if (Double.parseDouble(taskLength[j]) >= 8000) {
                        Cloudlet task = new Cloudlet(index + j, (long) Double.parseDouble(taskLength[j]), pesNumber3, fileSize3,
                                outputSize3, utilizationModel, utilizationModel, utilizationModel);
                        task.setUserId(brokerId);
                        letList.add(task);
                    }

                    if (letList.size() == cloudlets) {
                        br.close();
                        break;
                    }
                }
                //20 cloudlets each line in the file cloudlets.txt.
                index += 20;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return letList;

    }

    private static List<Cloudlet> createCloudlet(int userId,int cloudlets,int idShift) {
        LinkedList<Cloudlet> letList = new LinkedList<Cloudlet>();

        //cloudlet1 parameters ：数据密集任务参数
        long fileSize1 = 500;
        long outputSize1 = 500;
        long  memory1 = 200;
        int pesNumber1 = 1;

        //cloudlet2 parameters ：计算密集性任务参数
        long fileSize2 = 200;
        long outputSize2 = 200;
        long  memory2 = 100;
        int pesNumber2 = 2;

        //cloudlet3 parameters ：网络任务参数
        long fileSize3 = 300;
        long outputSize3 = 300;
        int pesNumber3 = 1;
        long  memory3 = 800;
        int magnification = 2;

        //UtilizationModel utilizationModel = new UtilizationModelFull();
        //UtilizationModelPlanetLabInMemory PlanetLabInMemory = new UtilizationModelPlanetLabInMemory(filePath,0.01,4);
        UtilizationModelStochastic utilizationModel = new UtilizationModelStochastic();
        Cloudlet[] cloudlet = new Cloudlet[cloudlets];
        //在这里创建多种任务-数据密集任务:数据密集任务：跨中心任务 = 6：3：1
        for (int i = 0; i < cloudlets*0.6 ; i++) {
            int dcId = (int) (mapping[i]);
            //long length1 = (long)(30*(commMatrix[i][dcId])+ 1e3*(execMatrix[i][dcId]));
            long length1 = (long)((commMatrix[i][dcId])+ 1e3*(execMatrix[i][dcId]));
            //long length = (long) (1e3*execMatrix[i][dcId]);
            cloudlet[i] = new Cloudlet(idShift + i, length1, pesNumber1, fileSize1, outputSize1, utilizationModel, utilizationModel, utilizationModel);
            //cloudlet[i] = new NetworkCloudlet(idShift + i, length1, pesNumber1, fileSize1, outputSize1, memory1, utilizationModel, utilizationModel, utilizationModel);
            cloudlet[i].setUserId(userId);
            letList.add(cloudlet[i]);
        }
        for (int i = (int)((int)cloudlets*0.6); i < cloudlets*0.9; i++) {
            int dcId = (int) (mapping[i]);
            //long length2 = (long)(30*(commMatrix[i][dcId])+ 1e3*(execMatrix[i][dcId]));
            long length2 = (long)((commMatrix[i][dcId])+ 1e3*(execMatrix[i][dcId]));
            //long length = (long) (1e3*execMatrix[i][dcId]);
            cloudlet[i] = new Cloudlet(idShift + i, length2, pesNumber2, fileSize2, outputSize2, utilizationModel, utilizationModel, utilizationModel);
            //cloudlet[i] = new NetworkCloudlet(idShift + i, length2, pesNumber2, fileSize2, outputSize2, memory2, utilizationModel, utilizationModel, utilizationModel);
            cloudlet[i].setUserId(userId);
            letList.add(cloudlet[i]);
        }
        //网络任务
        for (int i = (int)((int)cloudlets*0.9); i < cloudlets; i++) {
            int dcId = (int) (mapping[i]);
            //long length3 = (long)(30*(commMatrix[i][dcId]) + 1e3*(execMatrix[i][dcId]));
            long length3 = (long)((commMatrix[i][dcId]) + 1e3*(execMatrix[i][dcId]));
            //long length = (long) (1e3*execMatrix[i][dcId]);
            cloudlet[i] = new Cloudlet(idShift + i, length3, pesNumber3, fileSize3, outputSize3, utilizationModel, utilizationModel, utilizationModel);
            //cloudlet[i] = new NetworkCloudlet(idShift + i, length3, pesNumber3, fileSize3, outputSize3, memory3, utilizationModel, utilizationModel, utilizationModel);
            cloudlet[i].setUserId(userId);
            letList.add(cloudlet[i]);
        }

        return letList;
    }

    public static void main(String[] args) {
        Log.printLine("Starting PSO Scheduler...");

        new GenerateMatrices();
        commMatrix = GenerateMatrices.getCommMatrix();
        execMatrix = GenerateMatrices.getExecMatrix();
        PSOSchedularInstance = new PSO();
        mapping = PSOSchedularInstance.run();

        try {
            String filePath = "cloudlets.txt";
            int num_user = 1;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            datacenter = new Datacenter[Constants.NO_OF_DATA_CENTERS];
            //for (int i = 0; i < Constants.NO_OF_DATA_CENTERS; i++) {
                //datacenter[i] = DatacenterCreator.createDatacenter("Datacenter_" + i);
            //}
            //datacenter = DatacenterCreator.createDatacenter("DataCenter_"+1,Constants.NO_OF_VMS);

            //管理-数据中心
            datacenter[0] = DatacenterCreator.createDatacenter("Datacenter_manage",0,1);
            //设计-数据中心
            for (int i = 1; i < 3; i++) {
                datacenter[i] = DatacenterCreator.createDatacenter("Datacenter_design" + i,0,2);
            }
            //施工-数据中心
            for (int i = 3; i < 6; i++) {
                datacenter[i] = DatacenterCreator.createDatacenter("Datacenter_build" + i,0,3);
            }


            //Third step: Create Broker
            PSODatacenterBroker broker = createBroker("Broker_0");
            int brokerId = broker.getId();

            //Fourth step: Create VMs and Cloudlets and send them to broker
            vmList = createVM(brokerId, Constants.NO_OF_VMS);
            cloudletList = createTasks(brokerId, filePath, Constants.NO_OF_TASKS);
            //createTasks(brokerId,filePath,Constants.NO_OF_TASKS);
            // mapping our dcIds to cloudsim dcIds
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

            broker.submitVmList(vmList);
            broker.setMapping(mapping);
            broker.submitCloudletList(cloudletList);


            // Fifth step: Starts the simulation
            CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            //printCloudletList(newList);
            PrintResults(newList);
            PrintLatency(newList);
            PrintStartTime(newList);
            Log.printLine(PSO_Scheduler.class.getName() + " finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static PSODatacenterBroker createBroker(String name) throws Exception {
        return new PSODatacenterBroker(name);
    }

    /** 打印云任务对象 任务id 执行时间 结束时间
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
        PSOSchedularInstance.printBestFitness();
    }

    private static double PrintResults(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("================ Execution Result ==================");
        Log.printLine("No."+indent +
                "Cloudlet ID" + indent +
                "STATUS" + indent +
                "Data center ID" + indent +
                "VM ID" + indent+
                "VM mips"+ indent +
                "Current VM MIPS" + indent+
                "CloudletLength"+ indent+
                "Time" + indent +
                "Start Time" + indent +
                "Finish Time");
        double mxFinishTime = 0;
        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++)
        {
            cloudlet = list.get(i);
            Log.print(i+1+indent+indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus()== Cloudlet.SUCCESS)
            {
                Log.print("SUCCESS");

                Log.printLine(
                        indent +indent + indent + cloudlet.getResourceId()
                        + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent + getVmById(cloudlet.getVmId()).getMips()
                        + indent + indent + cloudlet.getCloudletLength()  //任务长度
                        + indent + indent+ indent + indent
                        + dft.format(cloudlet.getActualCPUTime()) + indent
                        + indent + dft.format(cloudlet.getExecStartTime())
                        + indent + indent
                        + dft.format(cloudlet.getFinishTime()));
            }
            mxFinishTime = Math.max(mxFinishTime, cloudlet.getFinishTime());
        }
        Log.printLine("================ Execution Result Ends here ==================");
        Log.printLine("最大完成时间mxFinishTime"+ mxFinishTime);
        return mxFinishTime;
    }


    private static double PrintLatency(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        //UtilizationModelStochastic UtilizationModelStochastic = new UtilizationModelStochastic();
        //UtilizationModelPlanetLabInMemory UtilizationMode = new UtilizationModelPlanetLabInMemory();
        //UtilizationModelFull UtilizationModelFull = new UtilizationModelFull();
        Log.printLine("================ Execution Result Latency ==================");

        double mxLatencyTime = 0;
        double LatencyTime = 0;
        double AvgLatencyTime = 0;
        double totalLatencyTime = 0;
        int sucnum = 0;
        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++)
        {
            cloudlet = list.get(i);

            if (cloudlet.getStatus()== Cloudlet.SUCCESS)
            {
                LatencyTime = cloudlet.getFinishTime()-cloudlet.getExecStartTime();
                totalLatencyTime += LatencyTime;
                sucnum++;
            }
            mxLatencyTime = Math.max(mxLatencyTime, LatencyTime);
        }
        AvgLatencyTime = Calculator.div(totalLatencyTime,sucnum);
        Log.printLine("mxLatencyTime:"+mxLatencyTime);
        Log.printLine("AvgLatencyTime:"+AvgLatencyTime);
        return mxLatencyTime;
    }

    private static double PrintStartTime(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        //UtilizationModelStochastic UtilizationModelStochastic = new UtilizationModelStochastic();
        //UtilizationModelPlanetLabInMemory UtilizationMode = new UtilizationModelPlanetLabInMemory();
        //UtilizationModelFull UtilizationModelFull = new UtilizationModelFull();
        Log.printLine("================ Execution Result Latency ==================");

        double maxStartTime = 0;
        double StartTime = 0;

        for (int i = 0; i < size; i++)
        {
            cloudlet = list.get(i);

            if (cloudlet.getStatus()== Cloudlet.SUCCESS)
            {
                StartTime = cloudlet.getExecStartTime();
            }
            maxStartTime = Math.max(maxStartTime, StartTime);
        }
        Log.printLine("maxStartTime:"+maxStartTime);
        return maxStartTime;
    }

    public static Vm getVmById(int vmId){
        for(Vm v:vmList)
        {
            if(v.getId()==vmId)
                return v;
        }
        return null;
    }

}