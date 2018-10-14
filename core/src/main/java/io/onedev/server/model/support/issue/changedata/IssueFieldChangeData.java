package io.onedev.server.model.support.issue.changedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;

import io.onedev.server.OneDev;
import io.onedev.server.manager.TeamManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Project;
import io.onedev.server.model.Team;
import io.onedev.server.model.User;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.util.IssueField;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;
import io.onedev.utils.StringUtils;

public class IssueFieldChangeData implements IssueChangeData {

	private static final long serialVersionUID = 1L;

	protected final Map<String, IssueField> oldFields;
	
	protected final Map<String, IssueField> newFields;
	
	public IssueFieldChangeData(Map<String, IssueField> oldFields, Map<String, IssueField> newFields) {
		this.oldFields = copyNonEmptyFields(oldFields);
		this.newFields = copyNonEmptyFields(newFields);
	}
	
	protected List<String> getOldLines() {
		List<String> oldLines = new ArrayList<>();
		for (IssueField oldField: oldFields.values()) {
			IssueField newField = newFields.get(oldField.getName());
			if (newField == null || !describe(oldField).equals(describe(newField)))
				oldLines.add(describe(oldField));
		}
		for (IssueField newField: newFields.values()) {
			if (!oldFields.containsKey(newField.getName()))
				oldLines.add("");
		}
		return oldLines;
	}
	
	protected List<String> getNewLines() {
		List<String> newLines = new ArrayList<>();
		for (IssueField oldField: oldFields.values()) {
			IssueField newField = newFields.get(oldField.getName());
			if (newField != null) {
				if (!describe(oldField).equals(describe(newField))) {
					newLines.add(describe(newField));
				}
			} else {
				newLines.add("");
			}
		}
		for (IssueField newField: newFields.values()) {
			if (!oldFields.containsKey(newField.getName()))
				newLines.add(describe(newField));
		}
		return newLines;
	}
	
	protected Map<String, String> getNewUsers() {
		Map<String, String> newUsers = new HashMap<>();
		for (IssueField oldField: oldFields.values()) {
			IssueField newField = newFields.get(oldField.getName());
			if (newField != null && !describe(oldField).equals(describe(newField)) 
					&& newField.getType().equals(InputSpec.USER) && !newField.getValues().isEmpty()) 
				newUsers.put(newField.getName(), newField.getValues().iterator().next());
		}
		for (IssueField newField: newFields.values()) {
			if (!oldFields.containsKey(newField.getName()) && newField.getType().equals(InputSpec.USER) 
					&& !newField.getValues().isEmpty()) { 
				newUsers.put(newField.getName(), newField.getValues().iterator().next());
			}
		}
		return newUsers;
	}
	
	protected Map<String, String> getNewTeams() {
		Map<String, String> newTeams = new HashMap<>();
		for (IssueField oldField: oldFields.values()) {
			IssueField newField = newFields.get(oldField.getName());
			if (newField != null 
					&& !describe(oldField).equals(describe(newField)) 
					&& newField.getType().equals(InputSpec.TEAM) 
					&& !newField.getValues().isEmpty()) 
				newTeams.put(newField.getName(), newField.getValues().iterator().next());
		}
		for (IssueField newField: newFields.values()) {
			if (!oldFields.containsKey(newField.getName()) 
					&& newField.getType().equals(InputSpec.TEAM) 
					&& !newField.getValues().isEmpty()) { 
				newTeams.put(newField.getName(), newField.getValues().iterator().next());
			}
		}
		return newTeams;
	}
	
	private Map<String, IssueField> copyNonEmptyFields(Map<String, IssueField> fields) {
		Map<String, IssueField> copy = new LinkedHashMap<>();
		for (Map.Entry<String, IssueField> entry: fields.entrySet()) {
			if (!entry.getValue().getValues().isEmpty())
				copy.put(entry.getKey(), entry.getValue());
		}
		return copy;
	}
	
	private String describe(IssueField field) {
		return field.getName() + ": " + StringUtils.join(field.getValues(), ", ");		
	}
	
	@Override
	public Component render(String componentId, IssueChange change) {
		return new PlainDiffPanel(componentId, getOldLines(), "a.txt", getNewLines(), "b.txt", true);
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
	public Map<String, User> getNewUsers(Project project) {
		Map<String, User> newUsers = new HashMap<>();
		for (Map.Entry<String, String> entry: getNewUsers().entrySet()) {
			User user = OneDev.getInstance(UserManager.class).findByName(entry.getValue());
			if (user != null)
				newUsers.put(entry.getKey(), user);
		}
		return newUsers;
	}

	@Override
	public Map<String, Team> getNewTeams(Project project) {
		Map<String, Team> newTeams = new HashMap<>();
		for (Map.Entry<String, String> entry: getNewTeams().entrySet()) {
			Team team = OneDev.getInstance(TeamManager.class).find(project, entry.getValue());
			if (team != null)
				newTeams.put(entry.getKey(), team);
		}
		return newTeams;
	}

}
