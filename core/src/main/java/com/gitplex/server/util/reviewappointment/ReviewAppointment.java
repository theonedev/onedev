package com.gitplex.server.util.reviewappointment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.model.User;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.Group;
import com.gitplex.server.util.reviewappointment.ReviewAppointmentParser.CountContext;
import com.gitplex.server.util.reviewappointment.ReviewAppointmentParser.CriteriaContext;
import com.gitplex.server.util.reviewappointment.ReviewAppointmentParser.ExprContext;

public class ReviewAppointment {
	
	private final List<User> users = new ArrayList<>();
	
	private final Map<Group, Integer> groups = new LinkedHashMap<>();
	
	public ReviewAppointment(Project project, String expr) {
		ExprContext exprContext = parse(expr);
		
		for (CriteriaContext criteriaContext: exprContext.criteria()) {
			if (criteriaContext.userCriteria() != null) {
				String userName = getBracedValue(criteriaContext.userCriteria().Value());
				User user = GitPlex.getInstance(UserManager.class).findByName(userName);
				if (user != null) {
					users.add(user);
				}
			} else if (criteriaContext.groupCriteria() != null) {
				String groupName = getBracedValue(criteriaContext.groupCriteria().Value());
				Group group = GitPlex.getInstance(GroupManager.class).find(groupName);
				if (group != null) {
					CountContext countContext = criteriaContext.groupCriteria().count();
					if (countContext != null) {
						if (countContext.DIGIT() != null)
							groups.put(group, Integer.parseInt(countContext.DIGIT().getText()));
						else
							groups.put(group, 0);
					} else {
						groups.put(group, 1);
					}
				}
			}
		}
	}

	public static ExprContext parse(String expr) {
		ANTLRInputStream is = new ANTLRInputStream(expr); 
		ReviewAppointmentLexer lexer = new ReviewAppointmentLexer(is);
		lexer.removeErrorListeners();
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ReviewAppointmentParser parser = new ReviewAppointmentParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		return parser.expr();
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
	public String toExpr() {
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
