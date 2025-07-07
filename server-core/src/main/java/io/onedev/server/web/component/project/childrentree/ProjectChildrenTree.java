package io.onedev.server.web.component.project.childrentree;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import io.onedev.server.web.util.WicketUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;

public class ProjectChildrenTree extends NestedTree<ProjectFacade> {

	private static final int MAX_DISPLAY_NODES = 1000;

	public static final ProjectFacade MARK_PROJECT = new ProjectFacade(null, null, null, null, null, false, false, null, null, null, null);
	
	private final Set<Long> expandedProjectIds = new HashSet<>();
	
	public ProjectChildrenTree(String id, Long projectId) {
		super(id, new ITreeProvider<>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends ProjectFacade> getRoots() {
				var roots = WicketUtils.getProjectCache().getChildren(projectId);
				if (roots.size() > MAX_DISPLAY_NODES) {
					roots = roots.subList(0, MAX_DISPLAY_NODES);
					roots.add(MARK_PROJECT);
				}
				return roots.iterator();
			}

			@Override
			public boolean hasChildren(ProjectFacade node) {
				if (node.getId() != null)
					return WicketUtils.getProjectCache().hasChildren(node.getId());
				else
					return false;
			}

			@Override
			public Iterator<? extends ProjectFacade> getChildren(ProjectFacade node) {
				if (node.getId() != null) {
					var children = WicketUtils.getProjectCache().getChildren(node.getId());
					if (children.size() > MAX_DISPLAY_NODES) {
						children = children.subList(0, MAX_DISPLAY_NODES);
						children.add(MARK_PROJECT);
					}
					return children.iterator();
				} else {
					return Collections.emptyIterator();
				}
			}

			@Override
			public IModel<ProjectFacade> model(ProjectFacade object) {
				return Model.of(object);
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new HumanTheme());
		add(AttributeAppender.append("class", "child-projects"));
	}

	@Override
	public void expand(ProjectFacade t) {
		super.expand(t);
		if (t.getId() != null)
			getExpandedProjectIds().add(t.getId());
	}

	@Override
	public void collapse(ProjectFacade t) {
		super.collapse(t);
		if (t.getId() != null)
			getExpandedProjectIds().remove(t.getId());
	}

	@Override
	public State getState(ProjectFacade t) {
		if (t.getId() != null) {
			if (getExpandedProjectIds().contains(t.getId()))
				return State.EXPANDED;
			else
				return State.COLLAPSED;
		} else {
			return State.COLLAPSED;
		}
	}

	@Override
	protected Component newContentComponent(String id, IModel<ProjectFacade> model) {
		ProjectFacade child = model.getObject();
		return new ChildLinkPanel(id, child) {

			@Override
			protected WebMarkupContainer newChildLink(String componentId, Long childId) {
				return ProjectChildrenTree.this.newChildLink(componentId, childId);
			}
			
		}; 
	}
	
	protected Set<Long> getExpandedProjectIds() {
		return expandedProjectIds;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectChildrenTreeCssResourceReference()));
	}
	
	protected WebMarkupContainer newChildLink(String componentId, Long childId) {
		return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, 
				ProjectDashboardPage.paramsOf(childId));		
	}
	
}
