package io.onedev.server.jetty;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.BooleanSupplier;

import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public class ProbeHandler extends Handler.Abstract {

	public static final String HEALTH_PATH = "/healthz";

	public static final String READINESS_PATH = "/readyz";

	private final BooleanSupplier readiness;

	public ProbeHandler(BooleanSupplier readiness) {
		this.readiness = readiness;
	}

	@Override
	public boolean handle(Request request, Response response, Callback callback) {
		String target = request.getHttpURI().getPath();
		if (target.equals(HEALTH_PATH) || target.equals(READINESS_PATH)) {
			if (request.getMethod().equals("GET") || request.getMethod().equals("HEAD")) {
				if (target.equals(HEALTH_PATH)) {
					respond(request, response, callback, HttpServletResponse.SC_OK);
				} else {
					respond(request, response, callback, readiness.getAsBoolean()
							? HttpServletResponse.SC_OK
							: HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				}
			} else {
				response.getHeaders().put("Allow", "GET, HEAD");
				respond(request, response, callback, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			}
			return true;
		}
		return false;
	}

	private void respond(Request request, Response response, Callback callback, int status) {
		response.setStatus(status);
		response.getHeaders().put("Content-Type", "text/plain;charset=UTF-8");
		response.getHeaders().put("Cache-Control", "no-store");
		String body = status == HttpServletResponse.SC_OK ? "ok\n"
				: status == HttpServletResponse.SC_SERVICE_UNAVAILABLE ? "not ready\n" : "";
		response.write(true, request.getMethod().equals("HEAD") ? null
				: ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)), callback);
	}

}
