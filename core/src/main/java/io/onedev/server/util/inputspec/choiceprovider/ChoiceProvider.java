package io.onedev.server.util.inputspec.choiceprovider;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.util.editable.annotation.Editable;

@Editable
public interface ChoiceProvider extends Serializable {
	
	List<String> getChoices(boolean allPossible);
	
}
