package io.onedev.server.web.component.project.childrentree;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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

import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;

@SuppressWarnings("serial")
public class ProjectChildrenTree extends NestedTree<ProjectFacade> {

	private final Set<Long> expandedProjectIds = new HashSet<>();
	
	public ProjectChildrenTree(String id, Long projectId) {
		super(id, new ITreeProvider<ProjectFacade>() {
			
			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends ProjectFacade> getRoots() {
				return getProjectManager().getChildren(projectId).iterator();
			}

			@Override
			public boolean hasChildren(ProjectFacade node) {
				return !getProjectManager().getChildren(node.getId()).isEmpty();
			}

			@Override
			public Iterator<? extends ProjectFacade> getChildren(ProjectFacade node) {
				return getProjectManager().getChildren(node.getId()).iterator();
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
		getExpandedProjectIds().add(t.getId());
	}

	@Override
	public void collapse(ProjectFacade t) {
		super.collapse(t);
		getExpandedProjectIds().remove(t.getId());
	}

	@Override
	public State getState(ProjectFacade t) {
		if (getExpandedProjectIds().contains(t.getId()))
			return State.EXPANDED;
		else
			return State.COLLAPSED;
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

	private static ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
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
