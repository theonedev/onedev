package io.onedev.server.ci.job.retry;

import java.io.Serializable;
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
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneException;
import io.onedev.server.ci.job.retry.RetryConditionParser.AndCriteriaContext;
import io.onedev.server.ci.job.retry.RetryConditionParser.ConditionContext;
import io.onedev.server.ci.job.retry.RetryConditionParser.CriteriaContext;
import io.onedev.server.ci.job.retry.RetryConditionParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.ci.job.retry.RetryConditionParser.NotCriteriaContext;
import io.onedev.server.ci.job.retry.RetryConditionParser.OrCriteriaContext;
import io.onedev.server.ci.job.retry.RetryConditionParser.ParensCriteriaContext;
import io.onedev.server.model.Build;

public class RetryCondition implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String FIELD_LOG = "Log";
	
	public static final String FIELD_ERROR_MESSAGE = "Error Message";

	private final Criteria criteria;
	
	public RetryCondition(Criteria criteria) {
		this.criteria = criteria;
	}

	private static String getValue(String token) {
		return StringUtils.unescape(FenceAware.unfence(token));
	}
	
	public boolean satisfied(Build build) {
		return criteria.satisfied(build);
	}
	
	public static RetryCondition parse(String conditionString) {
		CharStream is = CharStreams.fromString(conditionString); 
		RetryConditionLexer lexer = new RetryConditionLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new OneException("Malformed condition syntax", e);
			}
			
		});
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		RetryConditionParser parser = new RetryConditionParser(tokens);
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

		Criteria criteria = new RetryConditionBaseVisitor<Criteria>() {

			@Override
			public Criteria visitParensCriteria(ParensCriteriaContext ctx) {
				return visit(ctx.criteria());
			}

			@Override
			public Criteria visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
				String fieldName = getValue(ctx.Quoted(0).getText());
				String value = getValue(ctx.Quoted(1).getText());
				int operator = ctx.operator.getType();
				checkField(fieldName, operator);
				
				switch (operator) {
				case RetryConditionLexer.Contains:
					switch (fieldName) {
					case FIELD_LOG:
						return new LogCriteria(value);
					case FIELD_ERROR_MESSAGE:
						return new ErrorMessageCriteria(value);
					default:
						throw new IllegalStateException();
					}
				default:
					throw new IllegalStateException();
				}
			}
			
			@Override
			public Criteria visitOrCriteria(OrCriteriaContext ctx) {
				List<Criteria> childCriterias = new ArrayList<>();
				for (CriteriaContext childCtx: ctx.criteria())
					childCriterias.add(visit(childCtx));
				return new OrCriteria(childCriterias);
			}

			@Override
			public Criteria visitAndCriteria(AndCriteriaContext ctx) {
				List<Criteria> childCriterias = new ArrayList<>();
				for (CriteriaContext childCtx: ctx.criteria())
					childCriterias.add(visit(childCtx));
				return new AndCriteria(childCriterias);
			}

			@Override
			public Criteria visitNotCriteria(NotCriteriaContext ctx) {
				return new NotCriteria(visit(ctx.criteria()));
			}

		}.visit(conditionContext.criteria());

		return new RetryCondition(criteria);
	}
	
	public static void checkField(String fieldName, int operator) {
		if (!fieldName.equals(FIELD_ERROR_MESSAGE) && !fieldName.equals(FIELD_LOG))
			throw new OneException("Field not found: " + fieldName);
		switch (operator) {
		case RetryConditionLexer.Contains:
			if (!fieldName.equals(FIELD_LOG) && !fieldName.equals(FIELD_ERROR_MESSAGE))
				throw newOperatorException(fieldName, operator);
			break;
		}
	}
	
	private static OneException newOperatorException(String fieldName, int operator) {
		return new OneException("Field '" + fieldName + "' is not applicable for operator '" 
				+ AntlrUtils.getLexerRuleName(RetryConditionLexer.ruleNames, operator) + "'");
	}
		
}
