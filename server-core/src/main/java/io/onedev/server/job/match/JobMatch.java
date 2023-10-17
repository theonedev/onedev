package io.onedev.server.job.match;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;
import org.antlr.v4.runtime.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

import static io.onedev.commons.codeassist.AntlrUtils.getLexerRuleName;

public class JobMatch extends Criteria<JobMatchContext> {

	private static final long serialVersionUID = 1L;
	
	private final Criteria<JobMatchContext> criteria;
	
	public JobMatch(Criteria<JobMatchContext> criteria) {
		this.criteria = criteria;
	}

	public static String getValue(String token) {
		return StringUtils.unescape(FenceAware.unfence(token));
	}
	
	public static JobMatch parse(String jobMatchString, boolean withProjectCriteria, boolean withJobCriteria) {
		CharStream is = CharStreams.fromString(jobMatchString); 
		JobMatchLexer lexer = new JobMatchLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new RuntimeException("Malformed job match", e);
			}
			
		});
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JobMatchParser parser = new JobMatchParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		JobMatchParser.JobMatchContext JobMatchContext = parser.jobMatch();

		Criteria<io.onedev.server.job.match.JobMatchContext> criteria = new JobMatchBaseVisitor<Criteria<io.onedev.server.job.match.JobMatchContext>>() {

			@Override
			public Criteria<io.onedev.server.job.match.JobMatchContext> visitParensCriteria(JobMatchParser.ParensCriteriaContext ctx) {
				return visit(ctx.criteria()).withParens(true);
			}

			@Override
			public Criteria<io.onedev.server.job.match.JobMatchContext> visitFieldOperatorValueCriteria(JobMatchParser.FieldOperatorValueCriteriaContext ctx) {
				String fieldName = getValue(ctx.Quoted(0).getText());
				String fieldValue = getValue(ctx.Quoted(1).getText());
				checkField(fieldName, withProjectCriteria, withJobCriteria);

				if (fieldName.equals(Build.NAME_PROJECT)) 
					return new ProjectCriteria(fieldValue);
				else
					return new JobCriteria(fieldValue);
			}
			
			@Override
			public Criteria<io.onedev.server.job.match.JobMatchContext> visitOperatorValueCriteria(JobMatchParser.OperatorValueCriteriaContext ctx) {
				String fieldValue = getValue(ctx.Quoted().getText());
				switch (ctx.operator.getType()) {
					case JobMatchParser.OnBranch:
						return new BranchCriteria(fieldValue);
					case JobMatchParser.SubmittedByGroup:
						var group = OneDev.getInstance(GroupManager.class).find(fieldValue);
						if (group != null)
							return new GroupCriteria(group);
						else
							throw new ExplicitException("Unable to find group: " + fieldValue);
					case JobMatchParser.SubmittedByUser:
						var user = OneDev.getInstance(UserManager.class).findByName(fieldValue);
						if (user != null)
							return new UserCriteria(user);
						else
							throw new ExplicitException("Unable to find user with login: " + fieldValue);
					default:
						throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
				}
			}
			
			@Override
			public Criteria<io.onedev.server.job.match.JobMatchContext> visitOrCriteria(JobMatchParser.OrCriteriaContext ctx) {
				List<Criteria<io.onedev.server.job.match.JobMatchContext>> childCriterias = new ArrayList<>();
				for (JobMatchParser.CriteriaContext childCtx: ctx.criteria())
					childCriterias.add(visit(childCtx));
				return new OrCriteria<>(childCriterias);
			}

			@Override
			public Criteria<io.onedev.server.job.match.JobMatchContext> visitAndCriteria(JobMatchParser.AndCriteriaContext ctx) {
				List<Criteria<io.onedev.server.job.match.JobMatchContext>> childCriterias = new ArrayList<>();
				for (JobMatchParser.CriteriaContext childCtx: ctx.criteria())
					childCriterias.add(visit(childCtx));
				return new AndCriteria<>(childCriterias);
			}

			@Override
			public Criteria<io.onedev.server.job.match.JobMatchContext> visitNotCriteria(JobMatchParser.NotCriteriaContext ctx) {
				return new NotCriteria<>(visit(ctx.criteria()));
			}

		}.visit(JobMatchContext.criteria());
		
		return new JobMatch(criteria);
	}
	
	public static void checkField(String fieldName, boolean withProjectCriteria, boolean withJobCriteria) {
		if (fieldName.equals(Build.NAME_PROJECT)) {
			if (!withProjectCriteria)
				throw new ExplicitException("Project criteria is not supported here");
		} else if (fieldName.equals(Build.NAME_JOB)) {
			if (!withJobCriteria)
				throw new ExplicitException("Job criteria is not supported here");
		} else {
			throw new ExplicitException("Invalid field: " + fieldName);
		}
	}

	@Override
	public boolean matches(JobMatchContext context) {
		return criteria.matches(context);
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<JobMatchContext, JobMatchContext> from,
			CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void onRenameUser(String oldName, String newName) {
		criteria.onRenameUser(oldName, newName);
	}

	@Override
	public void onRenameGroup(String oldName, String newName) {
		criteria.onRenameGroup(oldName, newName);
	}
	
	@Override
	public void onMoveProject(String oldPath, String newPath) {
		criteria.onMoveProject(oldPath, newPath);
	}

	@Override
	public boolean isUsingUser(String userName) {
		return criteria.isUsingUser(userName);
	}

	@Override
	public boolean isUsingGroup(String groupName) {
		return criteria.isUsingGroup(groupName);
	}
	
	@Override
	public boolean isUsingProject(String projectPath) {
		return criteria.isUsingProject(projectPath);
	}
	
	public static String getRuleName(int rule) {
		return getLexerRuleName(JobMatchLexer.ruleNames, rule);
	}
	
	@Override
	public String toStringWithoutParens() {
		return criteria.toString();
	}

}
