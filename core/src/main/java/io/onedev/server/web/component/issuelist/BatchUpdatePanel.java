package io.onedev.server.web.component.issuelist;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueFieldUnaryManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.behavior.RunTaskBehavior;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.util.ajaxlistener.DisableGlobalLoadingIndicatorListener;

@SuppressWarnings("serial")
abstract class BatchUpdatePanel extends Panel implements InputContext {

	private Set<String> selectedFields = new HashSet<>();

	private BuiltInFieldsBean builtInFieldsBean;
	
	private Serializable customFieldsBean;
	
	private BeanEditor builtInFieldsEditor;
	
	private BeanEditor customFieldsEditor;
	
	public BatchUpdatePanel(String id) {
		super(id);
	}

	private Behavior newOnChangeBehavior(Form<?> form) {
		return new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				for (PropertyContext<?> propertyContext: builtInFieldsEditor.getPropertyContexts()) 
					propertyContext.setPropertyExcluded(!selectedFields.contains(propertyContext.getDisplayName()));
				for (PropertyDescriptor propertyDescriptor: builtInFieldsEditor.getBeanDescriptor().getPropertyDescriptors()) 
					propertyDescriptor.setPropertyExcluded(!selectedFields.contains(propertyDescriptor.getDisplayName()));
				for (PropertyContext<?> propertyContext: customFieldsEditor.getPropertyContexts()) 
					propertyContext.setPropertyExcluded(!selectedFields.contains(propertyContext.getDisplayName()));
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
		
		form.add(new Label("title", "Batch Editing " + getNumOfIssues() + " Issues"));
		
		form.add(new NotificationPanel("feedback", form));
		
		form.add(new CheckBox("stateCheck", new IModel<Boolean>() {

			@Override
			public void detach() {
			}

			@Override
			public Boolean getObject() {
				return selectedFields.contains(Issue.STATE);
			}

			@Override
			public void setObject(Boolean object) {
				if (object)
					selectedFields.add(Issue.STATE);
				else
					selectedFields.remove(Issue.STATE);
			}
			
		}).add(newOnChangeBehavior(form)));
		
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
		
		form.add(new ListView<String>("customFields", getProject().getIssueWorkflow().getFieldNames()) {

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
		try {
			customFieldsBean = OneDev.getInstance(IssueFieldUnaryManager.class).defineFieldBeanClass(getProject()).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		Issue issue = new Issue();
		if (getQuery().getCriteria() != null)
			getQuery().getCriteria().populate(issue, customFieldsBean);

		builtInFieldsBean.setState(issue.getState());
		builtInFieldsBean.setMilestone(issue.getMilestoneName());
		
		Set<String> excludedProperties = new HashSet<>();
		if (!selectedFields.contains(Issue.STATE))
			excludedProperties.add(Issue.BUILTIN_FIELDS.get(Issue.STATE));
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
						Milestone milestone = getProject().getMilestone(builtInFieldsBean.getMilestone());
						OneDev.getInstance(IssueManager.class).batchUpdate(getProject(), getQuery(), 
								selectedFields, builtInFieldsBean.getState(), milestone, customFieldsBean);
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
	
	protected abstract IssueQuery getQuery(); 
	
	protected abstract void onUpdated(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
	protected abstract long getNumOfIssues();
}
