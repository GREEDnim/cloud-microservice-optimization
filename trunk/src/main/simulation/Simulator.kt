package main.simulation

import main.infrastructure.Claudius
import main.infrastructure.LinuxVm
import main.services.Dockerfile
import main.services.Task
import main.Aggregator.RequestAggregator
import org.cloudbus.cloudsim.Cloudlet
import org.cloudbus.cloudsim.DatacenterBroker
import org.cloudbus.cloudsim.Host
import org.cloudbus.cloudsim.Vm
import org.cloudbus.cloudsim.core.CloudSim
import java.io.File
import java.io.FileWriter
import java.text.DecimalFormat
import java.util.*
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
    val datacenterBroker = RequestAggregator("Broker")
    val vm = LinuxVm(algorithmType, datacenterBroker.id).instance
    val containers = containerizeDockerfiles(microServices, datacenterBroker.id)

    private fun containerizeDockerfiles(count: Int, brokerId: Int): List<Dockerfile> {
        val dockerfiles = mutableListOf<Dockerfile>()
        val random = Random(69)

        for (serviceId in 1..count) {
            val serviceSpeed = random.nextLong(6000L, 1_2000L) // between 600 and 1200 Million Instructions
            val cores = 1
            val requestPayload = random.nextLong(1_024L, 2_048L)  // 1-2KB
            val responsePayload = random.nextLong(3_072L, 5_120L) // 3-5KB
            val groupId=random.nextInt(0,10);
            val dockerfile = Dockerfile(serviceId, serviceSpeed, cores, requestPayload, responsePayload,groupId)
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
//        val formatString = "%-15s%-15s%-15s%-15s%-15s%-10s%-15s%-15s"
//        val header = String.format(formatString, "DOCKER ID","GROUP ID", "STATUS", "Claudius ID", "VM ID", "Time", "Start", "Finish")

        val formatString = "%s,%s,%s,%s,%s,%s,%s,%s,%s"
        val header = String.format(formatString, "DOCKER ID","GROUP ID", "STATUS", "Claudius ID", "VM ID", "Time", "Start", "Finish","cloudletLength")

        println("========== RUN REPORT ==========")
        val filePath ="Z:\\cloud-microservice-optimization\\trunk\\src\\main\\outputs\\${algorithmType}.csv"
        var writer = FileWriter(filePath)
        writer.close()
        writer = FileWriter(filePath, true)
        writer.write("${header}\n")

        println(header)

        val precision = DecimalFormat("###.##")

        for(i in 0 until microServices) {
            val task = dockerfileTrace[i] as Task

            if (task.status == Cloudlet.SUCCESS) {
                val data = String.format(formatString,
                    task.cloudletId,
                    task.groupId,
                    "SUCCESS",
                    datacenter.id,
                    task.vmId,
                    precision.format(task.actualCPUTime),
                    precision.format(task.execStartTime),
                    precision.format(task.finishTime),
                    task.cloudletLength
                )
                println(data)
                writer.write("${data}\n")
                // TODO: compute other averages / percentage based on runtime etc
            }
        }
        writer.close()
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
    SJF,
    ANT
}

private fun serializeDockerfiles(){
    // TODO: write Dockerfiles into a file and read from there instead of regenerating everytime. also for consistent simulation across algorithms
    // and put into a separate file pls
}

fun main(){
    val simulator = Simulator(1000,AlgorithmType.SJF,1)
    simulator.simulate()
}

// TODO: write output in files
// TODO: initialize cloudsim library