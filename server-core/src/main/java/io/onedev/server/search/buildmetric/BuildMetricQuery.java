package io.onedev.server.search.buildmetric;

import io.onedev.commons.codeassist.AntlrUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.service.BuildParamService;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.BuildMetric;
import io.onedev.server.search.entity.EntityQuery;
import org.antlr.v4.runtime.*;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.onedev.server.model.Build.*;
import static io.onedev.server.search.buildmetric.BuildMetricQueryParser.*;

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
					throw new RuntimeException("Malformed query", e);
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
						case BuildIsSuccessful:
							return new BuildIsSuccessfulCriteria();
						case BuildIsFailed:
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
							return new PullRequestEmptyCriteria(operator);
						else
							return new ParamEmptyCriteria(fieldName, operator);
					}
					
					@Override
					public BuildMetricCriteria visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = EntityQuery.getValue(ctx.criteriaField.getText());
						int operator = ctx.operator.getType();
						checkField(project, fieldName, operator);
						
						var criterias = new ArrayList<BuildMetricCriteria>();
						for (var quoted: ctx.criteriaValue.Quoted()) {
							String value = EntityQuery.getValue(quoted.getText());
							if (operator == Is || operator == IsNot) {
								switch (fieldName) {
									case BuildMetric.NAME_REPORT:
										criterias.add(new ReportCriteria(value, operator));
										break;
									case NAME_JOB:
										criterias.add(new JobCriteria(value, operator));
										break;
									case NAME_BRANCH:
										criterias.add(new BranchCriteria(value, operator));
										break;
									default:
										criterias.add(new ParamCriteria(fieldName, value, operator));
								}
							} else {
								throw new IllegalStateException();
							}
						}
						return operator==IsNot? new AndBuildMetricCriteria(criterias): new OrBuildMetricCriteria(criterias);
					}
					
					@Override
					public BuildMetricCriteria visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						var criterias = new ArrayList<BuildMetricCriteria>();
						for (var quoted: ctx.criteriaValue.Quoted()) {
							int operator = ctx.operator.getType();
							String value = EntityQuery.getValue(quoted.getText());
							if (operator == Since || operator == Until)
								criterias.add(new DateCriteria(value, operator));
							else
								throw new RuntimeException("Unexpected criteria: " + ctx.operator.getText());
						}
						return new OrBuildMetricCriteria(criterias);
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
		Collection<String> paramNames = OneDev.getInstance(BuildParamService.class).getParamNames(null);
		if (!METRIC_QUERY_FIELDS.contains(fieldName) && !paramNames.contains(fieldName)) 
			throw new ExplicitException("Field not found: " + fieldName);
		switch (operator) {
			case Is:
			case IsNot:
				if (!fieldName.equals(NAME_JOB) && !fieldName.equals(NAME_BRANCH)
						&& !fieldName.equals(BuildMetric.NAME_REPORT)
						&& !paramNames.contains(fieldName)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case IsEmpty:
			case IsNotEmpty:
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
