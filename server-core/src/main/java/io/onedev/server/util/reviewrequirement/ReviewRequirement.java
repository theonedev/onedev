package io.onedev.server.util.reviewrequirement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang.StringUtils;

import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.util.reviewrequirement.ReviewRequirementParser.CountContext;
import io.onedev.server.util.reviewrequirement.ReviewRequirementParser.CriteriaContext;
import io.onedev.server.util.reviewrequirement.ReviewRequirementParser.RequirementContext;

public class ReviewRequirement {
	
	private final List<User> users;
	
	private final Map<Group, Integer> groups;
	
	public ReviewRequirement(List<User> users, Map<Group, Integer> groups) {
		this.users = users;
		this.groups = groups;
	}
	
	public static ReviewRequirement fromString(@Nullable String requirementString) {
		List<User> users = new ArrayList<>();
		Map<Group, Integer> groups = new LinkedHashMap<>();
		
		if (requirementString != null) {
			RequirementContext requirement = parse(requirementString);
			
			for (CriteriaContext criteria: requirement.criteria()) {
				if (criteria.userCriteria() != null) {
					String userName = getValue(criteria.userCriteria().Value());
					User user = OneDev.getInstance(UserManager.class).findByName(userName);
					if (user != null) {
						if (!users.contains(user)) { 
							users.add(user);
						} else {
							throw new OneException("User '" + userName + "' is included multiple times");
						}
					} else {
						throw new OneException("Unable to find user '" + userName + "'");
					}
				} else if (criteria.groupCriteria() != null) {
					String groupName = getValue(criteria.groupCriteria().Value());
					Group group = OneDev.getInstance(GroupManager.class).find(groupName);
					if (group != null) {
						if (!groups.containsKey(group)) {
							CountContext count = criteria.groupCriteria().count();
							if (count != null) {
								if (count.DIGIT() != null)
									groups.put(group, Integer.parseInt(count.DIGIT().getText()));
								else
									groups.put(group, 0);
							} else {
								groups.put(group, 1);
							}
						} else {
							throw new OneException("Group '" + groupName + "' is included multiple times");
						}
					} else {
						throw new OneException("Unable to find group '" + groupName + "'");
					}
				}
			}			
		}
		
		return new ReviewRequirement(users, groups);
	}

	public static RequirementContext parse(String requirementString) {
		CharStream is = CharStreams.fromString(requirementString); 
		ReviewRequirementLexer lexer = new ReviewRequirementLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new OneException("Malformed review requirement");
			}
			
		});
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ReviewRequirementParser parser = new ReviewRequirementParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		return parser.requirement();
	}
	
	private static String getValue(TerminalNode terminal) {
		String value = terminal.getText().substring(1);
		return unescapeBraces(value.substring(0, value.length()-1).trim());
	}
	
	public static String escapeBraces(String value) {
		value = StringUtils.replace(value, ")", "\\)");
		return StringUtils.replace(value, "(", "\\(");
	}
	
	public static String unescapeBraces(String value) {
		value = StringUtils.replace(value, "\\)", ")");
		return StringUtils.replace(value, "\\(", "(");
	}
	
	public List<User> getUsers() {
		return users;
	}

	public Map<Group, Integer> getGroups() {
		return groups;
	}
	
	public boolean satisfied(User user) {
		for (User eachUser: users) {
			if (!eachUser.equals(user))
				return false;
		}
		for (Map.Entry<Group, Integer> entry: groups.entrySet()) {
			Group group = entry.getKey();
			int requiredCount = entry.getValue();
			if (requiredCount == 0 || requiredCount > group.getMembers().size())
				requiredCount = group.getMembers().size();

			if (requiredCount > 1 || requiredCount == 1 && !group.getMembers().contains(user))
				return false;
		}
		return true;
	}
	
	@Nullable
	public static String onRenameGroup(String reviewRequirementString, String oldName, String newName) {
		ReviewRequirement reviewRequirement = fromString(reviewRequirementString);
		for (Group group: reviewRequirement.getGroups().keySet()) {
			if (group.getName().equals(oldName))
				group.setName(newName);
		}
		return reviewRequirement.toString();
	}

	public static String onRenameUser(String reviewRequirementString, String oldName, String newName) {
		ReviewRequirement reviewRequirement = fromString(reviewRequirementString);
		for (User user: reviewRequirement.getUsers()) {
			if (user.getName().equals(oldName))
				user.setName(newName);
		}
		return reviewRequirement.toString();
	}
	
	public static boolean isUsingUser(String reviewRequirementString, String userName) {
		ReviewRequirement reviewRequirement = fromString(reviewRequirementString);
		for (User user: reviewRequirement.getUsers()) {
			if (user.getName().equals(userName))
				return true;
		}
		return false;
	}
	
	public static boolean isUsingGroup(String reviewRequirementString, String groupName) {
		ReviewRequirement reviewRequirement = fromString(reviewRequirementString);
		for (Group group: reviewRequirement.getGroups().keySet()) {
			if (group.getName().equals(groupName))
				return true;
		}
		return false;
	}
	
	@Nullable
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (User user: users)
			builder.append("user(").append(ReviewRequirement.escapeBraces(user.getName())).append(") ");
		for (Map.Entry<Group, Integer> entry: groups.entrySet()) {
			builder.append("group(").append(ReviewRequirement.escapeBraces(entry.getKey().getName())).append(")");
			if (entry.getValue() == 0)
				builder.append(":all");
			else if (entry.getValue() != 1)
				builder.append(":").append(entry.getValue());
			builder.append(" ");
		}
		if (builder.length() != 0)
			return builder.toString().trim();
		else
			return null;
	}
}
