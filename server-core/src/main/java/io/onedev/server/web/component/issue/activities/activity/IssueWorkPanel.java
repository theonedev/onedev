package io.onedev.server.web.component.issue.activities.activity;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueWorkManager;
import io.onedev.server.model.IssueWork;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.web.util.editablebean.IssueWorkBean;
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
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import static io.onedev.server.util.DateUtils.formatDateTime;
import static io.onedev.server.util.DateUtils.formatWorkingPeriod;

@SuppressWarnings("serial")
class IssueWorkPanel extends GenericPanel<IssueWork> {

	private final DeleteCallback deleteCallback;
	
	public IssueWorkPanel(String id, IModel<IssueWork> model, DeleteCallback deleteCallback) {
		super(id, model);
		this.deleteCallback = deleteCallback;
	}
	
	private IssueWork getWork() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("user", getWork().getUser().getDisplayName()));
		
		add(new Label("description", "logged work"));
		add(new Label("age", DateUtils.formatAge(getWork().getDate()))
			.add(new AttributeAppender("title", formatDateTime(getWork().getDate()))));
		
		add(newDetailViewer("detail"));
	}
	
	private Component newDetailViewer(String componentId) {
		var fragment = new Fragment(componentId, "detailViewFrag", this);
		fragment.add(new Label("workingPeriod", formatWorkingPeriod(getWork().getMinutes())));
		if (getWork().getNote() != null)
			fragment.add(new MarkdownViewer("note", Model.of(getWork().getNote()), null));
		else 
			fragment.add(new WebMarkupContainer("note").setVisible(false));

		fragment.add(new AjaxLink<Void>("edit") {
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				var detailEditor = newDetailEditor("detail");
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
				deleteCallback.onDelete(target);
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
				work.setDay(DateUtils.toLocalDate(bean.getStartAt()).toEpochDay());
				work.setMinutes(bean.getSpentTime());
				getWorkManager().createOrUpdate(work);
				var detailViewer = newDetailViewer("detail");
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
				var detailViewer = newDetailViewer("detail");
				fragment.replaceWith(detailViewer);
				target.add(detailViewer);
			}
		});
		fragment.add(form);
		fragment.setOutputMarkupId(true);
		return fragment;
	}
	
	private IssueWorkManager getWorkManager() {
		return OneDev.getInstance(IssueWorkManager.class);
	}
	
	private void notifyIssueChange(AjaxRequestTarget target) {
		((BasePage)getPage()).notifyObservablesChange(target, getWork().getIssue().getChangeObservables(false));
	}

}
