package io.onedev.server.util.usermatch;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.usermatch.UserMatchParser.CriteriaContext;
import io.onedev.server.util.usermatch.UserMatchParser.ExceptCriteriaContext;
import io.onedev.server.util.usermatch.UserMatchParser.UserMatchContext;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.TerminalNode;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
			User user = OneDev.getInstance(UserManager.class).findByName(userName);
			if (user != null)
				return new UserCriteria(user);
			else
				throw new ExplicitException("Unable to find user with login: " + userName);
		} else if (criteriaContext.groupCriteria() != null) {
			String groupName = getValue(criteriaContext.groupCriteria().Value());
			Group group = OneDev.getInstance(GroupManager.class).find(groupName);
			if (group != null)
				return new GroupCriteria(group);
			else
				throw new ExplicitException("Unable to find group: " + groupName);
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
		List<String> criteriaStrings = criterias.stream().map(Object::toString).collect(Collectors.toList()); 
		builder.append(StringUtils.join(criteriaStrings, " or "));

		if (!exceptCriterias.isEmpty()) {
			List<String> exceptCriteriaStrings = exceptCriterias.stream().map(Object::toString).collect(Collectors.toList()); 
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
			if (criteria instanceof GroupCriteria) {  
				GroupCriteria groupCriteria = (GroupCriteria) criteria;
				if (groupCriteria.getGroup().getName().equals(oldName))
					groupCriteria.getGroup().setName(newName);
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
			if (criteria instanceof UserCriteria) {  
				UserCriteria userCriteria = (UserCriteria) criteria;
				if (userCriteria.getUser().getName().equals(oldName))
					userCriteria.getUser().setName(newName);
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
			if (criteria instanceof UserCriteria) {  
				UserCriteria userCriteria = (UserCriteria) criteria;
				if (userCriteria.getUser().getName().equals(userName))
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
			if (criteria instanceof GroupCriteria) {  
				GroupCriteria groupCriteria = (GroupCriteria) criteria;
				if (groupCriteria.getGroup().getName().equals(groupName))
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
