package io.onedev.server.plugin.imports.github;

import static io.onedev.server.plugin.imports.github.GitHubImportUtils.get;
import static io.onedev.server.plugin.imports.github.GitHubImportUtils.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
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
public class GitHubIssueImportSource extends GitHubProjectImportSource {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(GitHubIssueImportSource.class);
	
	private String repository;

	@Editable(order=400, name="GitHub Repository", description="Choose GitHub repository to "
			+ "import issues from. Available repositories will be populated when above options "
			+ "are specified")
	@ChoiceProvider("getRepositoryChoices")
	@NotEmpty
	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getRepositoryChoices() {
		List<String> choices = new ArrayList<>();
		
		String apiUrl = (String) EditContext.get().getInputValue(PROP_API_URL);
		String accessToken = (String) EditContext.get().getInputValue(PROP_ACCESS_TOKEN);
		
		if (StringUtils.isNotBlank(apiUrl) && StringUtils.isNotBlank(accessToken)) {
			Client client = ClientBuilder.newClient();
			client.register(HttpAuthenticationFeature.basic("git", accessToken));
			try {
				String apiEndpoint = getApiEndpoint(apiUrl, "/user");
				SimpleLogger logger = new SimpleLogger() {

					@Override
					public void log(String message, StyleBuilder styleBuilder) {
						GitHubIssueImportSource.logger.info(message);
					}
					
				};
				get(client, apiEndpoint, logger);
				
				apiEndpoint = getApiEndpoint(apiUrl, "/user/repos");
				for (JsonNode repoNode: list(client, apiEndpoint, logger)) {
					String repoName = repoNode.get("name").asText();
					String ownerName = repoNode.get("owner").get("login").asText();
					choices.add(ownerName + "/" + repoName);
				}
			} catch (Exception e) {
			} finally {
				client.close();
			}
		}
		
		Collections.sort(choices);
		return choices;
	}
	
}
