package com.exchange.core;

import io.aeron.archive.Archive;
import io.aeron.archive.client.AeronArchive;
import io.aeron.cluster.ClusteredMediaDriver;
import io.aeron.cluster.ConsensusModule;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import java.io.File;
import org.agrona.concurrent.ShutdownSignalBarrier;

public class Runner {

  private static final String AERON_DIR_NAME = "./media-driver";
  private static final File BASE_DIR = new File("./cluster-data");

  // memberId,clientFacingEndpoint,memberFacingEndpoint,logEndpoint,transferEndpoint,archiveEndpoint
  private static final String CLUSTER_MEMBERS =
      "0,localhost:9002,localhost:9003,localhost:9004,localhost:9005,localhost:9001";
  private static final String ARCHIVE_CONTROL_CHANNEL = "aeron:udp?endpoint=localhost:9001";
  private static final String ARCHIVE_LOCAL_CONTROL_CHANNEL = "aeron:ipc?term-length=64k";

  public static void main(String[] args) {
    final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();

    final MediaDriver.Context mediaDriverContext =
        new MediaDriver.Context()
            .aeronDirectoryName(AERON_DIR_NAME)
            .threadingMode(ThreadingMode.SHARED)
            .dirDeleteOnStart(true)
            .dirDeleteOnShutdown(true)
            .terminationHook(barrier::signal);

    final Archive.Context archiveContext =
        new Archive.Context()
            .aeronDirectoryName(AERON_DIR_NAME)
            .archiveDir(new File(BASE_DIR, "archive"))
            .controlChannel(ARCHIVE_CONTROL_CHANNEL)
            .replicationChannel("aeron:udp?endpoint=localhost:0")
            .deleteArchiveOnStart(true);

    final AeronArchive.Context aeronArchiveContext =
        new AeronArchive.Context()
            .aeronDirectoryName(AERON_DIR_NAME)
            .controlRequestChannel(ARCHIVE_LOCAL_CONTROL_CHANNEL)
            .controlResponseChannel(ARCHIVE_LOCAL_CONTROL_CHANNEL);

    final ConsensusModule.Context consensusModuleContext =
        new ConsensusModule.Context()
            .aeronDirectoryName(AERON_DIR_NAME)
            .clusterDir(new File(BASE_DIR, "cluster"))
            .clusterMemberId(0)
            .clusterMembers(CLUSTER_MEMBERS)
            .ingressChannel("aeron:udp?term-length=64k")
            .replicationChannel("aeron:udp?endpoint=localhost:0")
            .archiveContext(aeronArchiveContext.clone())
            .deleteDirOnStart(true)
            .terminationHook(barrier::signal);

    final ClusteredServiceContainer.Context serviceContainerContext =
        new ClusteredServiceContainer.Context()
            .aeronDirectoryName(AERON_DIR_NAME)
            .clusterDir(new File(BASE_DIR, "cluster"))
            .archiveContext(aeronArchiveContext.clone())
            .clusteredService(new CoreClusteredService());

    try (ClusteredMediaDriver clusteredMediaDriver =
            ClusteredMediaDriver.launch(
                mediaDriverContext, archiveContext, consensusModuleContext);
        ClusteredServiceContainer container =
            ClusteredServiceContainer.launch(serviceContainerContext)) {

      System.out.println("Started cluster node...");
      barrier.await();
      System.out.println("Shutting down cluster node...");

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      barrier.close();
    }
  }
}
