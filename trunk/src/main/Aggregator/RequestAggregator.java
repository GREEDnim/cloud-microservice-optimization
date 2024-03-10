package main.Aggregator;

import main.Optimizer.RouteDiscovery;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;

import java.util.List;

public class RequestAggregator extends DatacenterBroker {

    public RequestAggregator(String name) throws Exception {
        super(name);
    }

    @Override
    public void submitCloudletList(List<? extends Cloudlet> list) {
        super.submitCloudletList(list);
         RouteDiscovery ref = RouteDiscovery.getInstance(getCloudletList());
         ref.initiateOptimization();
         System.out.println("REF created");
    }
}
