package io.onedev.server.notification;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.jackson.RestView;

@Singleton
public class WebHookManager {

	private static final Logger logger = LoggerFactory.getLogger(WebHookManager.class);
	
	private static final String SIGNATURE_HEAD = "X-OneDev-Signature";
	
	private final ObjectMapper mapper;
	
	private final ExecutorService executor;

	@Inject
	public WebHookManager(ObjectMapper mapper, ExecutorService executor) {
		this.mapper = mapper.copy();
		this.mapper.setConfig(this.mapper.getSerializationConfig().withView(RestView.class));
		this.executor = executor;
	}
	
	@Sessional
	@Listen
	public void on(ProjectEvent event) {
		String jsonOfEvent;
		try {
			jsonOfEvent = mapper.writeValueAsString(event);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		
		for (WebHook webHook: event.getProject().getWebHooks()) {
			for (WebHook.EventType eventType: webHook.getEventTypes()) {
				if (eventType.includes(event)) {
					executor.submit(new Runnable() {

						@Override
						public void run() {
							CloseableHttpClient client = HttpClients.createDefault();
							try {
							    HttpPost httpPost = new HttpPost(webHook.getPostUrl());
							 
							    StringEntity entity = new StringEntity(jsonOfEvent, StandardCharsets.UTF_8.name());
							    httpPost.setEntity(entity);
							    httpPost.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
							    httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
							    httpPost.setHeader(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
							    httpPost.setHeader(SIGNATURE_HEAD, webHook.getSecret());
							 
							    CloseableHttpResponse response = client.execute(httpPost);
							    try {
							    	HttpEntity responseEntity = response.getEntity();
							    	String responseText = EntityUtils.toString(responseEntity);
							    	if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
							    		logger.error("Error calling web hooks: " + responseText);
							    } finally {
							    	try {
										response.close();
									} catch (IOException e) {
									}
							    }
							} catch (IOException e) {
								logger.error("Error calling web hooks", e);
							} finally {
								try {
									client.close();
								} catch (IOException e) {
								}
							}
						}
						
					});
					break;
				}
			}
		}
	}
	
}
