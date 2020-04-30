package io.onedev.server.model.support.inputspec.groupchoiceinput.choiceprovider;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.model.Group;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface ChoiceProvider extends Serializable {
	
	List<Group> getChoices(boolean allPossible);
	
}
