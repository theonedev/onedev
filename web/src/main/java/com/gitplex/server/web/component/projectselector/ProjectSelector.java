package com.gitplex.server.web.component.projectselector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.web.behavior.AbstractPostAjaxBehavior;
import com.gitplex.server.web.behavior.InputChangeBehavior;
import com.gitplex.server.web.component.link.PreventDefaultAjaxLink;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;

@SuppressWarnings("serial")
public abstract class ProjectSelector extends Panel {

	private final IModel<Collection<Project>> projectsModel;
	
	private final Long currentProjectId;

	private ListView<Project> projectsView;
	
	private String searchInput;
	
	public ProjectSelector(String id, IModel<Collection<Project>> projectsModel, Long currentProjectId) {
		super(id);
		
		this.projectsModel = projectsModel;
		this.currentProjectId = currentProjectId;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer projectsContainer = new WebMarkupContainer("projects") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!projectsView.getModelObject().isEmpty());
			}
			
		};
		projectsContainer.setOutputMarkupPlaceholderTag(true);
		add(projectsContainer);
		
		WebMarkupContainer noProjectsContainer = new WebMarkupContainer("noProjects") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(projectsView.getModelObject().isEmpty());
			}
			
		};
		noProjectsContainer.setOutputMarkupPlaceholderTag(true);
		add(noProjectsContainer);
		
		TextField<String> searchField = new TextField<String>("search", Model.of(""));
		add(searchField);
		searchField.add(new InputChangeBehavior() {
			
			@Override
			protected void onInputChange(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				target.add(projectsContainer);
				target.add(noProjectsContainer);
			}
			
		});
		searchField.add(new AbstractPostAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				Long id = params.getParameterValue("id").toLong();
				onSelect(target, GitPlex.getInstance(ProjectManager.class).load(id));
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				String script = String.format("gitplex.server.projectSelector.init('%s', %s)", 
						searchField.getMarkupId(true), 
						getCallbackFunction(CallbackParameter.explicit("id")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		
		projectsContainer.add(projectsView = new ListView<Project>("projects", 
				new LoadableDetachableModel<List<Project>>() {

			@Override
			protected List<Project> load() {
				List<Project> projects = new ArrayList<>();
				for (Project project: projectsModel.getObject()) {
					if (project.matches(searchInput)) {
						projects.add(project);
					}
				}
				projects.sort(Project::compareLastVisit);
				return projects;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Project> item) {
				Project project = item.getModelObject();
				AjaxLink<Void> link = new PreventDefaultAjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						onSelect(target, item.getModelObject());
					}
					
					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						PageParameters params = ProjectBlobPage.paramsOf(item.getModelObject());
						tag.put("href", urlFor(ProjectBlobPage.class, params).toString());
					}
					
				};
				if (project.getId().equals(currentProjectId)) 
					link.add(AttributeAppender.append("class", " current"));
				link.add(new Label("name", project.getName()));
				item.add(link);
				
				if (item.getIndex() == 0)
					item.add(AttributeAppender.append("class", "active"));
				item.add(AttributeAppender.append("data-id", project.getId()));
			}
			
		});
	}

	@Override
	protected void onDetach() {
		projectsModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new ProjectSelectorResourceReference()));
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, Project project);
	
}
