package io.onedev.server.model.support.build;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.RegEx;
import io.onedev.server.annotation.SuggestionProvider;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.NamedElement;

@Editable
public class JobProperty implements NamedElement {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String value;
	
	private boolean archived;

	@Editable(order=100)
	@RegEx(pattern="[^@]+", message="Character '@' not allowed in property name")
	@SuggestionProvider("getNameSuggestions")
	@NotEmpty
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@SuppressWarnings("unused")
	private static List<InputCompletion> getNameSuggestions(InputStatus status) {
		BuildSpec buildSpec = BuildSpec.get();
		if (buildSpec != null) {
			List<String> candidates = new ArrayList<>(buildSpec.getPropertyMap().keySet());
			buildSpec.getProperties().forEach(it->candidates.remove(it.getName()));
			return BuildSpec.suggestOverrides(candidates, status);
		}
		return new ArrayList<>();
	}
	
	@Editable(order=200)
	@NotEmpty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Editable(order=300, description = "Mark a property archived if it is no longer used by current " +
			"build spec, but still need to exist to reproduce old builds. Archived properties " +
			"will not be shown by default")
	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}
}
