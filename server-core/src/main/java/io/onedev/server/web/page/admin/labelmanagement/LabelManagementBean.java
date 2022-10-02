package io.onedev.server.web.page.admin.labelmanagement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.validation.ConstraintValidatorContext;

import io.onedev.server.model.LabelSpec;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable
@ClassValidating
public class LabelManagementBean implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	private List<LabelSpec> labels = new ArrayList<>();

	@Editable
	@OmitName
	public List<LabelSpec> getLabels() {
		return labels;
	}

	public void setLabels(List<LabelSpec> labels) {
		this.labels = labels;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		var labelNames = new HashSet<>();
		for (var label: labels) {
			if (!labelNames.add(label.getName())) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("Duplicate label: " + label.getName())
						.addPropertyNode("labels").addConstraintViolation();
				return false;
			}
		}
		return true;
	}
	
}
