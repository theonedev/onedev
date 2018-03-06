package io.onedev.server.web.component.groupchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.util.facade.GroupFacade;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.utils.matchscore.MatchScoreProvider;
import io.onedev.utils.matchscore.MatchScoreUtils;

public class GroupChoiceProvider extends AbstractGroupChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public void query(String term, int page, Response<GroupFacade> response) {
		CacheManager cacheManager = OneDev.getInstance(CacheManager.class);
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