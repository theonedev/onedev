package io.onedev.server.util.inputspec.groupchoiceinput.choiceprovider;

import java.util.ArrayList;
import java.util.List;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.facade.GroupFacade;

@Editable(order=100, name="All groups")
public class AllGroups implements ChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public List<GroupFacade> getChoices(boolean allPossible) {
		return new ArrayList<>(OneDev.getInstance(CacheManager.class).getGroups().values());
	}

}
