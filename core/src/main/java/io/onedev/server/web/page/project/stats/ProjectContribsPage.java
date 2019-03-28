package io.onedev.server.web.page.project.stats;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.cache.CommitInfoManager;
import io.onedev.server.git.Contribution;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Day;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.user.detail.UserDetailPanel;

@SuppressWarnings("serial")
public class ProjectContribsPage extends ProjectStatsPage {

	private static final String USER_DETAIL_ID = "userDetail";
	
	private AbstractDefaultAjaxBehavior userDetailBehavior;
	
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
		add(new WebMarkupContainer(USER_DETAIL_ID).setOutputMarkupId(true));
		add(userDetailBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				String jsonOfUserIdent = RequestCycle.get().getRequest().getPostParameters().getParameterValue("userIdent").toString();
				try {
					UserIdent userIdent = OneDev.getInstance(ObjectMapper.class).readValue(jsonOfUserIdent, UserIdent.class);
					Component userDetail = new UserDetailPanel(USER_DETAIL_ID, userIdent);
					userDetail.setOutputMarkupId(true);
					replace(userDetail);
					target.add(userDetail);
					target.appendJavaScript("onedev.server.stats.contribs.onUserDetailAvailable();");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
		});
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
		CharSequence callback = userDetailBehavior.getCallbackFunction(CallbackParameter.explicit("userIdent"));
		String script = String.format("onedev.server.stats.contribs.onDomReady(%s, '%s', %s);", 
				jsonOfData, topContributorsUrl, callback);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
