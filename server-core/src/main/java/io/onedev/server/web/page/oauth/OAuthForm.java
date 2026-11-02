package io.onedev.server.web.page.oauth;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.Model;

import com.nimbusds.oauth2.sdk.id.State;

/** Explicit anti-CSRF token for consent and application management POSTs. */
public class OAuthForm extends Form<Void> {

	private final String csrfToken = new State(32).getValue();

	public OAuthForm(String id) {
		super(id);
		var csrf = new HiddenField<>("csrf", Model.of(csrfToken));
		csrf.setRequired(true);
		csrf.add(validatable -> {
			if (!csrfToken.equals(validatable.getValue()))
				validatable.error(new org.apache.wicket.validation.ValidationError().setMessage("Invalid form token"));
		});
		add(csrf);
	}
}
