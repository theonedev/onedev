package io.onedev.server.util.reviewrequirement;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.util.reviewrequirement.ReviewRequirementParser.CriteriaContext;
import io.onedev.server.util.reviewrequirement.ReviewRequirementParser.RequirementContext;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.TerminalNode;

import javax.annotation.Nullable;
import java.util.*;

public class ReviewRequirement {
	
	private final List<User> users;
	
	private final Map<Group, Integer> groups;
	
	public ReviewRequirement(List<User> users, Map<Group, Integer> groups) {
		this.users = users;
		this.groups = groups;
	}
	
	public static ReviewRequirement parse(@Nullable String requirementString) {
		List<User> users = new ArrayList<>();
		Map<Group, Integer> groups = new LinkedHashMap<>();
		
		if (requirementString != null) {
			CharStream is = CharStreams.fromString(requirementString); 
			ReviewRequirementLexer lexer = new ReviewRequirementLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new RuntimeException("Malformed review requirement");
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ReviewRequirementParser parser = new ReviewRequirementParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			
			RequirementContext requirementContext = parser.requirement();
			
			for (CriteriaContext criteria: requirementContext.criteria()) {
				if (criteria.userCriteria() != null) {
					String userName = getValue(criteria.userCriteria().Value());
					User user = OneDev.getInstance(UserManager.class).findByName(userName);
					if (user != null) {
						if (!users.contains(user)) 
							users.add(user);
						else 
							throw new ExplicitException("User '" + userName + "' is included multiple times");
					} else {
						throw new ExplicitException("Unable to find user '" + userName + "'");
					}
				} else if (criteria.groupCriteria() != null) {
					String groupName = getValue(criteria.groupCriteria().Value());
					Group group = OneDev.getInstance(GroupManager.class).find(groupName);
					if (group != null) {
						if (!groups.containsKey(group)) {
							TerminalNode digit = criteria.groupCriteria().DIGIT();
							if (digit != null) 
								groups.put(group, Integer.parseInt(digit.getText()));
							else 
								groups.put(group, 1);
						} else {
							throw new ExplicitException("Group '" + groupName + "' is included multiple times");
						}
					} else {
						throw new ExplicitException("Unable to find group '" + groupName + "'");
					}
				}
			}			
		}
		
		return new ReviewRequirement(users, groups);
	}

	private static String getValue(TerminalNode terminal) {
		return StringUtils.unescape(FenceAware.unfence(terminal.getText()));
	}
	
	public List<User> getUsers() {
		return users;
	}

	public Map<Group, Integer> getGroups() {
		return groups;
	}
	
	@Nullable
	public static String onRenameGroup(@Nullable String reviewRequirementString, String oldName, String newName) {
		ReviewRequirement reviewRequirement = parse(reviewRequirementString);
		for (Group group: reviewRequirement.getGroups().keySet()) {
			if (group.getName().equals(oldName))
				group.setName(newName);
		}
		return reviewRequirement.toString();
	}

	public static String onRenameUser(@Nullable String reviewRequirementString, String oldName, String newName) {
		ReviewRequirement reviewRequirement = parse(reviewRequirementString);
		for (User user: reviewRequirement.getUsers()) {
			if (user.getName().equals(oldName))
				user.setName(newName);
		}
		return reviewRequirement.toString();
	}
	
	public static boolean isUsingUser(@Nullable String reviewRequirementString, String userName) {
		ReviewRequirement reviewRequirement = parse(reviewRequirementString);
		for (User user: reviewRequirement.getUsers()) {
			if (user.getName().equals(userName))
				return true;
		}
		return false;
	}
	
	public static boolean isUsingGroup(@Nullable String reviewRequirementString, String groupName) {
		ReviewRequirement reviewRequirement = parse(reviewRequirementString);
		for (Group group: reviewRequirement.getGroups().keySet()) {
			if (group.getName().equals(groupName))
				return true;
		}
		return false;
	}
	
	@Nullable
	@Override
	public String toString() {
		List<String> criterias = new ArrayList<>();
		for (var user: getUsers()) 
			criterias.add("user(" + StringUtils.escape(user.getName(), "()") + ")");
		for (var entry: getGroups().entrySet()) {
			StringBuilder builder = new StringBuilder();
			builder.append("group(").append(StringUtils.escape(entry.getKey().getName(), "()")).append(")");
			if (entry.getValue() != 1)
				builder.append(":").append(entry.getValue());
			criterias.add(builder.toString());
		}
		if (!criterias.isEmpty())
			return StringUtils.join(criterias, " and ");
		else
			return null;
	}
	
	public void mergeWith(ReviewRequirement requirement) {
		for (var user: requirement.getUsers()) {
			if (!users.contains(user))
				users.add(user);
		}
		for (var entry: requirement.getGroups().entrySet()) {
			Integer count = groups.get(entry.getKey());
			if (count == null || count < entry.getValue())
				groups.put(entry.getKey(), entry.getValue());
		}
	}
	
	public boolean covers(ReviewRequirement requirement) {
		if (new HashSet<>(getUsers()).containsAll(requirement.getUsers()) 
				&& getGroups().keySet().containsAll(requirement.getGroups().keySet())) {
			for (var entry: getGroups().entrySet()) {
				Integer count = requirement.getGroups().get(entry.getKey());
				if (count != null && count > entry.getValue())
					return false;
			}
			return true;
		} else {
			return false;
		}
	}
	
}
