package com.exchange.core;

import io.aeron.ChannelUriStringBuilder;
import io.aeron.archive.Archive;
import io.aeron.cluster.ConsensusModule;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.aeron.driver.MediaDriver;
import org.agrona.concurrent.ShutdownSignalBarrier;

public class Runner {
  public static void main(String[] args) {
    try {

      MediaDriver.Context mContext = new MediaDriver.Context().aeronDirectoryName("./media-driver");
      MediaDriver mediaDriver = MediaDriver.launch(mContext);
        
      Archive.Context context =
          new Archive.Context()
              .aeronDirectoryName("./media-driver")
              .archiveDirectoryName("./cluster-data/archive") 
              .controlChannel(
                  new ChannelUriStringBuilder()
                      .media("udp")
                      .termLength(65536)
                      .endpoint("localhost" + ":" + 9001)
                      .build())
              .replicationChannel("aeron:udp?endpoint=localhost:0");

      ConsensusModule.Context consContext =
          new ConsensusModule.Context().clusterMemberId(0).clusterMembers("localhost:9092");

      Archive archive = Archive.launch(context);
      ConsensusModule consensusModule = ConsensusModule.launch(consContext);

      ClusteredServiceContainer.Context clContext =
          new ClusteredServiceContainer.Context()
              .aeronDirectoryName("./media-driver")
              .clusterDirectoryName("./cluster-dir/consen");

      ClusteredServiceContainer.launch(clContext);

      new ShutdownSignalBarrier().await();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
