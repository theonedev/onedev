package io.onedev.server.search.entity.build;

import static io.onedev.server.model.AbstractEntity.NAME_NUMBER;
import static io.onedev.server.model.Build.NAME_BRANCH;
import static io.onedev.server.model.Build.NAME_COMMIT;
import static io.onedev.server.model.Build.NAME_FINISH_DATE;
import static io.onedev.server.model.Build.NAME_JOB;
import static io.onedev.server.model.Build.NAME_PENDING_DATE;
import static io.onedev.server.model.Build.NAME_PROJECT;
import static io.onedev.server.model.Build.NAME_PULL_REQUEST;
import static io.onedev.server.model.Build.NAME_RUNNING_DATE;
import static io.onedev.server.model.Build.NAME_STATUS;
import static io.onedev.server.model.Build.NAME_SUBMIT_DATE;
import static io.onedev.server.model.Build.NAME_TAG;
import static io.onedev.server.model.Build.NAME_VERSION;
import static io.onedev.server.model.Build.ORDER_FIELDS;
import static io.onedev.server.model.Build.QUERY_FIELDS;

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
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.build.BuildQueryParser.AndCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.CriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.FieldOperatorCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.NotCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.OperatorCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.OperatorValueCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.OrCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.OrderContext;
import io.onedev.server.search.entity.build.BuildQueryParser.ParensCriteriaContext;
import io.onedev.server.search.entity.build.BuildQueryParser.QueryContext;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;

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
					throw new RuntimeException("Malformed build query", e);
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
					public Criteria<Build> visitOperatorCriteria(OperatorCriteriaContext ctx) {
						switch (ctx.operator.getType()) {
						case BuildQueryLexer.Successful:
							return new SuccessfulCriteria();
						case BuildQueryLexer.Failed:
							return new FailedCriteria();
						case BuildQueryLexer.Cancelled:
							return new CancelledCriteria();
						case BuildQueryLexer.TimedOut:
							return new TimedOutCriteria();
						case BuildQueryLexer.Finished:
							if (!withUnfinishedCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new FinishedCriteria();
						case BuildQueryLexer.Waiting:
							if (!withUnfinishedCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new WaitingCriteria();
						case BuildQueryLexer.Pending:
							if (!withUnfinishedCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new PendingCriteria();
						case BuildQueryLexer.Running:
							if (!withUnfinishedCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new RunningCriteria();
						case BuildQueryLexer.SubmittedByMe:
							if (!withCurrentUserCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new SubmittedByMeCriteria();
						case BuildQueryLexer.CancelledByMe:
							if (!withCurrentUserCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new CancelledByMeCriteria();
						default:
							throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
					}
					
					@Override
					public Criteria<Build> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						String value = getValue(ctx.Quoted().getText());
						if (ctx.SubmittedBy() != null) 
							return new SubmittedByCriteria(getUser(value));
						else if (ctx.CancelledBy() != null) 
							return new CancelledByCriteria(getUser(value));
						else if (ctx.FixedIssue() != null) 
							return new FixedIssueCriteria(project, value);
						else if (ctx.DependsOn() != null) 
							return new DependsOnCriteria(project, value);
						else if (ctx.DependenciesOf() != null) 
							return new DependenciesOfCriteria(project, value);
						else if (ctx.InPipelineOf() != null) 
							return new InPipelineOfCriteria(project, value);
						else if (ctx.RanOn() != null)
							return new RanOnCriteria(value);
						else 
							throw new RuntimeException("Unexpected criteria: " + ctx.operator.getText());
					}
					
					@Override
					public Criteria<Build> visitParensCriteria(ParensCriteriaContext ctx) {
						return (Criteria<Build>) visit(ctx.criteria()).withParens(true);
					}

					@Override
					public Criteria<Build> visitFieldOperatorCriteria(FieldOperatorCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted().getText());
						int operator = ctx.operator.getType();
						checkField(project, fieldName, operator);
						if (fieldName.equals(NAME_VERSION))
							return new VersionIsEmptyCriteria();
						else if (fieldName.equals(NAME_PULL_REQUEST))
							return new PullRequestIsEmptyCriteria();
						else if (fieldName.equals(NAME_BRANCH))
							return new BranchIsEmptyCriteria();
						else if (fieldName.equals(NAME_TAG))
							return new TagIsEmptyCriteria();
						else
							return new ParamIsEmptyCriteria(fieldName);
					}
					
					@Override
					public Criteria<Build> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted(0).getText());
						String value = getValue(ctx.Quoted(1).getText());
						int operator = ctx.operator.getType();
						checkField(project, fieldName, operator);
						
						switch (operator) {
						case BuildQueryLexer.IsUntil:
						case BuildQueryLexer.IsSince:
							if (fieldName.equals(NAME_SUBMIT_DATE))
								return new SubmitDateCriteria(value, operator);
							else if (fieldName.equals(NAME_PENDING_DATE))
								return new PendingDateCriteria(value, operator);
							else if (fieldName.equals(NAME_RUNNING_DATE))
								return new RunningDateCriteria(value, operator);
							else if (fieldName.equals(NAME_FINISH_DATE))
								return new FinishDateCriteria(value, operator);
							else
								throw new IllegalStateException();
						case BuildQueryLexer.Is:
							switch (fieldName) {
							case NAME_PROJECT:
								return new ProjectCriteria(value);
							case NAME_STATUS:
								Build.Status status = Build.Status.of(value);
								if (status != null)
									return new StatusCriteria(status);
								else
									throw new ExplicitException("Invalid status: " + value);
							case NAME_COMMIT:
								ProjectScopedCommit commitId = getCommitId(project, value); 
								return new CommitCriteria(commitId.getProject(), commitId.getCommitId());
							case NAME_JOB:
								return new JobCriteria(value);
							case NAME_NUMBER:
								return new NumberCriteria(project, value, operator);
							case NAME_VERSION:
								return new VersionCriteria(value);
							case NAME_BRANCH:
								return new BranchCriteria(value);
							case NAME_TAG:
								return new TagCriteria(value);
							case NAME_PULL_REQUEST:
								return new PullRequestCriteria(project, value);
							default: 
								return new ParamCriteria(fieldName, value);
							}
						case BuildQueryLexer.IsLessThan:
						case BuildQueryLexer.IsGreaterThan:
							return new NumberCriteria(project, value, operator);
						default:
							throw new IllegalStateException();
						}
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
		case BuildQueryLexer.IsUntil:
		case BuildQueryLexer.IsSince:
			if (!fieldName.equals(NAME_SUBMIT_DATE) 
					&& !fieldName.equals(NAME_PENDING_DATE)
					&& !fieldName.equals(NAME_RUNNING_DATE)
					&& !fieldName.equals(NAME_FINISH_DATE)) 
				throw newOperatorException(fieldName, operator);
			break;
		case BuildQueryLexer.Is:
			if (!fieldName.equals(NAME_PROJECT) && !fieldName.equals(NAME_COMMIT) 
					&& !fieldName.equals(NAME_JOB) && !fieldName.equals(NAME_NUMBER) 
					&& !fieldName.equals(NAME_PULL_REQUEST) && !fieldName.equals(NAME_VERSION) 
					&& !fieldName.equals(NAME_BRANCH) && !fieldName.equals(NAME_TAG)
					&& !fieldName.equals(NAME_STATUS) && !paramNames.contains(fieldName)) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		case BuildQueryLexer.IsEmpty:
			if (!fieldName.equals(NAME_PULL_REQUEST) && !fieldName.equals(NAME_VERSION)  
					&& !fieldName.equals(NAME_BRANCH) && !fieldName.equals(NAME_TAG)
					&& !paramNames.contains(fieldName)) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		case BuildQueryLexer.IsLessThan:
		case BuildQueryLexer.IsGreaterThan:
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
