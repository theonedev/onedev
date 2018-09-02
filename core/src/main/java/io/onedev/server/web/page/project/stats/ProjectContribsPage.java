package io.onedev.server.web.page.project.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.git.Contribution;
import io.onedev.server.git.Contributor;
import io.onedev.server.manager.CommitInfoManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Constants;
import io.onedev.server.util.Day;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.link.UserLink;

@SuppressWarnings("serial")
public class ProjectContribsPage extends ProjectStatsPage {

	private final IModel<List<DayContribution>> overallContributionsModel = 
			new LoadableDetachableModel<List<DayContribution>>() {

		@Override
		protected List<DayContribution> load() {
			Map<Day, Contribution> map = OneDev.getInstance(CommitInfoManager.class).getOverallContributions(getProject());
			return sort(map);
		}
		
	};

	private String fromDayText;
	
	private String toDayText;
	
	private String orderBy = Contribution.Type.COMMITS.name();
	
	public ProjectContribsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onDetach() {
		overallContributionsModel.detach();
		super.onDetach();
	}

	private List<DayContribution> sort(Map<Day, Contribution> map) {
		List<DayContribution> list = new ArrayList<>();
		for (Map.Entry<Day, Contribution> entry: map.entrySet())
			list.add(new DayContribution(entry.getKey(), entry.getValue()));
		Collections.sort(list);
		return list;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ContributionListPanel("overallContribution", overallContributionsModel));
		
		Form<?> form = new Form<Void>("topOptions");
		form.add(new SubmitLink("update") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				
				if (StringUtils.isNotBlank(fromDayText)) {
					try {
						Constants.DATE_FORMATTER.parseDateTime(fromDayText);
					} catch (IllegalArgumentException e) {
						error("Invalid fromDay");
					}
				}
				
				if (StringUtils.isNotBlank(toDayText)) {
					try {
						Constants.DATE_FORMATTER.parseDateTime(toDayText);
					} catch (IllegalArgumentException e) {
						error("Invalid toDay");
					}
				}
			}
			
		});
		form.add(new NotificationPanel("feedback", form));
		
		form.add(new TextField<String>("fromDay", new PropertyModel<String>(this, "fromDayText")));
		form.add(new TextField<String>("toDay", new PropertyModel<String>(this, "toDayText")));
		List<String> orderByChoices = new ArrayList<>();
		for (Contribution.Type type: Contribution.Type.values())
			orderByChoices.add(type.name());
		
		form.add(new DropDownChoice<String>("orderBy", new PropertyModel<String>(this, "orderBy"), orderByChoices));
		
		add(form);
		
		add(new ListView<Contributor>("topContributors", new LoadableDetachableModel<List<Contributor>>() {

			@Override
			protected List<Contributor> load() {
				List<DayContribution> overallContributions = overallContributionsModel.getObject();
				int size = overallContributions.size();
				if (size != 0) {
					Day fromDay;
					if (StringUtils.isNotBlank(fromDayText)) {
						fromDay = new Day(Constants.DATE_FORMATTER.parseDateTime(fromDayText));
					} else {
						fromDay = overallContributions.get(0).getDay();
					}
					
					Day toDay;
					if (StringUtils.isNotBlank(toDayText)) {
						toDay = new Day(Constants.DATE_FORMATTER.parseDateTime(toDayText));
					} else {
						toDay = overallContributions.get(size-1).getDay();
					}
					
					return OneDev.getInstance(CommitInfoManager.class)
							.getTopContributors(getProject(), 100, Contribution.Type.valueOf(orderBy), fromDay, toDay);
				} else {
					return new ArrayList<>();
				}
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Contributor> item) {
				Contributor userContribution = item.getModelObject();
				Fragment fragment = new Fragment("user", "userFrag", ProjectContribsPage.this);
				fragment.add(new AvatarLink("avatar", userContribution.getUser()));
				fragment.add(new UserLink("link", userContribution.getUser()));
				item.add(fragment);

				item.add(new ContributionListPanel("contributions", new AbstractReadOnlyModel<List<DayContribution>>() {

					@Override
					public List<DayContribution> getObject() {
						return sort(item.getModelObject().getDailyContributions());
					}
					
				}));
			}
			
		});
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject().getFacade());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.stats.contribs.onDomReady();"));
	}

}
