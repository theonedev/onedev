package com.turbodev.server.web.component.groupchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.turbodev.utils.matchscore.MatchScoreProvider;
import com.turbodev.utils.matchscore.MatchScoreUtils;
import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.CacheManager;
import com.turbodev.server.util.facade.GroupFacade;
import com.turbodev.server.web.WebConstants;
import com.turbodev.server.web.component.select2.Response;
import com.turbodev.server.web.component.select2.ResponseFiller;

public class GroupChoiceProvider extends AbstractGroupChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public void query(String term, int page, Response<GroupFacade> response) {
		CacheManager cacheManager = TurboDev.getInstance(CacheManager.class);
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