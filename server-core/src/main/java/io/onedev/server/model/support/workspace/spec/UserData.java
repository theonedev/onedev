package io.onedev.server.model.support.workspace.spec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.UserDataFacade;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class UserData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String key;
		
	private List<UserDataEntry> entries = new ArrayList<>();
	
	@Editable(order=100, name="Data Key", description = """
			Specify a key to identify the user data. Data with same key will be shared across workspaces 
			even if project is different""")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Editable(order=200, name="Data Entries", description = "Specify data entries to persist across workspaces")
	@Valid
	@Size(min=1, max=100)
	public List<UserDataEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<UserDataEntry> entries) {
		this.entries = entries;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}
		
	public UserDataFacade getFacade() {
		return new UserDataFacade(key, entries.stream().map(UserDataEntry::getFacade).collect(Collectors.toList()));
	}
	
}
