package io.onedev.server.web.page.project.packs;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.PackSupport;
import io.onedev.server.validation.Validatable;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Editable
@ClassValidating
public class PackEditBean implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private String oldName;
	
	private String name;
	
	private PackSupport support;

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

	@Editable(order=200, name="Type")
	@NotNull
	public PackSupport getSupport() {
		return support;
	}

	public void setSupport(PackSupport support) {
		this.support = support;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		var packManager = OneDev.getInstance(PackManager.class);
		var packWithSameName = packManager.find(Project.get(), name);
		if (packWithSameName != null && (oldName == null || !oldName.equals(name))) {
			context.disableDefaultConstraintViolation();
			var message = "This name has already been used by another package in current project";
			context.buildConstraintViolationWithTemplate(message)
					.addPropertyNode("name")
					.addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
}
