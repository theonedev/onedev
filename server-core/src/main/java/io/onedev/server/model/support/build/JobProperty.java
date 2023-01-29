package io.onedev.server.model.support.build;

import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.NamedElement;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.SuggestionProvider;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Editable
public class JobProperty implements NamedElement, Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String value;

	@Editable(order=100)
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
	
}
