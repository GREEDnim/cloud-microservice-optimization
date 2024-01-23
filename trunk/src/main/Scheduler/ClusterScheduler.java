package main.Scheduler;

import main.services.Task;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Consts;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.List;

public class ClusterScheduler extends CloudletSchedulerSpaceShared {
    public double cloudletSubmit(Cloudlet cloudlet, double fileTransferTime) {
        Task task=(Task) cloudlet;
        System.out.println("\n------sub-------");
        System.out.println("gid: "+task.getGroupId()+" tskid :"+task.getCloudletId());
        System.out.println("------sub-------");

        if ((currentCpus - usedPes) >= cloudlet.getNumberOfPes() && task.getCloudletId()==task.getMicroServices() ) {
            ResCloudlet rcl = new ResCloudlet(cloudlet);
            rcl.setCloudletStatus(Cloudlet.INEXEC);
            for (int i = 0; i < cloudlet.getNumberOfPes(); i++) {
                rcl.setMachineAndPeId(0, i);
            }
            getCloudletExecList().add(rcl);
            usedPes += cloudlet.getNumberOfPes();
        } else {// no enough free PEs: go to the waiting queue
            ResCloudlet rcl = new ResCloudlet(cloudlet);
            rcl.setCloudletStatus(Cloudlet.QUEUED);
            getCloudletWaitingList().add(rcl);
            return 0.0;
        }

        // calculate the expected time for cloudlet completion
        double capacity = 0.0;
        int cpus = 0;
        for (Double mips : getCurrentMipsShare()) {
            capacity += mips;
            if (mips > 0) {
                cpus++;
            }
        }

        currentCpus = cpus;
        capacity /= cpus;

        // use the current capacity to estimate the extra amount of
        // time to file transferring. It must be added to the cloudlet length
        double extraSize = capacity * fileTransferTime;
        long length = cloudlet.getCloudletLength();
        length += extraSize;
        cloudlet.setCloudletLength(length);
        return cloudlet.getCloudletLength() / capacity;
    }
    int call=0;
    private void printList(List<ResCloudlet> list,String type){
        System.out.println("\n-------"+type+" call:"+call++ +"-------");
        for(ResCloudlet rcl : list){
            System.out.print(rcl.getCloudletId()+" ");
        }
        System.out.println("\n-------"+type+"-------");
    }
    public double updateVmProcessing(double currentTime, List<Double> mipsShare) {

        printList(getCloudletExecList(),"ex");

        printList(getCloudletWaitingList(),"w");

        setCurrentMipsShare(mipsShare);
        double timeSpam = currentTime - getPreviousTime(); // time since last update
        double capacity = 0.0;
        int cpus = 0;

        for (Double mips : mipsShare) { // count the CPUs available to the VMM
            capacity += mips;
            if (mips > 0) {
                cpus++;
            }
        }
        currentCpus = cpus;
        capacity /= cpus; // average capacity of each cpu

        // each machine in the exec list has the same amount of cpu
        for (ResCloudlet rcl : getCloudletExecList()) {
            rcl.updateCloudletFinishedSoFar((long) (capacity * timeSpam * rcl.getNumberOfPes() * Consts.MILLION));
        }

        // no more cloudlets in this scheduler
        if (getCloudletExecList().size() == 0 && getCloudletWaitingList().size() == 0) {
            setPreviousTime(currentTime);
            return 0.0;
        }

        // update each cloudlet
        int finished = 0;
        List<ResCloudlet> toRemove = new ArrayList<ResCloudlet>();
        for (ResCloudlet rcl : getCloudletExecList()) {
            // finished anyway, rounding issue...
            if (rcl.getRemainingCloudletLength() == 0) {
                toRemove.add(rcl);
                cloudletFinish(rcl);
                finished++;
            }
        }
        getCloudletExecList().removeAll(toRemove);

        //maintain priority of selecting cloudlets in waiting list that are present in toRemove

        // for each finished cloudlet, add a new one from the waiting list
        if (!getCloudletWaitingList().isEmpty()) {
            for (int i = 0; i < finished; i++) {
                toRemove.clear();
                for (ResCloudlet rcl : getCloudletWaitingList()) {
                    if ((currentCpus - usedPes) >= rcl.getNumberOfPes() ) {
                        rcl.setCloudletStatus(Cloudlet.INEXEC);
                        for (int k = 0; k < rcl.getNumberOfPes(); k++) {
                            rcl.setMachineAndPeId(0, i);
                        }
                        getCloudletExecList().add(rcl);
                        usedPes += rcl.getNumberOfPes();
                        toRemove.add(rcl);
                        break;
                    }
                }
                getCloudletWaitingList().removeAll(toRemove);
            }
        }

        // estimate finish time of cloudlets in the execution queue
        double nextEvent = Double.MAX_VALUE;
        for (ResCloudlet rcl : getCloudletExecList()) {
            double remainingLength = rcl.getRemainingCloudletLength();
            double estimatedFinishTime = currentTime + (remainingLength / (capacity * rcl.getNumberOfPes()));
            if (estimatedFinishTime - currentTime < CloudSim.getMinTimeBetweenEvents()) {
                estimatedFinishTime = currentTime + CloudSim.getMinTimeBetweenEvents();
            }
            if (estimatedFinishTime < nextEvent) {
                nextEvent = estimatedFinishTime;
            }
        }
        setPreviousTime(currentTime);
        return nextEvent;
    }
}
