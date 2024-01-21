package io.onedev.server.web.util.editablebean;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.validation.Validatable;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;

@Editable
@ClassValidating
public class MilestoneEditBean implements Serializable, Validatable {

	public String oldName;
	
	private String name;
	
	private String description;
	
	private Date startDate;
	
	private Date dueDate;

	@Editable(hidden=true)
	public String getOldName() {
		return oldName;
	}

	public void setOldName(String oldName) {
		this.oldName = oldName;
	}

	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=300)
	@Multiline
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Editable(order=400)
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Editable(order=500)
	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	
	public void readFrom(Milestone milestone) {
		oldName = milestone.getName();
		setName(milestone.getName());
		setDescription(milestone.getDescription());
		setDueDate(milestone.getDueDate());
		setStartDate(milestone.getStartDate());
	}
	
	public void writeTo(Milestone milestone) {
		milestone.setName(getName());
		milestone.setDescription(getDescription());
		milestone.setDueDate(getDueDate());
		milestone.setStartDate(getStartDate());
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		MilestoneManager milestoneManager = OneDev.getInstance(MilestoneManager.class);
		Milestone milestoneWithSameName = milestoneManager.findInHierarchy(Project.get(), name);
		if (milestoneWithSameName != null && (oldName == null || !oldName.equals(name))) {
			context.disableDefaultConstraintViolation();
			var message = "This name has already been used by another milestone in the project hierarchy";
			context.buildConstraintViolationWithTemplate(message)
					.addPropertyNode("name")
					.addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
}
