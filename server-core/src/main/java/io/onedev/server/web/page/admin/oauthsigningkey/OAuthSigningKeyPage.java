package io.onedev.server.web.page.admin.oauthsigningkey;

import static io.onedev.server.web.translation.Translation._T;

import jakarta.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.oauth.OAuthProviderService;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.page.admin.AdministrationPage;

public class OAuthSigningKeyPage extends AdministrationPage {

	@Inject
	private OAuthProviderService providerService;

	public OAuthSigningKeyPage(PageParameters parameters) {
		super(parameters);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		var key = providerService.getPublicSigningKey();
		if (key == null) {
			var fragment = new Fragment("content", "undefinedFrag", this);
			fragment.add(new AjaxLink<Void>("generate") {
				@Override public void onClick(AjaxRequestTarget target) {
					providerService.generateSigningKey();
					setResponsePage(OAuthSigningKeyPage.class);
				}
			});
			add(fragment);
		} else {
			var fragment = new Fragment("content", "definedFrag", this);
			fragment.add(new Label("keyID", key.getKeyID()));
			var publicKey = key.toJSONString();
			fragment.add(new Label("publicKey", publicKey));
			fragment.add(new CopyToClipboardLink("copyPublicKey", Model.of(publicKey)));
			fragment.add(newKeyAction("regenerate", false));
			fragment.add(newKeyAction("delete", true));
			add(fragment);
		}
	}

	private AjaxLink<Void> newKeyAction(String id, boolean delete) {
		return new AjaxLink<>(id) {
			@Override public void onClick(AjaxRequestTarget target) {
				new ConfirmModalPanel(target) {
					@Override protected void onConfirm(AjaxRequestTarget target) {
						if (delete)
							providerService.deleteSigningKey();
						else
							providerService.generateSigningKey();
						setResponsePage(OAuthSigningKeyPage.class);
					}

					@Override protected String getConfirmMessage() {
						return delete
								? _T("Deleting this key disables the OAuth2/OIDC provider and revokes all existing authorizations. "
										+ "Registered applications are preserved. Type <code>yes</code> below to continue.")
								: _T("Regenerating this key revokes all existing authorizations and replaces the public signing key. "
										+ "Applications need to sign in again and refresh their cached signing key. "
										+ "Type <code>yes</code> below to continue.");
					}

					@Override protected String getConfirmInput() {
						return "yes";
					}
				};
			}
		};
	}

	@Override
	protected Component newTopbarTitle(String id) {
		return new Label(id, _T("OAuth Signing Key"));
	}
}
