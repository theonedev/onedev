package io.onedev.server.util.input.userchoiceprovider;

import java.util.ArrayList;
import java.util.List;

import io.onedev.server.model.Project;
import io.onedev.server.security.ProjectPrivilege;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.facade.UserFacade;

@Editable(order=130, name="Users able to write the project")
public class ProjectWriters implements ChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public List<UserFacade> getChoices(boolean allPossible) {
		Project project = OneContext.get().getProject();
		return new ArrayList<>(SecurityUtils.getAuthorizedUsers(project.getFacade(), ProjectPrivilege.WRITE));
	}

}
