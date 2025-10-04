package io.onedev.server.security;

import io.onedev.server.service.SettingService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class CorsFilter implements Filter {

	private final SettingService settingService;
	
	@Inject
	public CorsFilter(SettingService settingService) {
		this.settingService = settingService;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		var origin = httpRequest.getHeader("Origin");
		if (origin != null && settingService.getSecuritySetting().getCorsAllowedOrigins().contains(origin)) {
			httpResponse.addHeader("Access-Control-Allow-Origin", origin);
			httpResponse.addHeader("Vary", "Origin");
			httpResponse.addHeader("Access-Control-Allow-Methods", "GET,HEAD,PUT,PATCH,POST,DELETE,OPTIONS");
			httpResponse.addHeader("Access-Control-Allow-Credentials", "true");
			httpResponse.addHeader("Access-Control-Allow-Headers", "" +
					"Authorization,Content-Type,Content-Length,Content-Encoding,Accept," +
					"Accept-Charset,Accept-Encoding,Accept-Language,Access-Control-Request-Method," +
					"Access-Control-Request-Headers,Cookie,Forwarded,From,Host,If-Modified-Since," +
					"If-Match,If-None-Match,Range,Origin,Pragma,Referer,User-Agent,Upgrade," +
					"X-Forwarded-For,X-Forwarded-Host,X-Forwarded-Proto,");
		}
		if (httpRequest.getMethod().equals("OPTIONS"))
			httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
		else
			chain.doFilter(request, response);
	}
	
	@Override
	public void destroy() {
	}
	
}
 