package com.gitplex.server.web.page.project.stats;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
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

import com.gitplex.server.GitPlex;
import com.gitplex.server.git.DayAndCommits;
import com.gitplex.server.git.UserContribution;
import com.gitplex.server.manager.CommitInfoManager;
import com.gitplex.server.util.Day;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.avatar.AvatarLink;
import com.gitplex.server.web.component.link.UserLink;
import com.gitplex.server.web.page.project.ProjectPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class ProjectStatsPage extends ProjectPage {

	private final IModel<List<DayAndCommits>> overallContributionsModel = 
			new LoadableDetachableModel<List<DayAndCommits>>() {

		@Override
		protected List<DayAndCommits> load() {
			return GitPlex.getInstance(CommitInfoManager.class).getOverallContributions(getProject());
		}
		
	};
	
	private String fromDayText;
	
	private String toDayText;
	
	public ProjectStatsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onDetach() {
		overallContributionsModel.detach();
		super.onDetach();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ContributionPanel("overallContribution", overallContributionsModel));
		
		Form<?> form = new Form<Void>("dateRange");
		form.add(new SubmitLink("update") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				
				if (StringUtils.isNotBlank(fromDayText)) {
					try {
						WebConstants.DATE_FORMATTER.parseDateTime(fromDayText);
					} catch (IllegalArgumentException e) {
						error("Invalid fromDay");
					}
				}
				
				if (StringUtils.isNotBlank(toDayText)) {
					try {
						WebConstants.DATE_FORMATTER.parseDateTime(toDayText);
					} catch (IllegalArgumentException e) {
						error("Invalid toDay");
					}
				}
			}
			
		});
		form.add(new NotificationPanel("feedback", form));
		
		form.add(new TextField<String>("fromDay", new PropertyModel<String>(this, "fromDayText")));
		form.add(new TextField<String>("toDay", new PropertyModel<String>(this, "toDayText")));
		add(form);
		
		add(new ListView<UserContribution>("userContributions", new LoadableDetachableModel<List<UserContribution>>() {

			@Override
			protected List<UserContribution> load() {
				List<DayAndCommits> overallContributions = overallContributionsModel.getObject();
				int size = overallContributions.size();
				if (size != 0) {
					Day fromDay;
					if (StringUtils.isNotBlank(fromDayText)) {
						fromDay = new Day(WebConstants.DATE_FORMATTER.parseDateTime(fromDayText));
					} else {
						fromDay = overallContributions.get(0).getDay();
					}
					
					Day toDay;
					if (StringUtils.isNotBlank(toDayText)) {
						toDay = new Day(WebConstants.DATE_FORMATTER.parseDateTime(toDayText));
					} else {
						toDay = overallContributions.get(size-1).getDay();
					}
					
					return GitPlex.getInstance(CommitInfoManager.class)
							.getUserContributions(getProject(), 10, fromDay, toDay);
				} else {
					return new ArrayList<>();
				}
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<UserContribution> item) {
				UserContribution userContribution = item.getModelObject();
				Fragment fragment = new Fragment("user", "userFrag", ProjectStatsPage.this);
				fragment.add(new AvatarLink("avatar", userContribution.getUser()));
				fragment.add(new UserLink("link", userContribution.getUser()));
				item.add(fragment);

				item.add(new ContributionPanel("userContribution", new AbstractReadOnlyModel<List<DayAndCommits>>() {

					@Override
					public List<DayAndCommits> getObject() {
						return item.getModelObject().getDayAndCommits();
					}
					
				}));
			}
			
		});
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ProjectStatsResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("gitplex.server.stats.onDomReady();"));
	}

}
