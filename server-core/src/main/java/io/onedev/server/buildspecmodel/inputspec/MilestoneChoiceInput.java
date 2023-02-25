package io.onedev.server.buildspecmodel.inputspec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;

public class MilestoneChoiceInput {
	
	public static String getPropertyDef(InputSpec inputSpec, Map<String, Integer> indexes) {
		int index = indexes.get(inputSpec.getName());
		StringBuffer buffer = new StringBuffer();
		inputSpec.appendField(buffer, index, inputSpec.isAllowMultiple()? "List<String>": "String");
		inputSpec.appendCommonAnnotations(buffer, index);
		if (!inputSpec.isAllowEmpty()) {
			if (inputSpec.isAllowMultiple())
				buffer.append("    @Size(min=1, message=\"At least one milestone needs to be selected\")\n");
			else
				buffer.append("    @NotEmpty\n");
		}
		
		buffer.append("    @MilestoneChoice\n");
		
		if (inputSpec.isAllowMultiple())
			inputSpec.appendMethods(buffer, index, "List<String>", null, null);
		else 
			inputSpec.appendMethods(buffer, index, "String", null, null);
		
		return buffer.toString();
	}

	public static Object convertToObject(InputSpec inputSpec, List<String> strings) {
		if (inputSpec.isAllowMultiple()) 
			return strings;
		else if (strings.size() == 0) 
			return null;
		else if (strings.size() == 1)
			return strings.iterator().next();
		else 
			throw new ValidationException("Not eligible for multi-value");
	}

	@SuppressWarnings("unchecked")
	public static List<String> convertToStrings(InputSpec inputSpec, Object value) {
		List<String> strings = new ArrayList<>();
		if (inputSpec.isAllowMultiple()) {
			if (inputSpec.checkListElements(value, String.class))
				strings.addAll((List<String>) value);
			Collections.sort(strings);
		} else if (value instanceof String) {
			strings.add((String) value);
		} 
		return strings;
	}

	public static int getOrdinal(String fieldValue) {
		int ordinal = -1;
		Project project = Project.get();
		if (project != null) {
			List<Milestone> milestones = new ArrayList<>(project.getHierarchyMilestones());
			Collections.sort(milestones);
			for (Milestone milestone: milestones) {
				ordinal++;
				if (milestone.getName().equals(fieldValue))
					break;
			}
		} 
		return ordinal;
	}
	
}
