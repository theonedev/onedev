package io.onedev.server.search.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.Path;

import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.unbescape.java.JavaEscape;

import com.google.common.base.Splitter;

import io.onedev.server.OneDev;
import io.onedev.server.exception.OneException;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.ConfigurationManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.Configuration;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.DateUtils;
import io.onedev.utils.WordUtils;

public abstract class EntityQuery<T extends AbstractEntity> implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract EntityCriteria<T> getCriteria();

	public abstract List<EntitySort> getSorts();
	
	public static String getValue(String tokenText) {
		String value = tokenText.substring(1);
		return JavaEscape.unescapeJava(value.substring(0, value.length()-1));
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

	public static Configuration getConfiguration(Project project, String name) {
		Configuration configuration = OneDev.getInstance(ConfigurationManager.class).find(project, name);
		if (configuration != null)
			return configuration;
		else
			throw new OneException("Unable to find configuration: " + name);
	}
	
	public static Issue getIssue(Project project, String numberStr) {
		if (numberStr.startsWith("#"))
			numberStr = numberStr.substring(1);
		Issue issue = OneDev.getInstance(IssueManager.class).find(project, getLongValue(numberStr));
		if (issue != null)
			return issue;
		else
			throw new OneException("Unable to find issue: #" + numberStr);
	}
	
	public static Build getBuild(Project project, String fqn) {
		Build build = OneDev.getInstance(BuildManager.class).findByFQN(project, fqn);
		if (build != null)
			return build;
		else
			throw new OneException("Unable to find build with FQN: " + fqn);
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
		return "\"" + JavaEscape.escapeJava(value) + "\"";
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
