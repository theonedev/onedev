package io.onedev.server.web.page.admin.issuesetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.setting.GlobalIssueSetting;
import io.onedev.server.util.IssueConstants;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.Choice;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathElement;
import io.onedev.server.web.util.ajaxlistener.ConfirmLeaveListener;

@SuppressWarnings("serial")
abstract class FieldEditPanel extends Panel implements InputContext {

	private final int fieldIndex;
	
	public FieldEditPanel(String id, int fieldIndex) {
		super(id);
	
		this.fieldIndex = fieldIndex;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		FieldBean bean = new FieldBean();
		if (fieldIndex != -1) {
			bean.setField(SerializationUtils.clone(getSetting().getFieldSpecs().get(fieldIndex)));
			bean.setPromptUponIssueOpen(getSetting().getDefaultPromptFieldsUponIssueOpen().contains(bean.getField().getName()));
			bean.setDisplayInIssueList(getSetting().getDefaultListFields().contains(bean.getField().getName()));
		}

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(FieldEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		BeanEditor editor = BeanContext.editBean("editor", bean);
		form.add(editor);
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				InputSpec field = bean.getField();
				if (fieldIndex != -1) { 
					InputSpec oldField = getSetting().getFieldSpecs().get(fieldIndex);
					if (!field.getName().equals(oldField.getName()) && getSetting().getFieldSpec(field.getName()) != null) {
						editor.getErrorContext(new PathElement.Named("field"))
								.getErrorContext(new PathElement.Named("name"))
								.addError("This name has already been used by another field");
					}
				} else if (getSetting().getFieldSpec(field.getName()) != null) {
					editor.getErrorContext(new PathElement.Named("field"))
							.getErrorContext(new PathElement.Named("name"))
							.addError("This name has already been used by another field");
				}

				if (!editor.hasErrors(true)) {
					if (fieldIndex != -1) {
						InputSpec oldField = getSetting().getFieldSpecs().get(fieldIndex);
						getSetting().getFieldSpecs().set(fieldIndex, field);
						getSetting().getDefaultPromptFieldsUponIssueOpen().remove(oldField.getName());
						getSetting().getDefaultListFields().remove(oldField.getName());
						if (!field.getName().equals(oldField.getName())) {
							getSetting().onRenameField(oldField.getName(), field.getName());
							getSetting().setReconciled(false);
						}
						if (oldField instanceof ChoiceInput && field instanceof ChoiceInput) {
							ChoiceInput oldChoiceInput = (ChoiceInput) oldField;
							ChoiceInput choiceInput = (ChoiceInput) field;
							if (oldChoiceInput.getChoiceProvider() instanceof SpecifiedChoices 
									&& choiceInput.getChoiceProvider() instanceof SpecifiedChoices) {
								SpecifiedChoices oldChoices = (SpecifiedChoices) oldChoiceInput.getChoiceProvider();
								SpecifiedChoices choices = (SpecifiedChoices) choiceInput.getChoiceProvider();
								Map<String, String> valueRenames = new HashMap<>();
								Set<String> valueDeletions = new HashSet<>();
								Map<String, String> uuid2value = new HashMap<>();
								Map<String, Integer> uuid2order = new HashMap<>();
								for (int i=0; i<choices.getChoices().size(); i++) {
									Choice choice = choices.getChoices().get(i);
									uuid2value.put(choice.getUuid(), choice.getValue());
									uuid2order.put(choice.getUuid(), i);
								}
								for (int i=0; i<oldChoices.getChoices().size(); i++) {
									Choice oldChoice = oldChoices.getChoices().get(i);
									String newValue = uuid2value.get(oldChoice.getUuid());
									if (newValue != null) {
										if (!newValue.equals(oldChoice.getValue())) {
											valueRenames.put(oldChoice.getValue(), newValue);
											getSetting().setReconciled(false);
										}
									} else {
										valueDeletions.add(oldChoice.getValue());
										getSetting().setReconciled(false);
									}
									Integer newOrder = uuid2order.get(oldChoice.getUuid());
									if (newOrder == null || newOrder != i)
										getSetting().setReconciled(false);
								}
								if (!valueRenames.isEmpty() || !valueDeletions.isEmpty()) {
									getSetting().onEditFieldValues(field.getName(), new ValueSetEdit(valueRenames, valueDeletions));
								}
							}
						}
					} else {
						getSetting().getFieldSpecs().add(field);
					}
					if (bean.isPromptUponIssueOpen())
						getSetting().getDefaultPromptFieldsUponIssueOpen().add(field.getName());
					if (bean.isDisplayInIssueList())
						getSetting().getDefaultListFields().add(field.getName());
					OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
					onSave(target);
				} else {
					target.add(form);
				}
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(FieldEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.setOutputMarkupId(true);
		
		add(form);
	}

	protected abstract GlobalIssueSetting getSetting();
	
	protected abstract void onSave(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);

	@Override
	public List<String> getInputNames() {
		List<String> inputNames = new ArrayList<>();
		int currentIndex = 0;
		for (InputSpec field: getSetting().getFieldSpecs()) {
			if (currentIndex != fieldIndex)
				inputNames.add(field.getName());
			currentIndex++;
		}
		return inputNames;
	}
	
	@Override
	public InputSpec getInputSpec(String inputName) {
		return getSetting().getFieldSpec(inputName);
	}
	
	@Override
	public boolean isReservedName(String inputName) {
		return IssueConstants.ALL_FIELDS.contains(inputName);
	}
	
}
