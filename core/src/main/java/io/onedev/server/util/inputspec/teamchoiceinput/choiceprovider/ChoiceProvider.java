package io.onedev.server.util.inputspec.teamchoiceinput.choiceprovider;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.util.facade.TeamFacade;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface ChoiceProvider extends Serializable {
	
	List<TeamFacade> getChoices(boolean allPossible);
	
}
