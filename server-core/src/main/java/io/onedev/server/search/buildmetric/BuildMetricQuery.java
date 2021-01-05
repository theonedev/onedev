package io.onedev.server.search.buildmetric;

import static io.onedev.server.model.Build.METRIC_QUERY_FIELDS;
import static io.onedev.server.model.Build.NAME_BRANCH;
import static io.onedev.server.model.Build.NAME_JOB;
import static io.onedev.server.model.Build.NAME_PULL_REQUEST;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import io.onedev.commons.codeassist.AntlrUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.BuildMetric;
import io.onedev.server.search.buildmetric.BuildMetricQueryParser.AndCriteriaContext;
import io.onedev.server.search.buildmetric.BuildMetricQueryParser.CriteriaContext;
import io.onedev.server.search.buildmetric.BuildMetricQueryParser.FieldOperatorCriteriaContext;
import io.onedev.server.search.buildmetric.BuildMetricQueryParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.search.buildmetric.BuildMetricQueryParser.NotCriteriaContext;
import io.onedev.server.search.buildmetric.BuildMetricQueryParser.OperatorCriteriaContext;
import io.onedev.server.search.buildmetric.BuildMetricQueryParser.OperatorValueCriteriaContext;
import io.onedev.server.search.buildmetric.BuildMetricQueryParser.OrCriteriaContext;
import io.onedev.server.search.buildmetric.BuildMetricQueryParser.ParensCriteriaContext;
import io.onedev.server.search.buildmetric.BuildMetricQueryParser.QueryContext;
import io.onedev.server.search.entity.EntityQuery;

public class BuildMetricQuery implements Serializable {

	private static final long serialVersionUID = 1L;

	private final BuildMetricCriteria criteria;
	
	public BuildMetricQuery(@Nullable BuildMetricCriteria criteria) {
		this.criteria = criteria;
	}

	public BuildMetricQuery() {
		this(null);
	}
	
	public static BuildMetricQuery parse(Project project, @Nullable String queryString) {
		if (queryString != null) {
			CharStream is = CharStreams.fromString(queryString); 
			BuildMetricQueryLexer lexer = new BuildMetricQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new RuntimeException("Malformed metric query", e);
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			BuildMetricQueryParser parser = new BuildMetricQueryParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			QueryContext queryContext = parser.query();
			CriteriaContext criteriaContext = queryContext.criteria();
			BuildMetricCriteria metricCriteria;
			if (criteriaContext != null) {
				metricCriteria = new BuildMetricQueryBaseVisitor<BuildMetricCriteria>() {

					@Override
					public BuildMetricCriteria visitOperatorCriteria(OperatorCriteriaContext ctx) {
						switch (ctx.operator.getType()) {
						case BuildMetricQueryLexer.BuildIsSuccessful:
							return new BuildIsSuccessfulCriteria();
						case BuildMetricQueryLexer.BuildIsFailed:
							return new BuildIsFailedCriteria();
						default:
							throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
					}
					
					@Override
					public BuildMetricCriteria visitParensCriteria(ParensCriteriaContext ctx) {
						return (BuildMetricCriteria) visit(ctx.criteria()).withParens(true);
					}

					@Override
					public BuildMetricCriteria visitFieldOperatorCriteria(FieldOperatorCriteriaContext ctx) {
						String fieldName = EntityQuery.getValue(ctx.Quoted().getText());
						int operator = ctx.operator.getType();
						checkField(project, fieldName, operator);
						if (fieldName.equals(NAME_PULL_REQUEST))
							return new PullRequestIsEmptyCriteria();
						else
							return new ParamIsEmptyCriteria(fieldName);
					}
					
					@Override
					public BuildMetricCriteria visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = EntityQuery.getValue(ctx.Quoted(0).getText());
						String value = EntityQuery.getValue(ctx.Quoted(1).getText());
						int operator = ctx.operator.getType();
						checkField(project, fieldName, operator);
						
						switch (operator) {
						case BuildMetricQueryLexer.Is:
							switch (fieldName) {
							case BuildMetric.NAME_REPORT:
								return new ReportCriteria(value);
							case NAME_JOB:
								return new JobCriteria(value);
							case NAME_BRANCH:
								return new BranchCriteria(value);
							default: 
								return new ParamCriteria(fieldName, value);
							}
						default:
							throw new IllegalStateException();
						}
					}
					
					@Override
					public BuildMetricCriteria visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						int operator = ctx.operator.getType();
						String value = EntityQuery.getValue(ctx.Quoted().getText());
						if (operator == BuildMetricQueryLexer.Since || operator == BuildMetricQueryLexer.Until)
							return new DateCriteria(value, operator);
						else 
							throw new RuntimeException("Unexpected criteria: " + ctx.operator.getText());
					}

					@Override
					public BuildMetricCriteria visitOrCriteria(OrCriteriaContext ctx) {
						List<BuildMetricCriteria> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrBuildMetricCriteria(childCriterias);
					}

					@Override
					public BuildMetricCriteria visitAndCriteria(AndCriteriaContext ctx) {
						List<BuildMetricCriteria> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndBuildMetricCriteria(childCriterias);
					}

					@Override
					public BuildMetricCriteria visitNotCriteria(NotCriteriaContext ctx) {
						return new NotBuildMetricCriteria(visit(ctx.criteria()));
					}

				}.visit(criteriaContext);
			} else {
				metricCriteria = null;
			}

			return new BuildMetricQuery(metricCriteria);
		} else {
			return new BuildMetricQuery();
		}
	}
	
	public static void checkField(Project project, String fieldName, int operator) {
		Collection<String> paramNames = OneDev.getInstance(BuildParamManager.class).getBuildParamNames(null);
		if (!METRIC_QUERY_FIELDS.contains(fieldName) && !paramNames.contains(fieldName)) 
			throw new ExplicitException("Field not found: " + fieldName);
		switch (operator) {
		case BuildMetricQueryLexer.Is:
			if (!fieldName.equals(NAME_JOB) && !fieldName.equals(NAME_BRANCH) 
					&& !fieldName.equals(BuildMetric.NAME_REPORT) 
					&& !paramNames.contains(fieldName)) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		case BuildMetricQueryLexer.IsEmpty:
			if (!fieldName.equals(NAME_PULL_REQUEST) && !paramNames.contains(fieldName)) 
				throw newOperatorException(fieldName, operator);
			break;
		}
	}
	
	private static ExplicitException newOperatorException(String fieldName, int operator) {
		return new ExplicitException("Field '" + fieldName + "' is not applicable for operator '" + getRuleName(operator) + "'");
	}
	
	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(BuildMetricQueryLexer.ruleNames, rule);
	}
	
	public static int getOperator(String operatorName) {
		return AntlrUtils.getLexerRule(BuildMetricQueryLexer.ruleNames, operatorName);
	}
	
	@Nullable
	public BuildMetricCriteria getCriteria() {
		return criteria;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (getCriteria() != null) 
			builder.append(getCriteria().toString());
		String toStringValue = builder.toString();
		if (toStringValue.length() == 0)
			toStringValue = null;
		return toStringValue;
	}
	
}
