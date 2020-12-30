package io.onedev.server.search.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.persistence.criteria.Path;

import com.google.common.base.Splitter;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.ProjectScopedRevision;

public abstract class EntityQuery<T extends AbstractEntity> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Pattern INSIDE_QUOTE = Pattern.compile("\"([^\"\\\\]|\\\\.)*");

	public abstract EntityCriteria<T> getCriteria();

	public abstract List<EntitySort> getSorts();
	
	public static String getValue(String token) {
		return StringUtils.unescape(FenceAware.unfence(token));
	}
	
	public static int getIntValue(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new ExplicitException("Invalid number: " + value);
		}
	}
	
	public static long getLongValue(String value) {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new ExplicitException("Invalid number: " + value);
		}
	}
	
	public static User getUser(String loginName) {
		User user = OneDev.getInstance(UserManager.class).findByName(loginName);
		if (user == null)
			throw new ExplicitException("Unable to find user with login: " + loginName);
		return user;
	}
	
	public static Project getProject(String projectName) {
		Project project = OneDev.getInstance(ProjectManager.class).find(projectName);
		if (project == null)
			throw new ExplicitException("Unable to find project with name: " + projectName);
		return project;
	}
	
	public static boolean getBooleanValue(String value) {
		if (value.equals("true"))
			return true;
		else if (value.equals("false"))
			return false;
		else
			throw new ExplicitException("Invalid boolean: " + value);
	}
	
	public static Date getDateValue(String value) {
		Date dateValue = DateUtils.parseRelaxed(value);
		if (dateValue == null)
			throw new ExplicitException("Unrecognized date: " + value);
		return dateValue;
	}
	
	public static ProjectScopedCommit getCommitId(@Nullable Project project, String value) {
		if (project != null && !value.contains(":"))
			value = project.getName() + ":" + value;
		ProjectScopedCommit commitId = ProjectScopedCommit.from(value);
		if (commitId != null && commitId.getCommitId() != null)
			return commitId;
		else
			throw new ExplicitException("Unable to find revision: " + value);
	}

	public static ProjectScopedRevision getRevision(@Nullable Project project, String value) {
		if (project != null && !value.contains(":"))
			value = project.getName() + ":" + value;
		ProjectScopedRevision revision = ProjectScopedRevision.from(value);
		if (revision != null)
			return revision;
		else
			throw new ExplicitException("Unable to find revision: " + value);
	}
	
	public static Issue getIssue(@Nullable Project project, String value) {
		if (project != null) {
			if (value.startsWith("#"))
				value = project.getName() + value;
			else if (!value.contains("#"))
				value = project.getName() + "#" + value;
		}
		Issue issue = OneDev.getInstance(IssueManager.class).find(value);
		if (issue != null)
			return issue;
		else
			throw new ExplicitException("Unable to find issue: " + value);
	}
	
	public static PullRequest getPullRequest(@Nullable Project project, String value) {
		if (project != null) {
			if (value.startsWith("#"))
				value = project.getName() + value;
			else if (!value.contains("#"))
				value = project.getName() + "#" + value;
		}
		PullRequest pullRequest = OneDev.getInstance(PullRequestManager.class).find(value);
		if (pullRequest != null)
			return pullRequest;
		else
			throw new ExplicitException("Unable to find pull request: " + value);
	}
	
	public static Build getBuild(@Nullable Project project, String value) {
		if (project != null) {
			if (value.startsWith("#"))
				value = project.getName() + value;
			else if (!value.contains("#"))
				value = project.getName() + "#" + value;
		}
		Build build = OneDev.getInstance(BuildManager.class).find(value);
		if (build != null)
			return build;
		else
			throw new ExplicitException("Unable to find build: " + value);
	}
	
	public static ProjectScopedNumber getProjectScopedNumber(@Nullable Project project, String value) {
		if (project != null) {
			if (value.startsWith("#"))
				value = project.getName() + value;
			else if (!value.contains("#"))
				value = project.getName() + "#" + value;
		}
		return ProjectScopedNumber.from(value);
	}
	
	public static Milestone getMilestone(@Nullable Project project, String value) {
		if (project != null && !value.contains(":")) 
			value = project.getName() + ":" + value;
		Milestone milestone = OneDev.getInstance(MilestoneManager.class).find(value);
		if (milestone != null)
			return milestone;
		else
			throw new ExplicitException("Unable to find milestone: " + value);
	}
	
	public boolean matches(T entity) {
		return getCriteria() == null || getCriteria().matches(entity);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (getCriteria() != null) 
			builder.append(getCriteria().toString()).append(" ");
		if (!getSorts().isEmpty()) {
			builder.append("order by ");
			builder.append(getSorts().stream().map(it->it.toString()).collect(Collectors.joining(" and ")));
		}
		String toStringValue = builder.toString().trim();
		if (toStringValue.length() == 0)
			toStringValue = null;
		return toStringValue;
	}
	
	public static <T> Path<T> getPath(Path<?> root, String pathName) {
		int index = pathName.indexOf('.');
		if (index != -1) {
			Path<T> path = root.get(pathName.substring(0, index));
			for (String field: Splitter.on(".").split(pathName.substring(index+1))) 
				path = path.get(field);
			return path;
		} else {
			return root.get(pathName);
		}
	}
	
	public static boolean isInsideQuote(String value) {
		return INSIDE_QUOTE.matcher(value.trim()).matches();
	}
	
}
