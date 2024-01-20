package main.infrastructure

import org.cloudbus.cloudsim.Datacenter
import org.cloudbus.cloudsim.DatacenterCharacteristics
import org.cloudbus.cloudsim.Host
import org.cloudbus.cloudsim.Pe
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.VmAllocationPolicySimple
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple
import java.util.LinkedList

/**
 * This is our physical datacenter to mimic serverless environment
 */
class Claudius {

    // Host Configs
    private val mips = 5_000.0; // MIPS (Million Instructions Per Second)
    private val hostId = 0
    private val ram = 2_048 // host memory (MB)
    private val storage = 20_000L // host storage
    private val bw = 1_000L // Mbps

    private val computeProvisioner = PeProvisionerSimple(mips)
    private val bandwidthProvisioner = BwProvisionerSimple(bw)
    private val memoryProvisioner = RamProvisionerSimple(ram)

    private val physicalProcessingElements = listOf(Pe(0, computeProvisioner))
    private val scheduler = VmSchedulerTimeSharedOverSubscription(physicalProcessingElements)

    // Host is the literal CPU + OS that runs on bare metal, like your laptop. VM runs on this host
    private val hosts = listOf(
            Host(hostId, memoryProvisioner, bandwidthProvisioner, storage, physicalProcessingElements, scheduler))

    // Datacenter Configs
    private val arch = "x86"
    private val os = "Linux"
    private val vmm = "Xen"
    private val timeZone = 5.30               // time zone this resource located
    private val costCompute = 3.0             // processing per second (TODO: divide to achieve millisecond cost)
    private val costMem = 0.05                // memory per MB
    private val costStorage = 0.001           // storage per MB
    private val costBw = 0.0                  // per consumed total Mbps ?
    private val storageList = LinkedList<Storage>() // we are not adding SAN devices by now
    private val vmPolicy = VmAllocationPolicySimple(hosts)

    private val claudiusMetadata =
            DatacenterCharacteristics(arch, os, vmm, hosts, timeZone,costCompute, costMem, costStorage, costBw)

    val datacenter = Datacenter("claudius", claudiusMetadata, vmPolicy, storageList, 0.0)
}

// TODO: come to some sensible values for host provisioning