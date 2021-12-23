package io.onedev.server;

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
import io.onedev.agent.MessageType;
import io.onedev.agent.WaitingForAgentResourceToBeReleased;
import io.onedev.agent.WebsocketUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.buildspec.job.JobContext;
import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.exception.SystemNotReadyException;
import io.onedev.server.job.resource.ResourceManager;
import io.onedev.server.tasklog.JobLogManager;

@WebSocket
public class ServerSocket {

	private static final Logger logger = LoggerFactory.getLogger(ServerSocket.class);
	
	private Session session;
	
	private Long agentId;
	
	@OnWebSocketClose
    public void onClose(int statusCode, String reason) {
		try {
			if (agentId != null) 
				getAgentManager().agentDisconnected(agentId);
			
			StringBuilder builder = new StringBuilder("Websocket closed (");
			if (session != null)
				builder.append("remote address: " + session.getRemoteAddress().toString() + ", ");
			builder.append("status code: " + statusCode);
			if (reason != null)
				builder.append(", reason: " + reason);
			builder.append(")");
			logger.debug(builder.toString());
		} catch (SystemNotReadyException e) {
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
    
    private AgentManager getAgentManager() {
    	return OneDev.getInstance(AgentManager.class);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
    	this.session = session;
    	try {
    		new Message(MessageType.UPDATE, getAgentManager().getAgentVersion()).sendBy(session);
		} catch (Exception e) {
			logger.error("Error sending websocket message", e);
			try {
				session.disconnect();
			} catch (IOException e2) {
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
	    		// be retrieved via Administrator
	    		AgentData data = (AgentData) SerializationUtils.deserialize(message.getData());
	    		try {
	    			agentId = getAgentManager().agentConnected(data, session);
	    		} catch (ExplicitException e) {
	    			new Message(MessageType.ERROR, e.getMessage()).sendBy(session);
	    		}
	    		break;
	    	case REQUEST:
	    		OneDev.getInstance(ExecutorService.class).execute(new Runnable() {

					@Override
					public void run() {
						try {
				    		CallData request = SerializationUtils.deserialize(messageData);
				    		CallData response = new CallData(request.getUuid(), service(request.getPayload()));
				    		new Message(MessageType.RESPONSE, response).sendBy(session);
						} catch (Exception e) {
							logger.error("Error processing websocket request", e);
						}
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
	    		TaskLogger logger = OneDev.getInstance(JobLogManager.class).getLogger(jobToken);
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
	    		JobContext jobContext = OneDev.getInstance(JobManager.class).getJobContext(jobToken, false);
	    		if (jobContext != null)
	    			jobContext.reportJobWorkspace(jobWorkspace);
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
    
    private Serializable service(Serializable request) {
		try {
			if (request instanceof WaitingForAgentResourceToBeReleased) {
				OneDev.getInstance(ResourceManager.class).waitingForAgentResourceToBeReleased(agentId);
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