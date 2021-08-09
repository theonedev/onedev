package io.onedev.server.plugin.imports.bitbucketcloud;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;

import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.util.JerseyUtils;
import io.onedev.server.util.JerseyUtils.PageDataConsumer;

public class ImportUtils {

	static final String NAME = "Bitbucket Cloud";

	static final int PER_PAGE = 50;
	
	static List<JsonNode> list(Client client, String apiEndpoint, TaskLogger logger) {
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
			TaskLogger logger) {
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
				JsonNode resultNode = JerseyUtils.get(client, uri.toString(), logger);
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
	
}