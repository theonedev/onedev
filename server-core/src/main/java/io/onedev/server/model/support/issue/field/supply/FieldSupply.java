package io.onedev.server.model.support.issue.field.supply;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import javax.validation.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.buildspecmodel.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.web.component.issue.workflowreconcile.ReconcileUtils;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.annotation.Editable;

@Editable
public class FieldSupply implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name;
	
	private boolean secret;
	
	private ValueProvider valueProvider = new SpecifiedValue();

	@Editable
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable
	@NotNull
	@Valid
	public ValueProvider getValueProvider() {
		return valueProvider;
	}

	public void setValueProvider(ValueProvider valueProvider) {
		this.valueProvider = valueProvider;
	}

	@Editable
	public boolean isSecret() {
		return secret;
	}

	public void setSecret(boolean secret) {
		this.secret = secret;
	}
	
	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	public Set<String> getUndefinedFields() {
		Set<String> undefinedFields = new HashSet<>();
		FieldSpec fieldSpec = getIssueSetting().getFieldSpec(getName());
		if (fieldSpec == null)
			undefinedFields.add(getName());
		return undefinedFields;
	}

	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>(); 
		if (getValueProvider() instanceof SpecifiedValue) {
			SpecifiedValue specifiedValue = (SpecifiedValue) getValueProvider();
			SpecifiedChoices specifiedChoices = SpecifiedChoices.of(getIssueSetting().getFieldSpec(getName()));
			if (specifiedChoices != null) {
				for (String value: specifiedValue.getValue()) {
					if (!specifiedChoices.getChoiceValues().contains(value)) 
						undefinedFieldValues.add(new UndefinedFieldValue(getName(), value));
				}
			}
		}
		
		return undefinedFieldValues;
	}
	
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			UndefinedFieldResolution resolution = entry.getValue();
			if (resolution.getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
				if (getName().equals(entry.getKey()))
					setName(resolution.getNewField());
			} else {
				if (getName().equals(entry.getKey())) 
					return false;
			} 
		}				
		return true;
	}
	
	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		if (getValueProvider() instanceof SpecifiedValue) {
			SpecifiedValue specifiedValue = (SpecifiedValue) getValueProvider();
			for (Map.Entry<String, UndefinedFieldValuesResolution> entry: resolutions.entrySet()) {
				if (entry.getKey().equals(getName())) {
					specifiedValue.getValue().removeAll(entry.getValue().getDeletions());
					for (Map.Entry<String, String> renameEntry: entry.getValue().getRenames().entrySet()) {
						ReconcileUtils.renameItem(specifiedValue.getValue(), 
								renameEntry.getKey(), renameEntry.getValue());
					}
					if (specifiedValue.getValue().isEmpty())
						return false;
				}
			}
		}
		return true;
	}		

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof FieldSupply)) 
			return false;
		if (this == other)
			return true;
		FieldSupply otherIssueField = (FieldSupply) other;
		return new EqualsBuilder()
			.append(name, otherIssueField.name)
			.append(valueProvider, otherIssueField.valueProvider)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(name)
			.append(valueProvider)
			.toHashCode();
	}

}
