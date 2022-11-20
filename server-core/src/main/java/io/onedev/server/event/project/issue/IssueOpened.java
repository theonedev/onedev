package io.onedev.server.event.project.issue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.util.Input;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

public class IssueOpened extends IssueEvent {

	private static final long serialVersionUID = 1L;

	public IssueOpened(Issue issue) {
		super(issue.getSubmitter(), issue.getSubmitDate(), issue);
	}

	@Override
	protected CommentText newCommentText() {
		return getIssue().getDescription()!=null? new MarkdownText(getProject(), getIssue().getDescription()): null;
	}

	@Override
	public boolean affectsListing() {
		return true;
	}

	@Override
	public Map<String, Collection<User>> getNewUsers() {
		Map<String, Collection<User>> newUsers = new HashMap<>();
		UserManager userManager = OneDev.getInstance(UserManager.class);
		for (Input field: getIssue().getFieldInputs().values()) {
			if (field.getType().equals(FieldSpec.USER)) {
				Set<User> usersOfField = field.getValues()
						.stream()
						.map(it->userManager.findByName(it))
						.filter(it->it!=null)
						.collect(Collectors.toSet());
				if (!usersOfField.isEmpty())
					newUsers.put(field.getName(), usersOfField);
			} 
		}
		return newUsers;
	}

	@Override
	public Map<String, Group> getNewGroups() {
		Map<String, Group> newGroups = new HashMap<>();
		GroupManager groupManager = OneDev.getInstance(GroupManager.class);
		for (Input field: getIssue().getFieldInputs().values()) {
			if (field.getType().equals(FieldSpec.GROUP)) {
				if (!field.getValues().isEmpty()) {
					Group newGroup = groupManager.find(field.getValues().iterator().next());
					if (newGroup != null)
						newGroups.put(field.getName(), newGroup);
				}
			} 
		}
		return newGroups;
	}

	@Override
	public String getActivity() {
		return "opened";
	}

}