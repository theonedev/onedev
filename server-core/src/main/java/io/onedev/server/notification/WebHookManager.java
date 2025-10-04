package io.onedev.server.notification;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.HttpHeaders;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.event.Listen;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.persistence.annotation.Sessional;

@Singleton
public class WebHookManager {

	private static final Logger logger = LoggerFactory.getLogger(WebHookManager.class);
	
	private static final String SIGNATURE_HEAD = "X-OneDev-Signature";

	@Inject
	private ObjectMapper mapper;

	@Inject
	private ExecutorService executorService;

	@Sessional
	@Listen
	public void on(ProjectEvent event) {
		String jsonOfEvent;
		try {
			jsonOfEvent = mapper.writeValueAsString(event);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		
		for (WebHook webHook: event.getProject().getHierarchyWebHooks()) {
			for (WebHook.EventType eventType: webHook.getEventTypes()) {
				if (eventType.includes(event)) {
					executorService.submit(() -> {
						try (var client = HttpClients.createDefault()) {
							HttpPost httpPost = new HttpPost(webHook.getPostUrl());

							StringEntity entity = new StringEntity(jsonOfEvent, UTF_8.name());
							httpPost.setEntity(entity);
							httpPost.setHeader(HttpHeaders.ACCEPT, APPLICATION_JSON);
							httpPost.setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
							httpPost.setHeader(HttpHeaders.ACCEPT_CHARSET, UTF_8.name());
							httpPost.setHeader(SIGNATURE_HEAD, webHook.getSecret());

							try (var response = client.execute(httpPost)) {
								HttpEntity responseEntity = response.getEntity();
								String responseText = EntityUtils.toString(responseEntity);
								if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
									logger.error("Error calling web hooks: " + responseText);
							}
						} catch (IOException e) {
							logger.error("Error calling web hooks", e);
						}
					});
					break;
				}
			}
		}
	}
	
}
