package io.onedev.server.web.resource;

import static io.onedev.agent.job.LogRequest.readLog;
import static io.onedev.agent.job.LogRequest.toZoneId;
import static io.onedev.commons.bootstrap.Bootstrap.installDir;
import static io.onedev.server.util.DateUtils.getZoneId;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;

import com.google.common.base.Joiner;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.security.SecurityUtils;

public class ServerLogResource extends AbstractResource {

	private static final String PARAM_SERVER = "server";
	
	private static final long serialVersionUID = 1L;

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();

		ResourceResponse response = new ResourceResponse();
		response.setContentType(MimeTypes.OCTET_STREAM);
		
		response.disableCaching();

		response.setFileName(URLEncoder.encode("server-log.txt", UTF_8));
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				var server = attributes.getParameters().get(PARAM_SERVER).toString();
				String content = Joiner.on("\n").join(toZoneId(readServerLog(server), getZoneId()));
				attributes.getResponse().getOutputStream().write(content.getBytes(UTF_8));
			}				
		});

		return response;
	}

	public static List<String> readServerLog(@Nullable String server) {
		var logPath = "logs/server.log";
		if (server != null) 
			return OneDev.getInstance(ClusterService.class).runOnServer(server, () -> readLog(new File(installDir, logPath)));
		else 
			return readLog(new File(installDir, logPath));
	}

	public static PageParameters paramsOf(@Nullable String server) {
		var params = new PageParameters();
		if (server != null)
			params.add(PARAM_SERVER, server);
		return params;
	}
	
}
