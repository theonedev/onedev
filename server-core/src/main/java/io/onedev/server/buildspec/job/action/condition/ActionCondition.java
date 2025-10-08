package io.onedev.server.buildspec.job.action.condition;

import static io.onedev.server.buildspec.job.action.condition.ActionConditionLexer.Contains;
import static io.onedev.server.buildspec.job.action.condition.ActionConditionLexer.Is;
import static io.onedev.server.buildspec.job.action.condition.ActionConditionLexer.IsEmpty;
import static io.onedev.server.buildspec.job.action.condition.ActionConditionLexer.IsNot;
import static io.onedev.server.buildspec.job.action.condition.ActionConditionLexer.IsNotEmpty;
import static io.onedev.server.model.Build.NAME_BRANCH;
import static io.onedev.server.model.Build.NAME_LOG;
import static io.onedev.server.model.Build.NAME_PROJECT;
import static io.onedev.server.model.Build.NAME_PULL_REQUEST;
import static io.onedev.server.model.Build.NAME_TAG;

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

import io.onedev.commons.codeassist.AntlrUtils;
import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.action.condition.ActionConditionParser.AndCriteriaContext;
import io.onedev.server.buildspec.job.action.condition.ActionConditionParser.ConditionContext;
import io.onedev.server.buildspec.job.action.condition.ActionConditionParser.CriteriaContext;
import io.onedev.server.buildspec.job.action.condition.ActionConditionParser.FieldOperatorCriteriaContext;
import io.onedev.server.buildspec.job.action.condition.ActionConditionParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.buildspec.job.action.condition.ActionConditionParser.NotCriteriaContext;
import io.onedev.server.buildspec.job.action.condition.ActionConditionParser.OperatorCriteriaContext;
import io.onedev.server.buildspec.job.action.condition.ActionConditionParser.OperatorValueCriteriaContext;
import io.onedev.server.buildspec.job.action.condition.ActionConditionParser.OrCriteriaContext;
import io.onedev.server.buildspec.job.action.condition.ActionConditionParser.ParensCriteriaContext;
import io.onedev.server.model.Build;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;

public class ActionCondition extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private final Criteria<Build> criteria;
	
	public ActionCondition(Criteria<Build> criteria) {
		this.criteria = criteria;
	}

	public static String getValue(String token) {
		return StringUtils.unescape(FenceAware.unfence(token));
	}
	
	@Override
	public boolean matches(Build build) {
		return criteria.matches(build);
	}
	
	public static ActionCondition parse(Job job, String conditionString) {
		CharStream is = CharStreams.fromString(conditionString); 
		ActionConditionLexer lexer = new ActionConditionLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new RuntimeException("Malformed action condition", e);
			}
			
		});
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ActionConditionParser parser = new ActionConditionParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		ConditionContext conditionContext = parser.condition();

		Criteria<Build> criteria;
		
		if (conditionContext.Always() != null) {
			criteria = new AlwaysCriteria();
		} else {
			criteria = new ActionConditionBaseVisitor<Criteria<Build>>() {
	
				@Override
				public Criteria<Build> visitParensCriteria(ParensCriteriaContext ctx) {
					return visit(ctx.criteria()).withParens(true);
				}
	
				@Override
				public Criteria<Build> visitOperatorCriteria(OperatorCriteriaContext ctx) {
					switch (ctx.operator.getType()) {
					case ActionConditionLexer.Successful:
						return new SuccessfulCriteria();
					case ActionConditionLexer.Failed:
						return new FailedCriteria();
					case ActionConditionLexer.Cancelled:
						return new CancelledCriteria();
					case ActionConditionLexer.TimedOut:
						return new TimedOutCriteria();
					case ActionConditionLexer.PreviousIsSuccessful:
						return new PreviousIsSuccessfulCriteria();
					case ActionConditionLexer.PreviousIsFailed:
						return new PreviousIsFailedCriteria();
					case ActionConditionLexer.PreviousIsCancelled:
						return new PreviousIsCancelledCriteria();
					case ActionConditionLexer.PreviousIsTimedOut:
						return new PreviousIsTimedOutCriteria();
					default:
						throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
					}
				}
				
				@Override
				public Criteria<Build> visitFieldOperatorCriteria(FieldOperatorCriteriaContext ctx) {
					String fieldName = getValue(ctx.Quoted().getText());
					int operator = ctx.operator.getType();
					checkField(job, fieldName, operator);
					if (fieldName.equals(NAME_BRANCH))
						return new BranchEmptyCriteria(operator);
					else if (fieldName.equals(NAME_TAG))
						return new TagEmptyCriteria(operator);
					else if (fieldName.equals(NAME_PULL_REQUEST))
						return new PullRequestEmptyCriteria(operator);
					else
						return new ParamEmptyCriteria(fieldName, operator);
				}
				
				@Override
				public Criteria<Build> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
					String fieldName = getValue(ctx.Quoted(0).getText());
					String fieldValue = getValue(ctx.Quoted(1).getText());
					int operator = ctx.operator.getType();
					checkField(job, fieldName, operator);
					
					switch (fieldName) {
						case NAME_PROJECT:
							return new ProjectCriteria(fieldValue, operator);
						case NAME_BRANCH:
							return new BranchCriteria(fieldValue, operator);
						case NAME_TAG:
							return new TagCriteria(fieldValue, operator);
						case NAME_LOG:
							return new LogCriteria(fieldValue);
						default:
							return new ParamCriteria(fieldName, fieldValue, operator);
					}
				}
				
				@Override
				public Criteria<Build> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
					String fieldValue = getValue(ctx.Quoted().getText());
					return new OnBranchCriteria(fieldValue);
				}
				
				@Override
				public Criteria<Build> visitOrCriteria(OrCriteriaContext ctx) {
					List<Criteria<Build>> childCriterias = new ArrayList<>();
					for (CriteriaContext childCtx: ctx.criteria())
						childCriterias.add(visit(childCtx));
					return new OrCriteria<>(childCriterias);
				}
	
				@Override
				public Criteria<Build> visitAndCriteria(AndCriteriaContext ctx) {
					List<Criteria<Build>> childCriterias = new ArrayList<>();
					for (CriteriaContext childCtx: ctx.criteria())
						childCriterias.add(visit(childCtx));
					return new AndCriteria<>(childCriterias);
				}
	
				@Override
				public Criteria<Build> visitNotCriteria(NotCriteriaContext ctx) {
					return new NotCriteria<>(visit(ctx.criteria()));
				}
	
			}.visit(conditionContext.criteria());
		}
		return new ActionCondition(criteria);
	}
	
	public static void checkField(Job job, String fieldName, int operator) {
		if (fieldName.equals(NAME_PROJECT)) {
			if (operator != Is && operator != IsNot)
				throw newOperatorException(fieldName, operator);
		} else if (fieldName.equals(NAME_BRANCH) || fieldName.equals(NAME_TAG)) {
			if (operator != Is && operator != IsNot && operator != IsEmpty && operator != IsNotEmpty)
				throw newOperatorException(fieldName, operator);
		} else if (fieldName.equals(NAME_PULL_REQUEST)) {
			if (operator != IsEmpty && operator != IsNotEmpty)
				throw newOperatorException(fieldName, operator);
		} else if (fieldName.equals(NAME_LOG)) {
			if (operator != Contains)
				throw newOperatorException(fieldName, operator);
		} else if (job.getParamSpecMap().containsKey(fieldName)) {
			if (operator != Is && operator != IsNot && operator != IsEmpty && operator != IsNotEmpty)
				throw newOperatorException(fieldName, operator);
		} else {
			throw new ExplicitException("Param not found: " + fieldName);
		}
	}
	
	private static ExplicitException newOperatorException(String fieldName, int operator) {
		return new ExplicitException("Field '" + fieldName + "' is not applicable for operator '" 
				+ AntlrUtils.getLexerRuleName(ActionConditionLexer.ruleNames, operator) + "'");
	}

	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(ActionConditionLexer.ruleNames, rule);
	}
	
	@Override
	public String toStringWithoutParens() {
		return criteria.toStringWithoutParens();
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
}
