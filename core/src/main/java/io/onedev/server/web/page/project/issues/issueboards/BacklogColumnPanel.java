package io.onedev.server.web.page.project.issues.issueboards;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.query.IssueCriteria;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
abstract class BacklogColumnPanel extends Panel {

	public BacklogColumnPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new ModalLink("addCard") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new NewCardPanel(id) {

					@Override
					protected void onClose(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected Project getProject() {
						return BacklogColumnPanel.this.getProject();
					}

					@Override
					protected IssueCriteria getTemplate() {
						return getBacklogQuery().getCriteria();
					}

					@Override
					protected void onAdded(AjaxRequestTarget target, Issue issue) {
						target.add(BacklogColumnPanel.this);
					}
					
				};
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBacklogQuery() != null);
			}
			
		});
		add(new Label("count", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return String.valueOf(OneDev.getInstance(IssueManager.class)
						.count(getProject(), getBacklogQuery().getCriteria()));
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBacklogQuery() != null);
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onBeforeRender() {
		addOrReplace(new CardListPanel("body") {

			@Override
			protected List<Issue> queryIssues(int page) {
				if (getBacklogQuery() != null) {
					return OneDev.getInstance(IssueManager.class).query(getProject(), getBacklogQuery(), 
							page*WebConstants.PAGE_SIZE, WebConstants.PAGE_SIZE);
				} else {
					return new ArrayList<>();
				}
			}
			
		});
		
		super.onBeforeRender();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		String script = String.format("onedev.server.issueBoards.onColumnDomReady('%s');", 
				getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract Project getProject();
	
	@Nullable
	protected abstract IssueQuery getBacklogQuery();
	
}
