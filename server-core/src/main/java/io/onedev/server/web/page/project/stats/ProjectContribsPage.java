package io.onedev.server.web.page.project.stats;

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
import org.eclipse.jgit.lib.PersonIdent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.git.GitContribution;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Day;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.user.card.PersonCardPanel;

@SuppressWarnings("serial")
public class ProjectContribsPage extends ProjectStatsPage {

	private static final String USER_CARD_ID = "userCard";
	
	private AbstractDefaultAjaxBehavior userCardBehavior;
	
	public ProjectContribsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (getProject().getDefaultBranch() != null)
			add(new Label("note", "Contributions to " + getProject().getDefaultBranch() + " branch, excluding merge commits"));
		else
			add(new WebMarkupContainer("note").setVisible(false));
		add(new WebMarkupContainer(USER_CARD_ID).setOutputMarkupId(true));
		add(userCardBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				String name = RequestCycle.get().getRequest().getPostParameters()
						.getParameterValue("name").toString();
				String emailAddress = RequestCycle.get().getRequest().getPostParameters()
						.getParameterValue("emailAddress").toString();
				PersonIdent author = new PersonIdent(name, emailAddress);
				Component userCard = new PersonCardPanel(USER_CARD_ID, author, "Author");
				userCard.setOutputMarkupId(true);
				replace(userCard);
				target.add(userCard);
				target.appendJavaScript("onedev.server.stats.contribs.onUserCardAvailable();");
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class);
		Map<Integer, Integer[]> data = new HashMap<>();
		Map<Day, GitContribution> overallContributions = commitInfoManager.getOverallContributions(getProject());
		for (Map.Entry<Day, GitContribution> entry: overallContributions.entrySet()) {
			GitContribution contribution = entry.getValue();
			Integer[] dataValue = new Integer[] {contribution.getCommits(), contribution.getAdditions(), contribution.getDeletions()};
			data.put(entry.getKey().getValue(), dataValue);
		}
		
		PageParameters params = TopContributorsResource.paramsOf(getProject()); 
		String topContributorsUrl = urlFor(new TopContributorsResourceReference(), params).toString();
		String jsonOfData;
		try {
			jsonOfData = OneDev.getInstance(ObjectMapper.class).writeValueAsString(data);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		CharSequence callback = userCardBehavior.getCallbackFunction(
				CallbackParameter.explicit("name"), CallbackParameter.explicit("emailAddress"));
		String script = String.format("onedev.server.stats.contribs.onDomReady(%s, '%s', %s);", 
				jsonOfData, topContributorsUrl, callback);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Contribution Statistics");
	}

}
