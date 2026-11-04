package io.onedev.server.web.page.oauth;

import static io.onedev.server.web.translation.Translation._T;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.OAuthApplication;
import io.onedev.server.oauth.OAuthProviderService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.security.LoginPage;

public abstract class OAuthApplicationPage extends LayoutPage {

	public static final String PARAM_APPLICATION = "application";

	@Inject
	private OAuthProviderService providerService;

	private final Long applicationId;
	private transient String secret;
	private final OAuthApplicationBean bean = new OAuthApplicationBean();

	protected OAuthApplicationPage(PageParameters parameters, boolean creating) {
		super(parameters);
		if (SecurityUtils.getAuthUser() == null)
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
		applicationId = creating ? null : parameters.get(PARAM_APPLICATION).toOptionalLong();
		if (!creating && applicationId == null)
			throw new RestartResponseException(getApplicationsPageClass());
	}

	@Override protected boolean isPermitted() {
		return SecurityUtils.getAuthUser() != null;
	}

	private OAuthApplication application() {
		var application = providerService.getApplication(applicationId);
		if (!SecurityUtils.isAdministrator() && !SecurityUtils.getAuthUser().equals(application.getOwner()))
			throw new org.apache.shiro.authz.UnauthorizedException();
		return application;
	}

	@Override protected void onInitialize() {
		super.onInitialize();
		if (applicationId != null) {
			var application = application();
			bean.setName(application.getName());
			bean.setHomepage(application.getHomepage());
			bean.setRedirectUris(application.getRedirectUris());
			bean.setConfidential(application.isConfidential());
		}
		var form = new OAuthForm("form") {
			@Override protected void onSubmit() {
				var app = applicationId != null ? application() : new OAuthApplication();
				if (app.isNew()) {
					app.setOwner(SecurityUtils.getAuthUser());
					app.setConfidential(bean.isConfidential());
				}
				app.setName(bean.getName());
				app.setHomepage(bean.getHomepage());
				app.setRedirectUris(bean.getRedirectUris());
				try {
					var newSecret = providerService.saveApplication(app);
					var parameters = paramsOf(app);
					OAuthApplicationPage page = new MyOAuthApplicationPage(parameters);
					page.secret = newSecret;
					Session.get().success(applicationId == null ? _T("Application registered") : _T("Basic settings updated"));
					setResponsePage(page);
				} catch (ExplicitException e) {
					error(e.getMessage());
				}
			}
		};
		add(form);
		form.add(new FencedFeedbackPanel("feedback", form));
		add(new Label("heading", applicationId != null ? _T("Basic Settings") : _T("Register Application")));
		form.add(BeanContext.edit("editor", bean, applicationId != null ? List.of("confidential") : List.of(), true));
		form.add(new Button("save"));
		form.add(new BookmarkablePageLink<>("cancel", getApplicationsPageClass()));
		var manage = new OAuthForm("manage");
		manage.setVisible(applicationId != null);
		add(manage);
		manage.add(new OAuthClientSecretPanel("secret", applicationId, bean.isConfidential(), secret));
		secret = null;
		manage.add(new FencedFeedbackPanel("feedback", manage));
		manage.add(new Label("type", bean.isConfidential() ? _T("Confidential") : _T("Public")));
		var clientId = applicationId != null ? application().getClientId() : "";
		manage.add(new Label("clientId", clientId));
		manage.add(new CopyToClipboardLink("copyClientId", Model.of(clientId)));
		manage.add(new Button("revoke") {
			@Override public void onSubmit() {
				providerService.revokeApplication(applicationId);
				success(_T("All authorizations for this application have been revoked"));
			}
		});
		manage.add(new Button("delete") {
			@Override public void onSubmit() {
				providerService.deleteApplication(applicationId);
				Session.get().success(_T("Application deleted"));
				setResponsePage(getApplicationsPageClass());
			}
		});
		add(new OAuthIntegrationPanel("integration").setVisible(applicationId != null));
	}

	@Override protected void onConfigure() {
		super.onConfigure();
		((WebResponse) getResponse()).setHeader("Cache-Control", "no-store");
	}

	@Override protected void onDetach() {
		secret = null;
		super.onDetach();
	}

	private Class<? extends OAuthApplicationsPage> getApplicationsPageClass() {
		return MyOAuthApplicationsPage.class;
	}

	public static PageParameters paramsOf(OAuthApplication application) {
		return new PageParameters().add(PARAM_APPLICATION, application.getId());
	}

	@Override protected Component newTopbarTitle(String id) {
		var fragment = new Fragment(id, "topbarTitleFrag", this);
		fragment.add(new BookmarkablePageLink<>("applications", getApplicationsPageClass()));
		fragment.add(new Label("applicationName", applicationId != null ? application().getName() : _T("Create New")));
		return fragment;
	}

}
