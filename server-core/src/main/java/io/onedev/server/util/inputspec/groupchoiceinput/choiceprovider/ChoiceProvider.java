package io.onedev.server.util.inputspec.groupchoiceinput.choiceprovider;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.util.facade.GroupFacade;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface ChoiceProvider extends Serializable {
	
	List<GroupFacade> getChoices(boolean allPossible);
	
}
