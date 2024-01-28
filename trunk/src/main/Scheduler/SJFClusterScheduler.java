package main.Scheduler;

import main.services.Task;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.ResCloudlet;


import java.util.*;

public class SJFClusterScheduler extends CloudletSchedulerSpaceShared {
    private HashMap<Integer,Integer>groupPriority=new HashMap<>();
    private Comparator<ResCloudlet> getComparator(){
        Comparator<ResCloudlet> comp=new Comparator<>() {
            @Override
            public int compare(ResCloudlet o1, ResCloudlet o2) {
                Task a=(Task) o1.getCloudlet();
                Task b=(Task) o2.getCloudlet();

                int aPriority=groupPriority.get(a.getGroupId());
                int bPriority=groupPriority.get(b.getGroupId());

                if(aPriority==bPriority) return (int)(a.getCloudletLength()-b.getCloudletLength());
                return aPriority-bPriority;
            }
        };
        return  comp;
    }
    public double cloudletSubmit(Cloudlet cloudlet, double fileTransferTime) {

        Task task=(Task) cloudlet;
        int gid=task.getGroupId();
        if(!groupPriority.containsKey(gid)) groupPriority.put(gid,groupPriority.size());

        return super.cloudletSubmit(cloudlet,fileTransferTime);
    }
    public double updateVmProcessing(double currentTime, List<Double> mipsShare) {

        Collections.sort(getCloudletWaitingList(),getComparator());
        return super.updateVmProcessing(currentTime,mipsShare);
    }
}


