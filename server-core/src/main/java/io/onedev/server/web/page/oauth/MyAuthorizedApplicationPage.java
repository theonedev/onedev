package io.onedev.server.web.page.oauth;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;

import jakarta.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.unbescape.javascript.JavaScriptEscape;

import io.onedev.server.model.OAuthApplication;
import io.onedev.server.model.OAuthGrant;
import io.onedev.server.oauth.OAuthProviderService;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardResourceReference;
import io.onedev.server.web.page.my.MyPage;

public class MyAuthorizedApplicationPage extends MyPage {

	public static final String PARAM_APPLICATION = "application";

	@Inject
	private OAuthProviderService providerService;

	private final Long applicationId;

	private final LoadableDetachableModel<List<OAuthGrant>> grants;

	public MyAuthorizedApplicationPage(PageParameters parameters) {
		super(parameters);
		applicationId = parameters.get(PARAM_APPLICATION).toOptionalLong();
		if (applicationId == null)
			throw new RestartResponseException(MyAuthorizedApplicationsPage.class);
		grants = new LoadableDetachableModel<>() {
			@Override protected List<OAuthGrant> load() {
				return providerService.getGrants(getUser()).stream()
						.filter(it -> it.getApplication().getId().equals(applicationId))
						.sorted(Comparator.comparing(OAuthGrant::getCreateDate).reversed()).toList();
			}
		};
	}

	private OAuthApplication getAuthorizedApplication() {
		if (grants.getObject().isEmpty())
			throw new RestartResponseException(MyAuthorizedApplicationsPage.class);
		return grants.getObject().get(0).getApplication();
	}

	@Override protected void onInitialize() {
		super.onInitialize();
		var application = getAuthorizedApplication();
		var form = new OAuthForm("form") {
			@Override public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(JavaScriptHeaderItem.forReference(new CopyToClipboardResourceReference()));
				response.render(OnDomReadyHeaderItem.forScript(String.format(
						"onedev.server.copyToClipboard.initCodeSpans($('#%s'), '%s', '%s');", getMarkupId(),
						JavaScriptEscape.escapeJavaScript(_T("Click to copy")), JavaScriptEscape.escapeJavaScript(_T("Copied")))));
			}
		};
		form.setOutputMarkupId(true);
		add(form);
		form.add(new Label("name", application.getName()));
		form.add(new ExternalLink("homepage", application.getHomepage(), application.getHomepage()));
		form.add(new Label("owner", application.getOwner().getDisplayName()));
		form.add(new Label("clientId", application.getClientId()));
		form.add(new Label("count", (IModel<Integer>) () -> grants.getObject().stream().mapToInt(it -> it.getAccessTokens().size()).sum()));
		var applicationName = application.getName();
		form.add(new AjaxButton("revokeAll") {
			@Override protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(
						MessageFormat.format(_T("Revoke all tokens for \"{0}\"?"), applicationName)));
			}

			@Override protected void onSubmit(AjaxRequestTarget target) {
				providerService.revokeGrants(getUser(), applicationId);
				getSession().success(_T("All authorizations for this application have been revoked"));
				setResponsePage(MyAuthorizedApplicationsPage.class);
			}
		});
		form.add(new ListView<>("tokens", grants) {
			@Override protected void populateItem(ListItem<OAuthGrant> item) {
				var grant = item.getModelObject();
				var token = grant.getAccessTokens().stream().findFirst().orElse(null);
				item.add(new Label("created", DateUtils.formatDateTime(grant.getCreateDate())));
				item.add(new Label("used", grant.getLastUsedDate() != null ? DateUtils.formatDateTime(grant.getLastUsedDate()) : _T("Not used")));
				item.add(new Label("accessExpires", grant.getLastUsedDate() != null && token != null
						? DateUtils.formatDateTime(token.getExpireDate()) : _T("Not issued")));
				item.add(new Label("expires", DateUtils.formatDateTime(grant.getLastUsedDate() != null
						? grant.getExpireDate() : grant.getCodeExpireDate())));
				item.add(new Label("redirectUri", grant.getRedirectUri()));
				item.add(new ListView<>("scopes", List.of(grant.getScopes().split(" "))) {
					@Override protected void populateItem(ListItem<String> scopeItem) {
						scopeItem.add(new Label("scope", scopeItem.getModelObject()));
					}
				});
				var projects = String.join(", ", grant.getAccessTokens().stream().flatMap(it -> it.getAuthorizations().stream())
						.map(it -> it.getProject().getPath() + " (" + it.getRole().getName() + ")").distinct().sorted().toList());
				item.add(new Label("projects", !projects.isEmpty() ? projects : _T("None")));
				var grantId = grant.getId();
				item.add(new AjaxButton("revoke") {
					@Override protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(
								_T("Revoke this token and its refresh token?")));
					}

					@Override protected void onSubmit(AjaxRequestTarget target) {
						providerService.revokeGrant(grantId);
						grants.detach();
						getSession().success(_T("Authorization revoked"));
						if (grants.getObject().isEmpty())
							setResponsePage(MyAuthorizedApplicationsPage.class);
						else
							target.add(form);
					}
				});
			}
		});
	}

	@Override protected void onConfigure() {
		super.onConfigure();
		getAuthorizedApplication();
	}

	@Override protected void onDetach() {
		grants.detach();
		super.onDetach();
	}

	@Override protected Component newTopbarTitle(String id) {
		var fragment = new Fragment(id, "topbarTitleFrag", this);
		fragment.add(new BookmarkablePageLink<>("applications", MyAuthorizedApplicationsPage.class));
		fragment.add(new Label("name", getAuthorizedApplication().getName()));
		return fragment;
	}

	public static PageParameters paramsOf(OAuthApplication application) {
		return new PageParameters().add(PARAM_APPLICATION, application.getId());
	}
}
