package io.onedev.server.web.page.admin.oauthsigningkey;

import static io.onedev.server.web.translation.Translation._T;

import jakarta.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.oauth.OAuthProviderService;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.admin.AdministrationPage;

public class OAuthSigningKeyPage extends AdministrationPage {

	@Inject
	private OAuthProviderService providerService;

	@Inject
	private ObjectMapper objectMapper;

	@Inject
	private SettingService settingService;

	public OAuthSigningKeyPage(PageParameters parameters) {
		super(parameters);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		var key = providerService.getPublicSigningKey();
		add(new Label("keySize", key.getModulus().decodeToBigInteger().bitLength()));
		var serverUrl = settingService.getSystemSetting().getServerUrl().replaceAll("/+$", "");
		var jwksUrl = serverUrl + "/~oauth/jwks";
		add(new ExternalLink("jwksUrl", jwksUrl, jwksUrl));
		add(new CopyToClipboardLink("copyJwksUrl", Model.of(jwksUrl)));
		var publicKey = objectMapper.valueToTree(key.toJSONObject()).toPrettyString();
		add(new Label("publicKey", publicKey));
		var copyPublicKey = new CopyToClipboardLink("copyPublicKey", Model.of(publicKey));
		copyPublicKey.add(new SpriteImage("copyPublicKeyIcon", "copy"));
		add(copyPublicKey);
		add(new AjaxLink<Void>("regenerate") {
			@Override public void onClick(AjaxRequestTarget target) {
				new ConfirmModalPanel(target) {
					@Override protected void onConfirm(AjaxRequestTarget target) {
						providerService.generateSigningKey();
						setResponsePage(OAuthSigningKeyPage.class);
					}

					@Override protected String getConfirmMessage() {
						return _T("Regenerating this key revokes all existing authorizations and replaces the public signing key. "
								+ "Applications need to sign in again and refresh their cached signing key. "
								+ "Type <code>yes</code> below to continue.");
					}

					@Override protected String getConfirmInput() {
						return "yes";
					}
				};
			}
		});
	}

	@Override
	protected Component newTopbarTitle(String id) {
		return new Label(id, _T("OAuth Signing Key"));
	}
}
