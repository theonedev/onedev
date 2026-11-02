package io.onedev.server.web.page.oauth;

import java.io.Serializable;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;

@Editable
public class OAuthApplicationBean implements Serializable {

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
}
