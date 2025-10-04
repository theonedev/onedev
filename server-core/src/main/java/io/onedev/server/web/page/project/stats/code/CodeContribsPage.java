package io.onedev.server.web.page.project.stats.code;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
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
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.user.card.PersonCardPanel;
import io.onedev.server.xodus.CommitInfoService;

public class CodeContribsPage extends CodeStatsPage {

	private static final String USER_CARD_ID = "userCard";
	
	private AbstractDefaultAjaxBehavior userCardBehavior;
	
	public CodeContribsPage(PageParameters params) {
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
			add(new Label("note", MessageFormat.format(_T("Contributions to {0} branch, excluding merge commits"), getProject().getDefaultBranch())));
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
				target.appendJavaScript("onedev.server.codeStats.contribs.onUserCardAvailable();");
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		CommitInfoService commitInfoService = OneDev.getInstance(CommitInfoService.class);
		Map<Integer, Integer[]> data = new HashMap<>();
		Map<Integer, GitContribution> overallContributions = 
				commitInfoService.getOverallContributions(getProject().getId());
		for (Map.Entry<Integer, GitContribution> entry: overallContributions.entrySet()) {
			GitContribution contribution = entry.getValue();
			Integer[] dataValue = new Integer[] {contribution.getCommits(), contribution.getAdditions(), contribution.getDeletions()};
			data.put(entry.getKey(), dataValue);
		}
		
		PageParameters params = TopContributorsResource.paramsOf(getProject()); 
		String topContributorsUrl = urlFor(new TopContributorsResourceReference(), params).toString();
		var mapper = OneDev.getInstance(ObjectMapper.class);
		try {
			var jsonOfData = mapper.writeValueAsString(data);
			var translations = new HashMap<String, String>();
			translations.put("commits", _T("commits"));
			translations.put("noData", _T("No data"));
			var jsonOfTranslations = mapper.writeValueAsString(translations);			
			CharSequence callback = userCardBehavior.getCallbackFunction(
					CallbackParameter.explicit("name"), CallbackParameter.explicit("emailAddress"));
			String script = String.format("onedev.server.codeStats.contribs.onDomReady(%s, '%s', %s, %b, %s);", 
					jsonOfData, topContributorsUrl, callback, isDarkMode(), jsonOfTranslations);
			response.render(OnDomReadyHeaderItem.forScript(script));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}	
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Code Contribution Statistics"));
	}

}
