package io.onedev.server.util.usermatch;

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
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.usermatch.UserMatchParser.CriteriaContext;
import io.onedev.server.util.usermatch.UserMatchParser.ExceptCriteriaContext;
import io.onedev.server.util.usermatch.UserMatchParser.UserMatchContext;

public class UserMatch implements Serializable {

	private static final long serialVersionUID = 1L;

	private final List<UserMatchCriteria> criterias;
	
	private final List<UserMatchCriteria> exceptCriterias;

	public UserMatch(List<UserMatchCriteria> criterias, List<UserMatchCriteria> exceptCriterias) {
		this.criterias = criterias;
		this.exceptCriterias = exceptCriterias;
	}
	
	public List<UserMatchCriteria> getCriterias() {
		return criterias;
	}

	public List<UserMatchCriteria> getExceptCriterias() {
		return exceptCriterias;
	}

	public boolean matches(Project project, User user) {
		for (UserMatchCriteria criteria: exceptCriterias) {
			if (criteria.matches(project, user))
				return false;
		}
		for (UserMatchCriteria criteria: criterias) {
			if (criteria.matches(project, user))
				return true;
		}
		return false;
	}
	
	private static UserMatchCriteria getUserMatchCriteria(CriteriaContext criteriaContext) {
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
			throw new ExplicitException("Unrecognized user match criteria");
		}
	}
	
	public static UserMatch parse(@Nullable String userMatchString) {
		List<UserMatchCriteria> criterias = new ArrayList<>();
		List<UserMatchCriteria> exceptCriterias = new ArrayList<>();
		
		if (userMatchString != null) {
			CharStream is = CharStreams.fromString(userMatchString); 
			UserMatchLexer lexer = new UserMatchLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new RuntimeException("Malformed user match");
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			UserMatchParser parser = new UserMatchParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			
			UserMatchContext userMatchContext = parser.userMatch();
			
			for (CriteriaContext criteriaContext: userMatchContext.criteria())
				criterias.add(getUserMatchCriteria(criteriaContext));
			
			for (ExceptCriteriaContext exceptCriteriaContext: userMatchContext.exceptCriteria()) {
				exceptCriterias.add(getUserMatchCriteria(exceptCriteriaContext.criteria()));
			}
		}
		
		return new UserMatch(criterias, exceptCriterias);
	}
	
	private static String getValue(TerminalNode terminal) {
		return StringUtils.unescape(FenceAware.unfence(terminal.getText()));
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

	private static void onRenameGroup(List<UserMatchCriteria> criterias, String oldName, String newName) {
		for (UserMatchCriteria criteria: criterias) {
			if (criteria instanceof SpecifiedGroup) {  
				SpecifiedGroup specifiedGroup = (SpecifiedGroup) criteria;
				if (specifiedGroup.getGroupName().equals(oldName))
					specifiedGroup.setGroupName(newName);
			}
		}
	}
	
	public static String onRenameGroup(String userMatchString, String oldName, String newName) {
		UserMatch userMatch = parse(userMatchString);
		onRenameGroup(userMatch.getCriterias(), oldName, newName);
		onRenameGroup(userMatch.getExceptCriterias(), oldName, newName);
		return userMatch.toString();
	}

	private static void onRenameUser(List<UserMatchCriteria> criterias, String oldName, String newName) {
		for (UserMatchCriteria criteria: criterias) {
			if (criteria instanceof SpecifiedUser) {  
				SpecifiedUser specifiedUser = (SpecifiedUser) criteria;
				if (specifiedUser.getUserName().equals(oldName))
					specifiedUser.setUserName(newName);
			}
		}
	}
	
	public static String onRenameUser(String userMatchString, String oldName, String newName) {
		UserMatch userMatch = parse(userMatchString);
		onRenameUser(userMatch.getCriterias(), oldName, newName);
		onRenameUser(userMatch.getExceptCriterias(), oldName, newName);
		return userMatch.toString();
	}

	private static boolean isUsingUser(List<UserMatchCriteria> criterias, String userName) {
		for (UserMatchCriteria criteria: criterias) {
			if (criteria instanceof SpecifiedUser) {  
				SpecifiedUser specifiedUser = (SpecifiedUser) criteria;
				if (specifiedUser.getUserName().equals(userName))
					return true;
			}
		}
		return false;
	}
	
	public static boolean isUsingUser(String userMatchString, String userName) {
		UserMatch userMatch = parse(userMatchString);
		return isUsingUser(userMatch.getCriterias(), userName) 
				|| isUsingUser(userMatch.getExceptCriterias(), userName);
	}
	
	private static boolean isUsingGroup(List<UserMatchCriteria> criterias, String groupName) {
		for (UserMatchCriteria criteria: criterias) {
			if (criteria instanceof SpecifiedGroup) {  
				SpecifiedGroup specifiedGroup = (SpecifiedGroup) criteria;
				if (specifiedGroup.getGroupName().equals(groupName))
					return true;
			}
		}
		return false;
	}
	
	public static boolean isUsingGroup(String userMatchString, String groupName) {
		UserMatch userMatch = parse(userMatchString);
		return isUsingGroup(userMatch.getCriterias(), groupName) 
				|| isUsingGroup(userMatch.getExceptCriterias(), groupName);
	}
	
}
