package com.exchange.me;


import com.exchange.me.config.ClusterConfig;
import com.exchange.me.config.ClusterMetrics;
import com.exchange.me.config.Environment;
import com.exchange.me.config.MetricsHttpServer;
import io.aeron.cluster.ClusteredMediaDriver;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.agrona.concurrent.ShutdownSignalBarrier;

public class Runner {
 public static void main(String[] args) {
        System.out.println("Starting core service ...");

     final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();
     final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
     final ClusterMetrics metrics = new ClusterMetrics(registry);
     final ClusterConfig clusterConfig = new ClusterConfig(Environment.getInstance(), barrier, metrics);

     //clusterConfig.getClusteredServiceContext().clusteredService(new CoreClusteredService(metrics));

        try (
                MetricsHttpServer metricsServer = new MetricsHttpServer(registry, 9400);
                ClusteredMediaDriver driver =
                        ClusteredMediaDriver.launch(
                                clusterConfig.getMediaDriverContext(),
                                clusterConfig.getArchiveContext(),
                                clusterConfig.getConsensusContext());

                ClusteredServiceContainer container =
                        ClusteredServiceContainer.launch(
                                clusterConfig.getClusteredServiceContext())
        ) {
            System.out.println("Cluster started successfully :)");
            System.out.println("Metrics available at http://localhost:9400/metrics");
            barrier.await();
            System.out.println("Shutting down cluster node...");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error starting cluster node");
        }
    }
}