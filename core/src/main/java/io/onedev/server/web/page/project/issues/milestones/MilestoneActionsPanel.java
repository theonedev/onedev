package io.onedev.server.web.page.project.issues.milestones;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;

@SuppressWarnings("serial")
abstract class MilestoneActionsPanel extends GenericPanel<Milestone> {

	public MilestoneActionsPanel(String id, IModel<Milestone> model) {
		super(id, model);
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
				Milestone milestone = getMilestone();
				milestone.setClosed(false);
				OneDev.getInstance(MilestoneManager.class).save(milestone);
				target.add(MilestoneActionsPanel.this);
				onUpdated(target);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getMilestone().isClosed());
			}
			
		});
		
		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				new ModalPanel(target) {

					@Override
					protected Component newContent(String id) {
						if (OneDev.getInstance(IssueManager.class).count(getMilestone(), StateSpec.Category.OPEN) != 0) {
							Fragment fragment = new Fragment(id, "confirmCloseWithOpenIssuesFrag", MilestoneActionsPanel.this);
							Form<?> form = new Form<Void>("form");
							form.setOutputMarkupId(true);
							
							form.add(new Label("title", "Confirm Closing Milestone '" + getMilestone().getName() + "'"));
							
							MilestoneCloseOption option = new MilestoneCloseOption();
							option.setMilestoneToClose(getMilestone().getName());
							form.add(BeanContext.editBean("editor", option, Sets.newHashSet("milestoneToClose"), true));
							
							form.add(new AjaxLink<Void>("close") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									close();
								}
								
							});
							form.add(new AjaxLink<Void>("cancel") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									close();
								}
								
							});
							form.add(new AjaxButton("ok") {

								@Override
								protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
									super.onSubmit(target, form);
									MilestoneManager milestoneManager = OneDev.getInstance(MilestoneManager.class);
									if (option.isMoveOpenIssuesToAnotherMilestone()) {
										Milestone newMilestone = milestoneManager.find(getMilestone().getProject(), option.getNewMilestone());
										Preconditions.checkNotNull(newMilestone);
										milestoneManager.close(getMilestone(), newMilestone);
									} else {
										milestoneManager.close(getMilestone(), null);
									}
									target.add(MilestoneActionsPanel.this);
									close();
									onUpdated(target);
								}

								@Override
								protected void onError(AjaxRequestTarget target, Form<?> form) {
									super.onError(target, form);
									target.add(form);
								}
								
							});
							fragment.add(form);
							
							return fragment;
						} else {
							Fragment fragment = new Fragment(id, "confirmCloseWithoutOpenIssuesFrag", MilestoneActionsPanel.this);
							fragment.add(new Label("milestone", getMilestone().getName()));
							fragment.add(new AjaxLink<Void>("ok") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									OneDev.getInstance(MilestoneManager.class).close(getMilestone(), null);
									target.add(MilestoneActionsPanel.this);
									close();
									onUpdated(target);
								}
								
							});
							fragment.add(new AjaxLink<Void>("cancel") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									close();
								}
								
							});
							return fragment;
						}
					}
					
				};
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getMilestone().isClosed());
			}
			
		});
		
		add(new BookmarkablePageLink<Void>("edit", MilestoneEditPage.class, 
				MilestoneEditPage.paramsOf(getMilestone())));

		add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				new ModalPanel(target) {
					
					@Override
					protected Component newContent(String id) {
						if (OneDev.getInstance(IssueManager.class).count(getMilestone(), null) != 0) {
							Fragment fragment = new Fragment(id, "confirmDeleteWithIssuesFrag", MilestoneActionsPanel.this);
							Form<?> form = new Form<Void>("form");
							form.setOutputMarkupId(true);
							
							form.add(new Label("title", "Confirm Deleting Milestone '" + getMilestone().getName() + "'"));
							
							MilestoneDeleteOption option = new MilestoneDeleteOption();
							option.setMilestoneToDelete(getMilestone().getName());
							form.add(BeanContext.editBean("editor", option, Sets.newHashSet("milestoneToDelete"), true));
							
							form.add(new AjaxLink<Void>("close") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									close();
								}
								
							});
							form.add(new AjaxLink<Void>("cancel") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									close();
								}
								
							});
							form.add(new AjaxButton("ok") {

								@Override
								protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
									super.onSubmit(target, form);
									MilestoneManager milestoneManager = OneDev.getInstance(MilestoneManager.class);
									if (option.isMoveIssuesToAnotherMilestone()) {
										Milestone newMilestone = milestoneManager.find(getMilestone().getProject(), option.getNewMilestone());
										Preconditions.checkNotNull(newMilestone);
										milestoneManager.delete(getMilestone(), newMilestone);
									} else {
										milestoneManager.delete(getMilestone(), null);
									}
									target.add(MilestoneActionsPanel.this);
									close();
									onDeleted(target);
								}

								@Override
								protected void onError(AjaxRequestTarget target, Form<?> form) {
									super.onError(target, form);
									target.add(form);
								}
								
							});
							fragment.add(form);
							
							return fragment;
						} else {
							Fragment fragment = new Fragment(id, "confirmDeleteWithoutIssuesFrag", MilestoneActionsPanel.this);
							fragment.add(new Label("milestone", getMilestone().getName()));
							fragment.add(new AjaxLink<Void>("ok") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									OneDev.getInstance(MilestoneManager.class).delete(getMilestone(), null);
									target.add(MilestoneActionsPanel.this);
									close();
									onDeleted(target);
								}
								
							});
							fragment.add(new AjaxLink<Void>("cancel") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									close();
								}
								
							});
							return fragment;
						}
					}
				};
			}
			
		});		
		setOutputMarkupId(true);
	}

	protected abstract void onDeleted(AjaxRequestTarget target);
	
	protected abstract void onUpdated(AjaxRequestTarget target);
	
}
