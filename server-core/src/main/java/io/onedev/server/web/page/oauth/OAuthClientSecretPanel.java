package io.onedev.server.web.page.oauth;

import static io.onedev.server.web.translation.Translation._T;

import jakarta.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.http.WebResponse;

import io.onedev.server.oauth.OAuthProviderService;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;

public class OAuthClientSecretPanel extends Panel {

	@Inject
	private OAuthProviderService providerService;

	private final Long applicationId;
	private final boolean confidential;
	private transient String secret;

	public OAuthClientSecretPanel(String id, Long applicationId, boolean confidential, String secret) {
		super(id);
		this.applicationId = applicationId;
		this.confidential = confidential;
		this.secret = secret;
		setOutputMarkupId(true);
	}

	@Override protected void onInitialize() {
		super.onInitialize();
		add(new Label("value", (IModel<String>) () -> {
			if (!confidential)
				return _T("Not applicable");
			return secret != null ? secret : "*".repeat(30);
		}));
		add(new CopyToClipboardLink("copy", (IModel<String>) () -> secret) {
			@Override protected void onConfigure() {
				super.onConfigure();
				setVisible(confidential && secret != null);
			}
		});
		add(new AjaxSubmitLink("rotate") {
			@Override protected void onConfigure() {
				super.onConfigure();
				setVisible(confidential && secret == null);
			}

			@Override protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(
						_T("Rotate the client secret and revoke all authorizations?")));
			}

			@Override protected void onSubmit(AjaxRequestTarget target) {
				secret = providerService.rotateSecret(applicationId);
				setVisible(false);
				target.add(OAuthClientSecretPanel.this);
			}
		});
		add(new WebMarkupContainer("notice") {
			@Override protected void onConfigure() {
				super.onConfigure();
				setVisible(confidential && secret != null);
			}
		});
	}

	@Override protected void onConfigure() {
		super.onConfigure();
		((WebResponse) getResponse()).setHeader("Cache-Control", "no-store");
	}

	@Override protected void onDetach() {
		// Keep the clear-text secret only for its initial response, never in the saved page.
		secret = null;
		super.onDetach();
	}
}
