package io.onedev.server.plugin.imports.gitlab;

import static io.onedev.server.plugin.imports.gitlab.GitLabImportUtils.get;
import static io.onedev.server.plugin.imports.gitlab.GitLabImportUtils.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.client.oauth2.OAuth2ClientSupport;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.buildspec.job.log.StyleBuilder;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
@ClassValidating
public class GitLabIssueImportSource extends GitLabProjectImportSource {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(GitLabIssueImportSource.class);
	
	private String project;

	@Editable(order=400, name="GitLab Project", description="Choose GitLab project to "
			+ "import issues from. Available projects will be populated when above options "
			+ "are specified")
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
		
		String apiUrl = (String) EditContext.get().getInputValue(PROP_API_URL);
		String accessToken = (String) EditContext.get().getInputValue(PROP_ACCESS_TOKEN);
		
		if (StringUtils.isNotBlank(apiUrl) && StringUtils.isNotBlank(accessToken)) {
			Client client = ClientBuilder.newClient();
			client.register(OAuth2ClientSupport.feature(accessToken));
			try {
				String apiEndpoint = getApiEndpoint(apiUrl, "/user");
				SimpleLogger logger = new SimpleLogger() {

					@Override
					public void log(String message, StyleBuilder styleBuilder) {
						GitLabIssueImportSource.logger.info(message);
					}
					
				};
				get(client, apiEndpoint, logger);
				
				apiEndpoint = getApiEndpoint(apiUrl, "/projects?membership=true");
				for (JsonNode projectNode: list(client, apiEndpoint, logger)) 
					choices.add(projectNode.get("path_with_namespace").asText());
			} catch (Exception e) {
			} finally {
				client.close();
			}
		}
		
		Collections.sort(choices);
		return choices;
	}
	
}
