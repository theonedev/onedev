package io.onedev.server.model.support.inputspec.choiceinput.choiceprovider;

import java.io.Serializable;
import java.util.Map;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public abstract class ChoiceProvider implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public abstract Map<String, String> getChoices(boolean allPossible);
	
}
