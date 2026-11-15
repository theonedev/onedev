package io.onedev.server.web.page.oauth;

import static io.onedev.server.web.translation.Translation._T;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.Serializable;
import java.net.URI;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.model.OAuthApplication;
import io.onedev.server.oauth.OAuthProtocol;
import io.onedev.server.validation.Validatable;

@Editable
@ClassValidating
public class OAuthApplicationBean implements Serializable, Validatable {

	private String name;
	private String homepage;
	private boolean confidential = true;
	private String redirectUris;

	@Editable(order=100)
	@NotEmpty
	@Size(max=255)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200)
	@NotEmpty
	@Size(max=2048)
	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	@Editable(order=300, name="Confidential Client", description="Check if the application can securely store a secret. "
			+ "Leave unchecked for desktop, mobile, and browser applications. S256 PKCE is required for all applications.")
	public boolean isConfidential() {
		return confidential;
	}

	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}

	@Editable(order=400, name="Redirect URIs", description="Specify one URI per line. "
			+ "Use HTTPS, loopback HTTP, or a native application scheme for public clients. "
			+ "URIs must match exactly, except public clients may choose any port for HTTP callbacks to 127.0.0.1 or [::1].")
	@Multiline(monospace=true)
	@NotEmpty
	@Size(max=16000)
	public String getRedirectUris() {
		return redirectUris;
	}

	public void setRedirectUris(String redirectUris) {
		this.redirectUris = redirectUris;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		context.disableDefaultConstraintViolation();
		boolean valid = true;
		if (homepage != null && !isValidHomepage()) {
			context.buildConstraintViolationWithTemplate(_T("Specify a valid HTTP or HTTPS homepage"))
					.addPropertyNode("homepage").addConstraintViolation();
			valid = false;
		}
		if (redirectUris != null && !areValidRedirectUris()) {
			context.buildConstraintViolationWithTemplate(_T("Specify up to 20 exact redirect URIs, using HTTPS, loopback HTTP, or a native app scheme"))
					.addPropertyNode("redirectUris").addConstraintViolation();
			valid = false;
		}
		return valid;
	}

	private boolean isValidHomepage() {
		try {
			var uri = URI.create(homepage);
			return ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
					&& uri.getHost() != null && uri.getUserInfo() == null;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private boolean areValidRedirectUris() {
		var uris = OAuthApplication.parseRedirectUris(redirectUris);
		return !uris.isEmpty() && uris.size() <= 20
				&& uris.stream().allMatch(it -> it.length() <= 2048 && OAuthProtocol.isValidRedirectUri(it, confidential));
	}
}
