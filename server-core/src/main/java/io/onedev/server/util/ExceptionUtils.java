package io.onedev.server.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.exception.ExceptionHandler;

public class ExceptionUtils extends io.onedev.commons.utils.ExceptionUtils {

	private static final Logger logger = LoggerFactory.getLogger(ExceptionUtils.class);
	
	public static void handle(HttpServletResponse httpServletResponse, Exception exception) {
		try {
			Response response = buildResponse(exception);
			if (response != null) {
				for (Map.Entry<String, List<Object>> entry: response.getHeaders().entrySet()) {
					for (Object value: entry.getValue())
						httpServletResponse.addHeader(entry.getKey(), value.toString());
				}
				if (response.getEntity() instanceof String)
					httpServletResponse.sendError(response.getStatus(), (String)response.getEntity());
				else
					httpServletResponse.sendError(response.getStatus());					
			} else {
				if (RequestCycle.get() == null)
					logger.error("Error serving request", exception);
				httpServletResponse.sendError(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
						"Internal server error: check server log for details");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static Response buildResponse(Throwable exception) {
		List<ExceptionHandler<? extends Throwable>> handlers = new ArrayList<>();
		for (ExceptionHandler<? extends Throwable> handler: OneDev.getExtensions(ExceptionHandler.class)) 
			handlers.add(handler);
		Collections.sort(handlers, (Comparator<ExceptionHandler<? extends Throwable>>) (o1, o2) -> {
			if (o1.getExceptionClass().isAssignableFrom(o2.getExceptionClass()))
				return 1;
			else if (o2.getExceptionClass().isAssignableFrom(o1.getExceptionClass()))
				return -1;
			else 
				return 0;
		});
		for (ExceptionHandler<? extends Throwable> handler: handlers) {
			Throwable expectedException = ExceptionUtils.find(exception, handler.getExceptionClass());
			if (expectedException != null) 
				return handler.getResponseWith(expectedException);
		}
		return null;
	}
	
}
