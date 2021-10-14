package io.onedev.server.web.component.project.path;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;

@SuppressWarnings("serial")
public class ProjectPathPanel extends GenericPanel<Project> {

	public ProjectPathPanel(String id, IModel<Project> model) {
		super(id, model);
	}

	private Project getProject() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<Project>("parents", new LoadableDetachableModel<List<Project>>() {

			@Override
			protected List<Project> load() {
				List<Project> ancestors = new ArrayList<>();
				Project parent = getProject().getParent();
				while (parent != null) {
					ancestors.add(parent);
					parent = parent.getParent();
				}
				Collections.reverse(ancestors);
				return ancestors;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Project> item) {
				Project parent = item.getModelObject();
				if (SecurityUtils.canAccess(parent)) {
					Link<Void> link = new ViewStateAwarePageLink<Void>("link", 
							ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(parent));
					link.add(new Label("label", parent.getName()));
					item.add(link);
				} else {
					WebMarkupContainer link = new WebMarkupContainer("link") {

						@Override
						protected void onComponentTag(ComponentTag tag) {
							super.onComponentTag(tag);
							tag.setName("span");
						}
						
					};
					link.add(new Label("label", parent.getName()));
					item.add(link);
				}
				item.add(newSeparator("separator"));
			}
			
		});
		
		Link<Void> link = new ViewStateAwarePageLink<Void>("link", 
				ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(getProject()));
		link.add(new Label("label", getProject().getName()));
		add(link);
	}

	protected Component newSeparator(String componentId) {
		return new Label(componentId, "/").add(AttributeAppender.append("class", "mx-1"));
	}
	
}
