package com.gitplex.server.web.component.groupchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.util.facade.GroupFacade;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.select2.Response;
import com.gitplex.server.web.component.select2.ResponseFiller;
import com.gitplex.utils.matchscore.MatchScoreProvider;
import com.gitplex.utils.matchscore.MatchScoreUtils;

public class GroupChoiceProvider extends AbstractGroupChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public void query(String term, int page, Response<GroupFacade> response) {
		CacheManager cacheManager = GitPlex.getInstance(CacheManager.class);
		List<GroupFacade> choices = new ArrayList<GroupFacade>(cacheManager.getGroups().values());
			
		choices.sort(Comparator.comparing(GroupFacade::getName));
		
		choices = MatchScoreUtils.filterAndSort(choices, new MatchScoreProvider<GroupFacade>() {

			@Override
			public double getMatchScore(GroupFacade object) {
				return MatchScoreUtils.getMatchScore(object.getName(), term);
			}
			
		});
		
		new ResponseFiller<GroupFacade>(response).fill(choices, page, WebConstants.PAGE_SIZE);
	}

}