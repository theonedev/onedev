package io.onedev.server.util.usermatcher;

import java.io.Serializable;
import java.util.ArrayList;
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
import org.antlr.v4.runtime.tree.TerminalNode;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneException;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.usermatcher.UserMatcherParser.CriteriaContext;
import io.onedev.server.util.usermatcher.UserMatcherParser.ExceptCriteriaContext;
import io.onedev.server.util.usermatcher.UserMatcherParser.UserMatcherContext;

public class UserMatcher implements Serializable {

	private static final long serialVersionUID = 1L;

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
		} else if (criteriaContext.userCriteria() != null) {
			String userName = getValue(criteriaContext.userCriteria().Value());
			SpecifiedUser specifiedUser = new SpecifiedUser();
			specifiedUser.setUserName(userName);
			return specifiedUser;
		} else if (criteriaContext.groupCriteria() != null) {
			String groupName = getValue(criteriaContext.groupCriteria().Value());
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
	
	private static String getValue(TerminalNode terminal) {
		return StringUtils.unescape(FenceAware.unfence(terminal.getText()));
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

	private static boolean isUsingUser(List<UserMatcherCriteria> criterias, String userName) {
		for (UserMatcherCriteria criteria: criterias) {
			if (criteria instanceof SpecifiedUser) {  
				SpecifiedUser specifiedUser = (SpecifiedUser) criteria;
				if (specifiedUser.getUserName().equals(userName))
					return true;
			}
		}
		return false;
	}
	
	public static boolean isUsingUser(String userMatcherString, String userName) {
		UserMatcher userMatcher = fromString(userMatcherString);
		return isUsingUser(userMatcher.getCriterias(), userName) 
				|| isUsingUser(userMatcher.getExceptCriterias(), userName);
	}
	
	private static boolean isUsingGroup(List<UserMatcherCriteria> criterias, String groupName) {
		for (UserMatcherCriteria criteria: criterias) {
			if (criteria instanceof SpecifiedGroup) {  
				SpecifiedGroup specifiedGroup = (SpecifiedGroup) criteria;
				if (specifiedGroup.getGroupName().equals(groupName))
					return true;
			}
		}
		return false;
	}
	
	public static boolean isUsingGroup(String userMatcherString, String groupName) {
		UserMatcher userMatcher = fromString(userMatcherString);
		return isUsingGroup(userMatcher.getCriterias(), groupName) 
				|| isUsingGroup(userMatcher.getExceptCriterias(), groupName);
	}
	
}
