package io.onedev.server.web.page.project.stats;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.git.Contribution;
import io.onedev.server.manager.CommitInfoManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Day;

@SuppressWarnings("serial")
public class ProjectContribsPage extends ProjectStatsPage {

	public ProjectContribsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject().getFacade());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Label("note", "Contributions to " + getProject().getDefaultBranch() + " branch, excluding merge commits"));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class);
		Map<Long, Integer[]> data = new HashMap<>();
		Map<Day, Contribution> overallContributions = commitInfoManager.getOverallContributions(getProject());
		for (Map.Entry<Day, Contribution> entry: overallContributions.entrySet()) {
			Contribution contribution = entry.getValue();
			Integer[] dataValue = new Integer[] {contribution.getCommits(), contribution.getAdditions(), contribution.getDeletions()};
			data.put(entry.getKey().getDate().getTime(), dataValue);
		}
		
		PageParameters params = TopContributorsResource.paramsOf(getProject()); 
		String topContributorsUrl = urlFor(new TopContributorsResourceReference(), params).toString();
		String jsonOfData;
		try {
			jsonOfData = OneDev.getInstance(ObjectMapper.class).writeValueAsString(data);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		String script = String.format("onedev.server.stats.contribs.onDomReady(%s, '%s');", jsonOfData, topContributorsUrl);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
