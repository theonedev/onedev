package io.onedev.server.job.match;

import static io.onedev.commons.codeassist.AntlrUtils.getLexerRuleName;
import static io.onedev.server.job.match.JobMatchParser.IsNot;
import static io.onedev.server.job.match.JobMatchParser.OnBranch;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Build;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;

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
				int operator = ctx.operator.getType();
				String fieldName = getValue(ctx.criteriaField.getText());
				checkField(fieldName, withProjectCriteria, withJobCriteria);
				
				var criterias = new ArrayList<Criteria<io.onedev.server.job.match.JobMatchContext>>();
				for (var quoted: ctx.criteriaValue.Quoted()) {
					String fieldValue = getValue(quoted.getText());

					if (fieldName.equals(Build.NAME_PROJECT))
						criterias.add(new ProjectCriteria(fieldValue, operator));
					else
						criterias.add(new JobCriteria(fieldValue, operator));
				}
				return operator == IsNot? Criteria.andCriterias(criterias): Criteria.orCriterias(criterias);
			}
			
			@Override
			public Criteria<io.onedev.server.job.match.JobMatchContext> visitOperatorValueCriteria(JobMatchParser.OperatorValueCriteriaContext ctx) {
				var criterias = new ArrayList<Criteria<io.onedev.server.job.match.JobMatchContext>>();
				for (var quoted: ctx.criteriaValue.Quoted()) {
					String fieldValue = getValue(quoted.getText());
					switch (ctx.operator.getType()) {
						case OnBranch:
							criterias.add(new OnBranchCriteria(fieldValue));
							break;
						default:
							throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
					}
				}
				return Criteria.orCriterias(criterias);
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
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<JobMatchContext, JobMatchContext> from,
			CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
		
	@Override
	public void onMoveProject(String oldPath, String newPath) {
		criteria.onMoveProject(oldPath, newPath);
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
