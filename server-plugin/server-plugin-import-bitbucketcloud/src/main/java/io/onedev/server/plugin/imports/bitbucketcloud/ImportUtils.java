package io.onedev.server.plugin.imports.bitbucketcloud;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.util.SimpleLogger;

public class ImportUtils {

	static final String NAME = "Bitbucket Cloud";

	static final int PER_PAGE = 50;
	
	static List<JsonNode> list(Client client, String apiEndpoint, SimpleLogger logger) {
		List<JsonNode> result = new ArrayList<>();
		list(client, apiEndpoint, new PageDataConsumer() {

			@Override
			public void consume(List<JsonNode> pageData) {
				result.addAll(pageData);
			}
			
		}, logger);
		return result;
	}
	
	static void list(Client client, String apiEndpoint, PageDataConsumer pageDataConsumer, 
			SimpleLogger logger) {
		URI uri;
		try {
			uri = new URIBuilder(apiEndpoint)
					.addParameter("pagelen", String.valueOf(PER_PAGE)).build();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		
		while (true) {
			try {
				List<JsonNode> pageData = new ArrayList<>();
				JsonNode resultNode = get(client, uri.toString(), logger);
				if (resultNode.hasNonNull("values")) {
					for (JsonNode each: resultNode.get("values"))
						pageData.add(each);
					pageDataConsumer.consume(pageData);
				}
				if (resultNode.hasNonNull("next"))
					uri = new URIBuilder(resultNode.get("next").asText()).build();
				else
					break;
			} catch (URISyntaxException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	static JsonNode get(Client client, String apiEndpoint, SimpleLogger logger) {
		WebTarget target = client.target(apiEndpoint);
		Invocation.Builder builder =  target.request();
		while (true) {
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
	}
	
	static interface PageDataConsumer {
		
		void consume(List<JsonNode> pageData) throws InterruptedException;
		
	}
	
}