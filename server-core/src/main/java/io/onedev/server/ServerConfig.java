package io.onedev.server;

import com.google.common.base.Splitter;
import io.onedev.server.persistence.HibernateConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static io.onedev.commons.utils.FileUtils.loadProperties;
import static java.lang.Integer.parseInt;

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
			httpPort = parseInt(httpPortStr.trim());
		} else {
			logger.warn(PROP_HTTP_PORT + " not specified, default to 6610");
			httpPort = 6610;
		}

		String sshPortStr = System.getenv(PROP_SSH_PORT);
		if (StringUtils.isBlank(sshPortStr))
			sshPortStr = props.getProperty(PROP_SSH_PORT);
		if (StringUtils.isNotBlank(sshPortStr)) {
			sshPort = parseInt(sshPortStr.trim());
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
				Map<String, Integer> dbPorts = new HashMap<>();
				String tempStr = StringUtils.substringAfter(dbUrl, "//");
				if (dbUrl.startsWith("mysql") || dbUrl.startsWith("mariadb")) {
					if (tempStr.contains(":")) {
						String dbHost = StringUtils.substringBefore(tempStr, ":");
						tempStr = StringUtils.substringAfter(tempStr, ":");
						tempStr = StringUtils.substringBefore(tempStr, "/");
						dbPorts.put(dbHost, parseInt(tempStr));
					} else {
						String dbHost = StringUtils.substringBefore(tempStr, "/");
						dbPorts.put(dbHost, 3306);
					}
				} else if (dbUrl.startsWith("sqlserver")) {
					if (tempStr.contains(":")) {
						String dbHost = StringUtils.substringBefore(tempStr, ":");
						tempStr = StringUtils.substringAfter(tempStr, ":");
						tempStr = StringUtils.substringBefore(tempStr, ";");
						dbPorts.put(dbHost, parseInt(tempStr));
					} else {
						String dbHost = StringUtils.substringBefore(tempStr, ";");
						dbPorts.put(dbHost, 1433);
					}
				} else {
					tempStr = StringUtils.substringBefore(tempStr, "/");
					for (var part: Splitter.on(",").omitEmptyStrings().trimResults().split(tempStr)) {
						if (part.contains(":")) {
							dbPorts.put(StringUtils.substringBefore(part, ":"), 
									parseInt(StringUtils.substringAfter(part, ":")));
						} else {
							dbPorts.put(part, 5432);
						}
					}
				}

				for (var entry: dbPorts.entrySet()) {
					try (Socket socket = new Socket()) {
						socket.connect(new InetSocketAddress(entry.getKey(), entry.getValue()));
						clusterIp = socket.getLocalAddress().getHostAddress();
						break;
					} catch (Exception e) {
						logger.warn(String.format("Connection failed (host: %s, port: %d)", entry.getKey(), entry.getValue()), e);
					}
				}
				if (StringUtils.isBlank(clusterIp)) 
					throw new RuntimeException("Unable to discover cluster ip from database connection url: " + dbUrl);
			}
		}
		this.clusterIp = clusterIp;

		String clusterPortStr = System.getenv(PROP_CLUSTER_PORT);
		if (StringUtils.isBlank(clusterPortStr))
			clusterPortStr = props.getProperty(PROP_CLUSTER_PORT);
		if (StringUtils.isBlank(clusterPortStr))
			clusterPort = 5710;
		else
			clusterPort = parseInt(clusterPortStr.trim());
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
