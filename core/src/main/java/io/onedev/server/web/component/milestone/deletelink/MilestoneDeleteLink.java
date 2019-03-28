package io.onedev.server.web.component.milestone.deletelink;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.milestone.closelink.MilestoneCloseLinkCssResourceReference;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class MilestoneDeleteLink extends ModalLink {

	public MilestoneDeleteLink(String id) {
		super(id);
	}

	@Override
	protected Component newContent(String id, ModalPanel modal) {
		if (OneDev.getInstance(IssueManager.class).count(getMilestone(), SecurityUtils.getUser(), null) != 0) {
			return new ConfirmDeleteWithIssuesPanel(id) {

				@Override
				protected Milestone getMilestone() {
					return MilestoneDeleteLink.this.getMilestone();
				}

				@Override
				protected void onClose(AjaxRequestTarget target) {
					modal.close();
				}

				@Override
				protected void onMilestoneDeleted(AjaxRequestTarget target) {
					modal.close();
					MilestoneDeleteLink.this.onMilestoneDeleted(target);
				}
				
			};
		} else {
			return new ConfirmDeleteWithoutIssuesPanel(id) {

				@Override
				protected Milestone getMilestone() {
					return MilestoneDeleteLink.this.getMilestone();
				}

				@Override
				protected void onClose(AjaxRequestTarget target) {
					modal.close();
				}

				@Override
				protected void onMilestoneDeleted(AjaxRequestTarget target) {
					modal.close();
					MilestoneDeleteLink.this.onMilestoneDeleted(target);
				}
				
			};
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MilestoneCloseLinkCssResourceReference()));
	}
	
	protected abstract Milestone getMilestone();
	
	protected abstract void onMilestoneDeleted(AjaxRequestTarget target);

}
