package io.onedev.server.commandhandler;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AbstractPlugin;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.OneDev;
import io.onedev.server.jetty.MaintenanceProbeServer;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import static io.onedev.server.persistence.PersistenceUtils.callWithLock;
import static io.onedev.server.persistence.PersistenceUtils.openConnection;

public abstract class CommandHandler extends AbstractPlugin {
	
	private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

	private final HibernateConfig hibernateConfig;
	
	public CommandHandler(HibernateConfig hibernateConfig) {
		this.hibernateConfig = hibernateConfig;
	}
	
	protected <T> T doMaintenance(Callable<T> callable) {
		try {
			var maintenanceFile = OneDev.getMaintenanceFile(Bootstrap.installDir);
			if (maintenanceFile.exists()) {
				waitForServerStop();
				return callWithProbe(callable);
			} else if (!hibernateConfig.isHSQLDialect()) {
				try (var conn = openConnection(hibernateConfig, Thread.currentThread().getContextClassLoader())) {
					return callWithLock(conn, () -> {
						FileUtils.touchFile(maintenanceFile);
						try {
							waitForServerStop();
							return callWithProbe(callable);
						} finally {
							FileUtils.deleteFile(maintenanceFile);
						}
					});
				}
			} else {
				FileUtils.touchFile(maintenanceFile);
				try {
					waitForServerStop();
					return callWithProbe(callable);
				} finally {
					FileUtils.deleteFile(maintenanceFile);
				}
			}
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
	}

	private <T> T callWithProbe(Callable<T> callable) throws Exception {
		try (var ignored = MaintenanceProbeServer.start(Bootstrap.installDir)) {
			return callable.call();
		}
	}

	private void waitForServerStop() throws InterruptedException {
		logger.info("Waiting for server to stop...");
		while (OneDev.isServerRunning(Bootstrap.installDir)) {
			Thread.sleep(1000);
		}
	}
}
