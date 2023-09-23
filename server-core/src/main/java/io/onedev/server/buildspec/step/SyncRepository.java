package io.onedev.server.buildspec.step;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.server.annotation.*;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Null;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Editable
public abstract class SyncRepository extends ServerSideStep {

	private static final long serialVersionUID = 1L;

	private String remoteUrl;
	
	private String userName;
	
	private String passwordSecret;

	private String certificate;

	private boolean force;
	
	private String proxy;

	@Editable(order=100, name="Remote URL", description="Specify URL of remote git repository. "
			+ "Only http/https protocol is supported")
	@RegEx(pattern = "^http(s)?://.*", message="Only http/https protocol is supported")
	@Interpolative(variableSuggester="suggestVariables", exampleVar = "http://localhost/test")
	@NotEmpty
	public String getRemoteUrl() {
		return remoteUrl;
	}

	@Editable(order=200)
	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}

	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}
	
	@Editable(order=300, description="Optionally specify user name to access above repository")
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Editable(order=400, name="Password / Access Token", 
			description="Specify a secret to be used as password or access token to access above repository")
	@ChoiceProvider("getPasswordSecretChoices")
	public String getPasswordSecret() {
		return passwordSecret;
	}

	public void setPasswordSecret(String passwordSecret) {
		this.passwordSecret = passwordSecret;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getPasswordSecretChoices() {
		return Project.get().getHierarchyJobSecrets()
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}

	@Editable(order=450, name="Certificates to Trust", placeholder = "Base64 encoded PEM format, starting with " +
			"-----BEGIN CERTIFICATE----- and ending with -----END CERTIFICATE-----", 
			description = "Specify certificate to trust if you are using self-signed certificate for above url")
	@Multiline(monospace = true)
	@Interpolative(variableSuggester="suggestVariables")
	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}
	
	@SuppressWarnings("unused")
	private static String getLfsDescription() {
		if (!Bootstrap.isInDocker()) {
			return "If this option is enabled, git lfs command needs to be installed on OneDev server "
					+ "(even this step runs on other node)";
		} else {
			return null;
		}
	}

	@Editable(order=500, description="Whether or not use force option to overwrite changes in case ref updating "
			+ "can not be fast-forwarded")
	public boolean isForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

	@Editable(order=600, placeholder = "No proxy", description = "Optionally configure proxy for this step. Proxy " +
			"should be in the format of &lt;proxy host&gt;:&lt;proxy port&gt;")
	public String getProxy() {
		return proxy;
	}

	public void setProxy(String proxy) {
		this.proxy = proxy;
	}

	public String getRemoteUrlWithCredential(Build build) {
		String encodedPassword = null;
		if (getPasswordSecret() != null) {
			try {
				String password = build.getJobAuthorizationContext().getSecretValue(getPasswordSecret());
				encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}

		String protocol = StringUtils.substringBefore(getRemoteUrl(), "//");
		String hostAndPath = StringUtils.substringAfter(getRemoteUrl(), "//");
		
		String remoteUrlWithCredentials = protocol + "//";
		
		if (getUserName() != null && encodedPassword != null)
			remoteUrlWithCredentials += getUserName() + ":" + encodedPassword + "@" + hostAndPath;
		else if (getUserName() != null)
			remoteUrlWithCredentials += getUserName() + "@" + hostAndPath;
		else if (encodedPassword != null)
			remoteUrlWithCredentials += encodedPassword + "@" + hostAndPath;
		else
			remoteUrlWithCredentials += hostAndPath;
		
		return remoteUrlWithCredentials;
	}
	
	protected static void configureProxy(Commandline git, @Nullable String proxy) {
		if (proxy != null)
			git.addArgs("-c", "http.proxy=" + proxy, "-c", "https.proxy=" + proxy);
	}
	
	@Nullable
	protected static File writeCertificate(@Nullable String certificate) {
		if (certificate != null) {
			try {
				var file = File.createTempFile("certificate", "pem");
				FileUtils.writeFile(file, certificate);
				return file;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}

	protected static void configureCertificate(Commandline git, @Nullable File certificateFile) {
		if (certificateFile != null)
			git.addArgs("-c", "http.sslCAInfo=" + certificateFile.getAbsolutePath());
	}
	
}
