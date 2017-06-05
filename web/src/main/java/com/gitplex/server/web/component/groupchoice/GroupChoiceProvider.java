package com.gitplex.server.web.component.groupchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.util.facade.GroupFacade;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.select2.Response;
import com.gitplex.server.web.component.select2.ResponseFiller;

public class GroupChoiceProvider extends AbstractGroupChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public void query(String term, int page, Response<GroupFacade> response) {
		CacheManager cacheManager = GitPlex.getInstance(CacheManager.class);
		List<GroupFacade> choices = new ArrayList<GroupFacade>(cacheManager.getGroups().values());
			
		for (Iterator<GroupFacade> it = choices.iterator(); it.hasNext();) {
			GroupFacade group = it.next();
			if (!group.matchesQuery(term))
				it.remove();
		}
		choices.sort(Comparator.comparing(GroupFacade::getName));
		
		new ResponseFiller<GroupFacade>(response).fill(choices, page, WebConstants.PAGE_SIZE);
	}

}