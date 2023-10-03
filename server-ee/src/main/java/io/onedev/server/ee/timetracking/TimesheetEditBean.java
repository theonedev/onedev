package io.onedev.server.ee.timetracking;

import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.TimesheetSetting;
import io.onedev.server.validation.Validatable;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;

@Editable(name="Edit Timesheet")
@ClassValidating
public class TimesheetEditBean extends TimesheetSetting implements Validatable {

	private static final long serialVersionUID = 1L;
	
	private String oldName;
	
	private String name;

	@Editable(hidden=true)
	public String getOldName() {
		return oldName;
	}

	public void setOldName(String oldName) {
		this.oldName = oldName;
	}

	@Editable(order=10)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (Project.get().getIssueSetting().getTimesheetSettings().containsKey(getName())
				&& (oldName == null || !oldName.equals(getName()))) {
			context.disableDefaultConstraintViolation();
			var message = "Name already used by another timesheet in this project";
			context.buildConstraintViolationWithTemplate(message)
					.addPropertyNode("name").addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
}
