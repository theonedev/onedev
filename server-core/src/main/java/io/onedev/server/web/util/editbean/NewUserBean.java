package io.onedev.server.web.util.editbean;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.User;
import io.onedev.server.model.support.AiModelSetting;

@Editable
public class NewUserBean extends User {

	private static final long serialVersionUID = 1L;

	public static final String PROP_EMAIL_ADDRESS = "emailAddress";

	public static final String PROP_AI_MODEL_SETTING = "aiModelSetting";
	
	private String emailAddress;

	private AiModelSetting aiModelSetting = new AiModelSetting();

	@Editable(order=1000)
	@DependsOn(property="type", value="ORDINARY")
	@NotEmpty
	@Email
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	@Editable(order=1000, name="AI Model Settings")
	@DependsOn(property="type", value="AI")
	@NotNull
	public AiModelSetting getAiModelSetting() {
		return aiModelSetting;
	}

	public void setAiModelSetting(AiModelSetting aiModelSetting) {
		this.aiModelSetting = aiModelSetting;
	}
}
