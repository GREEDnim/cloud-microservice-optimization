package main.services
import org.cloudbus.cloudsim.Cloudlet
import org.cloudbus.cloudsim.UtilizationModel
import org.cloudbus.cloudsim.UtilizationModelFull

/** Represents a microservices hosted using Dockerfile on a VM
 *
 * @property serviceId The unique identifier for the service.
 * @property serviceSpeed MillionInstructions speed for this dockerized microservice.
 * @property cores The number of CPU cores allocated for the service.
 * @property requestPayload The size of the request payload in bytes.
 * @property responsePayload The size of the response payload in bytes.
 */
data class Dockerfile (
        val serviceId: Int,
        val serviceSpeed: Long,
        val cores: Int,
        val requestPayload: Long,
        val responsePayload: Long,
        val groupId:Int,
) {
    private val usageCap = UtilizationModelFull()
    val task = Task(
            serviceId,
            serviceSpeed,
            cores,
            requestPayload,
            responsePayload,
            groupId,
            usageCap
    )
}

class Task(
        cloudletId: Int,
        cloudletLength: Long,
        pesNumber: Int,
        cloudletFileSize: Long,
        cloudletOutputSize: Long,
        val groupId: Int,
        usageCap: UtilizationModel = UtilizationModelFull(),
) : Cloudlet(
        cloudletId,
        cloudletLength,
        pesNumber,
        cloudletFileSize,
        cloudletOutputSize,
        usageCap,
        usageCap,
        usageCap
) {
    // You can add additional properties and methods for the Task class if needed
}

// Dockerfiles - Tasks operations
// TODO: power
// TODO: Million Instructions
// TODO: Cloudlet schedulers
// TODO: IO file (can be used to pass around multiple tasks ig)
// TODO: Assign to a VM
// TODO: Consume a fixed amount of resource from VM
// TODO: how to chain Dockerfiles ?

// LinuxVm - VM Operations
// TODO: Define vm resources [CPU, MIPS, RAM, BANDWIDTH, STORAGE_SIZE]
// TODO: Define vm scheduling policy [time / space shared]
// TODO: Handle cluster of Dockerfile parallelly, but, Dockerfile runs sequentially - need to refine
// TODO: use methods offered by cloudlet to get execution time, logs, and other performance metrics
// ADVANCED:
// TODO: PowerVM CPU utilization
// TODO: NetworkedVM bandwidth utilization
// TODO: Extend existing VM with custom billing (pay by millisecond)

// Datacenter Operations
// TODO: Create Broker
// ADVANCED:
// TODO: Power-aware Datacenter
// TODO: Network-aware Datacenter
// TODO: Aggregate billing

