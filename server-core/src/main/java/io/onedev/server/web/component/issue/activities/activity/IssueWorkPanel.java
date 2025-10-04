package io.onedev.server.web.component.issue.activities.activity;

import static io.onedev.server.util.DateUtils.formatDateTime;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import io.onedev.server.OneDev;
import io.onedev.server.service.IssueWorkService;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.IssueWork;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.util.editbean.IssueWorkBean;

class IssueWorkPanel extends Panel {
		
	public IssueWorkPanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserIdentPanel("user", getWork().getUser(), Mode.AVATAR_AND_NAME));
		var timeTrackingSetting = OneDev.getInstance(SettingService.class).getIssueSetting().getTimeTrackingSetting();
		add(new Label("workingPeriod", timeTrackingSetting.formatWorkingPeriod(getWork().getMinutes(), true)));	
		add(new Label("age", DateUtils.formatAge(getWork().getDate()))
			.add(new AttributeAppender("title", formatDateTime(getWork().getDate()))));
		
		add(newDetailViewer("body"));

		setMarkupId(getWork().getAnchor());
		setOutputMarkupId(true);
	}
	
	private Component newDetailViewer(String componentId) {
		var fragment = new Fragment(componentId, "detailViewFrag", this);
		if (getWork().getNote() != null)
			fragment.add(new MarkdownViewer("note", Model.of(getWork().getNote()), null));
		else 
			fragment.add(new WebMarkupContainer("note").setVisible(false));

		fragment.add(new AjaxLink<Void>("edit") {
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				var detailEditor = newDetailEditor("body");
				fragment.replaceWith(detailEditor);
				target.add(detailEditor);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canModifyOrDelete(getWork()));
			}
		});
		
		fragment.add(new AjaxLink<Void>("delete") {
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this work?"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				notifyIssueChange(target);
				getWorkService().delete(getWork());
				IssueWorkPanel.this.remove();
				target.appendJavaScript(String.format("$('#%s').remove();", IssueWorkPanel.this.getMarkupId()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canModifyOrDelete(getWork()));
			}
		});

		fragment.setOutputMarkupId(true);
		return fragment;
	}
	
	private Component newDetailEditor(String componentId) {
		var fragment = new Fragment(componentId, "detailEditFrag", this);
		var form = new Form<Void>("form");
		var bean = new IssueWorkBean();
		bean.setNote(getWork().getNote());
		bean.setStartAt(getWork().getDate());
		bean.setSpentTime(getWork().getMinutes());
		form.add(BeanContext.edit("editor", bean));
		form.add(new AjaxButton("save") {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				var work = getWork();
				work.setNote(bean.getNote());
				work.setDate(bean.getStartAt());
				work.setMinutes(bean.getSpentTime());
				getWorkService().createOrUpdate(work);
				var detailViewer = newDetailViewer("body");
				fragment.replaceWith(detailViewer);
				target.add(detailViewer);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				var detailViewer = newDetailViewer("body");
				fragment.replaceWith(detailViewer);
				target.add(detailViewer);
			}
		});
		fragment.add(form);
		fragment.setOutputMarkupId(true);
		return fragment;
	}
	
	private IssueWorkService getWorkService() {
		return OneDev.getInstance(IssueWorkService.class);
	}

	private IssueWork getWork() {
		return ((IssueWorkActivity) getDefaultModelObject()).getWork();
	}
	
	private void notifyIssueChange(AjaxRequestTarget target) {
		((BasePage)getPage()).notifyObservablesChange(target, getWork().getIssue().getChangeObservables(false));
	}

}
