package io.onedev.server.model.support.issue.fieldspec;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.inputspec.showcondition.ShowCondition;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.usermatcher.Anyone;
import io.onedev.server.util.validation.annotation.FieldName;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.UserMatcher;

@Editable
public abstract class FieldSpec extends InputSpec {
	
	private static final long serialVersionUID = 1L;
	
	private String nameOfEmptyValue;
	
	private String canBeChangedBy = new Anyone().toString();
	
	@Editable(order=10)
	@FieldName
	@NotEmpty
	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public void setName(String name) {
		super.setName(name);
	}

	@Editable(order=30, description="Optionally describes the custom field")
	@NameOfEmptyValue("No description")
	@Override
	public String getDescription() {
		return super.getDescription();
	}

	@Override
	public void setDescription(String description) {
		super.setDescription(description);
	}

	@Editable(order=35, description="Whether or not multiple values can be specified for this field")
	@Override
	public boolean isAllowMultiple() {
		return super.isAllowMultiple();
	}

	@Override
	public void setAllowMultiple(boolean allowMultiple) {
		super.setAllowMultiple(allowMultiple);
	}

	@Editable(order=40, name="Show Conditionally", description="Enable if visibility of this field depends on other fields")
	@NameOfEmptyValue("Always")
	@Valid
	@Override
	public ShowCondition getShowCondition() {
		return super.getShowCondition();
	}

	@Override
	public void setShowCondition(ShowCondition showCondition) {
		super.setShowCondition(showCondition);
	}
	
	@Editable(order=50, name="Allow Empty Value", description="Whether or not this field accepts empty value")
	@Override
	public boolean isAllowEmpty() {
		return super.isAllowEmpty();
	}

	@Override
	public void setAllowEmpty(boolean allowEmpty) {
		super.setAllowEmpty(allowEmpty);
	}
	
	@Editable(order=60)
	@io.onedev.server.web.editable.annotation.ShowCondition("isNameOfEmptyValueVisible")
	@NotEmpty
	public String getNameOfEmptyValue() {
		return nameOfEmptyValue;
	}

	public void setNameOfEmptyValue(String nameOfEmptyValue) {
		this.nameOfEmptyValue = nameOfEmptyValue;
	}
	
	@Editable(order=65, description="This setting tells who can change this field")
	@UserMatcher
	@NotEmpty
	public String getCanBeChangedBy() {
		return canBeChangedBy;
	}

	public void setCanBeChangedBy(String canBeChangedBy) {
		this.canBeChangedBy = canBeChangedBy;
	}

	@SuppressWarnings("unused")
	private static boolean isNameOfEmptyValueVisible() {
		return (boolean) EditContext.get().getInputValue("allowEmpty");
	}

	@Override
	public void appendCommonAnnotations(StringBuffer buffer, int index) {
		super.appendCommonAnnotations(buffer, index);
		if (getNameOfEmptyValue() != null)
			buffer.append("    @NameOfEmptyValue(\"" + escape(getNameOfEmptyValue()) + "\")");
	}

}
