package io.onedev.server.web.component.project.selector;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.Similarities;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.asset.selectbytyping.SelectByTypingResourceReference;
import io.onedev.server.web.behavior.InputChangeBehavior;
import io.onedev.server.web.behavior.infinitescroll.InfiniteScrollBehavior;
import io.onedev.server.web.component.link.PreventDefaultAjaxLink;
import io.onedev.server.web.component.project.avatar.ProjectAvatar;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;

@SuppressWarnings("serial")
public abstract class ProjectSelector extends Panel {

	private final IModel<List<Project>> projectsModel;
	
	private final IModel<List<Project>> similarProjectsModel = new LoadableDetachableModel<List<Project>>() {

		@Override
		protected List<Project> load() {
			ProjectCache cache = getProjectManager().cloneCache();
			
			return new Similarities<Project>(projectsModel.getObject()) {

				@Override
				protected double getSimilarScore(Project item) {
					return cache.getSimilarScore(item, searchInput);
				}
				
			};
		}
		
	};
	
	private RepeatingView projectsView;
	
	private TextField<String> searchField;	
	
	private String searchInput;
	
	public ProjectSelector(String id, IModel<List<Project>> projectsModel) {
		super(id);
		
		this.projectsModel = projectsModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer projectsContainer = new WebMarkupContainer("projects") {

			@Override
			protected void onBeforeRender() {
				projectsView = new RepeatingView("projects");
				int index = 0;
				for (Project project: similarProjectsModel.getObject()) {
					Component item = newItem(projectsView.newChildId(), project);
					if (index == 0)
						item.add(AttributeAppender.append("class", "active"));
					projectsView.add(item);
					if (++index >= WebConstants.PAGE_SIZE)
						break;
				}
				addOrReplace(projectsView);
				
				super.onBeforeRender();
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!similarProjectsModel.getObject().isEmpty());
			}
			
		};
		
		projectsContainer.add(new InfiniteScrollBehavior(WebConstants.PAGE_SIZE) {

			@Override
			protected void appendMore(AjaxRequestTarget target, int offset, int count) {
				for (int i=offset; i<offset+count; i++) {
					if (i >= similarProjectsModel.getObject().size())
						break;
					Project project = similarProjectsModel.getObject().get(i);
					
					Component item = newItem(projectsView.newChildId(), project);
					projectsView.add(item);
					String script = String.format("$('#%s').append('<li id=\"%s\"></li>');", 
							projectsContainer.getMarkupId(), item.getMarkupId());
					target.prependJavaScript(script);
					target.add(item);
				}
			}
			
		});
		
		projectsContainer.setOutputMarkupPlaceholderTag(true);
		add(projectsContainer);
		
		WebMarkupContainer noProjectsContainer = new WebMarkupContainer("noProjects") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(similarProjectsModel.getObject().isEmpty());
			}
			
		};
		noProjectsContainer.setOutputMarkupPlaceholderTag(true);
		add(noProjectsContainer);
		
		searchField = new TextField<String>("search", Model.of(""));
		add(searchField);
		searchField.add(new InputChangeBehavior() {
			
			@Override
			protected void onInputChange(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				
				target.add(projectsContainer);
				target.add(noProjectsContainer);
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	private Component newItem(String componentId, Project project) {
		WebMarkupContainer item = new WebMarkupContainer(componentId);
		
		Long projectId = project.getId();
		AjaxLink<Void> link = new PreventDefaultAjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelect(target, getProjectManager().load(projectId));
			}
			
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				PageParameters params = ProjectBlobPage.paramsOf(getProjectManager().load(projectId));
				tag.put("href", urlFor(ProjectBlobPage.class, params).toString());
			}
			
		};
		if (project.equals(getCurrent())) 
			link.add(AttributeAppender.append("class", " current"));
		link.add(new ProjectAvatar("avatar", projectId));
		link.add(new Label("path", project.getPath()));
		item.add(link);
		
		return item;
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}

	@Override
	protected void onDetach() {
		similarProjectsModel.detach();
		projectsModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new SelectByTypingResourceReference()));
		response.render(CssHeaderItem.forReference(new ProjectSelectorCssResourceReference()));
		
		String script = String.format("$('#%s').selectByTyping($('#%s'));", searchField.getMarkupId(), getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	@Nullable
	protected Project getCurrent() {
		return null;
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, Project project);
	
}
