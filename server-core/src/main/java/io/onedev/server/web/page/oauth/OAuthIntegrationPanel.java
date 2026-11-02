package io.onedev.server.web.page.oauth;

import static io.onedev.server.web.translation.Translation._T;

import jakarta.inject.Inject;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.unbescape.javascript.JavaScriptEscape;

import io.onedev.server.oauth.OAuthProviderService;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardResourceReference;

public class OAuthIntegrationPanel extends Panel {

	@Inject
	private OAuthProviderService providerService;
	@Inject
	private SettingService settingService;

	public OAuthIntegrationPanel(String id) {
		super(id);
	}

	@Override protected void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
		var serverUrl = settingService.getSystemSetting().getServerUrl().replaceAll("/+$", "");
		add(new Label("openidDiscoveryUrl", serverUrl + "/.well-known/openid-configuration"));
		add(new Label("oauthDiscoveryUrl", serverUrl + "/.well-known/oauth-authorization-server"));
		var scopes = providerService.getScopes();
		add(new ListView<>("scopes", scopes.keySet().stream().toList()) {
			@Override protected void populateItem(ListItem<String> item) {
				item.add(new Label("name", item.getModelObject()));
				item.add(new Label("description", scopes.get(item.getModelObject())));
			}
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new CopyToClipboardResourceReference()));
		String script = String.format("onedev.server.copyToClipboard.initCodeSpans($('#%s'), '%s', '%s');",
				getMarkupId(), JavaScriptEscape.escapeJavaScript(_T("Click to copy")),
				JavaScriptEscape.escapeJavaScript(_T("Copied")));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
}
