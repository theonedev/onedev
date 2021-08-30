package io.onedev.server.web.resource;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.resource.AbstractResource;

import com.google.common.base.Joiner;

import io.onedev.agent.job.LogRequest;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.server.security.SecurityUtils;

public class ServerLogResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();

		ResourceResponse response = new ResourceResponse();
		response.setContentType(MimeTypes.OCTET_STREAM);
		
		response.disableCaching();
		
		try {
			response.setFileName(URLEncoder.encode("server-log.txt", StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				String content = Joiner.on("\n").join(readServerLog());
				attributes.getResponse().getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
			}				
		});

		return response;
	}

	public static List<String> readServerLog() {
		return LogRequest.readLog(new File(Bootstrap.installDir, "logs/server.log"));
	}
	
}
