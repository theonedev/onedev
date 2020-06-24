package io.onedev.server.model.support.issue.changedata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.wicket.Component;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.util.CommentAware;
import io.onedev.server.util.Input;
import io.onedev.server.web.component.propertychangepanel.PropertyChangePanel;

public class IssueFieldChangeData implements IssueChangeData {

	private static final long serialVersionUID = 1L;

	protected final Map<String, Input> oldFields;
	
	protected final Map<String, Input> newFields;
	
	public IssueFieldChangeData(Map<String, Input> oldFields, Map<String, Input> newFields) {
		this.oldFields = copyNonEmptyFields(oldFields);
		this.newFields = copyNonEmptyFields(newFields);
	}
	
	public Map<String, String> getOldFieldValues() {
		Map<String, String> oldFieldValues = new LinkedHashMap<>();
		for (Map.Entry<String, Input> entry: oldFields.entrySet())
			oldFieldValues.put(entry.getKey(), StringUtils.join(entry.getValue().getValues()));
		return oldFieldValues;
	}
	
	public Map<String, String> getNewFieldValues() {
		Map<String, String> newFieldValues = new LinkedHashMap<>();
		for (Map.Entry<String, Input> entry: newFields.entrySet())
			newFieldValues.put(entry.getKey(), StringUtils.join(entry.getValue().getValues()));
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
	public Component render(String componentId, IssueChange change) {
		return new PropertyChangePanel(componentId, getOldFieldValues(), getNewFieldValues(), false);
	}

	@Override
	public String getActivity(Issue withIssue) {
		String activity = "changed fields";
		if (withIssue != null)
			activity += " of issue " + withIssue.getNumberAndTitle();
		return activity;
	}

	public List<String> getLines(Map<String, Input> fields) {
		List<String> lines = new ArrayList<>();
		for (Map.Entry<String, Input> entry: fields.entrySet())
			lines.add(entry.getKey() + ": " + StringUtils.join(entry.getValue().getValues(), ", "));
		return lines;
	}
	
	@Override
	public CommentAware getCommentAware() {
		return null;
	}

	@Override
	public Map<String, Collection<User>> getNewUsers() {
		UserManager userManager = OneDev.getInstance(UserManager.class);
		Map<String, Collection<User>> newUsers = new HashMap<>();
		for (Input oldField: oldFields.values()) {
			Input newField = newFields.get(oldField.getName());
			if (newField != null 
					&& !describe(oldField).equals(describe(newField)) 
					&& newField.getType().equals(FieldSpec.USER)) { 
				Set<User> newUsersOfField = newField.getValues()
						.stream()
						.filter(it->!oldField.getValues().contains(it))
						.map(it->userManager.findByName(it))
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
						.map(it->userManager.findByName(it))
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
		GroupManager groupManager = OneDev.getInstance(GroupManager.class);
		for (Input oldField: oldFields.values()) {
			Input newField = newFields.get(oldField.getName());
			if (newField != null 
					&& !describe(oldField).equals(describe(newField)) 
					&& newField.getType().equals(FieldSpec.GROUP) 
					&& !newField.getValues().isEmpty()) { 
				Group group = groupManager.find(newField.getValues().iterator().next());
				if (group != null)
					newGroups.put(newField.getName(), group);
			}
		}
		for (Input newField: newFields.values()) {
			if (!oldFields.containsKey(newField.getName()) 
					&& newField.getType().equals(FieldSpec.GROUP) 
					&& !newField.getValues().isEmpty()) { 
				Group group = groupManager.find(newField.getValues().iterator().next());
				if (group != null)
					newGroups.put(newField.getName(), group);
			}
		}
		return newGroups;
	}
	
	@Override
	public boolean affectsBoards() {
		return true;
	}

}
