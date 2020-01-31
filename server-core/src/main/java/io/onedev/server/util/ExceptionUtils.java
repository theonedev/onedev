package io.onedev.server.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.HttpHeaders;

import io.onedev.server.OneDev;

public class ExceptionUtils extends io.onedev.commons.utils.ExceptionUtils {

	private static final Logger logger = LoggerFactory.getLogger(ExceptionUtils.class);
	
	public static void handle(HttpServletResponse response, Exception exception) {
		try {
			if (ExceptionUtils.find(exception, UnauthenticatedException.class) != null) {
				requireAuthentication(response);
			} else if (find(exception, UnauthorizedException.class) != null) {
				if (!SecurityUtils.getSubject().isAuthenticated()) 
					requireAuthentication(response);
				else 
					response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
			} else if (find(exception, IncorrectCredentialsException.class) != null) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Incorrect credentials.");
			} else if (find(exception, UnknownAccountException.class) != null) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unknown user name.");
			} else {
				logger.warn("Error serving request", exception);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
			} 
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void requireAuthentication(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    String authcHeader = HttpServletRequest.BASIC_AUTH + " realm=\"" + OneDev.NAME + "\"";
	    response.setHeader(HttpHeaders.WWW_AUTHENTICATE, authcHeader);
	}
}
