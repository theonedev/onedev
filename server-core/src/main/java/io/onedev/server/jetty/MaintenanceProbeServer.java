package io.onedev.server.jetty;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.ServerConfig;

public class MaintenanceProbeServer implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(MaintenanceProbeServer.class);

	private final Server server;

	private MaintenanceProbeServer(Server server) {
		this.server = server;
	}

	public static MaintenanceProbeServer start(File installDir) {
		var server = new Server();
		ServerConfig serverConfig = null;
		try {
			serverConfig = new ServerConfig(installDir);
			if (isMaintenanceProbeRunning(serverConfig))
				return new MaintenanceProbeServer(null);
			var connector = new ServerConnector(server);
			connector.setHost(serverConfig.getHttpHost());
			connector.setPort(serverConfig.getHttpPort());
			server.addConnector(connector);
			server.setHandler(new ProbeHandler(() -> false));
			server.start();
			return new MaintenanceProbeServer(server);
		} catch (Exception e) {
			try {
				server.stop();
			} catch (Exception ignored) {
			}
			if (serverConfig == null || !isMaintenanceProbeRunning(serverConfig))
				logger.warn("Unable to start maintenance probe server", e);
			return new MaintenanceProbeServer(null);
		}
	}

	private static boolean isMaintenanceProbeRunning(ServerConfig serverConfig) {
		var host = serverConfig.getHttpHost();
		if (host.equals("0.0.0.0") || host.equals("::"))
			host = "127.0.0.1";
		return getResponseCode(host, serverConfig.getHttpPort(), ProbeHandler.HEALTH_PATH) == HttpURLConnection.HTTP_OK
				&& getResponseCode(host, serverConfig.getHttpPort(), ProbeHandler.READINESS_PATH)
						== HttpURLConnection.HTTP_UNAVAILABLE;
	}

	private static int getResponseCode(String host, int port, String path) {
		try {
			var connection = (HttpURLConnection) new URL("http", host, port, path).openConnection();
			connection.setConnectTimeout(1000);
			connection.setReadTimeout(1000);
			connection.setRequestMethod("HEAD");
			try {
				return connection.getResponseCode();
			} finally {
				connection.disconnect();
			}
		} catch (Exception e) {
			return -1;
		}
	}

	@Override
	public void close() {
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				logger.warn("Unable to stop maintenance probe server", e);
			}
		}
	}

}
