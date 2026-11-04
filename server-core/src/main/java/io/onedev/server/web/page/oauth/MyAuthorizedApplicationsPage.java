package io.onedev.server.web.page.oauth;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;

import jakarta.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.OAuthApplication;
import io.onedev.server.model.OAuthGrant;
import io.onedev.server.oauth.OAuthProviderService;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.page.my.MyPage;

public class MyAuthorizedApplicationsPage extends MyPage {

	@Inject
	private OAuthProviderService providerService;

	public MyAuthorizedApplicationsPage(PageParameters parameters) {
		super(parameters);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		var grants = new LoadableDetachableModel<List<OAuthGrant>>() {
			@Override protected List<OAuthGrant> load() {
				return providerService.getGrants(getUser());
			}
		};
		var applications = new LoadableDetachableModel<List<OAuthApplication>>() {
			@Override protected List<OAuthApplication> load() {
				return grants.getObject().stream().map(OAuthGrant::getApplication).distinct()
						.sorted(Comparator.comparing(OAuthApplication::getName, String.CASE_INSENSITIVE_ORDER)).toList();
			}

			@Override protected void onDetach() {
				grants.detach();
				super.onDetach();
			}
		};
		var form = new OAuthForm("applications");
		form.setOutputMarkupId(true);
		add(form);
		form.add(new WebMarkupContainer("empty") {
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
		form.add(table);
		table.add(new ListView<>("rows", applications) {
			@Override protected void populateItem(ListItem<OAuthApplication> item) {
				var application = item.getModelObject();
				var applicationGrants = grants.getObject().stream()
						.filter(it -> it.getApplication().equals(application)).toList();
				var link = new BookmarkablePageLink<>("detail", MyAuthorizedApplicationPage.class,
						MyAuthorizedApplicationPage.paramsOf(application));
				link.add(new Label("name", application.getName()));
				item.add(link);
				item.add(new Label("owner", application.getOwner().getDisplayName()));
				item.add(new Label("tokens", applicationGrants.stream().mapToInt(it -> it.getAccessTokens().size()).sum()));
				var used = applicationGrants.stream().map(OAuthGrant::getLastUsedDate)
						.filter(it -> it != null).max(Comparator.naturalOrder()).orElse(null);
				item.add(new Label("used", used != null ? DateUtils.formatDateTime(used) : _T("Not used")));
				var applicationId = application.getId();
				var applicationName = application.getName();
				item.add(new AjaxButton("revoke") {
					@Override protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(
								MessageFormat.format(_T("Revoke all tokens for \"{0}\"?"), applicationName)));
					}

					@Override protected void onSubmit(AjaxRequestTarget target) {
						providerService.revokeGrants(getUser(), applicationId);
						applications.detach();
						getSession().success(_T("All authorizations for this application have been revoked"));
						target.add(form);
					}
				});
			}
		});
	}

	@Override protected Component newTopbarTitle(String id) {
		return new Label(id, _T("Authorized Applications"));
	}
}
