package io.onedev.server.web.component.project.stats.issue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import io.onedev.server.search.entity.issue.ProjectIsCurrentCriteria;
import io.onedev.server.util.criteria.AndCriteria;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;

@SuppressWarnings("serial")
public class IssueStatsPanel extends Panel {

	private final IModel<Project> projectModel;
	
	private final IModel<Map<Integer, Long>> statsModel;
	
	public IssueStatsPanel(String id, IModel<Project> projectModel, IModel<Map<Integer, Long>> statsModel) {
		super(id);

		this.projectModel = projectModel;
		this.statsModel = statsModel;
	}
	
	private long getTotalCount() {
		return getStats().values().stream().collect(Collectors.summingLong(Long::longValue));
	}
	
	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		IssueQuery query = new IssueQuery(new ProjectIsCurrentCriteria());
		PageParameters params = ProjectIssueListPage.paramsOf(getProject(), query.toString(), 0);
		Link<Void> issuesLink = new BookmarkablePageLink<Void>("issues", ProjectIssueListPage.class, params);
		issuesLink.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				
				return getTotalCount() + " issues";
			}
			
		}));
		add(issuesLink);
		
		add(new ListView<>("states", new LoadableDetachableModel<List<Map.Entry<Integer, Long>>>() {

			@Override
			protected List<Map.Entry<Integer, Long>> load() {
				return new ArrayList<>(getStats().entrySet());
			}

		}) {

			@Override
			protected void populateItem(ListItem<Map.Entry<Integer, Long>> item) {
				Project project = getProject();
				Map.Entry<Integer, Long> entry = item.getModelObject();
				StateSpec stateSpec = getIssueSetting().getStateSpecs().get(entry.getKey());
				IssueQuery query = new IssueQuery(new AndCriteria<>(Lists.newArrayList(
						new ProjectIsCurrentCriteria(),
						new StateCriteria(stateSpec.getName(), IssueQueryLexer.Is))));
				PageParameters params = ProjectIssueListPage.paramsOf(project, query.toString(), 0);
				Link<Void> stateLink = new BookmarkablePageLink<Void>("link", ProjectIssueListPage.class, params);
				stateLink.add(new Label("label", entry.getValue() + " " + stateSpec.getName()));
				stateLink.add(AttributeAppender.append("style", "color:" + stateSpec.getColor()));
				item.add(stateLink);
			}

		});

	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(getTotalCount() != 0);
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		statsModel.detach();
		super.onDetach();
	}

	private Project getProject() {
		return projectModel.getObject();
	}
	
	private Map<Integer, Long> getStats() {
		return statsModel.getObject();
	}

}
