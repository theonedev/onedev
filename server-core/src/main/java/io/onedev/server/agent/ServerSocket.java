package io.onedev.server.agent;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

import io.onedev.agent.*;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.OneDev;
import io.onedev.server.service.AgentService;
import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.exception.ServerNotReadyException;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobService;
import io.onedev.server.job.ResourceAllocator;
import io.onedev.server.job.log.LogService;
import io.onedev.server.terminal.AgentShell;

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
						TaskLogger logger = OneDev.getInstance(LogService.class).getJobLogger(jobToken);
						if (logger != null)
							logger.log(logMessage, sessionId);
					} catch (Exception e) {
						logger.error("Error processing job log", e);
					}
					break;
				case REPORT_JOB_WORKSPACE:
					String dataString = new String(messageData, StandardCharsets.UTF_8);
					String jobToken = StringUtils.substringBefore(dataString, ":");
					String jobWorkspace = StringUtils.substringAfter(dataString, ":");
					JobContext jobContext = getJobService().getJobContext(jobToken, false);
					if (jobContext != null)
						getJobService().reportJobWorkspace(jobContext, jobWorkspace);
					break;
				case SHELL_OUTPUT:
					dataString = new String(messageData, StandardCharsets.UTF_8);
					String sessionId = StringUtils.substringBefore(dataString, ":");
					String output = StringUtils.substringAfter(dataString, ":");
					AgentShell shell = (AgentShell) getJobService().getShell(sessionId);
					if (shell != null)
						shell.getTerminal().sendOutput(output);
					break;
				case SHELL_ERROR:
					dataString = new String(messageData, StandardCharsets.UTF_8);
					sessionId = StringUtils.substringBefore(dataString, ":");
					String error = StringUtils.substringAfter(dataString, ":");
					shell = (AgentShell) getJobService().getShell(sessionId);
					if (shell != null)
						shell.getTerminal().sendError(error);
					break;
				case SHELL_CLOSED:
					sessionId = new String(messageData, StandardCharsets.UTF_8);
					shell = (AgentShell) getJobService().getShell(sessionId);
					if (shell != null)
						shell.getTerminal().close();
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
					OneDev.getInstance(ResourceAllocator.class).agentDisconnecting(agentId);
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