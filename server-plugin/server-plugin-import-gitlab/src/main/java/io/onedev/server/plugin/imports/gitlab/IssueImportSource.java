package io.onedev.server.plugin.imports.gitlab;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.client.Client;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class IssueImportSource implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(IssueImportSource.class);
	
	private String project;

	@Editable(order=400, name="GitLab Project", description="Choose GitLab project to import issues from")
	@ChoiceProvider("getProjectChoices")
	@NotEmpty
	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getProjectChoices() {
		List<String> choices = new ArrayList<>();
		
		ImportServer server = WicketUtils.getPage().getMetaData(ImportServer.META_DATA_KEY);
		
		Client client = server.newClient();
		try {
			String apiEndpoint = server.getApiEndpoint("/projects?membership=true");
			Collection<JsonNode> projectNodes = ImportUtils.list(client, apiEndpoint, new TaskLogger() {

				@Override
				public void log(String message, String sessionId) {
					logger.info(message);
				}
				
			});
			for (JsonNode projectNode: projectNodes) 
				choices.add(projectNode.get("path_with_namespace").asText());
		} finally {
			client.close();
		}
		
		Collections.sort(choices);
		return choices;
	}
	
}
