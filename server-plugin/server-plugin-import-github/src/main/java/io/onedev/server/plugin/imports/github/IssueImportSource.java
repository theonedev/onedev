package io.onedev.server.plugin.imports.github;

import static io.onedev.server.plugin.imports.github.ImportUtils.list;

import java.io.Serializable;
import java.util.ArrayList;
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
	
	private String repository;

	@Editable(order=100, name="GitHub Repository", description="Choose GitHub repository to "
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

		ImportServer server = WicketUtils.getPage().getMetaData(ImportServer.META_DATA_KEY);

		Client client = server.newClient();
		try {
			String apiEndpoint = server.getApiEndpoint("/user/repos");
			TaskLogger logger = new TaskLogger() {

				@Override
				public void log(String message, String sessionId) {
					IssueImportSource.logger.info(message);
				}
				
			};
			for (JsonNode repoNode: list(client, apiEndpoint, logger)) {
				String repoName = repoNode.get("name").asText();
				String ownerName = repoNode.get("owner").get("login").asText();
				choices.add(ownerName + "/" + repoName);
			}
		} finally {
			client.close();
		}
		
		Collections.sort(choices);
		return choices;
	}
	
}
