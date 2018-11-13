package io.onedev.server.util.inputspec.groupchoiceinput.choiceprovider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.util.facade.GroupFacade;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=100, name="All groups")
public class AllGroups implements ChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public List<GroupFacade> getChoices(boolean allPossible) {
		List<GroupFacade> groups = new ArrayList<>(OneDev.getInstance(CacheManager.class).getGroups().values());
		Collections.sort(groups, new Comparator<GroupFacade>() {

			@Override
			public int compare(GroupFacade o1, GroupFacade o2) {
				return o1.getName().compareTo(o2.getName());
			}
			
		});
		return groups;
	}

}
