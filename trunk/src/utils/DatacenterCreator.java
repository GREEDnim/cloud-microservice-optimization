package utils;


import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimple;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import utils.ProcessorsCharacter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DatacenterCreator {

    /*ProcessorsCharacter Character = new ProcessorsCharacter();*/
    public static List<Host> hostLists;


    public static Datacenter createDatacenter(String name) {

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store one or more Machines
        List<Host> hostList = new ArrayList<Host>();

        // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
        //    create a list to store these PEs before creating a Machine.
        List<Pe> peList = new ArrayList<Pe>();

        int mips = 1000;

        // 3. Create PEs and add these into the list.
        peList.add(new Pe(0, new PeProvisionerSimple(mips)));

        //4. Create Hosts with its id and list of PEs and add them to the list of machines
        int hostId = 0;
        int ram = 2048; //host memory (MB)
        long storage = 1000000; //host storage
        int bw = 10000;

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList,
                        new VmSchedulerTimeShared(peList)
                )
        );
        // This is our first machine


        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located 资源所在时区
        double cost = 3.0;              // the cost of using processing in this resource 使用成本
        double costPerMem = 0.05;        // the cost of using memory in this resource   内存成本
        double costPerStorage = 0.1;    // the cost of using storage in this resource  存储成本
        double costPerBw = 0.1;            // the cost of using bw in this resource  带宽成本
        LinkedList<Storage> storageList = new LinkedList<Storage>();    //we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datacenter;
    }


    public static Datacenter createDatacenter(String name,int PeNum) {
        List<Host> hostList = new ArrayList<Host>();
        List<Pe> peList = new ArrayList<Pe>();
        int mips = 2000;
        for(int i=0;i<PeNum;i++)
        {
            peList.add(new Pe(1, new PeProvisionerSimple(mips)));
        }
        peList.add(new Pe(1, new PeProvisionerSimple(mips)));
        int hostId = 0;
        int ram = 40960; //host memory (MB)
        long storage = 1000000; //host storage
        int bw = 100000;

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList,
                        new VmSchedulerTimeShared(peList)
                )
        ); // This is our first machine


        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;        // the cost of using memory in this resource
        double costPerStorage = 0.1;    // the cost of using storage in this resource
        double costPerBw = 0.1;            // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();    //we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datacenter;
    }

    public static Datacenter createDatacenter(String name,int  hostId, int centerType) {
        List<Host> hostList = new ArrayList<Host>();
        List<Pe> peList = new ArrayList<Pe>();
        //定义初始值
        int ram = 40960; //host memory (MB)
        long storage = 1000000; //host storage
        int bw = 10000;
        int mips = 10000;
        int PeNum = 6;

        //数据中心类型
        String arch = "x86";          // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";           //虚拟机
        double time_zone = 10.0;      // time zone this resource located
        double cost = 3.0;            // the cost of using processing in this resource
        double costPerMem = 0.05;     // the cost of using memory in this resource
        double costPerStorage = 0.1;  // the cost of using storage in this resource
        double costPerBw = 0.1;       // the cost of using bw in this resource

        //判断数据中心类型
        if(centerType == 1){
            ram = ProcessorsCharacter.Type_manage.ram;
            storage = ProcessorsCharacter.Type_manage.storage;
            PeNum = ProcessorsCharacter.Type_manage.cores;
            mips = ProcessorsCharacter.Type_manage.mips;
        }
        else if(centerType == 2){
            ram = ProcessorsCharacter.Type_design.ram;
            storage = ProcessorsCharacter.Type_design.storage;
            PeNum = ProcessorsCharacter.Type_design.cores;
            mips = ProcessorsCharacter.Type_design.mips;
        }
        else if(centerType == 3){
            ram = ProcessorsCharacter.Type_build.ram;
            storage = ProcessorsCharacter.Type_build.storage;
            PeNum = ProcessorsCharacter.Type_build.cores;
            mips = ProcessorsCharacter.Type_build.mips;
        }


        for(int i=0;i<PeNum;i++)
        {
            peList.add(new Pe(1, new PeProvisionerSimple(mips)));
        }

        hostList.add(
                new Host(
                        0,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList,
                        new VmSchedulerTimeShared(peList)
                )
        );




        LinkedList<Storage> storageList = new LinkedList<Storage>();    //we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //hostLists.add(hostList.get(0));
        return datacenter;
    }
}
