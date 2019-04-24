package io.onedev.server.util.usermatcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.exception.OneException;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.usermatcher.UserMatcherParser.CriteriaContext;
import io.onedev.server.util.usermatcher.UserMatcherParser.ExceptCriteriaContext;
import io.onedev.server.util.usermatcher.UserMatcherParser.UserMatcherContext;

public class UserMatcher implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String ESCAPE_CHARS = "\\()";
	
	private final List<UserMatcherCriteria> criterias;
	
	private final List<UserMatcherCriteria> exceptCriterias;

	public UserMatcher(List<UserMatcherCriteria> criterias, List<UserMatcherCriteria> exceptCriterias) {
		this.criterias = criterias;
		this.exceptCriterias = exceptCriterias;
	}
	
	public List<UserMatcherCriteria> getCriterias() {
		return criterias;
	}

	public List<UserMatcherCriteria> getExceptCriterias() {
		return exceptCriterias;
	}

	public boolean matches(Project project, User user) {
		for (UserMatcherCriteria criteria: exceptCriterias) {
			if (criteria.matches(project, user))
				return false;
		}
		for (UserMatcherCriteria criteria: criterias) {
			if (criteria.matches(project, user))
				return true;
		}
		return false;
	}
	
	private static UserMatcherCriteria getUserMatcherCriteria(CriteriaContext criteriaContext) {
		if (criteriaContext.Anyone() != null) {
			return new Anyone();
		} else if (criteriaContext.ProjectAdministrators() != null) {
			return new ProjectAdministrators();
		} else if (criteriaContext.CodeWriters() != null) {
			return new CodeWriters();
		} else if (criteriaContext.CodeReaders() != null) {
			return new CodeReaders();
		} else if (criteriaContext.IssueReaders() != null) {
			return new IssueReaders();
		} else if (criteriaContext.userCriteria() != null) {
			String userName = unescape(removeParens(criteriaContext.userCriteria().Value().getText()));
			SpecifiedUser specifiedUser = new SpecifiedUser();
			specifiedUser.setUserName(userName);
			return specifiedUser;
		} else if (criteriaContext.groupCriteria() != null) {
			String groupName = unescape(removeParens(criteriaContext.groupCriteria().Value().getText()));
			SpecifiedGroup specifiedGroup = new SpecifiedGroup();
			specifiedGroup.setGroupName(groupName);
			return specifiedGroup;
		} else {
			throw new OneException("Unrecognized user matcher criteria");
		}
	}
	
	public static UserMatcher fromString(@Nullable String userMatcherString) {
		List<UserMatcherCriteria> criterias = new ArrayList<>();
		List<UserMatcherCriteria> exceptCriterias = new ArrayList<>();
		
		if (userMatcherString != null) {
			UserMatcherContext userMatcherContext = parse(userMatcherString);
			
			for (CriteriaContext criteriaContext: userMatcherContext.criteria())
				criterias.add(getUserMatcherCriteria(criteriaContext));
			
			for (ExceptCriteriaContext exceptCriteriaContext: userMatcherContext.exceptCriteria()) {
				exceptCriterias.add(getUserMatcherCriteria(exceptCriteriaContext.criteria()));
			}
		}
		
		return new UserMatcher(criterias, exceptCriterias);
	}
	
	public static String unescape(String value) {
		return value.replace("\\(", "(").replace("\\)", ")").replace("\\\\", "\\");
	}

	public static String removeParens(String value) {
		if (value.startsWith("("))
			value = value.substring(1);
		if (value.endsWith(")"))
			value = value.substring(0, value.length()-1);
		return value;
	}
	
	public static UserMatcherContext parse(String userMatcherString) {
		CharStream is = CharStreams.fromString(userMatcherString); 
		UserMatcherLexer lexer = new UserMatcherLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new OneException("Malformed user matcher");
			}
			
		});
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		UserMatcherParser parser = new UserMatcherParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		return parser.userMatcher();
	}

	public static String escape(String value) {
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<value.length(); i++) {
			char ch = value.charAt(i);
			if (ESCAPE_CHARS.indexOf(ch) != -1)
				builder.append("\\");
			builder.append(ch);
		}
		return builder.toString();
	}	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		List<String> criteriaStrings = criterias.stream().map(it->it.toString()).collect(Collectors.toList()); 
		builder.append(StringUtils.join(criteriaStrings, " or "));

		if (!exceptCriterias.isEmpty()) {
			List<String> exceptCriteriaStrings = exceptCriterias.stream().map(it->it.toString()).collect(Collectors.toList()); 
			if (builder.length() != 0)
				builder.append(" ");
			builder.append("except ").append(StringUtils.join(exceptCriteriaStrings, " and "));
		}
		
		if (builder.length() != 0)
			return builder.toString();
		else
			return new Anyone().toString();
	}

	private static void onRenameGroup(List<UserMatcherCriteria> criterias, String oldName, String newName) {
		for (UserMatcherCriteria criteria: criterias) {
			if (criteria instanceof SpecifiedGroup) {  
				SpecifiedGroup specifiedGroup = (SpecifiedGroup) criteria;
				if (specifiedGroup.getGroupName().equals(oldName))
					specifiedGroup.setGroupName(newName);
			}
		}
	}
	
	public static String onRenameGroup(String userMatcherString, String oldName, String newName) {
		UserMatcher userMatcher = fromString(userMatcherString);
		onRenameGroup(userMatcher.getCriterias(), oldName, newName);
		onRenameGroup(userMatcher.getExceptCriterias(), oldName, newName);
		return userMatcher.toString();
	}

	private static void onDeleteGroup(List<UserMatcherCriteria> criterias, String groupName) {
		for (Iterator<UserMatcherCriteria> it = criterias.iterator(); it.hasNext();) {
			UserMatcherCriteria criteria = it.next();
			if (criteria instanceof SpecifiedGroup) {  
				SpecifiedGroup specifiedGroup = (SpecifiedGroup) criteria;
				if (specifiedGroup.getGroupName().equals(groupName))
					it.remove();
			}
		}
	}
	
	public static String onDeleteGroup(String userMatcherString, String groupName) {
		UserMatcher userMatcher = fromString(userMatcherString);
		onDeleteGroup(userMatcher.getCriterias(), groupName);
		onDeleteGroup(userMatcher.getExceptCriterias(), groupName);
		return userMatcher.toString();
	}

	private static void onRenameUser(List<UserMatcherCriteria> criterias, String oldName, String newName) {
		for (UserMatcherCriteria criteria: criterias) {
			if (criteria instanceof SpecifiedUser) {  
				SpecifiedUser specifiedUser = (SpecifiedUser) criteria;
				if (specifiedUser.getUserName().equals(oldName))
					specifiedUser.setUserName(newName);
			}
		}
	}
	
	public static String onRenameUser(String userMatcherString, String oldName, String newName) {
		UserMatcher userMatcher = fromString(userMatcherString);
		onRenameUser(userMatcher.getCriterias(), oldName, newName);
		onRenameUser(userMatcher.getExceptCriterias(), oldName, newName);
		return userMatcher.toString();
	}

	private static void onDeleteUser(List<UserMatcherCriteria> criterias, String userName) {
		for (Iterator<UserMatcherCriteria> it = criterias.iterator(); it.hasNext();) {
			UserMatcherCriteria criteria = it.next();
			if (criteria instanceof SpecifiedUser) {  
				SpecifiedUser specifiedUser = (SpecifiedUser) criteria;
				if (specifiedUser.getUserName().equals(userName))
					it.remove();
			}
		}
	}
	
	public static String onDeleteUser(String userMatcherString, String userName) {
		UserMatcher userMatcher = fromString(userMatcherString);
		onDeleteUser(userMatcher.getCriterias(), userName);
		onDeleteUser(userMatcher.getExceptCriterias(), userName);
		return userMatcher.toString();
	}
	
}
