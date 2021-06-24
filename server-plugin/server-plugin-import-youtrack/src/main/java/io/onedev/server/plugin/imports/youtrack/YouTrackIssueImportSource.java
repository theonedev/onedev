package io.onedev.server.plugin.imports.youtrack;

import static io.onedev.server.plugin.imports.youtrack.YouTrackImportUtils.get;
import static io.onedev.server.plugin.imports.youtrack.YouTrackImportUtils.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.server.buildspec.job.log.StyleBuilder;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class YouTrackIssueImportSource extends YouTrackProjectImportSource {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(YouTrackIssueImportSource.class);
		
	private String project;

	@Editable(order=400, name="YouTrack Project", description="Choose YouTrack project to "
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
		String userName = (String) EditContext.get().getInputValue(PROP_USER_NAME);
		String password = (String) EditContext.get().getInputValue(PROP_PASSWORD);
		
		if (StringUtils.isNotBlank(apiUrl) && StringUtils.isNotBlank(userName) 
				&& StringUtils.isNotBlank(password)) {
			Client client = ClientBuilder.newClient();
			try {
				client.register(HttpAuthenticationFeature.basic(userName, password));
				SimpleLogger logger = new SimpleLogger() {

					@Override
					public void log(String message, StyleBuilder styleBuilder) {
						YouTrackIssueImportSource.logger.info(message);
					}
					
				};
				String apiEndpoint = getApiEndpoint(apiUrl, "/users/me?fields=guest");
				if (!get(client, apiEndpoint, logger).get("guest").asBoolean()) {
					apiEndpoint = getApiEndpoint(apiUrl, "/admin/projects?fields=name");
					for (JsonNode projectNode: list(client, apiEndpoint, logger)) 
						choices.add(projectNode.get("name").asText());
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
