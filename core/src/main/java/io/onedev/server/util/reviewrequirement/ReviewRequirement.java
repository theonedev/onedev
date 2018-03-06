package io.onedev.server.util.reviewrequirement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.onedev.server.util.reviewrequirement.ReviewRequirementSpecLexer;
import io.onedev.server.util.reviewrequirement.ReviewRequirementSpecParser;
import io.onedev.server.util.reviewrequirement.ReviewRequirementSpecParser.CountContext;
import io.onedev.server.util.reviewrequirement.ReviewRequirementSpecParser.CriteriaContext;
import io.onedev.server.util.reviewrequirement.ReviewRequirementSpecParser.SpecContext;

import io.onedev.server.OneDev;
import io.onedev.server.manager.GroupManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.User;

public class ReviewRequirement {
	
	private final List<User> users = new ArrayList<>();
	
	private final Map<Group, Integer> groups = new LinkedHashMap<>();
	
	public ReviewRequirement(String spec) {
		SpecContext specContext = parse(spec);
		
		for (CriteriaContext criteriaContext: specContext.criteria()) {
			if (criteriaContext.userCriteria() != null) {
				String userName = getBracedValue(criteriaContext.userCriteria().Value());
				User user = OneDev.getInstance(UserManager.class).findByName(userName);
				if (user != null) {
					if (!users.contains(user)) { 
						users.add(user);
					} else {
						throw new InvalidReviewRuleException("User '" + userName + "' is included multiple times");
					}
				} else {
					throw new InvalidReviewRuleException("Unable to find user '" + userName + "'");
				}
			} else if (criteriaContext.groupCriteria() != null) {
				String groupName = getBracedValue(criteriaContext.groupCriteria().Value());
				Group group = OneDev.getInstance(GroupManager.class).find(groupName);
				if (group != null) {
					if (!groups.containsKey(group)) {
						CountContext countContext = criteriaContext.groupCriteria().count();
						if (countContext != null) {
							if (countContext.DIGIT() != null)
								groups.put(group, Integer.parseInt(countContext.DIGIT().getText()));
							else
								groups.put(group, 0);
						} else {
							groups.put(group, 1);
						}
					} else {
						throw new InvalidReviewRuleException("Group '" + groupName + "' is included multiple times");
					}
				} else {
					throw new InvalidReviewRuleException("Unable to find group '" + groupName + "'");
				}
			}
		}
	}

	public static SpecContext parse(String spec) {
		ANTLRInputStream is = new ANTLRInputStream(spec); 
		ReviewRequirementSpecLexer lexer = new ReviewRequirementSpecLexer(is);
		lexer.removeErrorListeners();
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ReviewRequirementSpecParser parser = new ReviewRequirementSpecParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		return parser.spec();
	}
	
	private String getBracedValue(TerminalNode terminal) {
		String value = terminal.getText().substring(1);
		return value.substring(0, value.length()-1).trim();
	}
	
	public List<User> getUsers() {
		return users;
	}

	public Map<Group, Integer> getGroups() {
		return groups;
	}
	
	public boolean matches(User user) {
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
	public String toSpec() {
		StringBuilder builder = new StringBuilder();
		for (User user: users)
			builder.append("user(").append(user.getName()).append(") ");
		for (Map.Entry<Group, Integer> entry: groups.entrySet()) {
			builder.append("group(").append(entry.getKey().getName()).append(")");
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
