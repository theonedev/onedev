package io.onedev.server.web.page.project.issues.milestones;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;

@SuppressWarnings("serial")
abstract class MilestoneActionsPanel extends GenericPanel<Milestone> {

	private final boolean withMilestoneCreation;
	
	public MilestoneActionsPanel(String id, IModel<Milestone> model, boolean withMilestoneCreation) {
		super(id, model);
		this.withMilestoneCreation = withMilestoneCreation;
	}

	private Milestone getMilestone() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("reopen") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				getMilestone().setClosed(false);
				getMilestoneManager().save(getMilestone());
				target.add(MilestoneActionsPanel.this);
				onUpdated(target);
				getSession().success("Milestone '" + getMilestone().getName() + "' reopened");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getMilestone().isClosed());
			}
			
		});
		
		add(new AjaxLink<Void>("close") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getMilestone().isClosed());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				getMilestone().setClosed(true);
				getMilestoneManager().save(getMilestone());
				target.add(MilestoneActionsPanel.this);
				onUpdated(target);
				getSession().success("Milestone '" + getMilestone().getName() + "' closed");
			}

		});
		
		add(new BookmarkablePageLink<Void>("edit", MilestoneEditPage.class, 
				MilestoneEditPage.paramsOf(getMilestone())));

		add(new AjaxLink<Void>("delete") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(
						"Do you really want to delete milestone '" + getMilestone().getName() + "'?"));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				getMilestoneManager().delete(getMilestone());
				target.add(MilestoneActionsPanel.this);
				onDeleted(target);
				getSession().success("Milestone '" + getMilestone().getName() + "' deleted");
			}

		});		
		
		if (withMilestoneCreation) {
			add(new BookmarkablePageLink<Void>("create", NewMilestonePage.class, 
					NewMilestonePage.paramsOf(getMilestone().getProject())));
		} else {
			add(new WebMarkupContainer("create").setVisible(false));
		}
		
		setOutputMarkupId(true);
	}
	
	private MilestoneManager getMilestoneManager() {
		return OneDev.getInstance(MilestoneManager.class);
	}

	protected abstract void onDeleted(AjaxRequestTarget target);
	
	protected abstract void onUpdated(AjaxRequestTarget target);
	
}
