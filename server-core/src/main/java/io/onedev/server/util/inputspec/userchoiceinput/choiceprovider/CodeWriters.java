package io.onedev.server.util.inputspec.userchoiceinput.choiceprovider;

import java.util.ArrayList;
import java.util.List;

import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ProjectPrivilege;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=130, name="Users allowed to write code")
public class CodeWriters implements ChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public List<UserFacade> getChoices(boolean allPossible) {
		Project project = OneContext.get().getProject();
		if (project != null)
			return new ArrayList<>(SecurityUtils.getAuthorizedUsers(project.getFacade(), ProjectPrivilege.CODE_WRITE));
		else
			return new ArrayList<>();
	}

}
