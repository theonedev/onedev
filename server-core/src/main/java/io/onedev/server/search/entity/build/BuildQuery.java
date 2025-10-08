package io.onedev.server.search.entity.build;

import static io.onedev.server.model.AbstractEntity.NAME_NUMBER;
import static io.onedev.server.model.Build.NAME_BRANCH;
import static io.onedev.server.model.Build.NAME_COMMIT;
import static io.onedev.server.model.Build.NAME_FINISH_DATE;
import static io.onedev.server.model.Build.NAME_JOB;
import static io.onedev.server.model.Build.NAME_LABEL;
import static io.onedev.server.model.Build.NAME_PENDING_DATE;
import static io.onedev.server.model.Build.NAME_PROJECT;
import static io.onedev.server.model.Build.NAME_PULL_REQUEST;
import static io.onedev.server.model.Build.NAME_RUNNING_DATE;
import static io.onedev.server.model.Build.NAME_STATUS;
import static io.onedev.server.model.Build.NAME_SUBMIT_DATE;
import static io.onedev.server.model.Build.NAME_TAG;
import static io.onedev.server.model.Build.NAME_VERSION;
import static io.onedev.server.model.Build.QUERY_FIELDS;
import static io.onedev.server.model.Build.SORT_FIELDS;
import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;
import static io.onedev.server.search.entity.EntitySort.Direction.DESCENDING;
import static io.onedev.server.search.entity.build.BuildQueryParser.Cancelled;
import static io.onedev.server.search.entity.build.BuildQueryParser.CancelledByMe;
import static io.onedev.server.search.entity.build.BuildQueryParser.Failed;
import static io.onedev.server.search.entity.build.BuildQueryParser.Finished;
import static io.onedev.server.search.entity.build.BuildQueryParser.Is;
import static io.onedev.server.search.entity.build.BuildQueryParser.IsEmpty;
import static io.onedev.server.search.entity.build.BuildQueryParser.IsGreaterThan;
import static io.onedev.server.search.entity.build.BuildQueryParser.IsLessThan;
import static io.onedev.server.search.entity.build.BuildQueryParser.IsNot;
import static io.onedev.server.search.entity.build.BuildQueryParser.IsNotEmpty;
import static io.onedev.server.search.entity.build.BuildQueryParser.IsSince;
import static io.onedev.server.search.entity.build.BuildQueryParser.IsUntil;
import static io.onedev.server.search.entity.build.BuildQueryParser.Pending;
import static io.onedev.server.search.entity.build.BuildQueryParser.Running;
import static io.onedev.server.search.entity.build.BuildQueryParser.SubmittedByMe;
import static io.onedev.server.search.entity.build.BuildQueryParser.Successful;
import static io.onedev.server.search.entity.build.BuildQueryParser.TimedOut;
import static io.onedev.server.search.entity.build.BuildQueryParser.Waiting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;

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
import io.onedev.server.service.BuildParamService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.build.BuildQueryParser.AndCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.CriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.FieldOperatorCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.FuzzyCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.NotCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.OperatorCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.OperatorValueCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.OrCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.OrderContext;
import io.onedev.server.search.entity.build.BuildQueryParser.ParensCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.QueryContext;
import io.onedev.server.search.entity.build.BuildQueryParser.ReferenceCriteriaContext;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;

public class BuildQuery extends EntityQuery<Build> {

	private static final long serialVersionUID = 1L;
	
	public BuildQuery(@Nullable Criteria<Build> criteria, List<EntitySort> sorts) {
		super(criteria, sorts);
	}

	public BuildQuery(@Nullable Criteria<Build> criteria) {
		super(criteria, new ArrayList<>());
	}
	
	public BuildQuery() {
		super(null, new ArrayList<>());
	}
	
	public static BuildQuery parse(@Nullable Project project, @Nullable String queryString, 
			boolean withCurrentUserCriteria, boolean withUnfinishedCriteria) {
		if (queryString != null) {
			CharStream is = CharStreams.fromString(queryString); 
			BuildQueryLexer lexer = new BuildQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new RuntimeException("Malformed query", e);
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			BuildQueryParser parser = new BuildQueryParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			QueryContext queryContext = parser.query();
			CriteriaContext criteriaContext = queryContext.criteria();
			Criteria<Build> buildCriteria;
			if (criteriaContext != null) {
				buildCriteria = new BuildQueryBaseVisitor<Criteria<Build>>() {

					@Override
					public Criteria<Build> visitReferenceCriteria(ReferenceCriteriaContext ctx) {
						return new ReferenceCriteria(project, ctx.getText(), BuildQueryParser.Is);
					}

					@Override
					public Criteria<Build> visitFuzzyCriteria(FuzzyCriteriaContext ctx) {
						return new FuzzyCriteria(getValue(ctx.getText()));
					}
					
					@Override
					public Criteria<Build> visitOperatorCriteria(OperatorCriteriaContext ctx) {
						switch (ctx.operator.getType()) {
						case Successful:
							return new SuccessfulCriteria();
						case Failed:
							return new FailedCriteria();
						case Cancelled:
							return new CancelledCriteria();
						case TimedOut:
							return new TimedOutCriteria();
						case Finished:
							if (!withUnfinishedCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new FinishedCriteria();
						case Waiting:
							if (!withUnfinishedCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new WaitingCriteria();
						case Pending:
							if (!withUnfinishedCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new PendingCriteria();
						case Running:
							if (!withUnfinishedCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new RunningCriteria();
						case SubmittedByMe:
							if (!withCurrentUserCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new SubmittedByMeCriteria();
						case CancelledByMe:
							if (!withCurrentUserCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new CancelledByMeCriteria();
						default:
							throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
					}
					
					@Override
					public Criteria<Build> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						var criterias = new ArrayList<Criteria<Build>>();
						for (var quoted: ctx.criteriaValue.Quoted()) {
							String value = getValue(quoted.getText());
							if (ctx.SubmittedBy() != null)
								criterias.add(new SubmittedByUserCriteria(getUser(value)));
							else if (ctx.CancelledBy() != null)
								criterias.add(new CancelledByUserCriteria(getUser(value)));
							else if (ctx.FixedIssue() != null)
								criterias.add(new FixedIssueCriteria(project, value));
							else if (ctx.DependsOn() != null)
								criterias.add(new DependsOnCriteria(project, value));
							else if (ctx.DependenciesOf() != null)
								criterias.add(new DependenciesOfCriteria(project, value));
							else if (ctx.RanOn() != null)
								criterias.add(new RanOnCriteria(value));
							else
								throw new RuntimeException("Unexpected criteria: " + ctx.operator.getText());
						}
						return Criteria.orCriterias(criterias);
					}
					
					@Override
					public Criteria<Build> visitParensCriteria(ParensCriteriaContext ctx) {
						return visit(ctx.criteria()).withParens(true);
					}

					@Override
					public Criteria<Build> visitFieldOperatorCriteria(FieldOperatorCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted().getText());
						int operator = ctx.operator.getType();
						checkField(project, fieldName, operator);
						switch (fieldName) {
							case NAME_VERSION:
								return new VersionEmptyCriteria(operator);
							case NAME_PULL_REQUEST:
								return new PullRequestEmptyCriteria(operator);
							case NAME_BRANCH:
								return new BranchEmptyCriteria(operator);
							case NAME_TAG:
								return new TagEmptyCriteria(operator);
							default:
								return new ParamEmptyCriteria(fieldName, operator);
						}
					}
					
					@Override
					public Criteria<Build> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.criteriaField.getText());
						int operator = ctx.operator.getType();
						checkField(project, fieldName, operator);

						var criterias = new ArrayList<Criteria<Build>>();
						for (var quoted: ctx.criteriaValue.Quoted()) {
							String value = getValue(quoted.getText());
							switch (operator) {
								case IsUntil:
								case IsSince:
									switch (fieldName) {
										case NAME_SUBMIT_DATE:
											criterias.add(new SubmitDateCriteria(value, operator));
											break;
										case NAME_PENDING_DATE:
											criterias.add(new PendingDateCriteria(value, operator));
											break;
										case NAME_RUNNING_DATE:
											criterias.add(new RunningDateCriteria(value, operator));
											break;
										case NAME_FINISH_DATE:
											criterias.add(new FinishDateCriteria(value, operator));
											break;
										default:
											throw new IllegalStateException();
									}
									break;
								case Is:
								case IsNot:
									switch (fieldName) {
										case NAME_PROJECT:
											criterias.add(new ProjectCriteria(value, operator));
											break;
										case NAME_COMMIT:
											ProjectScopedCommit commitId = getCommitId(project, value);
											criterias.add(new CommitCriteria(commitId.getProject(), commitId.getCommitId(), operator));
											break;
										case NAME_JOB:
											criterias.add(new JobCriteria(value, operator));
											break;
										case NAME_NUMBER:
											criterias.add(new ReferenceCriteria(project, value, operator));
											break;
										case NAME_VERSION:
											criterias.add(new VersionCriteria(value, operator));
											break;
										case NAME_BRANCH:
											criterias.add(new BranchCriteria(value, operator));
											break;
										case NAME_TAG:
											criterias.add(new TagCriteria(value, operator));
											break;
										case NAME_LABEL:
											criterias.add(new LabelCriteria(getLabelSpec(value), operator));
											break;
										case NAME_PULL_REQUEST:
											criterias.add(new PullRequestCriteria(project, value, operator));
											break;
										default:
											criterias.add(new ParamCriteria(fieldName, value, operator));
									}
									break;
								case IsLessThan:
								case IsGreaterThan:
									criterias.add(new ReferenceCriteria(project, value, operator));
									break;
								default:
									throw new IllegalStateException();
							}
						}
						return operator==IsNot? Criteria.andCriterias(criterias): Criteria.orCriterias(criterias);
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

				}.visit(criteriaContext);
			} else {
				buildCriteria = null;
			}

			List<EntitySort> buildSorts = new ArrayList<>();
			for (OrderContext order: queryContext.order()) {
				var fieldName = getValue(order.Quoted().getText());
				var sortField = SORT_FIELDS.get(fieldName);
				if (sortField == null)
					throw new ExplicitException("Can not order by field: " + fieldName);
				
				EntitySort buildSort = new EntitySort();
				buildSort.setField(fieldName);
				if (order.direction != null) {
					if (order.direction.getText().equals("desc"))
						buildSort.setDirection(DESCENDING);
					else
						buildSort.setDirection(ASCENDING);
				} else {
					buildSort.setDirection(sortField.getDefaultDirection());
				}
				buildSorts.add(buildSort);
			}
			
			return new BuildQuery(buildCriteria, buildSorts);
		} else {
			return new BuildQuery();
		}
	}
	
	public static void checkField(Project project, String fieldName, int operator) {
		Collection<String> paramNames = OneDev.getInstance(BuildParamService.class).getParamNames(null);
		if (!QUERY_FIELDS.contains(fieldName) && !paramNames.contains(fieldName))
			throw new ExplicitException("Field not found: " + fieldName);
		switch (operator) {
			case IsUntil:
			case IsSince:
				if (!fieldName.equals(NAME_SUBMIT_DATE)
						&& !fieldName.equals(NAME_PENDING_DATE)
						&& !fieldName.equals(NAME_RUNNING_DATE)
						&& !fieldName.equals(NAME_FINISH_DATE))
					throw newOperatorException(fieldName, operator);
				break;
			case Is:
			case IsNot:
				if (!fieldName.equals(NAME_PROJECT) && !fieldName.equals(NAME_COMMIT)
						&& !fieldName.equals(NAME_JOB) && !fieldName.equals(NAME_NUMBER)
						&& !fieldName.equals(NAME_PULL_REQUEST) && !fieldName.equals(NAME_VERSION)
						&& !fieldName.equals(NAME_BRANCH) && !fieldName.equals(NAME_TAG)
						&& !fieldName.equals(NAME_LABEL) && !fieldName.equals(NAME_STATUS)
						&& !paramNames.contains(fieldName)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case IsEmpty:
			case IsNotEmpty:
				if (!fieldName.equals(NAME_PULL_REQUEST) && !fieldName.equals(NAME_VERSION)
						&& !fieldName.equals(NAME_BRANCH) && !fieldName.equals(NAME_TAG)
						&& !paramNames.contains(fieldName)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case IsLessThan:
			case IsGreaterThan:
				if (!fieldName.equals(NAME_NUMBER))
					throw newOperatorException(fieldName, operator);
				break;
		}
	}
	
	private static ExplicitException newOperatorException(String fieldName, int operator) {
		return new ExplicitException("Field '" + fieldName + "' is not applicable for operator '" + getRuleName(operator) + "'");
	}
	
	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(BuildQueryLexer.ruleNames, rule);
	}
	
	public static int getOperator(String operatorName) {
		return AntlrUtils.getLexerRule(BuildQueryLexer.ruleNames, operatorName);
	}
		
	public static BuildQuery merge(BuildQuery baseQuery, BuildQuery query) {
		List<Criteria<Build>> criterias = new ArrayList<>();
		if (baseQuery.getCriteria() != null)
			criterias.add(baseQuery.getCriteria());
		if (query.getCriteria() != null)
			criterias.add(query.getCriteria());
		List<EntitySort> sorts = new ArrayList<>();
		sorts.addAll(query.getSorts());
		sorts.addAll(baseQuery.getSorts());
		return new BuildQuery(Criteria.andCriterias(criterias), sorts);
	}
	
}
