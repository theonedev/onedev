package io.onedev.server.search.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.Path;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Splitter;

import io.onedev.commons.utils.WordUtils;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.DateUtils;

public abstract class EntityQuery<T extends AbstractEntity> implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract EntityCriteria<T> getCriteria();

	public abstract List<EntitySort> getSorts();
	
	public static String getValue(String tokenText) {
		String value = tokenText.substring(1);
		value = value.substring(0, value.length()-1);
		return unescapeQuotes(value);
	}

	public static int getIntValue(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new OneException("Invalid number: " + value);
		}
	}
	
	public static long getLongValue(String value) {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new OneException("Invalid number: " + value);
		}
	}
	
	public static User getUser(String loginName) {
		User user = OneDev.getInstance(UserManager.class).findByName(loginName);
		if (user == null)
			throw new OneException("Unable to find user with login: " + loginName);
		return user;
	}
	
	public static boolean getBooleanValue(String value) {
		if (value.equals("true"))
			return true;
		else if (value.equals("false"))
			return false;
		else
			throw new OneException("Invalid boolean: " + value);
	}
	
	public static Date getDateValue(String value) {
		Date dateValue = DateUtils.parseRelaxed(value);
		if (dateValue == null)
			throw new OneException("Unrecognized date: " + value);
		return dateValue;
	}
	
	public static ObjectId getCommitId(Project project, String revision) {
		try {
			ObjectId commitId = project.getRepository().resolve(revision);
			if (commitId == null)
				throw new RevisionSyntaxException("");
			return commitId;								
		} catch (RevisionSyntaxException | IOException e) {
			throw new OneException("Invalid revision string: " + revision);
		}
	}

	public static Issue getIssue(Project project, String number) {
		if (number.startsWith("#"))
			number = number.substring(1);
		Issue issue = OneDev.getInstance(IssueManager.class).find(project, getLongValue(number));
		if (issue != null)
			return issue;
		else
			throw new OneException("Unable to find issue: #" + number);
	}
	
	public static PullRequest getPullRequest(Project project, String number) {
		if (number.startsWith("#"))
			number = number.substring(1);
		PullRequest request = OneDev.getInstance(PullRequestManager.class).find(project, getLongValue(number));
		if (request != null)
			return request;
		else
			throw new OneException("Unable to find pull request: #" + number);
	}
	
	public static Build getBuild(Project project, String number) {
		if (number.startsWith("#"))
			number = number.substring(1);
		Build build = OneDev.getInstance(BuildManager.class).find(project, getLongValue(number));
		if (build != null)
			return build;
		else
			throw new OneException("Unable to find build: #" + number);
	}
	
	public boolean needsLogin() {
		return getCriteria() != null && getCriteria().needsLogin();
	}
	
	public boolean matches(T entity, User user) {
		return getCriteria() == null || getCriteria().matches(entity, user);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (getCriteria() != null) 
			builder.append(getCriteria().toString()).append(" ");
		else
			builder.append("all");
		if (!getSorts().isEmpty()) {
			builder.append("order by ");
			for (EntitySort sort: getSorts())
				builder.append(sort.toString()).append(" ");
		}
		return builder.toString().trim();
	}
	
	public static String quote(String value) {
		return "\"" + escapeQuotes(value) + "\"";
	}
	
	public static String escapeQuotes(String value) {
		return StringUtils.replace(value, "\"", "\\\"");
	}
	
	public static String unescapeQuotes(String value) {
		return StringUtils.replace(value, "\\\"", "\"");
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
	
	public static String getLexerRuleName(String[] lexerRuleNames, int rule) {
		return WordUtils.uncamel(lexerRuleNames[rule-1]).toLowerCase();
	}
	
	public static int getLexerRule(String[] lexerRuleNames, String lexerRuleName) {
		for (int i=0; i<lexerRuleNames.length; i++) {
			String each = lexerRuleNames[i];
			if (WordUtils.uncamel(each).toLowerCase().equals(lexerRuleName))
				return i+1;
		}
		throw new OneException("Unable to find lexer rule: " + lexerRuleName);
	}
		
}
