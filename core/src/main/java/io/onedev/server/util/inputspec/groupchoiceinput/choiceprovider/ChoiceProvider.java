package io.onedev.server.util.inputspec.groupchoiceinput.choiceprovider;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.facade.GroupFacade;

@Editable
public interface ChoiceProvider extends Serializable {
	
	List<GroupFacade> getChoices(boolean allPossible);
	
}
