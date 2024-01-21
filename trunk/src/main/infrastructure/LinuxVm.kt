package main.infrastructure
import main.simulation.AlgorithmType
import org.cloudbus.cloudsim.CloudletScheduler
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared
import org.cloudbus.cloudsim.Vm

/**
* A custom Linux Virtual Machine
*/
class LinuxVm(val algorithmType: AlgorithmType, val brokerId: Int) {

    // VM configs
    private var vmId = 1; // VM ID
//    private var userId = 1 // user ID
    private var mips = 4_000.0; // MIPS (Million Instructions Per Second)
    private var numberOfPes = 1; // Number of CPU cores
    private var ram = 1_024; // VM memory (MB)
    private var bw = 1_000L; // Bandwidth (MB/s)
    private var size = 10_000L; // Storage (MB)
    private var vmm = "Xen"; // Virtual Machine Monitor, or 'Hypervisor'

    val instance = Vm(vmId, brokerId, mips, numberOfPes, ram, bw, size, vmm, chooseScheduler())

    private fun chooseScheduler(): CloudletScheduler {
        // TODO: make custom Scheduler by extending CloudletScheduler
        return when(algorithmType){
            AlgorithmType.FCFS -> CloudletSchedulerSpaceShared()
            AlgorithmType.SJF -> CloudletSchedulerSpaceShared()
        }
    }
}