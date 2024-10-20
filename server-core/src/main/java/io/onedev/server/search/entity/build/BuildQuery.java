package io.onedev.server.search.entity.build;

import io.onedev.commons.codeassist.AntlrUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;
import org.antlr.v4.runtime.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.onedev.server.model.AbstractEntity.NAME_NUMBER;
import static io.onedev.server.model.Build.*;
import static io.onedev.server.search.entity.build.BuildQueryParser.*;

public class BuildQuery extends EntityQuery<Build> {

	private static final long serialVersionUID = 1L;

	private final Criteria<Build> criteria;
	
	private final List<EntitySort> sorts;
	
	public BuildQuery(@Nullable Criteria<Build> criteria, List<EntitySort> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	public BuildQuery(@Nullable Criteria<Build> criteria) {
		this(criteria, new ArrayList<>());
	}
	
	public BuildQuery() {
		this(null);
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
								criterias.add(new SubmittedByCriteria(getUser(value)));
							else if (ctx.CancelledBy() != null)
								criterias.add(new CancelledByCriteria(getUser(value)));
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
						return new OrCriteria<>(criterias);
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
										case NAME_STATUS:
											Status status = Status.of(value);
											if (status != null)
												criterias.add(new StatusCriteria(status, operator));
											else
												throw new ExplicitException("Invalid status: " + value);
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
						return operator==IsNot? new AndCriteria<>(criterias): new OrCriteria<>(criterias);
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
				String fieldName = getValue(order.Quoted().getText());
				if (!ORDER_FIELDS.containsKey(fieldName)) 
					throw new ExplicitException("Can not order by field: " + fieldName);
				
				EntitySort buildSort = new EntitySort();
				buildSort.setField(fieldName);
				if (order.direction != null && order.direction.getText().equals("desc"))
					buildSort.setDirection(Direction.DESCENDING);
				else
					buildSort.setDirection(Direction.ASCENDING);
				buildSorts.add(buildSort);
			}
			
			return new BuildQuery(buildCriteria, buildSorts);
		} else {
			return new BuildQuery();
		}
	}
	
	public static void checkField(Project project, String fieldName, int operator) {
		Collection<String> paramNames = OneDev.getInstance(BuildParamManager.class).getParamNames(null);
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
	
	@Override
	public Criteria<Build> getCriteria() {
		return criteria;
	}

	@Override
	public List<EntitySort> getSorts() {
		return sorts;
	}
	
	public static BuildQuery merge(BuildQuery query1, BuildQuery query2) {
		List<Criteria<Build>> criterias = new ArrayList<>();
		if (query1.getCriteria() != null)
			criterias.add(query1.getCriteria());
		if (query2.getCriteria() != null)
			criterias.add(query2.getCriteria());
		List<EntitySort> sorts = new ArrayList<>();
		sorts.addAll(query1.getSorts());
		sorts.addAll(query2.getSorts());
		return new BuildQuery(Criteria.andCriterias(criterias), sorts);
	}
	
}
