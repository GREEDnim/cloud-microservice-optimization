package MOPSO;


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

public class MOPSO_Scheduler_Users {


    private static List<Cloudlet> cloudletList1 = new LinkedList<>();
    private static List<Cloudlet> cloudletList2 = new LinkedList<>();
    private static List<Cloudlet> cloudletList = new LinkedList<>();

    private static List<Vm> vmList;
    private static List<Vm> vmList1;
    private static List<Vm> vmList2;

    private static Datacenter[] datacenter;
    //private static Datacenter datacenter;
    private static MOPSO SAWPSOSchedularInstance1;
    private static MOPSO SAWPSOSchedularInstance2;

    private static double mapping[];
    private static double mapping1[];
    private static double mapping2[];

    private static double[][] commMatrix;
    private static double[][] execMatrix;



    private static List<Vm> createVM(int userId, int vms,long size,int ram,int mips,long bw,int pesNumber,String vmm ) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> list = new LinkedList<Vm>();

        //VM Parameters
/*        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        int mips = 1000;
        long bw = 1000;// VM带宽（mbps）
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name*/

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

    private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int idShift, double[] mapping,long fileSize,long outputSize,int pesNumber) {
        LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

        //cloudlet parameters
/*        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;*/
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet[] cloudlet = new Cloudlet[cloudlets];

        for (int i = 0; i < cloudlets; i++) {
            int dcId = (int) (mapping[i]);
            long length = (long) ((commMatrix[i][dcId] + execMatrix[i][dcId]));
            //long length = (long) (1e3*execMatrix[i][dcId]);
            cloudlet[i] = new Cloudlet(idShift + i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet[i].setUserId(userId);
            list.add(cloudlet[i]);
        }

        return list;
    }

    public static void main(String[] args) {
        Log.printLine("Starting PSO Scheduler...");

        new GenerateMatrices();
        commMatrix = GenerateMatrices.getCommMatrix();
        execMatrix = GenerateMatrices.getExecMatrix();
        SAWPSOSchedularInstance1 = new MOPSO();
        SAWPSOSchedularInstance2 = new MOPSO();
        mapping1 = SAWPSOSchedularInstance1.run();
        mapping2 = SAWPSOSchedularInstance2.run();



        try {
            String filePath = "cloudlets1.txt";
            int num_user = 2;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = true;  // mean trace events-false

            CloudSim.init(num_user, calendar, trace_flag);

            //Second step: Create Datacenters  创建数据中心--分别创建多个不同的数据中心
            //(String name,int PeNum,int mips,int hostId,int ram,long storage, int bw)
            //datacenter = new Datacenter[Constants.NO_OF_DATA_CENTERS];
/*            for (int i = 0; i < Constants.NO_OF_DATA_CENTERS; i++) {
                datacenter[i] = DatacenterCreator.createDatacenter("Datacenter_" + i);
            }*/
            //datacenter = DatacenterCreator.createDatacenter("DataCenter_"+1,Constants.NO_OF_VMS);
            //数据中心1：
            datacenter = new Datacenter[Constants.NO_OF_DATA_CENTERS];

            MOPSODatacenterBroker broker1 = createBroker("Broker_0");
            int brokerId1 = broker1.getId();
            MOPSODatacenterBroker broker2 = createBroker("Broker_1");
            int brokerId2 = broker2.getId();



            //Fourth step: Create VMs and Cloudlets and send them to broker
            /*long size = 10000; //image size (MB)
            int ram = 512; //vm memory (MB)
            int mips = 1000;
            long bw = 1000;// VM带宽（mbps）
            int pesNumber = 1; //number of cpus
            String vmm = "Xen"; //VMM name*/
            vmList1 = createVM(brokerId1, Constants.NO_OF_VMS,10000,512,1000,1000,1,"Xen");
            vmList2 = createVM(brokerId2, Constants.NO_OF_VMS,10000,512,1000,1000,1,"Xen");
            vmList = vmList1;
            vmList.addAll(vmList2);





            //cloudletList = createCloudlet(brokerId, Constants.NO_OF_TASKS, 0);
            //createTasks(brokerId1,filePath,Constants.NO_OF_TASKS);
            cloudletList1 = createCloudlet(brokerId1, Constants.NO_OF_TASKS, 0,mapping1,300,300,1);
            cloudletList2 = createCloudlet(brokerId2, Constants.NO_OF_TASKS, 0,mapping2,300,300,1);
            cloudletList = cloudletList1;
            cloudletList.addAll(cloudletList2);






            // mapping our dcIds to cloudsim dcIds
            // 将数据中心id映射到cloudSim
            //HashSet<Integer> dcIds = new HashSet<>();
            //HashMap<Integer, Integer> hm = new HashMap<>();
            HashSet<Integer> dcIds1 = new HashSet<>();
            HashSet<Integer> dcIds2 = new HashSet<>();
            HashMap<Integer, Integer> hm1 = new HashMap<>();
            HashMap<Integer, Integer> hm2 = new HashMap<>();
/*            for (Vm dc : vmList) {
                if (!dcIds.contains(dc.getId()))
                    dcIds.add(dc.getId());
            }*/
            for (Vm dc : vmList1) {
                if (!dcIds1.contains(dc.getId()))
                    dcIds1.add(dc.getId());
            }
            for (Vm dc : vmList2) {
                if (!dcIds2.contains(dc.getId()))
                    dcIds2.add(dc.getId());
            }
            Iterator<Integer> it1 = dcIds1.iterator();
            Iterator<Integer> it2 = dcIds2.iterator();
            for (int i = 0; i < mapping1.length; i++) {
                if (hm1.containsKey((int) mapping1[i])) continue;
                hm1.put((int) mapping1[i], it1.next());
            }
            for (int i = 0; i < mapping2.length; i++) {
                if (hm2.containsKey((int) mapping2[i])) continue;
                hm2.put((int) mapping2[i], it2.next());
            }
            for (int i = 0; i < mapping1.length; i++)
                mapping1[i] = hm1.containsKey((int) mapping1[i]) ? hm1.get((int) mapping1[i]) : mapping1[i];
            for (int i = 0; i < mapping2.length; i++)
                mapping2[i] = hm2.containsKey((int) mapping2[i]) ? hm2.get((int) mapping2[i]) : mapping2[i];

            broker1.submitVmList(vmList1);
            broker1.setMapping(mapping1);
            broker1.submitCloudletList(cloudletList1);

            broker2.submitVmList(vmList2);
            broker2.setMapping(mapping2);
            broker2.submitCloudletList(cloudletList2);


            // Fifth step: Starts the simulation
            CloudSim.startSimulation();

            List<Cloudlet> newList1 = broker1.getCloudletReceivedList();
            List<Cloudlet> newList2 = broker2.getCloudletReceivedList();

            CloudSim.stopSimulation();

            //printCloudletList(newList);
            PrintResults(newList1);
            PrintResults(newList2);
            Log.printLine(MOPSO_Scheduler_Users.class.getName() + " finished!");
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
        SAWPSOSchedularInstance1.printBestFitness();
        SAWPSOSchedularInstance2.printBestFitness();
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
        Log.printLine(mxFinishTime);
        return mxFinishTime;
    }

    public static Vm getVmById(int vmId) {
/*        vmList.addAll(vmList1);
        vmList.addAll(vmList2);*/
        for(Vm v:vmList)
        {
            if(v.getId()==vmId)
                return v;
        }
        return null;
    }
}