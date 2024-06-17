package io.onedev.server.web.page.project.issues.boards;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.ProjectScope;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.visit.IVisitor;

import javax.annotation.Nullable;

@SuppressWarnings("serial")
abstract class AbstractColumnPanel extends Panel implements EditContext {

	protected final IModel<Integer> countModel = new LoadableDetachableModel<>() {

		@Override
		protected Integer load() {
			if (getQuery() != null) {
				try {
					return getIssueManager().count(getProjectScope(), getQuery().getCriteria());
				} catch (ExplicitException ignored) {
				}
			}
			return 0;
		}

	};
	
	public AbstractColumnPanel(String id) {
		super(id);
	}

	@Override
	protected void onDetach() {
		countModel.detach();
		super.onDetach();
	}
	
	protected void onCardAdded(AjaxRequestTarget target, Issue issue) {
		findParent(RepeatingView.class).visitChildren(AbstractColumnPanel.class, (IVisitor<AbstractColumnPanel, Void>) (columnPanel, visit) -> {
			if (columnPanel.getQuery() != null && columnPanel.getQuery().matches(issue)) {
				var cardListPanel = columnPanel.getCardListPanel();
				var firstCard = cardListPanel.findCard(null);
				if (firstCard != null)
					issue.setBoardPosition(getIssueManager().load(firstCard.getIssueId()).getBoardPosition() - 1);
				getIssueManager().open(issue);
				cardListPanel.onCardAdded(target, issue.getId());
				visit.stop();
			}
		});		
	}
	
	protected abstract IssueQuery getQuery();
	
	protected abstract CardListPanel getCardListPanel();

	protected GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	protected IssueChangeManager getIssueChangeManager() {
		return OneDev.getInstance(IssueChangeManager.class);
	}
	
	protected IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	@Override
	public Object getInputValue(String name) {
		return null;
	}

	protected Project getProject() {
		return getProjectScope().getProject();
	}
	
	protected abstract ProjectScope getProjectScope();

	protected abstract MilestoneSelection getMilestoneSelection();
	
	@Nullable
	protected abstract String getMilestonePrefix();
	
}
