package io.onedev.server.search.entity.build;

import static io.onedev.server.model.Build.NAME_COMMIT;
import static io.onedev.server.model.Build.NAME_FINISH_DATE;
import static io.onedev.server.model.Build.NAME_JOB;
import static io.onedev.server.model.Build.NAME_NUMBER;
import static io.onedev.server.model.Build.NAME_PENDING_DATE;
import static io.onedev.server.model.Build.NAME_PROJECT;
import static io.onedev.server.model.Build.NAME_RUNNING_DATE;
import static io.onedev.server.model.Build.NAME_SUBMIT_DATE;
import static io.onedev.server.model.Build.NAME_VERSION;
import static io.onedev.server.model.Build.NAME_PULL_REQUEST;
import static io.onedev.server.model.Build.NAME_BRANCH;
import static io.onedev.server.model.Build.NAME_TAG;
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
import io.onedev.server.search.entity.AndEntityCriteria;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.NotEntityCriteria;
import io.onedev.server.search.entity.OrEntityCriteria;
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

public class BuildQuery extends EntityQuery<Build> {

	private static final long serialVersionUID = 1L;

	private final EntityCriteria<Build> criteria;
	
	private final List<EntitySort> sorts;
	
	public BuildQuery(@Nullable EntityCriteria<Build> criteria, List<EntitySort> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	public BuildQuery(@Nullable EntityCriteria<Build> criteria) {
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
			EntityCriteria<Build> buildCriteria;
			if (criteriaContext != null) {
				buildCriteria = new BuildQueryBaseVisitor<EntityCriteria<Build>>() {

					@Override
					public EntityCriteria<Build> visitOperatorCriteria(OperatorCriteriaContext ctx) {
						switch (ctx.operator.getType()) {
						case BuildQueryLexer.Successful:
							return new SuccessfulCriteria();
						case BuildQueryLexer.Failed:
							return new FailedCriteria();
						case BuildQueryLexer.Cancelled:
							return new CancelledCriteria();
						case BuildQueryLexer.TimedOut:
							return new TimedOutCriteria();
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
					public EntityCriteria<Build> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
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
						else 
							throw new RuntimeException("Unexpected criteria: " + ctx.operator.getText());
					}
					
					@Override
					public EntityCriteria<Build> visitParensCriteria(ParensCriteriaContext ctx) {
						return (EntityCriteria<Build>) visit(ctx.criteria()).withParens(true);
					}

					@Override
					public EntityCriteria<Build> visitFieldOperatorCriteria(FieldOperatorCriteriaContext ctx) {
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
					public EntityCriteria<Build> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
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
					public EntityCriteria<Build> visitOrCriteria(OrCriteriaContext ctx) {
						List<EntityCriteria<Build>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrEntityCriteria<Build>(childCriterias);
					}

					@Override
					public EntityCriteria<Build> visitAndCriteria(AndCriteriaContext ctx) {
						List<EntityCriteria<Build>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndEntityCriteria<Build>(childCriterias);
					}

					@Override
					public EntityCriteria<Build> visitNotCriteria(NotCriteriaContext ctx) {
						return new NotEntityCriteria<Build>(visit(ctx.criteria()));
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
		Collection<String> paramNames = OneDev.getInstance(BuildParamManager.class).getBuildParamNames(null);
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
					&& !paramNames.contains(fieldName)) {
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
	public EntityCriteria<Build> getCriteria() {
		return criteria;
	}

	@Override
	public List<EntitySort> getSorts() {
		return sorts;
	}
	
	public static BuildQuery merge(BuildQuery query1, BuildQuery query2) {
		List<EntityCriteria<Build>> criterias = new ArrayList<>();
		if (query1.getCriteria() != null)
			criterias.add(query1.getCriteria());
		if (query2.getCriteria() != null)
			criterias.add(query2.getCriteria());
		List<EntitySort> sorts = new ArrayList<>();
		sorts.addAll(query1.getSorts());
		sorts.addAll(query2.getSorts());
		return new BuildQuery(EntityCriteria.andCriterias(criterias), sorts);
	}
	
}
