package io.onedev.server.web.component.project.selector;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

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

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.match.MatchScoreProvider;
import io.onedev.server.util.match.MatchScoreUtils;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.InputChangeBehavior;
import io.onedev.server.web.component.link.PreventDefaultAjaxLink;
import io.onedev.server.web.component.project.avatar.ProjectAvatar;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;

@SuppressWarnings("serial")
public abstract class ProjectSelector extends Panel {

	private final IModel<Collection<Project>> projectsModel;
	
	private ListView<Project> projectsView;
	
	private String searchInput;
	
	public ProjectSelector(String id, IModel<Collection<Project>> projectsModel) {
		super(id);
		
		this.projectsModel = projectsModel;
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
				onSelect(target, OneDev.getInstance(ProjectManager.class).load(id));
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				String script = String.format("onedev.server.projectSelector.init('%s', %s)", 
						searchField.getMarkupId(true), 
						getCallbackFunction(CallbackParameter.explicit("id")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		
		projectsContainer.add(projectsView = new ListView<Project>("projects", 
				new LoadableDetachableModel<List<Project>>() {

			@Override
			protected List<Project> load() {
				MatchScoreProvider<Project> matchScoreProvider = new MatchScoreProvider<Project>() {

					@Override
					public double getMatchScore(Project object) {
						return MatchScoreUtils.getMatchScore(object.getName(), searchInput);
					}
					
				};
				
				return MatchScoreUtils.filterAndSort(projectsModel.getObject(), matchScoreProvider);
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
				if (project.equals(getCurrent())) 
					link.add(AttributeAppender.append("class", " current"));
				link.add(new ProjectAvatar("avatar", project));
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
	
	@Nullable
	protected Project getCurrent() {
		return null;
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, Project project);
	
}
