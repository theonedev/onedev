package io.onedev.server.agent;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.agent.AgentData;
import io.onedev.agent.CallData;
import io.onedev.agent.Message;
import io.onedev.agent.MessageTypes;
import io.onedev.agent.WaitingForAgentResourceToBeReleased;
import io.onedev.agent.WantToDisconnectAgent;
import io.onedev.agent.WebsocketUtils;
import io.onedev.agent.shell.ShellOutputRequest;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.exception.ServerNotReadyException;
import io.onedev.server.job.AgentShell;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobService;
import io.onedev.server.logging.LogService;
import io.onedev.server.service.AgentService;
import io.onedev.server.service.ResourceService;

@WebSocket
public class ServerSocket {

	private static final Logger logger = LoggerFactory.getLogger(ServerSocket.class);

	private Session session;

	private Long agentId;

	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		try {
			if (agentId != null)
				getAgentService().agentDisconnected(agentId);

			StringBuilder builder = new StringBuilder("Websocket closed (");
			if (session != null && session.getRemoteAddress() != null)
				builder.append("remote address: " + session.getRemoteAddress().toString() + ", ");
			builder.append("status code: " + statusCode);
			if (reason != null)
				builder.append(", reason: " + reason);
			builder.append(")");
			logger.debug(builder.toString());
		} catch (ServerNotReadyException e) {
		}
	}

	@OnWebSocketError
	public void onError(Throwable t) {
		if (session != null) {
			logger.error("Websocket error (remote address: " + session.getRemoteAddress().toString() + ")", t);
			try {
				session.disconnect();
			} catch (IOException e) {
			}
		} else {
			logger.error("Websocket error", t);
		}
	}

	private AgentService getAgentService() {
		return OneDev.getInstance(AgentService.class);
	}

	@OnWebSocketConnect
	public void onConnect(Session session) {
		this.session = session;
		try {
			new Message(MessageTypes.UPDATE, getAgentService().getAgentVersion()).sendBy(session);
		} catch (Exception e) {
			logger.error("Error sending websocket message", e);
			try {
				session.disconnect();
			} catch (IOException ignored) {
			}
		}
	}

	@OnWebSocketMessage
	public void onMessage(byte[] bytes, int offset, int count) {
		Message message = Message.of(bytes, offset, count);
		byte[] messageData = message.getData();
		try {
			switch (message.getType()) {
				case AGENT_DATA:
					// It is fine to deserialize from agent as they have valid agent token which can only 
					// be assigned via Administrator
					AgentData data = SerializationUtils.deserialize(message.getData());
					try {
						agentId = getAgentService().agentConnected(data, session);
					} catch (Exception e) {
						var explicitException = ExceptionUtils.find(e, ExplicitException.class);
						if (explicitException != null) {
							new Message(MessageTypes.ERROR, e.getMessage()).sendBy(session);
						} else {
							logger.error("Error connecting agent", e);
							new Message(MessageTypes.ERROR, "Internal server error, check log for details").sendBy(session);
						}
					}
					break;
				case REQUEST:
					OneDev.getInstance(ExecutorService.class).execute(() -> {
						try {
							CallData request = SerializationUtils.deserialize(messageData);
							CallData response = new CallData(request.getUuid(), service(request.getPayload()));
							new Message(MessageTypes.RESPONSE, response).sendBy(session);
						} catch (Exception e) {
							logger.error("Error processing websocket request", e);
						}
					});
					break;
				case RESPONSE:
					WebsocketUtils.onResponse(SerializationUtils.deserialize(messageData));
					break;
				case JOB_LOG:
					try {
						String dataString = new String(messageData, StandardCharsets.UTF_8);
						String jobToken = StringUtils.substringBefore(dataString, ":");
						String remaining = StringUtils.substringAfter(dataString, ":");
						String sessionId = StringUtils.substringBefore(remaining, ":");
						if (sessionId.length() == 0)
							sessionId = null;
						String logMessage = StringUtils.substringAfter(remaining, ":");
						TaskLogger logger = OneDev.getInstance(LogService.class).getLogger(jobToken);
						if (logger != null)
							logger.log(logMessage, sessionId);
					} catch (Exception e) {
						logger.error("Error processing job log", e);
					}
					break;
				case REPORT_JOB_WORKSPACE:
					String dataString = new String(messageData, StandardCharsets.UTF_8);
					String jobToken = StringUtils.substringBefore(dataString, ":");
					String jobWorkDir = StringUtils.substringAfter(dataString, ":");
					JobContext jobContext = getJobService().getJobContext(jobToken, false);
					if (jobContext != null)
						getJobService().reportJobWorkDir(jobContext, jobWorkDir);
					break;
				case SHELL_EXIT:
					String sessionId = new String(messageData, StandardCharsets.UTF_8);
					AgentShell shell = (AgentShell) getJobService().getShell(sessionId);
					if (shell != null)
						shell.getTerminal().onShellExit();
					break;
				default:
			}
		} catch (Exception e) {
			logger.error("Error processing websocket message (remote address: " + session.getRemoteAddress().toString() + ")", e);
			try {
				session.disconnect();
			} catch (IOException e2) {
			}
		}
	}

	private JobService getJobService() {
		return OneDev.getInstance(JobService.class);
	}

	private Serializable service(Serializable request) {
		try {
			if (request instanceof WantToDisconnectAgent || request instanceof WaitingForAgentResourceToBeReleased) {
				if (agentId != null)
					OneDev.getInstance(ResourceService.class).agentDisconnecting(agentId);
				return null;
			} else if (request instanceof ShellOutputRequest shellOutputRequest) {
				AgentShell shell = (AgentShell) getJobService().getShell(shellOutputRequest.getSessionId());
				if (shell != null)
					shell.getTerminal().onShellOutput(shellOutputRequest.getBase64Data());
				return null;
			} else {
				throw new ExplicitException("Unknown request: " + request.getClass());
			}
		} catch (Exception e) {
			logger.error("Error servicing websocket request", e);
			return e;
		}
	}

}