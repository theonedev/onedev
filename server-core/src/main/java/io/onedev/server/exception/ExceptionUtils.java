package io.onedev.server.exception;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.exception.handler.ExceptionHandler;

import org.jspecify.annotations.Nullable;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public class ExceptionUtils extends io.onedev.commons.utils.ExceptionUtils {
	
	public static void handle(HttpServletResponse servletResponse, Exception exception) {
		try {
			var httpResponse = buildResponse(exception);
			if (httpResponse != null) {
				for (MultivaluedMap.Entry<String, List<String>> entry: httpResponse.getHeaders().entrySet()) {
					for (String value: entry.getValue())
						servletResponse.addHeader(entry.getKey(), value);
				}
				servletResponse.setStatus(httpResponse.getStatus());
				if (httpResponse.getBody() != null) {
					servletResponse.setContentType(httpResponse.getBody().getContentType());
					try (var os = servletResponse.getOutputStream()) {
						os.write(httpResponse.getBody().getText().getBytes(UTF_8));
					}
				}
			} else {
				servletResponse.setStatus(SC_INTERNAL_SERVER_ERROR);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static HttpResponse buildResponse(Throwable exception) {
		List<ExceptionHandler<? extends Throwable>> handlers = new ArrayList<>();
		for (ExceptionHandler<? extends Throwable> handler: OneDev.getExtensions(ExceptionHandler.class)) 
			handlers.add(handler);
		Collections.sort(handlers, (Comparator<ExceptionHandler<? extends Throwable>>) (o1, o2) -> 
				getInheritanceLevel(o2.getExceptionClass()) - getInheritanceLevel(o1.getExceptionClass()));
		for (ExceptionHandler<? extends Throwable> handler: handlers) {
			ExceptionHandler<Throwable> convertedHandler = (ExceptionHandler<Throwable>) handler;
			Throwable expectedException = ExceptionUtils.find(exception, convertedHandler.getExceptionClass());
			if (expectedException != null) 
				return convertedHandler.getResponse(expectedException);
		}
		return null;
	}

	private static int getInheritanceLevel(Class<?> clazz) {
		int inheritanceLevel = 0;
		while (clazz != Object.class) {
			inheritanceLevel ++;
			clazz = clazz.getSuperclass();
		}
		return inheritanceLevel;
	}
	
}
