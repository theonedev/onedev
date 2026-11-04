package io.onedev.server.web.page.oauth;

import static io.onedev.server.web.translation.Translation._T;

import java.util.Collection;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.openid.connect.sdk.OIDCError;
import com.nimbusds.openid.connect.sdk.Prompt;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Project;
import io.onedev.server.oauth.OAuthProtocol;
import io.onedev.server.oauth.OAuthProviderService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ManageProject;
import io.onedev.server.service.ProjectService;
import io.onedev.server.web.component.project.choice.ProjectMultiChoice;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.page.simple.SimplePage;

public class OAuthAuthorizePage extends SimplePage {

	@Inject
	private OAuthProviderService providerService;
	@Inject
	private ProjectService projectService;

	private String authorizationQuery;
	private Long ownerId;
	private List<Long> selectedProjectIds = List.of();

	public OAuthAuthorizePage(PageParameters parameters) {
		super(parameters);
		var http = (HttpServletRequest) getRequest().getContainerRequest();
		authorizationQuery = http.getQueryString();
		if (authorizationQuery == null || authorizationQuery.length() > 16000)
			throw new ExplicitException("Invalid authorization request");
		var request = request();
		var application = providerService.findApplication(request.getClientID().getValue());
		// Never redirect errors before both client and exact callback have been validated.
		if (application == null || !OAuthProtocol.hasRegisteredRedirect(application, request)
				|| application.getOwner().isDisabled())
			throw new ExplicitException("Unknown OAuth application or unregistered redirect URI");
		try {
			OAuthProtocol.validateAuthorization(request);
			providerService.getRoles(request.getScope());
		} catch (ParseException e) {
			redirectError(request, e.getErrorObject());
		}
		if (request.getPrompt() != null) {
			if (request.getPrompt().contains(Prompt.Type.NONE))
				redirectError(request, SecurityUtils.getAuthUser() == null ? OIDCError.LOGIN_REQUIRED : OIDCError.CONSENT_REQUIRED);
			if (!request.getPrompt().equals(new Prompt(Prompt.Type.CONSENT)))
				redirectError(request, OAuth2Error.INVALID_REQUEST.setDescription("Only prompt=consent and prompt=none are supported"));
		}
		if (SecurityUtils.getAuthUser() == null)
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
		ownerId = SecurityUtils.getAuthUser().getId();
	}

	private AuthorizationRequest request() {
		try {
			var request = AuthorizationRequest.parse(authorizationQuery);
			OAuthProtocol.checkSingleParameters(com.nimbusds.oauth2.sdk.util.URLUtils.parseParameters(authorizationQuery));
			return request;
		} catch (ParseException e) {
			throw new ExplicitException("Invalid authorization request");
		}
	}

	private void redirectError(AuthorizationRequest request, ErrorObject error) {
		throw new RedirectToUrlException(new AuthorizationErrorResponse(request.getRedirectionURI(), error,
				request.getState(), ResponseMode.QUERY).toURI().toString());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		var request = request();
		var application = providerService.findApplication(request.getClientID().getValue());
		add(new Label("application", application.getName()));
		add(new ExternalLink("homepage", application.getHomepage(), application.getHomepage()));
		add(new Label("owner", application.getOwner().getDisplayName()));
		add(new Label("user", SecurityUtils.getAuthUser().getDisplayName()));
		add(new Label("redirect", request.getRedirectionURI().toString()));
		var descriptions = providerService.getScopes();
		add(new ListView<>("scopes", request.getScope().toStringList()) {
			@Override protected void populateItem(ListItem<String> item) {
				item.add(new Label("description", descriptions.get(item.getModelObject())));
			}
		});
		List<Long> roleIds;
		try {
			roleIds = providerService.getRoles(request.getScope()).stream().map(it -> it.getId()).toList();
		} catch (ParseException e) {
			throw new ExplicitException("Requested role no longer exists");
		}
		var projects = new LoadableDetachableModel<List<Project>>() {
			@Override protected List<Project> load() {
				return SecurityUtils.getAuthorizedProjects(SecurityUtils.getAuthUser().asSubject(), new ManageProject()).stream()
						.sorted(java.util.Comparator.comparing(Project::getPath)).toList();
			}
		};
		var selected = new IModel<Collection<Project>>() {
			@Override public Collection<Project> getObject() {
				return selectedProjectIds.stream().map(projectService::load).toList();
			}

			@Override public void setObject(Collection<Project> projects) {
				selectedProjectIds = projects.stream().map(Project::getId).toList();
			}
		};
		var form = new OAuthForm("form");
		add(form);
		form.add(new FencedFeedbackPanel("feedback", form));
		var choices = new ProjectMultiChoice("projects", selected, projects) {
			@Override protected void onInitialize() {
				super.onInitialize();
				getSettings().setPlaceholder(_T("Choose projects..."));
				getSettings().setWidth("100%");
			}
		};
		choices.setVisible(!roleIds.isEmpty());
		form.add(choices);
		form.add(new Button("authorize") {
			@Override public void onSubmit() {
				var request = request();
				var user = SecurityUtils.getAuthUser();
				if (user == null || !user.getId().equals(ownerId))
					throw new ExplicitException("Your signed-in account changed. Start authorization again");
				try {
					String code = providerService.authorize(SecurityUtils.getSubject(), request, selectedProjectIds);
					throw new RedirectToUrlException(new AuthorizationSuccessResponse(request.getRedirectionURI(),
							new AuthorizationCode(code), null, request.getState(), ResponseMode.QUERY).toURI().toString());
				} catch (ParseException e) {
					redirectError(request, e.getErrorObject());
				} catch (ExplicitException e) {
					form.error(e.getMessage());
				}
			}
		});
		form.add(new Button("deny") {
			@Override public void onSubmit() { redirectError(request(), OAuth2Error.ACCESS_DENIED); }
		});
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		var response = (WebResponse) getResponse();
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Referrer-Policy", "no-referrer");
		response.setHeader("Content-Security-Policy", "frame-ancestors 'none'");
	}

	@Override protected String getTitle() { return _T("Authorize Application"); }
	@Override protected String getSubTitle() { return null; }
}
