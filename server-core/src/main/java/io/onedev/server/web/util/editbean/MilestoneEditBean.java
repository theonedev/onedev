package io.onedev.server.web.util.editbean;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.validation.Validatable;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;

@Editable
@ClassValidating
public class MilestoneEditBean implements Validatable, Serializable {

	private static final Pattern ENDS_WITH_DIGITS = Pattern.compile("(.*)(\\d+)");
	
	public String oldName;

	public String namePrefix;
	
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

	@Editable(hidden = true)
	public String getNamePrefix() {
		return namePrefix;
	}

	public void setNamePrefix(String namePrefix) {
		this.namePrefix = namePrefix;
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

	public void update(Milestone milestone) {
		milestone.setName(getName());
		milestone.setDescription(getDescription());
		milestone.setStartDate(getStartDate());
		milestone.setDueDate(getDueDate());
	}
	
	public static MilestoneEditBean ofNew(Project project, @Nullable String namePrefix) {
		var bean = new MilestoneEditBean();
		bean.namePrefix = namePrefix;
		var milestones = project.getSortedHierarchyMilestones().stream()
				.filter(it -> (namePrefix == null || it.getName().startsWith(namePrefix)))
				.collect(toList());
		if (!milestones.isEmpty()) {
			var lastMilestone = milestones.get(milestones.size()-1);
			var matcher = ENDS_WITH_DIGITS.matcher(lastMilestone.getName());
			if (matcher.matches())
				bean.setName(matcher.group(1) + (parseInt(matcher.group(2)) + 1));
			if (lastMilestone.getStartDate() != null && lastMilestone.getDueDate() != null) {
				bean.setStartDate(new DateTime(lastMilestone.getDueDate()).plusDays(1).toDate());
				var duration = lastMilestone.getDueDate().getTime() - lastMilestone.getStartDate().getTime();
				bean.setDueDate(new DateTime(bean.getStartDate()).plusMillis((int) duration).toDate());
			}
		}
		return bean;
	}
	
	public static MilestoneEditBean of(Milestone milestone, @Nullable String namePrefix) {
		var bean = new MilestoneEditBean();
		bean.oldName = milestone.getName();
		bean.namePrefix = namePrefix;
		bean.setName(milestone.getName());
		bean.setDescription(milestone.getDescription());
		bean.setStartDate(milestone.getStartDate());
		bean.setDueDate(milestone.getDueDate());
		return bean;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (namePrefix != null && !name.startsWith(namePrefix)) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Name must prefix with: " + namePrefix)
					.addPropertyNode("name").addConstraintViolation();
			return false;
		}
		MilestoneManager milestoneManager = OneDev.getInstance(MilestoneManager.class);
		Milestone milestoneWithSameName = milestoneManager.findInHierarchy(Project.get(), name);
		if (milestoneWithSameName != null && (oldName == null || !oldName.equals(name))) {
			context.disableDefaultConstraintViolation();
			var message = "Name has already been used by another milestone in the project hierarchy";
			context.buildConstraintViolationWithTemplate(message)
					.addPropertyNode("name")
					.addConstraintViolation();
			return false;
		}
		return true;
	}
	
}
