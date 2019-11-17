package io.onedev.server.ci.job.action.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import io.onedev.commons.codeassist.AntlrUtils;
import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneException;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.action.condition.ActionConditionParser.AndCriteriaContext;
import io.onedev.server.ci.job.action.condition.ActionConditionParser.ConditionContext;
import io.onedev.server.ci.job.action.condition.ActionConditionParser.CriteriaContext;
import io.onedev.server.ci.job.action.condition.ActionConditionParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.ci.job.action.condition.ActionConditionParser.NotCriteriaContext;
import io.onedev.server.ci.job.action.condition.ActionConditionParser.OperatorCriteriaContext;
import io.onedev.server.ci.job.action.condition.ActionConditionParser.OperatorValueCriteriaContext;
import io.onedev.server.ci.job.action.condition.ActionConditionParser.OrCriteriaContext;
import io.onedev.server.ci.job.action.condition.ActionConditionParser.ParensCriteriaContext;
import io.onedev.server.model.Build;
import io.onedev.server.util.BuildConstants;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;

public class ActionCondition implements Predicate<Build> {

	private final Predicate<Build> criteria;
	
	public ActionCondition(Predicate<Build> criteria) {
		this.criteria = criteria;
	}

	public static String getValue(String token) {
		return StringUtils.unescape(FenceAware.unfence(token));
	}
	
	public boolean test(Build build) {
		return criteria.test(build);
	}
	
	public static ActionCondition parse(Job job, String conditionString) {
		CharStream is = CharStreams.fromString(conditionString); 
		ActionConditionLexer lexer = new ActionConditionLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new OneException("Malformed condition syntax", e);
			}
			
		});
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ActionConditionParser parser = new ActionConditionParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		ConditionContext conditionContext;
		try {
			conditionContext = parser.condition();
		} catch (Exception e) {
			if (e instanceof OneException)
				throw e;
			else
				throw new OneException("Malformed condition syntax", e);
		}

		Predicate<Build> criteria;
		
		if (conditionContext.Always() != null) {
			criteria = new AlwaysCriteria();
		} else {
			criteria = new ActionConditionBaseVisitor<Predicate<Build>>() {
	
				@Override
				public Predicate<Build> visitParensCriteria(ParensCriteriaContext ctx) {
					return visit(ctx.criteria());
				}
	
				@Override
				public Predicate<Build> visitOperatorCriteria(OperatorCriteriaContext ctx) {
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
					case ActionConditionLexer.AssociatedWithPullRequests:
						return new AssociatedWithPullRequestsCriteria();
					case ActionConditionLexer.RequiredByPullRequests:
						return new RequiredByPullRequestsCriteria();
					default:
						throw new OneException("Unexpected operator: " + ctx.operator.getText());
					}
				}
				
				@Override
				public Predicate<Build> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
					String fieldName = getValue(ctx.Quoted(0).getText());
					String fieldValue = getValue(ctx.Quoted(1).getText());
					int operator = ctx.operator.getType();
					checkField(job, fieldName, operator);
					
					switch (fieldName) {
					case BuildConstants.FIELD_LOG:
						return new LogCriteria(fieldValue);
					case BuildConstants.FIELD_ERROR_MESSAGE:
						return new ErrorMessageCriteria(fieldValue);
					default:
						return new ParamCriteria(fieldName, fieldValue);
					}
				}
				
				@Override
				public Predicate<Build> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
					String fieldValue = getValue(ctx.Quoted().getText());
					return new OnBranchCriteria(fieldValue);
				}
				
				@Override
				public Predicate<Build> visitOrCriteria(OrCriteriaContext ctx) {
					List<Predicate<Build>> childCriterias = new ArrayList<>();
					for (CriteriaContext childCtx: ctx.criteria())
						childCriterias.add(visit(childCtx));
					return new OrCriteria<Build>(childCriterias);
				}
	
				@Override
				public Predicate<Build> visitAndCriteria(AndCriteriaContext ctx) {
					List<Predicate<Build>> childCriterias = new ArrayList<>();
					for (CriteriaContext childCtx: ctx.criteria())
						childCriterias.add(visit(childCtx));
					return new AndCriteria<Build>(childCriterias);
				}
	
				@Override
				public Predicate<Build> visitNotCriteria(NotCriteriaContext ctx) {
					return new NotCriteria<Build>(visit(ctx.criteria()));
				}
	
			}.visit(conditionContext.criteria());
		}
		return new ActionCondition(criteria);
	}
	
	public static void checkField(Job job, String fieldName, int operator) {
		if (fieldName.equals(BuildConstants.FIELD_ERROR_MESSAGE) || fieldName.equals(BuildConstants.FIELD_LOG)) {
			if (operator != ActionConditionLexer.Contains)
				throw newOperatorException(fieldName, operator);
		} else if (job.getParamSpecMap().containsKey(fieldName)) {
			if (operator != ActionConditionLexer.Is)
				throw newOperatorException(fieldName, operator);
		} else {
			throw new OneException("Param not found: " + fieldName);
		}
	}
	
	private static OneException newOperatorException(String fieldName, int operator) {
		return new OneException("Field '" + fieldName + "' is not applicable for operator '" 
				+ AntlrUtils.getLexerRuleName(ActionConditionLexer.ruleNames, operator) + "'");
	}
	
}
