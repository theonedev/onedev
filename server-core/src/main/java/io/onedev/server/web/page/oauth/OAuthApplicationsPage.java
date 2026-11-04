package io.onedev.server.web.page.oauth;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.List;

import jakarta.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.oauth.OAuthProviderService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.security.LoginPage;

public abstract class OAuthApplicationsPage extends LayoutPage {

	@Inject
	private OAuthProviderService providerService;

	public OAuthApplicationsPage(PageParameters parameters) {
		super(parameters);
		if (SecurityUtils.getAuthUser() == null)
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
		if (!parameters.get(OAuthApplicationPage.PARAM_APPLICATION).isEmpty())
			throw new RestartResponseException(getApplicationPageClass(), parameters);
	}

	@Override protected boolean isPermitted() {
		return SecurityUtils.getAuthUser() != null;
	}

	private Class<? extends OAuthApplicationPage> getApplicationPageClass() {
		return MyOAuthApplicationPage.class;
	}

	@Override protected void onInitialize() {
		super.onInitialize();
		add(new BookmarkablePageLink<>("newApplication", NewMyOAuthApplicationPage.class));
		add(new OAuthIntegrationPanel("integration"));
		var applications = new LoadableDetachableModel<List<Long>>() {
			@Override protected List<Long> load() {
				return providerService.getApplications(SecurityUtils.getAuthUser()).stream().map(it -> it.getId()).toList();
			}
		};
		var applicationList = new OAuthForm("applicationList");
		applicationList.setOutputMarkupId(true);
		add(applicationList);
		applicationList.add(new WebMarkupContainer("empty") {
			@Override protected void onConfigure() {
				super.onConfigure();
				setVisible(applications.getObject().isEmpty());
			}
		});
		var table = new WebMarkupContainer("table") {
			@Override protected void onConfigure() {
				super.onConfigure();
				setVisible(!applications.getObject().isEmpty());
			}
		};
		applicationList.add(table);
		table.add(new ListView<>("applications", applications) {
			@Override protected void populateItem(ListItem<Long> item) {
				var app = providerService.getApplication(item.getModelObject());
				var link = new BookmarkablePageLink<>("application", getApplicationPageClass(), OAuthApplicationPage.paramsOf(app));
				link.add(new Label("name", app.getName()));
				item.add(link);
				item.add(new Label("clientId", app.getClientId()));
				item.add(new CopyToClipboardLink("copyClientId", Model.of(app.getClientId())));
				item.add(new OAuthClientSecretPanel("secret", app.getId(), app.isConfidential(), null));
				item.add(new BookmarkablePageLink<>("edit", getApplicationPageClass(), OAuthApplicationPage.paramsOf(app)));
				var applicationName = app.getName();
				item.add(new AjaxButton("delete") {
					@Override protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						var message = MessageFormat.format(_T("Delete application \"{0}\" and all its authorizations?"), applicationName);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
					}

					@Override protected void onSubmit(AjaxRequestTarget target) {
						providerService.deleteApplication(item.getModelObject());
						applications.detach();
						Session.get().success(_T("Application deleted"));
						target.add(applicationList);
					}
				});
			}
		});
	}

	@Override protected void onConfigure() {
		super.onConfigure();
		((WebResponse) getResponse()).setHeader("Cache-Control", "no-store");
	}

	@Override protected Component newTopbarTitle(String id) { return new Label(id, _T("OAuth Applications")); }
}
