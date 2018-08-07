package io.onedev.server.entityquery;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.Path;

import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.unbescape.java.JavaEscape;

import com.google.common.base.Splitter;

import io.onedev.server.exception.OneException;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
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
	
	public static ObjectId getCommitId(Project project, String value) {
		try {
			ObjectId commitId = project.getRepository().resolve(value);
			if (commitId == null)
				throw new RevisionSyntaxException("");
			return commitId;								
		} catch (RevisionSyntaxException | IOException e) {
			throw new OneException("Invalid revision string: " + value);
		}
	}

	public boolean needsLogin() {
		return getCriteria() != null && getCriteria().needsLogin();
	}
	
	public boolean matches(T entity) {
		return getCriteria() == null || getCriteria().matches(entity);
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
	
	public static String getRuleName(String[] lexerRuleNames, int rule) {
		return WordUtils.uncamel(lexerRuleNames[rule-1]).toLowerCase();
	}
	
	public static int getOperator(String[] lexerRuleNames, String operatorName) {
		for (int i=0; i<lexerRuleNames.length; i++) {
			String ruleName = lexerRuleNames[i];
			if (WordUtils.uncamel(ruleName).toLowerCase().equals(operatorName))
				return i+1;
		}
		throw new OneException("Unable to find operator: " + operatorName);
	}
		
}
