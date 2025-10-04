package io.onedev.server.model.support.administration.jobexecutor;

import com.google.common.collect.Lists;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.k8shelper.RegistryLoginFacade;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Password;
import io.onedev.server.buildspec.job.JobVariable;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.interpolative.VariableInterpolator;
import io.onedev.server.web.util.SuggestionUtils;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Editable
public class RegistryLogin implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String registryUrl;
	
	private String userName;
	
	private String password;

	@Editable(order=100, placeholder="Docker Hub", description="Specify registry url. Leave empty for official registry")
	@Interpolative(variableSuggester = "suggestRegistryUrlVariables")
	public String getRegistryUrl() {
		return registryUrl;
	}
	
	public void setRegistryUrl(String registryUrl) {
		this.registryUrl = registryUrl;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestRegistryUrlVariables(String matchWith) {
		return SuggestionUtils.suggest(Lists.newArrayList(JobVariable.SERVER_URL.name().toLowerCase()), matchWith);
	}
	
	@Editable(order=200, description = "Specify user name of specified registry")
	@Interpolative(variableSuggester = "suggestUserNameVariables")
	@NotEmpty
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestUserNameVariables(String matchWith) {
		return SuggestionUtils.suggest(Lists.newArrayList(JobVariable.JOB_TOKEN.name().toLowerCase()), matchWith);
	}
	
	@Editable(order=300, name="Password", description = "Specify password or access token of specified registry")
	@NotEmpty
	@Password
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public RegistryLoginFacade getFacade(String jobToken) {
		var interpolator = new VariableInterpolator(t -> {
			if (t.equalsIgnoreCase(JobVariable.SERVER_URL.name()))
				return OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
			else if (t.equalsIgnoreCase(JobVariable.JOB_TOKEN.name()))
				return jobToken;
			else
				throw new ExplicitException("Unrecognized interpolation variable: " + t);
		});
		return new RegistryLoginFacade(
				interpolator.interpolate(getRegistryUrl()), 
				interpolator.interpolate(getUserName()), 
				getPassword());
	}
	
}