package io.onedev.server.web.util.editbean;

import static io.onedev.server.util.DateUtils.toDate;
import static java.lang.Integer.parseInt;
import static java.time.LocalDate.ofEpochDay;
import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.service.IterationService;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.util.DateUtils;
import io.onedev.server.validation.Validatable;

@Editable
@ClassValidating
public class IterationEditBean implements Validatable, Serializable {

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

	public void update(Iteration iteration) {
		iteration.setName(getName());
		iteration.setDescription(getDescription());
		if (getStartDate() != null) {
			iteration.setStartDay(DateUtils.toLocalDate(getStartDate()).toEpochDay());
		} else {	
			iteration.setStartDay(null);
		}
		if (getDueDate() != null) {
			iteration.setDueDay(DateUtils.toLocalDate(getDueDate()).toEpochDay());
		} else {
			iteration.setDueDay(null);
		}
	}
	
	public static IterationEditBean ofNew(Project project, @Nullable String namePrefix) {
		var bean = new IterationEditBean();
		bean.namePrefix = namePrefix;
		var iterations = project.getHierarchyIterations().stream()
				.filter(it -> namePrefix == null || it.getName().startsWith(namePrefix)).collect(toList());
		var datesComparator = new Iteration.DatesComparator();
		var lastIterationOpt = iterations.stream().filter(it->!it.isClosed()).max(datesComparator);
		if (!lastIterationOpt.isPresent())
			lastIterationOpt = iterations.stream().filter(it->it.isClosed()).max(datesComparator);
		if (lastIterationOpt.isPresent()) {
			var lastIteration = lastIterationOpt.get();
			var matcher = ENDS_WITH_DIGITS.matcher(lastIteration.getName());
			if (matcher.matches())
				bean.setName(matcher.group(1) + (parseInt(matcher.group(2)) + 1));
			if (lastIteration.getStartDay() != null && lastIteration.getDueDay() != null) {
				bean.setStartDate(toDate(ofEpochDay(lastIteration.getDueDay() + 1).atStartOfDay()));
				var duration = lastIteration.getDueDay() - lastIteration.getStartDay();
				bean.setDueDate(toDate(ofEpochDay(lastIteration.getDueDay() + duration).atStartOfDay()));
			}
		}
		return bean;
	}
	
	public static IterationEditBean of(Iteration iteration, @Nullable String namePrefix) {
		var bean = new IterationEditBean();
		bean.oldName = iteration.getName();
		bean.namePrefix = namePrefix;
		bean.setName(iteration.getName());
		bean.setDescription(iteration.getDescription());
		if (iteration.getStartDay() != null) {
			bean.setStartDate(toDate(ofEpochDay(iteration.getStartDay()).atStartOfDay()));
		} else {
			bean.setStartDate(null);
		}
		if (iteration.getDueDay() != null) {
			bean.setDueDate(toDate(ofEpochDay(iteration.getDueDay()).atStartOfDay()));
		} else {
			bean.setDueDate(null);
		}
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
		IterationService iterationService = OneDev.getInstance(IterationService.class);
		Iteration iterationWithSameName = iterationService.findInHierarchy(Project.get(), name);
		if (iterationWithSameName != null && (oldName == null || !oldName.equals(name))) {
			context.disableDefaultConstraintViolation();
			var message = "Name has already been used by another iteration in the project hierarchy";
			context.buildConstraintViolationWithTemplate(message)
					.addPropertyNode("name")
					.addConstraintViolation();
			return false;
		}
		return true;
	}
	
}
