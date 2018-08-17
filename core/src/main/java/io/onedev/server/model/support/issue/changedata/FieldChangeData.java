package io.onedev.server.model.support.issue.changedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;

import io.onedev.server.OneDev;
import io.onedev.server.manager.GroupManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.IssueAction;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CommentSupport;
import io.onedev.server.model.support.DiffSupport;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;
import io.onedev.utils.StringUtils;

public class FieldChangeData implements ActionData {

	private static final long serialVersionUID = 1L;

	protected final List<String> oldLines = new ArrayList<>();
	
	protected final List<String> newLines = new ArrayList<>();
	
	protected final Map<String, String> newUserNames = new HashMap<>();
	
	protected final Map<String, String> newGroupNames = new HashMap<>();
	
	public FieldChangeData(Map<String, IssueField> oldFields, Map<String, IssueField> newFields) {
		oldFields = copyNonEmptyFields(oldFields);
		newFields = copyNonEmptyFields(newFields);

		for (IssueField oldField: oldFields.values()) {
			IssueField newField = newFields.get(oldField.getName());
			if (newField != null) {
				if (!describe(oldField).equals(describe(newField))) {
					oldLines.add(describe(oldField));
					newLines.add(describe(newField));
					extractUsersAndGroups(newField);
				}
			} else {
				oldLines.add(describe(oldField));
				newLines.add("");
			}
		}
		for (IssueField newField: newFields.values()) {
			IssueField oldField = oldFields.get(newField.getName());
			if (oldField == null) {
				oldLines.add("");
				newLines.add(describe(newField));
				extractUsersAndGroups(newField);
			}
		}
	}
	
	private Map<String, IssueField> copyNonEmptyFields(Map<String, IssueField> fields) {
		Map<String, IssueField> copy = new LinkedHashMap<>();
		for (Map.Entry<String, IssueField> entry: fields.entrySet()) {
			if (!entry.getValue().getValues().isEmpty())
				copy.put(entry.getKey(), entry.getValue());
		}
		return copy;
	}
	
	private void extractUsersAndGroups(IssueField field) {
		if (field.getType().equals(InputSpec.USER) && !field.getValues().isEmpty()) 
			newUserNames.put(field.getName(), field.getValues().iterator().next());
		if (field.getType().equals(InputSpec.GROUP) && !field.getValues().isEmpty()) 
			newGroupNames.put(field.getName(), field.getValues().iterator().next());
	}

	private String describe(IssueField field) {
		return field.getName() + ": " + StringUtils.join(field.getValues(), ", ");		
	}
	
	@Override
	public Component render(String componentId, IssueAction action) {
		return new PlainDiffPanel(componentId, oldLines, "a.txt", newLines, "b.txt", true);
	}

	@Override
	public String getDescription() {
		return "changed fields";
	}

	public List<String> getLines(Map<String, IssueField> fields) {
		List<String> lines = new ArrayList<>();
		for (Map.Entry<String, IssueField> entry: fields.entrySet())
			lines.add(entry.getKey() + ": " + StringUtils.join(entry.getValue().getValues(), ", "));
		return lines;
	}
	
	@Override
	public CommentSupport getCommentSupport() {
		return null;
	}

	@Override
	public DiffSupport getDiffSupport() {
		return new DiffSupport() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<String> getOldLines() {
				return oldLines;
			}

			@Override
			public List<String> getNewLines() {
				return newLines;
			}

			@Override
			public String getOldFileName() {
				return "a.txt";
			}

			@Override
			public String getNewFileName() {
				return "b.txt";
			}
			
		};
	}

	@Override
	public Map<String, User> getNewUsers() {
		Map<String, User> newUsers = new HashMap<>();
		for (Map.Entry<String, String> entry: newUserNames.entrySet()) {
			User user = OneDev.getInstance(UserManager.class).findByName(entry.getValue());
			if (user != null)
				newUsers.put(entry.getKey(), user);
		}
		return newUsers;
	}

	@Override
	public Map<String, Group> getNewGroups() {
		Map<String, Group> newGroups = new HashMap<>();
		for (Map.Entry<String, String> entry: newGroupNames.entrySet()) {
			Group group = OneDev.getInstance(GroupManager.class).find(entry.getValue());
			if (group != null)
				newGroups.put(entry.getKey(), group);
		}
		return newGroups;
	}

}
