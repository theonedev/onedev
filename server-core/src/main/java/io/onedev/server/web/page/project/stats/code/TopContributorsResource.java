package io.onedev.server.web.page.project.stats.code;

import static io.onedev.server.util.DateUtils.formatISO8601Date;
import static io.onedev.server.util.DateUtils.toDate;
import static java.time.LocalDate.ofEpochDay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.eclipse.jgit.lib.PersonIdent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.git.GitContribution;
import io.onedev.server.git.GitContributor;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.search.commit.AfterCriteria;
import io.onedev.server.search.commit.AuthorCriteria;
import io.onedev.server.search.commit.BeforeCriteria;
import io.onedev.server.search.commit.CommitQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.avatar.AvatarService;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;
import io.onedev.server.web.page.user.profile.UserProfilePage;
import io.onedev.server.xodus.CommitInfoService;

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
		int fromDay = params.get(PARAM_FROM).toInteger();
		int toDay = params.get(PARAM_TO).toInteger();
		GitContribution.Type type = GitContribution.Type.valueOf(params.get(PARAM_TYPE).toString());

		ResourceResponse response = new ResourceResponse();
		response.setContentType("application/json");
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				Project project = OneDev.getInstance(Dao.class).load(Project.class, projectId);
				
				if (!SecurityUtils.canReadCode(project))
					throw new UnauthorizedException();

				String fromDate = formatISO8601Date(toDate(ofEpochDay(fromDay).atStartOfDay()));
				String toDate = formatISO8601Date(toDate(ofEpochDay(toDay).plusDays(1).atStartOfDay()));
				
				List<GitContributor> topContributors = OneDev.getInstance(CommitInfoService.class)
						.getTopContributors(project.getId(), TOP_CONTRIBUTORS, type, fromDay, toDay);
				
				AvatarService avatarService = OneDev.getInstance(AvatarService.class);
				EmailAddressService emailAddressService = OneDev.getInstance(EmailAddressService.class);

				List<Map<String, Object>> data = new ArrayList<>();
				for (GitContributor contributor: topContributors) {
					Map<String, Object> contributorData = new HashMap<>();
					PersonIdent author = contributor.getAuthor();
					contributorData.put("authorName", author.getName());
					contributorData.put("authorEmailAddress", author.getEmailAddress());
					contributorData.put("authorAvatarUrl", avatarService.getPersonAvatarUrl(author));
					contributorData.put("totalCommits", contributor.getTotalContribution().getCommits());
					contributorData.put("totalAdditions", contributor.getTotalContribution().getAdditions());
					contributorData.put("totalDeletions", contributor.getTotalContribution().getDeletions());

					AuthorCriteria authorCriteria;
					EmailAddress emailAddress = emailAddressService.findByValue(author.getEmailAddress());
					if (emailAddress != null && emailAddress.isVerified()) {
						var user = emailAddress.getOwner();
						authorCriteria = new AuthorCriteria(Lists.newArrayList("@" + user.getName()));
						contributorData.put("authorProfileUrl", RequestCycle.get().urlFor(UserProfilePage.class, UserProfilePage.paramsOf(user)));
					} else {
						authorCriteria = new AuthorCriteria(Lists.newArrayList(
								author.getName() + " <" + author.getEmailAddress() + ">"
						));
					}
					
					CommitQuery query = new CommitQuery(Lists.newArrayList(
							authorCriteria,
							new BeforeCriteria(Lists.newArrayList(toDate)), 
							new AfterCriteria(Lists.newArrayList(fromDate))
					));

					contributorData.put("commitsUrl", RequestCycle.get().urlFor(
							ProjectCommitsPage.class, ProjectCommitsPage.paramsOf(project, query.toString(), null)));
					
					Map<Integer, Integer> dailyContributionsData = new HashMap<>();
					for (Map.Entry<Integer, Integer> entry: contributor.getDailyContributions().entrySet()) 
						dailyContributionsData.put(entry.getKey(), entry.getValue());
					
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
