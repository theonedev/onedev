package io.onedev.server;

import com.google.common.base.Splitter;
import io.onedev.agent.Agent;
import io.onedev.server.persistence.HibernateConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import static io.onedev.commons.utils.FileUtils.loadProperties;

public class ServerConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);

	private static final String PROP_HTTP_PORT = "http_port";

	private static final String PROP_SSH_PORT = "ssh_port";

	private static final String PROP_SERVER_NAME = "server_name";
	
	private static final String PROP_CLUSTER_IP = "cluster_ip";

	private static final String PROP_CLUSTER_PORT = "cluster_port";

	private final int httpPort;

	private final int sshPort;

	private final String serverName;
	
	private final String clusterIp;

	private final int clusterPort;

	public ServerConfig(File installDir) {
		File file = new File(installDir, "conf/server.properties");
		Properties props = loadProperties(file);
		
		String httpPortStr = System.getenv(PROP_HTTP_PORT);
		if (StringUtils.isBlank(httpPortStr))
			httpPortStr = props.getProperty(PROP_HTTP_PORT);
		if (StringUtils.isNotBlank(httpPortStr)) {
			httpPort = Integer.parseInt(httpPortStr.trim());
		} else {
			logger.warn(PROP_HTTP_PORT + " not specified, default to 6610");
			httpPort = 6610;
		}

		String sshPortStr = System.getenv(PROP_SSH_PORT);
		if (StringUtils.isBlank(sshPortStr))
			sshPortStr = props.getProperty(PROP_SSH_PORT);
		if (StringUtils.isNotBlank(sshPortStr)) {
			sshPort = Integer.parseInt(sshPortStr.trim());
		} else {
			logger.warn(PROP_SSH_PORT + " not specified, default to 6611");
			sshPort = 6611;
		}

		String serverName = System.getenv(PROP_SERVER_NAME);
		if (StringUtils.isBlank(serverName))
			serverName = props.getProperty(PROP_SERVER_NAME);
		if (StringUtils.isBlank(serverName)) 
			serverName = System.getenv("HOSTNAME");
		if (StringUtils.isBlank(serverName)) {
			try {
				serverName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			}
		}
		this.serverName = serverName;
		
		String clusterIp = System.getenv(PROP_CLUSTER_IP);
		if (StringUtils.isBlank(clusterIp))
			clusterIp = props.getProperty(PROP_CLUSTER_IP);

		if (StringUtils.isBlank(clusterIp)) {
			HibernateConfig hibernateConfig = new HibernateConfig(installDir);
			String dbUrl = StringUtils.substringAfter(hibernateConfig.getUrl(), ":");
			if (dbUrl.startsWith("hsqldb")) {
				clusterIp = "127.0.0.1";
			} else {
				String tempStr = StringUtils.substringAfter(dbUrl, "//");
				String dbHost = StringUtils.substringBefore(tempStr, ":");
				tempStr = StringUtils.substringAfter(tempStr, ":");
				int dbPort;
				if (dbUrl.startsWith("sqlserver")) {
					dbPort = Integer.parseInt(StringUtils.substringBefore(tempStr, ";"));
				} else {
					tempStr = StringUtils.substringBefore(tempStr, "/");
					// Fix issue https://code.onedev.io/onedev/server/~issues/1086
					tempStr = StringUtils.substringBefore(tempStr, ",");
					dbPort = Integer.parseInt(tempStr);
				}

				try (Socket socket = new Socket()) {
					socket.connect(new InetSocketAddress(dbHost, dbPort));
					clusterIp = socket.getLocalAddress().getHostAddress();
				} catch (Exception e) {
					logger.error("Unable to discover cluster ip, using arbitrary ip address", e);
					try {
						clusterIp = InetAddress.getLocalHost().getHostAddress();
					} catch (Exception e2) {
						logger.error("Unable to get local ip address, using loopback instead", e);
						clusterIp = "127.0.0.1";
					}
				}
			}
		}
		this.clusterIp = clusterIp;

		String clusterPortStr = System.getenv(PROP_CLUSTER_PORT);
		if (StringUtils.isBlank(clusterPortStr))
			clusterPortStr = props.getProperty(PROP_CLUSTER_PORT);
		if (StringUtils.isBlank(clusterPortStr))
			clusterPort = 5710;
		else
			clusterPort = Integer.parseInt(clusterPortStr.trim());
	}

	public int getHttpPort() {
		return httpPort;
	}

	public int getSshPort() {
		return sshPort;
	}

	public String getClusterIp() {
		return clusterIp;
	}

	public int getClusterPort() {
		return clusterPort;
	}
	
	public String getServerName() {
		return serverName;
	}
	
}
