package io.onedev.server.web.resource;

import static io.onedev.agent.job.LogRequest.toZoneId;
import static io.onedev.server.util.DateUtils.getZoneId;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;

import com.google.common.base.Joiner;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.service.AgentService;
import io.onedev.server.model.Agent;
import io.onedev.server.security.SecurityUtils;

public class AgentLogResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_AGENT = "agent";
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();

		String agentName = attributes.getParameters().get(PARAM_AGENT).toString();
		Agent agent = OneDev.getInstance(AgentService.class).findByName(agentName);
		if (agent == null)
			throw new EntityNotFoundException("Unable to find agent: " + agentName);
		
		if (!agent.isOnline())
			throw new ExplicitException("Unable to read log: agent is offline");
		
		ResourceResponse response = new ResourceResponse();
		response.setContentType(MimeTypes.OCTET_STREAM);
		
		response.disableCaching();
		
		try {
			response.setFileName(URLEncoder.encode("agent-log.txt", StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		Long agentId = agent.getId();
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				Agent agent = OneDev.getInstance(AgentService.class).load(agentId);
				List<String> agentLog = OneDev.getInstance(AgentService.class).getAgentLog(agent);
				String content = Joiner.on("\n").join(toZoneId(agentLog, getZoneId()));
				attributes.getResponse().getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
			}				
		});

		return response;
	}

	public static PageParameters paramsOf(Agent agent) {
		PageParameters params = new PageParameters();
		params.add(PARAM_AGENT, agent.getName());
		return params;
	}
	
}
