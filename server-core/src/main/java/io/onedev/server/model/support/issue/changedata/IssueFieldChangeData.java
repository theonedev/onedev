package io.onedev.server.model.support.issue.changedata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.service.GroupService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.DateUtils;
import io.onedev.server.buildspecmodel.inputspec.Input;

public class IssueFieldChangeData extends IssueChangeData {

	private static final long serialVersionUID = 1L;

	protected final Map<String, Input> oldFields;
	
	protected final Map<String, Input> newFields;
	
	public IssueFieldChangeData(Map<String, Input> oldFields, Map<String, Input> newFields) {
		this.oldFields = copyNonEmptyFields(oldFields);
		this.newFields = copyNonEmptyFields(newFields);
	}
	
	public Map<String, Input> getOldFields() {
		return oldFields;
	}

	public Map<String, Input> getNewFields() {
		return newFields;
	}

	private List<String> getDisplayValues(Input input) {
		var displayValues = new ArrayList<String>();
		for (var value: input.getValues()) {
			if (input.getType().equals(InputSpec.DATE)) {
				try {
					displayValues.add(DateUtils.formatDate(new Date(Long.parseLong(value))));
				} catch (Exception e) {
					displayValues.add(value);
				}
			} else if (input.getType().equals(InputSpec.DATE_TIME)) {
				try {
					displayValues.add(DateUtils.formatDateTime(new Date(Long.parseLong(value))));
				} catch (Exception e) {
					displayValues.add(value);
				}
			} else {
				displayValues.add(value);
			}
		}
		return displayValues;
	}

	public Map<String, String> getOldFieldValues() {
		Map<String, String> oldFieldValues = new LinkedHashMap<>();
		for (Map.Entry<String, Input> entry: oldFields.entrySet())
			oldFieldValues.put(entry.getKey(), StringUtils.join(getDisplayValues(entry.getValue())));
		return oldFieldValues;
	}
	
	public Map<String, String> getNewFieldValues() {
		Map<String, String> newFieldValues = new LinkedHashMap<>();
		for (Map.Entry<String, Input> entry: newFields.entrySet())
			newFieldValues.put(entry.getKey(), StringUtils.join(getDisplayValues(entry.getValue())));
		return newFieldValues;
	}
	
	private Map<String, Input> copyNonEmptyFields(Map<String, Input> fields) {
		Map<String, Input> copy = new LinkedHashMap<>();
		for (Map.Entry<String, Input> entry: fields.entrySet()) {
			if (!entry.getValue().getValues().isEmpty())
				copy.put(entry.getKey(), entry.getValue());
		}
		return copy;
	}
	
	private String describe(Input field) {
		return field.getName() + ": " + StringUtils.join(field.getValues(), ", ");		
	}
	
	@Override
	public String getActivity() {
		return "changed fields";
	}

	public List<String> getLines(Map<String, Input> fields) {
		List<String> lines = new ArrayList<>();
		for (Map.Entry<String, Input> entry: fields.entrySet())
			lines.add(entry.getKey() + ": " + StringUtils.join(entry.getValue().getValues(), ", "));
		return lines;
	}
	
	@Override
	public Map<String, Collection<User>> getNewUsers() {
		UserService userService = OneDev.getInstance(UserService.class);
		Map<String, Collection<User>> newUsers = new HashMap<>();
		for (Input oldField: oldFields.values()) {
			Input newField = newFields.get(oldField.getName());
			if (newField != null 
					&& !describe(oldField).equals(describe(newField)) 
					&& newField.getType().equals(FieldSpec.USER)) { 
				Set<User> newUsersOfField = newField.getValues()
						.stream()
						.filter(it->!oldField.getValues().contains(it))
						.map(it->userService.findByName(it))
						.filter(it->it!=null)
						.collect(Collectors.toSet());
				if (!newUsersOfField.isEmpty())
					newUsers.put(newField.getName(), newUsersOfField);
			}
		}
		for (Input newField: newFields.values()) {
			if (!oldFields.containsKey(newField.getName()) 
					&& newField.getType().equals(FieldSpec.USER)) { 
				Set<User> usersOfField = newField.getValues()
						.stream()
						.map(it->userService.findByName(it))
						.filter(it->it!=null)
						.collect(Collectors.toSet());
				if (!usersOfField.isEmpty())
					newUsers.put(newField.getName(), usersOfField);
			}
		}
		return newUsers;
	}
	
	@Override
	public Map<String, Group> getNewGroups() {
		Map<String, Group> newGroups = new HashMap<>();
		GroupService groupService = OneDev.getInstance(GroupService.class);
		for (Input oldField: oldFields.values()) {
			Input newField = newFields.get(oldField.getName());
			if (newField != null 
					&& !describe(oldField).equals(describe(newField)) 
					&& newField.getType().equals(FieldSpec.GROUP) 
					&& !newField.getValues().isEmpty()) { 
				Group group = groupService.find(newField.getValues().iterator().next());
				if (group != null)
					newGroups.put(newField.getName(), group);
			}
		}
		for (Input newField: newFields.values()) {
			if (!oldFields.containsKey(newField.getName()) 
					&& newField.getType().equals(FieldSpec.GROUP) 
					&& !newField.getValues().isEmpty()) { 
				Group group = groupService.find(newField.getValues().iterator().next());
				if (group != null)
					newGroups.put(newField.getName(), group);
			}
		}
		return newGroups;
	}
	
	@Override
	public boolean affectsListing() {
		return true;
	}

	@Override
	public ActivityDetail getActivityDetail() {
		return ActivityDetail.compare(getOldFieldValues(), getNewFieldValues(), false);
	}

}
