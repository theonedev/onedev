package io.onedev.server.web.page.project.issues;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.link.FirstIssueQueryLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.PageTabLink;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.issueboards.IssueBoardsPage;
import io.onedev.server.web.page.project.issues.issuelist.IssueListPage;
import io.onedev.server.web.page.project.issues.workflowreconcile.WorkflowReconcilePanel;

@SuppressWarnings("serial")
public abstract class IssuesPage extends ProjectPage {

	public IssuesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (SecurityUtils.canManage(getProject())) {
			add(new ModalLink("reconcile") {

				@Override
				protected Component newContent(String id, ModalPanel modal) {
					return new WorkflowReconcilePanel(id) {
						
						@Override
						protected Project getProject() {
							return IssuesPage.this.getProject();
						}

						@Override
						protected void onCancel(AjaxRequestTarget target) {
							modal.close();
						}

						@Override
						protected void onCompleted(AjaxRequestTarget target) {
							setResponsePage(IssuesPage.this);
						}
						
					};
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!getProject().getIssueWorkflow().isReconciled());
				}

				@Override
				public IModel<?> getBody() {
					return Model.of("reconcile");
				}
				
			});
		} else {
			add(new Label("reconcile", "contact project administrator to reconcile") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!getProject().getIssueWorkflow().isReconciled());
				}
				
			});
		}
		
		List<Tab> tabs = new ArrayList<>();
		tabs.add(new IssuesTab("List", IssueListPage.class) {

			@Override
			public Component render(String componentId) {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new FirstIssueQueryLink(linkId, getProject());
					}
				};
				
			}
			
		});
		tabs.add(new IssuesTab("Boards", IssueBoardsPage.class));
		
		add(new Tabbable("issuesTabs", tabs));
	}

	private class IssuesTab extends PageTab {

		public IssuesTab(String title, Class<? extends Page> pageClass) {
			super(Model.of(title), pageClass);
		}
		
		@Override
		public Component render(String componentId) {
			return new PageTabLink(componentId, this) {

				@Override
				protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
					return new ViewStateAwarePageLink<Void>(linkId, pageClass, paramsOf(getProject()));
				}
				
			};
		}
		
	}
	
}
