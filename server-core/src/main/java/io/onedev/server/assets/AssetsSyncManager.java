package io.onedev.server.assets;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.event.Listen;
import io.onedev.server.event.cluster.NodeStarted;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.util.SiteSyncUtils;
import io.onedev.server.util.concurrent.BatchWorkExecutionService;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;

@Singleton
public class AssetsSyncManager {

    private static final Logger logger = LoggerFactory.getLogger(AssetsSyncManager.class);

    private static final int SYNC_PRIORITY = 50;

	@Inject
	private BatchWorkExecutionService batchWorkExecutionService;

	private static final BatchWorker SYNC_WORKER = new BatchWorker("assets-sync") {

		private static final long serialVersionUID = 1L;

		@Override
		public void doWorks(List<Prioritized> works) {
			var syncWithServer = ((AssetsSyncWork) works.get(works.size() - 1)).syncWithServer;
			try {
				SiteSyncUtils.syncDirectory(syncWithServer, "assets", false, null, null);
			} catch (Exception e) {
				logger.error(String.format("Error syncing assets from server '%s'", syncWithServer), e);
			}
		}
	};

	@Listen
	public void on(NodeStarted event) {
		requestToSync(event.getServer());
	}

	@Listen
	public void on(SystemStarted event) {
		var newestServer = SiteSyncUtils.findNewestServer("assets");
		if (newestServer != null)
			requestToSync(newestServer);
	}

	private void requestToSync(String syncWithServer) {
		batchWorkExecutionService.submit(SYNC_WORKER, new AssetsSyncWork(SYNC_PRIORITY, syncWithServer));
	}

	private static class AssetsSyncWork extends Prioritized {

		final String syncWithServer;

		AssetsSyncWork(int priority, String syncWithServer) {
			super(priority);
			this.syncWithServer = syncWithServer;
		}
	}

}
