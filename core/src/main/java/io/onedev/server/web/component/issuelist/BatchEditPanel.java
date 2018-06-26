package io.onedev.server.web.component.issuelist;

import java.io.Serializable;
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

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueChangeManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.behavior.RunTaskBehavior;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.util.IssueFieldBeanUtils;
import io.onedev.server.web.util.ajaxlistener.DisableGlobalLoadingIndicatorListener;

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
				for (PropertyDescriptor propertyDescriptor: builtInFieldsEditor.getBeanDescriptor().getPropertyDescriptors()) 
					propertyDescriptor.setPropertyExcluded(!selectedFields.contains(propertyDescriptor.getDisplayName()));
				for (PropertyDescriptor propertyDescriptor: customFieldsEditor.getBeanDescriptor().getPropertyDescriptors()) 
					propertyDescriptor.setPropertyExcluded(!selectedFields.contains(propertyDescriptor.getDisplayName()));
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
		
		form.add(new NotificationPanel("feedback", form));
		
		form.add(new CheckBox("milestoneCheck", new IModel<Boolean>() {

			@Override
			public void detach() {
			}

			@Override
			public Boolean getObject() {
				return selectedFields.contains(Issue.MILESTONE);
			}

			@Override
			public void setObject(Boolean object) {
				if (object)
					selectedFields.add(Issue.MILESTONE);
				else
					selectedFields.remove(Issue.MILESTONE);
			}
			
		}).add(newOnChangeBehavior(form)));
		
		List<String> customFieldNames = Lists.newArrayList(Issue.STATE);
		customFieldNames.addAll(getProject().getIssueWorkflow().getFieldNames());
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
		Class<? extends Serializable> fieldBeanClass = IssueFieldBeanUtils.defineBeanClass(getProject());
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
			IssueFieldBeanUtils.clearFields(customFieldsBean);
		}
		
		Set<String> excludedProperties = new HashSet<>();
		if (!selectedFields.contains(Issue.MILESTONE))
			excludedProperties.add(Issue.BUILTIN_FIELDS.get(Issue.MILESTONE));
		
		builtInFieldsEditor = BeanContext.editBean("builtInFieldsEditor", builtInFieldsBean, excludedProperties); 
		form.add(builtInFieldsEditor);

		excludedProperties = new HashSet<>();
		for (PropertyDescriptor property: new BeanDescriptor(customFieldsBean.getClass()).getPropertyDescriptors()) {
			if (!selectedFields.contains(property.getDisplayName()))
				excludedProperties.add(property.getPropertyName());
		}
		
		customFieldsEditor = BeanContext.editBean("customFieldsEditor", customFieldsBean, excludedProperties); 
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
						Optional<Milestone> milestone;
						if (selectedFields.contains(Issue.MILESTONE))
							milestone = Optional.fromNullable(getProject().getMilestone(builtInFieldsBean.getMilestone()));
						else
							milestone = null;
						String state = (String) IssueFieldBeanUtils.getFieldValue(customFieldsBean, Issue.STATE);
						
						Map<String, Object> fieldValues = IssueFieldBeanUtils.getFieldValues(customFieldsBean);
						fieldValues.keySet().retainAll(selectedFields);
						
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

	@Override
	public List<String> getInputNames() {
		return getProject().getIssueWorkflow().getFieldNames();
	}

	@Override
	public InputSpec getInputSpec(String inputName) {
		return getProject().getIssueWorkflow().getFieldSpec(inputName);
	}
	
	@Override
	public boolean isReservedName(String inputName) {
		throw new UnsupportedOperationException();
	}

	protected abstract Project getProject();
	
	@Nullable
	protected abstract IssueQuery getIssueQuery();
	
	protected abstract Iterator<? extends Issue> getIssueIterator(); 
	
	protected abstract int getIssueCount();
	
	protected abstract void onUpdated(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
}