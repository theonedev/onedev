package io.onedev.server.pack;

import io.onedev.commons.loader.ExtensionPoint;

import org.jspecify.annotations.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@ExtensionPoint
public interface PackHandler {

	String getHandlerId();
	
	void handle(HttpServletRequest request, HttpServletResponse response,
				 Long projectId, @Nullable Long buildId, List<String> pathSegments);

	@Nullable
	String getApiKey(HttpServletRequest request);
	
	List<String> normalize(List<String> pathSegments);
	
}
