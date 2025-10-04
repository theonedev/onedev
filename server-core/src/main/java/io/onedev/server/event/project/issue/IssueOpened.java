package io.onedev.server.event.project.issue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.Input;
import io.onedev.server.service.GroupService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

public class IssueOpened extends IssueEvent implements CommitAware {

	private static final long serialVersionUID = 1L;

	private final Collection<String> notifiedEmailAddresses;
	
	public IssueOpened(Issue issue, Collection<String> notifiedEmailAddresses) {
		super(issue.getSubmitter(), issue.getSubmitDate(), issue);
		this.notifiedEmailAddresses = notifiedEmailAddresses;
	}

	@Override
	protected CommentText newCommentText() {
		return getIssue().getDescription()!=null? new MarkdownText(getProject(), getIssue().getDescription()): null;
	}

	@Override
	public boolean affectsListing() {
		return true;
	}

	public Collection<String> getNotifiedEmailAddresses() {
		return notifiedEmailAddresses;
	}
	
	@Override
	public Map<String, Collection<User>> getNewUsers() {
		Map<String, Collection<User>> newUsers = new HashMap<>();
		UserService userService = OneDev.getInstance(UserService.class);
		for (Input field: getIssue().getFieldInputs().values()) {
			if (field.getType().equals(FieldSpec.USER)) {
				Set<User> usersOfField = field.getValues()
						.stream()
						.map(it->userService.findByName(it))
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
		GroupService groupService = OneDev.getInstance(GroupService.class);
		for (Input field: getIssue().getFieldInputs().values()) {
			if (field.getType().equals(FieldSpec.GROUP)) {
				if (!field.getValues().isEmpty()) {
					Group newGroup = groupService.find(field.getValues().iterator().next());
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

	@Override
	public ProjectScopedCommit getCommit() {
		var project = getIssue().getProject();
		if (project.getDefaultBranch() != null)
			return new ProjectScopedCommit(project, project.getObjectId(project.getDefaultBranch(), true));
		else
			return null;
	}

}