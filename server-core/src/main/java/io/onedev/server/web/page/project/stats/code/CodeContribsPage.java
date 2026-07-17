package io.onedev.server.web.page.project.stats.code;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.PersonIdent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.user.card.PersonCardPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.overview.ProjectOverviewPage;
import io.onedev.server.xodus.CommitInfoService;

public class CodeContribsPage extends ProjectPage {

	private static final String USER_CARD_ID = "userCard";
	
	private static final String PARAM_PERIOD = "period";
	
	private static final ContributionPeriod DEFAULT_PERIOD = ContributionPeriod.SIX_MONTHS;
	
	private AbstractDefaultAjaxBehavior userCardBehavior;
	
	private ContributionPeriod period;
	
	public CodeContribsPage(PageParameters params) {
		super(params);
		
		var periodValue = params.get(PARAM_PERIOD).toOptionalString();
		if (periodValue != null) {
			try {
				period = ContributionPeriod.valueOf(periodValue.toUpperCase());
			} catch (IllegalArgumentException e) {
				period = DEFAULT_PERIOD;
			}
		} else {
			period = DEFAULT_PERIOD;
		}
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
		
		var periods = Arrays.asList(ContributionPeriod.values());
		var periodChoice = new DropDownChoice<>("period", new IModel<ContributionPeriod>() {

			@Override
			public void detach() {
			}

			@Override
			public ContributionPeriod getObject() {
				return period;
			}

			@Override
			public void setObject(ContributionPeriod object) {
				period = object;
			}
			
		}, periods, new IChoiceRenderer<ContributionPeriod>() {

			@Override
			public Object getDisplayValue(ContributionPeriod object) {
				return object.getDisplayName();
			}

			@Override
			public String getIdValue(ContributionPeriod object, int index) {
				return object.name();
			}

			@Override
			public ContributionPeriod getObject(String id, IModel<? extends List<? extends ContributionPeriod>> choices) {
				return ContributionPeriod.valueOf(id);
			}
			
		});
		periodChoice.add(new OnChangeAjaxBehavior() {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				setResponsePage(CodeContribsPage.class, paramsOf(getProject(), period));
			}
			
		});
		add(periodChoice);
		
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
				target.appendJavaScript("onedev.server.codeContribs.onUserCardAvailable();");
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(new CodeContribsResourceReference()));
		
		CommitInfoService commitInfoService = OneDev.getInstance(CommitInfoService.class);
		Map<Integer, Integer> overallContributions = 
				commitInfoService.getOverallContributions(getProject().getId());
		
		Integer fromDay = period.getFromDay();
		if (fromDay != null) {
			var fromDayValue = fromDay;
			overallContributions = overallContributions.entrySet().stream()
					.filter(it -> it.getKey() >= fromDayValue)
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		
		PageParameters params = TopContributorsResource.paramsOf(getProject()); 
		String topContributorsUrl = urlFor(new TopContributorsResourceReference(), params).toString();
		var mapper = OneDev.getInstance(ObjectMapper.class);
		try {
			var jsonOfData = mapper.writeValueAsString(overallContributions);
			var translations = new HashMap<String, String>();
			translations.put("commits", _T("commits"));
			translations.put("noData", _T("No data"));
			var jsonOfTranslations = mapper.writeValueAsString(translations);			
			CharSequence callback = userCardBehavior.getCallbackFunction(
					CallbackParameter.explicit("name"), CallbackParameter.explicit("emailAddress"));
			String toDay = fromDay != null ? String.valueOf((int) LocalDate.now().toEpochDay()) : "null";
			String script = String.format("onedev.server.codeContribs.onDomReady(%s, '%s', %s, %b, %s, %s, %s);", 
					jsonOfData, topContributorsUrl, callback, isDarkMode(), jsonOfTranslations,
					fromDay != null ? fromDay : "null", toDay);
			response.render(OnDomReadyHeaderItem.forScript(script));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}	
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Code Contributions"));
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isCodeManagement() && SecurityUtils.canReadCode(project))
			return new ViewStateAwarePageLink<>(componentId, getPageClass(), paramsOf(project));
		else
			return new ViewStateAwarePageLink<>(componentId, ProjectOverviewPage.class, ProjectOverviewPage.paramsOf(project.getId()));
	}
	
	public static PageParameters paramsOf(Project project, ContributionPeriod period) {
		PageParameters params = paramsOf(project);
		if (period != DEFAULT_PERIOD)
			params.add(PARAM_PERIOD, period.name().toLowerCase());
		return params;
	}

}
