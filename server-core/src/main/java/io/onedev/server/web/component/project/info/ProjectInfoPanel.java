package io.onedev.server.web.component.project.info;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Project;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.search.entity.project.ProjectQueryLexer;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;

@SuppressWarnings("serial")
public abstract class ProjectInfoPanel extends Panel {

	private final IModel<Project> projectModel;
	
	public ProjectInfoPanel(String id, IModel<Project> projectModel) {
		super(id);
		this.projectModel = projectModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("name", getProject().getName()));
		
		String query = ProjectQuery.getRuleName(ProjectQueryLexer.ForksOf) + " " 
				+ Criteria.quote(getProject().getName());
		PageParameters params = ProjectListPage.paramsOf(query, 0, getProject().getForks().size());
		Link<Void> forksLink = new BookmarkablePageLink<Void>("forks", ProjectListPage.class, params) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (!isEnabled())
					tag.setName("span");
			}
			
		};
		forksLink.add(new Label("label", getProject().getForks().size() + " forks"));
		add(forksLink);
		
		boolean canReadCode = SecurityUtils.canReadCode(getProject());
        add(new ModalLink("forkNow") {
			
			@Override
			protected String getModalCssClass() {
				return "modal-lg";
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				super.onClick(target);
				onPromptForkOption(target);
			}

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new ForkOptionPanel(id, projectModel) {

					@Override
					protected void onClose(AjaxRequestTarget target) {
						modal.close();
					}
					
				};
			}
			
		}.setVisible(SecurityUtils.canCreateProjects() && canReadCode));
		
		if (getProject().getDescription() != null)
			add(new MarkdownViewer("description", Model.of(getProject().getDescription()), null));
		else 
			add(new WebMarkupContainer("description").setVisible(false));
				
		if (getProject().getForkedFrom() != null) {
			Link<Void> forkedFromLink = new ViewStateAwarePageLink<Void>("forkedFrom", 
					ProjectDashboardPage.class, ProjectBlobPage.paramsOf(getProject().getForkedFrom()));
			forkedFromLink.add(new Label("label", getProject().getForkedFrom().getName()));
			forkedFromLink.setVisible(SecurityUtils.canAccess(getProject().getForkedFrom()));
			add(forkedFromLink);
		} else {
			WebMarkupContainer forkedFromLink = new WebMarkupContainer("forkedFrom");
			forkedFromLink.add(new Label("label"));
			forkedFromLink.setVisible(false);
			add(forkedFromLink);
		}
		
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectInfoCssResourceReference()));
	}

	protected abstract void onPromptForkOption(AjaxRequestTarget target);
	
}
