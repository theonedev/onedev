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
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.manager.TeamManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.Team;
import com.gitplex.server.util.reviewappointment.ReviewAppointmentParser.CountContext;
import com.gitplex.server.util.reviewappointment.ReviewAppointmentParser.CriteriaContext;
import com.gitplex.server.util.reviewappointment.ReviewAppointmentParser.ExprContext;

public class ReviewAppointment {
	
	private final List<Account> users = new ArrayList<>();
	
	private final Map<Team, Integer> teams = new LinkedHashMap<>();
	
	public ReviewAppointment(Depot depot, String expr) {
		ExprContext exprContext = parse(expr);
		
		for (CriteriaContext criteriaContext: exprContext.criteria()) {
			if (criteriaContext.userCriteria() != null) {
				String userName = getBracedValue(criteriaContext.userCriteria().Value());
				Account user = GitPlex.getInstance(AccountManager.class).findByName(userName);
				if (user != null) {
					users.add(user);
				}
			} else if (criteriaContext.teamCriteria() != null) {
				String teamName = getBracedValue(criteriaContext.teamCriteria().Value());
				Team team = GitPlex.getInstance(TeamManager.class).find(depot.getAccount(), teamName);
				if (team != null) {
					CountContext countContext = criteriaContext.teamCriteria().count();
					if (countContext != null) {
						if (countContext.DIGIT() != null)
							teams.put(team, Integer.parseInt(countContext.DIGIT().getText()));
						else
							teams.put(team, 0);
					} else {
						teams.put(team, 1);
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
	
	public List<Account> getUsers() {
		return users;
	}

	public Map<Team, Integer> getTeams() {
		return teams;
	}
	
	public boolean matches(Account user) {
		for (Account eachUser: users) {
			if (!eachUser.equals(user))
				return false;
		}
		for (Map.Entry<Team, Integer> entry: teams.entrySet()) {
			Team team = entry.getKey();
			int requiredCount = entry.getValue();
			if (requiredCount == 0 || requiredCount > team.getMembers().size())
				requiredCount = team.getMembers().size();

			if (requiredCount > 1 || requiredCount == 1 && !team.getMembers().contains(user))
				return false;
		}
		return true;
	}
	
	@Nullable
	public String toExpr() {
		StringBuilder builder = new StringBuilder();
		for (Account user: users)
			builder.append("user(").append(user.getName()).append(") ");
		for (Map.Entry<Team, Integer> entry: teams.entrySet()) {
			builder.append("team(").append(entry.getKey().getName()).append(")");
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
