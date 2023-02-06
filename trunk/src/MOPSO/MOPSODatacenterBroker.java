package MOPSO;


import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;
import utils.Constants;

import java.util.List;


public class MOPSODatacenterBroker extends DatacenterBroker {

    private double[] mapping;

    MOPSODatacenterBroker(String name) throws Exception {
        super(name);
    }

    public void setMapping(double[] mapping) {
        this.mapping = mapping;
    }

    public void setUserId(int userId){

    }

    /**
     * 分配任务到虚拟机---修改
     * @param cloudlist
     * @return
     */
    private List<Cloudlet> assignCloudletsToVms(List<Cloudlet> cloudlist) {

        //计算当前的负载情况
/*        double[] vmcostLength = new double[Constants.NO_OF_VMS];
        double[] position = MOPSO.Position;
        double[][] commMatrix = MOPSO_Scheduler.commMatrix;
        double[][] execMatrix = MOPSO_Scheduler.execMatrix;
        for (int i = 0; i < Constants.NO_OF_VMS; i++) {
            double totalcloudletLength = 0.0;
            for (int j = 0; j < Constants.NO_OF_TASKS; j++) {
                if (i == (int) position[j]) {
                    long length1 = (long)(10*(commMatrix[j][i])+ 1e3*(execMatrix[j][i]));
                    totalcloudletLength += length1;
                }
            }
            vmcostLength[i] = totalcloudletLength;
        }

        int id = 0;
        double currentMin = vmcostLength[0];
        for (int i = 1; i < Constants.NO_OF_VMS; i++){
            if(currentMin >= vmcostLength[i]){
                currentMin = vmcostLength[i];
                id = i;
            }
        }*/

        int idx = 0;
        for (Cloudlet cl : cloudlist) {
            cl.setVmId((int)mapping[idx++]);
        }
        return cloudlist;
    }

    /**
     * 提交云任务---分配任务到虚拟机
     */
    @Override
    public void submitCloudlets() {
        Log.print("任务数量检查"+cloudletList.size() + mapping.length);
        List<Cloudlet> tasks = assignCloudletsToVms(getCloudletList());
        int vmIndex = 0;
        for (Cloudlet cloudlet : tasks) {
            Vm vm;
            // if user didn't bind this cloudlet and it has not been executed yet
            if (cloudlet.getVmId() == -1) {
                vm = getVmsCreatedList().get(vmIndex);
            } else { // submit to the specific vm
                vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
                if (vm == null) { // vm was not created
                    Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
                            + cloudlet.getCloudletId() + ": bount VM not available");
                    continue;
                }
            }

            Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
                    + cloudlet.getCloudletId() + " to VM #" + vm.getId());
            cloudlet.setVmId(vm.getId());
            sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
            cloudletsSubmitted++;
            vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
            getCloudletSubmittedList().add(cloudlet);
        }
    }
    @Override
    public void submitVmList(List<? extends Vm> list) {
        this.getVmList().addAll(list);
    }

    @Override
    protected void processResourceCharacteristics(SimEvent ev) {
        DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
        getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

        if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
            distributeRequestsForNewVmsAcrossDatacenters();
        }
    }

    protected void distributeRequestsForNewVmsAcrossDatacenters() {
        int numberOfVmsAllocated = 0;
        int i = 0;

        final List<Integer> availableDatacenters = getDatacenterIdsList();

        for (Vm vm : getVmList()) {
            int datacenterId = availableDatacenters.get(i++ % availableDatacenters.size());
            String datacenterName = CloudSim.getEntityName(datacenterId);

            if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
                Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId() + " in " + datacenterName);
                sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
                numberOfVmsAllocated++;
            }
        }

        setVmsRequested(numberOfVmsAllocated);
        setVmsAcks(0);
    }
}
