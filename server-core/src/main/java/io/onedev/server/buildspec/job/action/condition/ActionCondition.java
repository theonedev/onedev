package io.onedev.server.buildspec.job.action.condition;

import java.util.ArrayList;
import java.util.List;

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
					if (fieldName.equals(Build.NAME_PULL_REQUEST))
						return new PullRequestIsEmptyCriteria();
					else
						return new ParamIsEmptyCriteria(fieldName);
				}
				
				@Override
				public Criteria<Build> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
					String fieldName = getValue(ctx.Quoted(0).getText());
					String fieldValue = getValue(ctx.Quoted(1).getText());
					int operator = ctx.operator.getType();
					checkField(job, fieldName, operator);
					
					switch (fieldName) {
					case Build.NAME_LOG:
						return new LogCriteria(fieldValue);
					case Build.NAME_ERROR_MESSAGE:
						return new ErrorMessageCriteria(fieldValue);
					default:
						return new ParamCriteria(fieldName, fieldValue);
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
					return new OrCriteria<Build>(childCriterias);
				}
	
				@Override
				public Criteria<Build> visitAndCriteria(AndCriteriaContext ctx) {
					List<Criteria<Build>> childCriterias = new ArrayList<>();
					for (CriteriaContext childCtx: ctx.criteria())
						childCriterias.add(visit(childCtx));
					return new AndCriteria<Build>(childCriterias);
				}
	
				@Override
				public Criteria<Build> visitNotCriteria(NotCriteriaContext ctx) {
					return new NotCriteria<Build>(visit(ctx.criteria()));
				}
	
			}.visit(conditionContext.criteria());
		}
		return new ActionCondition(criteria);
	}
	
	public static void checkField(Job job, String fieldName, int operator) {
		if (fieldName.equals(Build.NAME_ERROR_MESSAGE) || fieldName.equals(Build.NAME_LOG)) {
			if (operator != ActionConditionLexer.Contains)
				throw newOperatorException(fieldName, operator);
		} else if (fieldName.equals(Build.NAME_PULL_REQUEST)) {
			if (operator != ActionConditionLexer.IsEmpty)
				throw newOperatorException(fieldName, operator);
		} else if (job.getParamSpecMap().containsKey(fieldName)) {
			if (operator != ActionConditionLexer.IsEmpty && operator != ActionConditionLexer.Is)
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
	
}
