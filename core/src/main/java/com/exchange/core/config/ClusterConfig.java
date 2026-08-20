package com.exchange.core.config;

import com.exchange.core.CoreClusteredService;
import io.aeron.archive.Archive;
import io.aeron.archive.client.AeronArchive;
import io.aeron.cluster.ConsensusModule;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import org.agrona.concurrent.ShutdownSignalBarrier;

import java.io.File;

public class ClusterConfig {
    private static final String AERON_DIR_NAME = "./media-driver";
    private static final File BASE_DIR = new File("./cluster-data");

    private static final String CLUSTER_MEMBERS =
            "0,localhost:9002,localhost:9003,localhost:9004,localhost:9005,localhost:9001";
    private static final String ARCHIVE_CONTROL_CHANNEL = "aeron:udp?endpoint=localhost:9001";
    private static final String ARCHIVE_LOCAL_CONTROL_CHANNEL = "aeron:ipc?term-length=64k";

    private final MediaDriver.Context mediaDriverContext;
    private final Archive.Context archiveContext;
    private final AeronArchive.Context aeronContext;
    private final ConsensusModule.Context consensusContext;
    private final ClusteredServiceContainer.Context clusteredServiceContext;

    public ClusterConfig(Environment env, ShutdownSignalBarrier barrier) {
        mediaDriverContext =
                new MediaDriver.Context()
                        .aeronDirectoryName(AERON_DIR_NAME)
                        .threadingMode(ThreadingMode.SHARED)
                        .dirDeleteOnStart(true)
                        .dirDeleteOnShutdown(true)
                        .terminationHook(barrier::signal);

        archiveContext = new Archive.Context()
                .aeronDirectoryName(AERON_DIR_NAME)
                .archiveDir(new File(BASE_DIR, "archive"))
                .controlChannel(ARCHIVE_CONTROL_CHANNEL)
                .replicationChannel("aeron:udp?endpoint=localhost:0")
                .deleteArchiveOnStart(false);

        aeronContext = new AeronArchive.Context()
                .aeronDirectoryName(AERON_DIR_NAME)
                .controlRequestChannel(ARCHIVE_LOCAL_CONTROL_CHANNEL)
                .controlResponseChannel(ARCHIVE_LOCAL_CONTROL_CHANNEL);

        consensusContext = new ConsensusModule.Context()
                .aeronDirectoryName(AERON_DIR_NAME)
                .clusterDir(new File(BASE_DIR, "cluster"))
                .clusterMemberId(0)
                .clusterMembers(CLUSTER_MEMBERS)
                .ingressChannel("aeron:udp?term-length=64k")
                .replicationChannel("aeron:udp?endpoint=localhost:0")
                .archiveContext(aeronContext.clone())
                .deleteDirOnStart(false)
                .terminationHook(barrier::signal);

        clusteredServiceContext = new ClusteredServiceContainer.Context()
                .aeronDirectoryName(AERON_DIR_NAME)
                .clusterDir(new File(BASE_DIR, "cluster"))
                .archiveContext(aeronContext.clone())
                .clusteredService(new CoreClusteredService());
    }

    public MediaDriver.Context getMediaDriverContext() {
        return mediaDriverContext;
    }

    public Archive.Context getArchiveContext() {
        return archiveContext;
    }

    public AeronArchive.Context getAeronContext() {
        return aeronContext;
    }

    public ConsensusModule.Context getConsensusContext() {
        return consensusContext;
    }

    public ClusteredServiceContainer.Context getClusteredServiceContext() {
        return clusteredServiceContext;
    }
}
