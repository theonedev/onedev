package io.onedev.server.plugin.github;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Editable(name="GitHub Integration", descriptionProvider="getDescription")
public class GitHubSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private String clientId;
	
	private String clientSecret;
	
	@Editable(order=100, description="Client ID when register OneDev as OAuth application in GitHub")
	@NotEmpty
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Editable(order=200, description="Client secret when register OneDev as OAuth application in GitHub")
	@Password
	@NotEmpty
	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	
	@SuppressWarnings("unused")
	private static String getDescription() {
		String callbackUrl = OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl() + "/" + GitHubCallbackPage.MOUNT_PATH;
		
		String copyClipboardScript = String.format("new Clipboard(\"#copy-github-callback-url\", {text: function() {return \"%s\";}});", callbackUrl);
		return String.format("Register OneDev as an OAuth application in GitHub and you will be able to:"
				+ "<p>"
				+ "<ul>"
				+ "<li> Sign into OneDev with GitHub account"
				+ "<li> Import GitHub repositories and issues when creating new projects"
				+ "</ul>"
				+ "When register at GitHub side, use authorization call back URL <code>%s</code> <a class='pressable' id='copy-github-callback-url' title='Copy to clipboard' onclick='javascript:%s'><svg class='icon icon-sm mr-2'><use xlink:href='%s'/></svg></a> "
				+ "For details on how to set up GitHub integration, please check <a href='$docRoot/pages/github-integration.md' target='_blank'>online manual</a>", 
				callbackUrl, copyClipboardScript, SpriteImage.getVersionedHref("copy"));		
	}
	
}
