package io.onedev.server.web.page.project.stats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.git.Contribution;
import io.onedev.server.git.Contributor;
import io.onedev.server.manager.CommitInfoManager;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Day;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.util.avatar.AvatarManager;

class TopContributorsResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";
	
	private static final String PARAM_FROM = "from";
	
	private static final String PARAM_TO = "to";
	
	private static final String PARAM_TYPE = "type";
	
	private static final int TOP_CONTRIBUTORS = 100;
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();
		Long projectId = params.get(PARAM_PROJECT).toLong();
		Day fromDay = new Day(new Date(params.get(PARAM_FROM).toLong()));
		Day toDay = new Day(new Date(params.get(PARAM_TO).toLong()));
		Contribution.Type type = Contribution.Type.valueOf(params.get(PARAM_TYPE).toString());

		ResourceResponse response = new ResourceResponse();
		response.setContentType("application/json");
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				Project project = OneDev.getInstance(Dao.class).load(Project.class, projectId);
				
				if (!SecurityUtils.canReadCode(project.getFacade()))
					throw new UnauthorizedException();

				List<Contributor> topContributors = OneDev.getInstance(CommitInfoManager.class).getTopContributors(project, TOP_CONTRIBUTORS, type, fromDay, toDay);
				
				AvatarManager avatarManager = OneDev.getInstance(AvatarManager.class);

				List<Map<String, Object>> data = new ArrayList<>();
				for (Contributor contributor: topContributors) {
					Map<String, Object> contributorData = new HashMap<>();
					UserIdent userIdent = UserIdent.of(contributor.getAuthor(), "Git author");
					contributorData.put("author", userIdent);
					contributorData.put("authorAvatarUrl", avatarManager.getAvatarUrl(userIdent));
					contributorData.put("totalCommits", contributor.getTotalContribution().getCommits());
					contributorData.put("totalAdditions", contributor.getTotalContribution().getAdditions());
					contributorData.put("totalDeletions", contributor.getTotalContribution().getDeletions());
					
					Map<Long, Integer> dailyContributionsData = new HashMap<>();
					for (Map.Entry<Day, Integer> entry: contributor.getDailyContributions().entrySet()) {
						dailyContributionsData.put(entry.getKey().getDate().getTime(), entry.getValue());
					}
					contributorData.put("dailyContributions", dailyContributionsData);
					data.add(contributorData);
				}
				attributes.getResponse().write(OneDev.getInstance(ObjectMapper.class).writeValueAsBytes(data));
			}
			
		});

		return response;
	}

	public static PageParameters paramsOf(Project project) {
		PageParameters params = new PageParameters();
		params.set(PARAM_PROJECT, project.getId());
		return params;
	}
	
}
