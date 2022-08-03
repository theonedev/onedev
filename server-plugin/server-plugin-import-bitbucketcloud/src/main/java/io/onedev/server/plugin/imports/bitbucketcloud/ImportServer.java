package io.onedev.server.plugin.imports.bitbucketcloud;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintValidatorContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.http.client.utils.URIBuilder;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

import io.onedev.commons.bootstrap.SensitiveMasker;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.git.command.LsRemoteCommand;
import io.onedev.server.model.Project;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.JerseyUtils;
import io.onedev.server.util.JerseyUtils.PageDataConsumer;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Editable
@ClassValidating
public class ImportServer implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ImportServer.class);
	
	private static final int PER_PAGE = 50;
	
	private String userName;
	
	private String appPassword;

	@Editable(order=10, name="Bitbucket Login Name")
	@NotEmpty
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Editable(order=100, name="Bitbucket App Password", description="Bitbucket app password should be generated with "
			+ "permission <b>account/read</b>, <b>repositories/read</b> and <b>issues:read</b>")
	@Password
	@NotEmpty
	public String getAppPassword() {
		return appPassword;
	}

	public void setAppPassword(String appPassword) {
		this.appPassword = appPassword;
	}
	
	private String getApiEndpoint(String apiPath) {
		return "https://api.bitbucket.org/2.0/" + StringUtils.stripStart(apiPath, "/");
	}
	
	Map<String, String> listWorkspaces() {
		Map<String, String> workspaces = new HashMap<>();
		
		Client client = newClient();
		try {
			String apiEndpoint = getApiEndpoint("/user/permissions/workspaces");
			for (JsonNode valueElementNode: list(client, apiEndpoint, new TaskLogger() {

				@Override
				public void log(String message, String sessionId) {
					logger.info(message);
				}
				
			})) {
				JsonNode workspaceNode = valueElementNode.get("workspace");
				workspaces.put(workspaceNode.get("slug").asText(), workspaceNode.get("name").asText());
			}	
			CollectionUtils.sortByValue(workspaces);
		} catch (Exception e) {
			logger.error("Error listing workspaces", e);
		} finally {
			client.close();
		}
		
		return workspaces;
	}
	
	List<String> listRepositories(String workspaceId) {
		Client client = newClient();
		try {
			List<String> repositories = new ArrayList<>();
			
			String apiEndpoint = getApiEndpoint("/repositories/" + workspaceId);
			for (JsonNode repoNode: list(client, apiEndpoint, new TaskLogger() {

				@Override
				public void log(String message, String sessionId) {
					logger.info(message);
				}
				
			})) {
				repositories.add(repoNode.get("full_name").asText());
			}					
			
			return repositories;
		} finally {
			client.close();
		}
	}
	
	private List<JsonNode> list(Client client, String apiEndpoint, TaskLogger logger) {
		List<JsonNode> result = new ArrayList<>();
		list(client, apiEndpoint, new PageDataConsumer() {

			@Override
			public void consume(List<JsonNode> pageData) {
				result.addAll(pageData);
			}
			
		}, logger);
		return result;
	}
	
	private void list(Client client, String apiEndpoint, PageDataConsumer pageDataConsumer, 
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
	
	String importProjects(ImportRepositories repositories, ImportOption option, boolean dryRun, TaskLogger logger) {
		Collection<Long> projectIds = new ArrayList<>();
		
		Client client = newClient();
		try {
			for (ProjectMapping projectMapping: repositories.getProjectMappings()) {
				logger.log("Cloning code from repository " + projectMapping.getBitbucketRepo() + "...");
				
				String apiEndpoint = getApiEndpoint("/repositories/" + projectMapping.getBitbucketRepo());
				JsonNode repoNode = JerseyUtils.get(client, apiEndpoint, logger);
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);                               
				Project project = projectManager.initialize(projectMapping.getOneDevProject());
				Preconditions.checkState(project.isNew());				
				
				project.setDescription(repoNode.get("description").asText(null));
				
				boolean isPrivate = repoNode.get("is_private").asBoolean();
				if (!isPrivate && option.getPublicRole() != null)
					project.setDefaultRole(option.getPublicRole());

				String cloneUrl = null;
				for (JsonNode cloneNode: repoNode.get("links").get("clone")) {
					if (cloneNode.get("name").asText().equals("https")) {
						cloneUrl = cloneNode.get("href").asText();
						break;
					}
				}
				if (cloneUrl == null)
					throw new ExplicitException("Https clone url not found");
				
				URIBuilder builder = new URIBuilder(cloneUrl);
				builder.setUserInfo(getUserName(), getAppPassword());
				
				SensitiveMasker.push(new SensitiveMasker() {

					@Override
					public String mask(String text) {
						return StringUtils.replace(text, getAppPassword(), "******");
					}
					
				});
				try {
					if (dryRun) {
						new LsRemoteCommand().remote(builder.build().toString()).refs("HEAD").quiet(true).call();
					} else {
						projectManager.clone(project, builder.build().toString());
						projectIds.add(project.getId());
					}
				} finally {
					SensitiveMasker.pop();
				}
			}
			
			return "Repositories imported successfully";
		} catch (Exception e) {
			for (Long projectId: projectIds)
				OneDev.getInstance(StorageManager.class).deleteProjectDir(projectId);
			throw new RuntimeException(e);
		} finally {
			client.close();
		}
	}	

	private Client newClient() {
		Client client = ClientBuilder.newClient();
		client.property(ClientProperties.FOLLOW_REDIRECTS, true);
		client.register(HttpAuthenticationFeature.basic(getUserName(), getAppPassword()));
		return client;
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Client client = ClientBuilder.newClient();
		client.register(HttpAuthenticationFeature.basic(getUserName(), getAppPassword()));
		try {
			String apiEndpoint = getApiEndpoint("/user");
			WebTarget target = client.target(apiEndpoint);
			Invocation.Builder builder =  target.request();
			try (Response response = builder.get()) {
				if (response.getStatus() == 401) {
					context.disableDefaultConstraintViolation();
					String errorMessage = "Authentication failed";
					context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
					return false;
				} else {
					String errorMessage = JerseyUtils.checkStatus(apiEndpoint, response);
					if (errorMessage != null) {
						context.disableDefaultConstraintViolation();
						context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
						return false;
					} 
				}
			}
		} catch (Exception e) {
			context.disableDefaultConstraintViolation();
			String errorMessage = "Error connecting api service";
			if (e.getMessage() != null)
				errorMessage += ": " + e.getMessage();
			context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
			return false;
		} finally {
			client.close();
		}
		return true;
	}
	
}
