package io.onedev.server.buildspecmodel.inputspec.workingperiodinput;

import com.google.common.collect.Lists;
import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.buildspecmodel.inputspec.workingperiodinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.issue.TimeTrackingSetting;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkingPeriodInput {

	public static String getPropertyDef(InputSpec inputSpec, Map<String, Integer> indexes, 
			DefaultValueProvider defaultValueProvider) {
		int index = indexes.get(inputSpec.getName());
		StringBuffer buffer = new StringBuffer();
		inputSpec.appendField(buffer, index, "Integer");
		inputSpec.appendCommonAnnotations(buffer, index);
		if (!inputSpec.isAllowEmpty())
			buffer.append("    @NotNull(message=\"May not be empty\")\n");
		buffer.append("    @WorkingPeriod\n");
		inputSpec.appendMethods(buffer, index, "Integer", null, defaultValueProvider);
		
		return buffer.toString();
	}
	
	private static TimeTrackingSetting getTimeTrackingSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting().getTimeTrackingSetting();
	}

	public static Object convertToObject(List<String> strings) {
		try {
			if (strings.size() == 1)
				return getTimeTrackingSetting().parseWorkingPeriod(strings.iterator().next());
			else if (strings.size() == 0)
				return null;
			else
				throw new ValidationException("Not eligible for multi-value");
		} catch (IllegalArgumentException e) {
			throw new ValidationException(e.getMessage());
		}
	}

	public static List<String> convertToStrings(Object value) {
		if (value != null)
			return Lists.newArrayList(getTimeTrackingSetting().formatWorkingPeriod((int) value));
		else
			return new ArrayList<>();
	}

}
