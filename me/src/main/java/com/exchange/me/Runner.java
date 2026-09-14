package com.exchange.me;


import com.exchange.me.config.ClusterConfig;
import com.exchange.me.config.Environment;
import io.aeron.cluster.ClusteredMediaDriver;
import io.aeron.cluster.service.ClusteredServiceContainer;
import org.agrona.concurrent.ShutdownSignalBarrier;

public class Runner {
 public static void main(String[] args) {
        System.out.println("Starting core service ...");

        final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();
        final ClusterConfig clusterConfig =
                new ClusterConfig(Environment.getInstance(), barrier);

        try (
            ClusteredMediaDriver ignored =
                    ClusteredMediaDriver.launch(
                            clusterConfig.getMediaDriverContext(),
                            clusterConfig.getArchiveContext(),
                            clusterConfig.getConsensusContext());

            ClusteredServiceContainer ignored1 =
                    ClusteredServiceContainer.launch(
                            clusterConfig.getClusteredServiceContext())
        ) {
            System.out.println("Cluster started successfully :)");

            barrier.await();

            System.out.println("Shutting down cluster node...");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error starting cluster node");
        }
    }
}