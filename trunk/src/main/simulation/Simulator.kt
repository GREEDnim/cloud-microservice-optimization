package main.simulation

import main.infrastructure.Claudius
import main.services.Dockerfile
import main.infrastructure.LinuxVm
import org.cloudbus.cloudsim.Cloudlet
import org.cloudbus.cloudsim.DatacenterBroker
import org.cloudbus.cloudsim.Host
import org.cloudbus.cloudsim.Log
import org.cloudbus.cloudsim.Vm
import org.cloudbus.cloudsim.core.CloudSim
import java.text.DecimalFormat
import java.util.Calendar
import kotlin.random.Random

/**
 * Run various algorithms to simulate microservices in a cloud environment. Creates a Datacenter, a vm, tasks
 * applies the requested algorithm and runs simulation
 *
 * @param microServices count of tasks, known as cloudlets
 * @param algorithmType choose a algorithm
 * @param hosts count of vm
 */
class Simulator (
        private val microServices: Int,
        private val algorithmType: AlgorithmType,
        private val hosts: Int,
) {

    val init = CloudSim.init(1, Calendar.getInstance(), true)

    val datacenter = connectToDatacenter(hosts).datacenter
    val datacenterBroker = DatacenterBroker("Broker")
    val vm = LinuxVm(algorithmType, datacenterBroker.id).instance
    val containers = containerizeDockerfiles(microServices, datacenterBroker.id)

    private fun containerizeDockerfiles(count: Int, brokerId: Int): List<Dockerfile> {
        val dockerfiles = mutableListOf<Dockerfile>()
        val random = Random.Default

        for (serviceId in 1..count) {
            val serviceSpeed = random.nextLong(600L, 1_200L) // between 600 and 1200 Million Instructions
            val cores = 1
            val requestPayload = random.nextLong(1_024L, 2_048L)  // 1-2KB
            val responsePayload = random.nextLong(3_072L, 5_120L) // 3-5KB

            val dockerfile = Dockerfile(serviceId, serviceSpeed, cores, requestPayload, responsePayload)
            dockerfile.task.userId = brokerId
            dockerfiles.add(dockerfile)
        }

        // TODO: read from file using deserializeDockerfile()
        return dockerfiles
    }
    private fun connectToDatacenter(hosts:Int): Claudius {
        return Claudius()
    }

    fun simulate(){
        checkDependencies("before submission")
        datacenterBroker.submitVmList(listOf(vm))
        datacenterBroker.submitCloudletList(containers.map { it.task })
        checkDependencies("after submission")
        CloudSim.startSimulation()
        val dockerfileTrace = datacenterBroker.getCloudletReceivedList<Cloudlet>()
        // TODO: understand what gets returned and print the trace (use this to plot the graphs and etc)
        CloudSim.stopSimulation()
        reportGeneration(dockerfileTrace)
    }

    private fun reportGeneration(dockerfileTrace: List<Cloudlet>){

        checkDependencies("after simulation")
        val indent = "    "
        Log.printLine()
        Log.printLine("========== RUN REPORT ==========")
        Log.printLine("DOCKER ID" + indent + "STATUS" + indent
                + "Claudius ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Service Start" + indent + "Service Finish")

        val precision = DecimalFormat("###.##")

        for(i in 0 until microServices) {
            val task = dockerfileTrace[i]
            Log.printLine(indent + task.cloudletId + indent + indent)

            if (task.status == Cloudlet.SUCCESS) {
                Log.print("SUCCESS")
                Log.printLine(indent + indent + task.resourceId + indent + indent + indent + task.vmId
                        + indent + indent + indent + precision.format(task.actualCPUTime)
                        + indent + indent + precision.format(task.execStartTime) + indent + indent + indent + precision.format(task.finishTime))
                // TODO: compute other averages / percentage based on runtime etc
            }
        }
    }

    fun checkDependencies(phase: String){
        println("Dependencies: $phase phase")
        println("VMs: ${datacenter.getVmList<Vm>()} ${datacenterBroker.getVmList<Vm>()} ${datacenterBroker.getVmsCreatedList<Vm>()}")
        println("HOST: ${datacenter.getHostList<Host>()} ${vm.host}")
        println("TASK: ${datacenterBroker.getCloudletSubmittedList<Cloudlet>()}")
        println("BRK: ${datacenterBroker.id}")
    }
}

enum class AlgorithmType {
    FCFS,
    SJF
}

private fun serializeDockerfiles(){
    // TODO: write Dockerfiles into a file and read from there instead of regenerating everytime. also for consistent simulation across algorithms
    // and put into a separate file pls
}

fun main(){
    val simulator = Simulator(10,AlgorithmType.FCFS,1)
    simulator.simulate()
}

// TODO: write output in files
// TODO: initialize cloudsim library