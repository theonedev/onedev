package io.onedev.server.jetty;

import java.io.IOException;
import java.util.function.BooleanSupplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class ProbeHandler extends AbstractHandler {

	public static final String HEALTH_PATH = "/healthz";

	public static final String READINESS_PATH = "/readyz";

	private final BooleanSupplier readiness;

	public ProbeHandler(BooleanSupplier readiness) {
		this.readiness = readiness;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (target.equals(HEALTH_PATH) || target.equals(READINESS_PATH)) {
			if (request.getMethod().equals("GET") || request.getMethod().equals("HEAD")) {
				if (target.equals(HEALTH_PATH)) {
					respond(baseRequest, response, HttpServletResponse.SC_OK);
				} else {
					respond(baseRequest, response, readiness.getAsBoolean()
							? HttpServletResponse.SC_OK
							: HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				}
			} else {
				response.setHeader("Allow", "GET, HEAD");
				respond(baseRequest, response, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			}
		}
	}

	private void respond(Request baseRequest, HttpServletResponse response, int status) throws IOException {
		response.setStatus(status);
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Cache-Control", "no-store");
		if (status == HttpServletResponse.SC_OK)
			response.getWriter().println("ok");
		else if (status == HttpServletResponse.SC_SERVICE_UNAVAILABLE)
			response.getWriter().println("not ready");
		baseRequest.setHandled(true);
	}

}
