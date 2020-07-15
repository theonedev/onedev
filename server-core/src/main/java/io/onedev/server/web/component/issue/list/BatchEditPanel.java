package io.onedev.server.web.component.issue.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputContext;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.web.ajaxlistener.DisableGlobalLoadingIndicatorListener;
import io.onedev.server.web.behavior.RunTaskBehavior;
import io.onedev.server.web.component.project.comment.CommentInput;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PropertyDescriptor;

@SuppressWarnings("serial")
abstract class BatchEditPanel extends Panel implements InputContext {

	private Set<String> selectedFields = new HashSet<>();

	private BuiltInFieldsBean builtInFieldsBean;
	
	private Serializable customFieldsBean;
	
	private BeanEditor builtInFieldsEditor;
	
	private BeanEditor customFieldsEditor;
	
	private String comment;
	
	public BatchEditPanel(String id) {
		super(id);
	}

	private Behavior newOnChangeBehavior(Form<?> form) {
		return new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				for (List<PropertyDescriptor> values: builtInFieldsEditor.getDescriptor().getProperties().values()) {
					for (PropertyDescriptor value: values) 
						value.setPropertyExcluded(!selectedFields.contains(value.getDisplayName()));
				}
				for (List<PropertyDescriptor> values: customFieldsEditor.getDescriptor().getProperties().values()) { 
					for (PropertyDescriptor value: values)
						value.setPropertyExcluded(!selectedFields.contains(value.getDisplayName()));
				}
				target.add(form);
			}
			
		};		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		
		form.add(new Label("title", "Batch Editing " + getIssueCount() + " Issues"));
		
		form.add(new FencedFeedbackPanel("feedback", form));
		
		form.add(new CheckBox("stateCheck", new IModel<Boolean>() {

			@Override
			public void detach() {
			}

			@Override
			public Boolean getObject() {
				return selectedFields.contains(Issue.NAME_STATE);
			}

			@Override
			public void setObject(Boolean object) {
				if (object)
					selectedFields.add(Issue.NAME_STATE);
				else
					selectedFields.remove(Issue.NAME_STATE);
			}
			
		}).add(newOnChangeBehavior(form)));
		
		form.add(new CheckBox("milestoneCheck", new IModel<Boolean>() {

			@Override
			public void detach() {
			}

			@Override
			public Boolean getObject() {
				return selectedFields.contains(Issue.NAME_MILESTONE);
			}

			@Override
			public void setObject(Boolean object) {
				if (object)
					selectedFields.add(Issue.NAME_MILESTONE);
				else
					selectedFields.remove(Issue.NAME_MILESTONE);
			}
			
		}).add(newOnChangeBehavior(form)));
		
		List<String> customFieldNames = new ArrayList<>();
		for (FieldSpec field: getIssueSetting().getFieldSpecs()) {
			if (SecurityUtils.canEditIssueField(getProject(), field.getName()))
				customFieldNames.add(field.getName());
		}
		
		form.add(new ListView<String>("customFields", customFieldNames) {

			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new CheckBox("customFieldCheck", new IModel<Boolean>() {

					@Override
					public void detach() {
					}

					@Override
					public Boolean getObject() {
						return selectedFields.contains(item.getModelObject());
					}

					@Override
					public void setObject(Boolean object) {
						if (object)
							selectedFields.add(item.getModelObject());
						else
							selectedFields.remove(item.getModelObject());
					}
					
				}).add(newOnChangeBehavior(form)));
				item.add(new Label("name", item.getModelObject()));
			}
			
		});
		
		builtInFieldsBean = new BuiltInFieldsBean();
		Class<? extends Serializable> fieldBeanClass = IssueUtils.defineFieldBeanClass(getProject());
		Issue issue = new Issue();
		issue.setProject(getProject());
		if (getIssueQuery() != null && getIssueQuery().getCriteria() != null) {
			getIssueQuery().getCriteria().fill(issue);
			builtInFieldsBean.setMilestone(issue.getMilestoneName());
			customFieldsBean = issue.getFieldBean(fieldBeanClass, false);
		} else {
			try {
				customFieldsBean = fieldBeanClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			IssueUtils.clearFields(customFieldsBean);
		}
		
		Set<String> excludedProperties = new HashSet<>();
		if (!selectedFields.contains(Issue.NAME_STATE))
			excludedProperties.add(Issue.PROP_STATE);
		if (!selectedFields.contains(Issue.NAME_MILESTONE))
			excludedProperties.add(Issue.PROP_MILESTONE);
		
		builtInFieldsEditor = BeanContext.edit("builtInFieldsEditor", builtInFieldsBean, excludedProperties, true); 
		form.add(builtInFieldsEditor);

		excludedProperties = new HashSet<>();
		for (List<PropertyDescriptor> values: new BeanDescriptor(customFieldsBean.getClass()).getProperties().values()) {
			for (PropertyDescriptor value: values) {
				if (!selectedFields.contains(value.getDisplayName()))
					excludedProperties.add(value.getPropertyName());
			}
		}
		
		customFieldsEditor = BeanContext.edit("customFieldsEditor", customFieldsBean, excludedProperties, true); 
		form.add(customFieldsEditor);				

		form.add(new CommentInput("comment", new PropertyModel<String>(this, "comment"), false) {

			@Override
			protected Project getProject() {
				return BatchEditPanel.this.getProject();
			}

			@Override
			protected List<AttributeModifier> getInputModifiers() {
				return Lists.newArrayList(AttributeModifier.replace("placeholder", "Leave a comment"));
			}
			
		});
		form.add(new AjaxButton("save") {

			private RunTaskBehavior runTaskBehavior;
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new DisableGlobalLoadingIndicatorListener());
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(runTaskBehavior = new RunTaskBehavior() {
					
					@Override
					protected void runTask(AjaxRequestTarget target) {
						String state;
						if (selectedFields.contains(Issue.NAME_STATE))
							state = builtInFieldsBean.getState();
						else
							state = null;
						
						Optional<Milestone> milestone;
						if (selectedFields.contains(Issue.NAME_MILESTONE))
							milestone = Optional.fromNullable(getProject().getMilestone(builtInFieldsBean.getMilestone()));
						else
							milestone = null;
						
						Map<String, Object> fieldValues = IssueUtils.getFieldValues(customFieldsEditor.newComponentContext(), 
								customFieldsBean, selectedFields);
						OneDev.getInstance(IssueChangeManager.class).batchUpdate(
								getIssueIterator(), state, milestone, fieldValues, comment);
						onUpdated(target);
					}
					
				});
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				if (!selectedFields.isEmpty()) {
					runTaskBehavior.requestRun(target);
				} else {
					form.error("Please select fields to update");
					target.add(form);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}
			
		});
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		add(form);		
	}
	
	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();		
	}

	@Override
	public List<String> getInputNames() {
		return getIssueSetting().getFieldNames();
	}

	@Override
	public InputSpec getInputSpec(String inputName) {
		return getIssueSetting().getFieldSpec(inputName);
	}
	
	protected abstract Project getProject();
	
	@Nullable
	protected abstract IssueQuery getIssueQuery();
	
	protected abstract Iterator<? extends Issue> getIssueIterator(); 
	
	protected abstract int getIssueCount();
	
	protected abstract void onUpdated(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
}