package io.onedev.server.web.component.issue.operation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.TransitionSpec;
import io.onedev.server.model.support.issue.transitiontrigger.PressButtonTrigger;
import io.onedev.server.model.support.setting.GlobalIssueSetting;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IssueConstants;
import io.onedev.server.util.IssueField;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.dateinput.DateInput;
import io.onedev.server.web.component.issue.IssueStateLabel;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.project.comment.CommentInput;
import io.onedev.server.web.component.sideinfo.SideInfoClosed;
import io.onedev.server.web.component.sideinfo.SideInfoOpened;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.util.ProjectAttachmentSupport;

@SuppressWarnings("serial")
public abstract class IssueOperationsPanel extends Panel {

	private static final String ACTION_OPTIONS_ID = "actionOptions";
	
	public IssueOperationsPanel(String id) {
		super(id);
	}

	private void newEmptyActionOptions(@Nullable AjaxRequestTarget target) {
		WebMarkupContainer actionOptions = new WebMarkupContainer(ACTION_OPTIONS_ID);
		actionOptions.setOutputMarkupPlaceholderTag(true);
		actionOptions.setVisible(false);
		if (target != null) {
			replace(actionOptions);
			target.add(actionOptions);
		} else {
			add(actionOptions);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new IssueStateLabel("state", new AbstractReadOnlyModel<Issue>() {

			@Override
			public Issue getObject() {
				return getIssue();
			}
			
		}));
		
		RepeatingView transitionsView = new RepeatingView("transitions");

		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		List<TransitionSpec> transitions = getIssue().getProject().getIssueSetting().getTransitionSpecs(true);
		for (TransitionSpec transition: transitions) {
			if (transition.canTransite(getIssue()) && transition.getTrigger() instanceof PressButtonTrigger) {
				PressButtonTrigger trigger = (PressButtonTrigger) transition.getTrigger();
				if (trigger.isAuthorized()) {
					AjaxLink<Void> link = new AjaxLink<Void>(transitionsView.newChildId()) {

						private String comment;
						
						@Override
						public void onClick(AjaxRequestTarget target) {
							Fragment fragment = new Fragment(ACTION_OPTIONS_ID, "transitionFrag", IssueOperationsPanel.this);
							Class<?> fieldBeanClass = IssueUtils.defineFieldBeanClass(getIssue().getProject());
							Serializable fieldBean = getIssue().getFieldBean(fieldBeanClass, true);

							Form<?> form = new Form<Void>("form") {

								@Override
								protected void onError() {
									super.onError();
									RequestCycle.get().find(AjaxRequestTarget.class).add(this);
								}
								
							};
							
							Collection<String> propertyNames = IssueUtils.getPropertyNames(getIssue().getProject(), 
									fieldBeanClass, trigger.getPromptFields());
							BeanEditor editor = BeanContext.edit("fields", fieldBean, propertyNames, false); 
							form.add(editor);
							
							form.add(new CommentInput("comment", new PropertyModel<String>(this, "comment"), false) {

								@Override
								protected AttachmentSupport getAttachmentSupport() {
									return new ProjectAttachmentSupport(getProject(), getIssue().getUUID());
								}

								@Override
								protected Project getProject() {
									return getIssue().getProject();
								}
								
								@Override
								protected List<AttributeModifier> getInputModifiers() {
									return Lists.newArrayList(AttributeModifier.replace("placeholder", "Leave a comment"));
								}
								
							});

							form.add(new AjaxButton("save") {

								@Override
								protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
									super.onSubmit(target, form);

									getIssue().removeFields(transition.getRemoveFields());
									Map<String, Object> fieldValues = IssueUtils.getFieldValues(editor.getOneContext(), fieldBean, trigger.getPromptFields());
									IssueChangeManager manager = OneDev.getInstance(IssueChangeManager.class);
									manager.changeState(getIssue(), transition.getToState(), fieldValues, comment, SecurityUtils.getUser());
									onStateChanged(target);
								}
								
							});
							
							form.add(new AjaxLink<Void>("cancel") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									newEmptyActionOptions(target);
								}
								
							});
							fragment.add(form);
							
							fragment.setOutputMarkupId(true);
							IssueOperationsPanel.this.replace(fragment);
							target.add(fragment);
						}
						
					};
					link.add(new Label("label", trigger.getButtonLabel()));
					transitionsView.add(link);
				}
			}
		}
		
		add(transitionsView);

		List<String> criterias = new ArrayList<>();
		if (getIssue().getMilestone() != null)
			criterias.add(IssueQuery.quote(IssueConstants.FIELD_MILESTONE) + " is " + IssueQuery.quote(getIssue().getMilestoneName()));
		for (Map.Entry<String, IssueField> entry: getIssue().getFields().entrySet()) {
			List<String> strings = entry.getValue().getValues();
			if (strings.isEmpty()) {
				criterias.add(IssueQuery.quote(entry.getKey()) + " is empty");
			} else { 
				InputSpec field = issueSetting.getFieldSpec(entry.getKey());
				if (field instanceof ChoiceInput && ((ChoiceInput)field).isAllowMultiple()) {
					for (String string: strings)
						criterias.add(IssueQuery.quote(entry.getKey()) + " contains " + IssueQuery.quote(string));
				} else if (!(field instanceof DateInput)) { 
					criterias.add(IssueQuery.quote(entry.getKey()) + " is " + IssueQuery.quote(strings.iterator().next()));
				}
			}
		}

		String query;
		if (!criterias.isEmpty())
			query = StringUtils.join(criterias, " and ");
		else
			query = null;
		
		add(newCreateIssueButton("newIssue", query));
		
		add(new AjaxLink<Void>("moreInfo") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof SideInfoClosed) {
					SideInfoClosed moreInfoSideClosed = (SideInfoClosed) event.getPayload();
					setVisible(true);
					moreInfoSideClosed.getHandler().add(this);
				}
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				setVisible(false);
				target.add(this);
				send(getPage(), Broadcast.BREADTH, new SideInfoOpened(target));
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		newEmptyActionOptions(null);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueOperationsCssResourceReference()));
	}

	protected abstract Issue getIssue();
	
	protected abstract void onStateChanged(AjaxRequestTarget target);
	
	protected abstract Component newCreateIssueButton(String componentId, String templateQuery);
	
}
