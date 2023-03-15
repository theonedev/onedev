package io.onedev.server.util;

import com.fasterxml.jackson.databind.JsonNode;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.List;

public class JerseyUtils {

	@Nullable
	public static String checkStatus(String url, Response response) {
		int status = response.getStatus();
		if (status != 200) {
			String errorMessage = response.readEntity(String.class);
			if (StringUtils.isNotBlank(errorMessage)) {
				return String.format("Http request failed (url: %s, status code: %d, error message: %s)", 
						url, status, errorMessage);
			} else {
				return String.format("Http request failed (url: %s, status code: %d)", url, status);
			}
		} else {
			return null;
		}
	}
	
	public static JsonNode get(Client client, String apiEndpoint, TaskLogger logger) {
		WebTarget target = client.target(apiEndpoint);
		Invocation.Builder builder =  target.request();
		try (Response response = builder.get()) {
			int status = response.getStatus();
			if (status != 200) {
				String errorMessage = response.readEntity(String.class);
				if (StringUtils.isNotBlank(errorMessage)) {
					throw new ExplicitException(String.format("Http request failed (url: %s, status code: %d, error message: %s)", 
							apiEndpoint, status, errorMessage));
				} else {
					throw new ExplicitException(String.format("Http request failed (status: %s)", status));
				}
			} 
			return response.readEntity(JsonNode.class);
		}
	}
	
	public static interface PageDataConsumer {
		
		void consume(List<JsonNode> pageData) throws InterruptedException;
		
	}
	
}
