package io.onedev.server.entityquery.pullrequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.entityquery.EntityQuery;
import io.onedev.server.entityquery.EntitySort;
import io.onedev.server.entityquery.EntitySort.Direction;
import io.onedev.server.exception.OneException;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryBaseVisitor;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryLexer;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryParser;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryParser.AndCriteriaContext;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryParser.CriteriaContext;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryParser.NotCriteriaContext;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryParser.OperatorCriteriaContext;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryParser.OperatorValueCriteriaContext;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryParser.OrCriteriaContext;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryParser.OrderContext;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryParser.ParensCriteriaContext;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryParser.QueryContext;
import io.onedev.server.util.DateUtils;

public class PullRequestQuery extends EntityQuery<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final PullRequestCriteria criteria;
	
	private final List<EntitySort> sorts;
	
	public PullRequestQuery(@Nullable PullRequestCriteria criteria, List<EntitySort> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	public PullRequestQuery() {
		this(null, new ArrayList<>());
	}
	
	public static PullRequestQuery parse(Project project, @Nullable String queryString, boolean validate) {
		if (queryString != null) {
			ANTLRInputStream is = new ANTLRInputStream(queryString); 
			PullRequestQueryLexer lexer = new PullRequestQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new OneException("Malformed query syntax", e);
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			PullRequestQueryParser parser = new PullRequestQueryParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			QueryContext queryContext;
			try {
				queryContext = parser.query();
			} catch (Exception e) {
				if (e instanceof OneException)
					throw e;
				else
					throw new OneException("Malformed query syntax", e);
			}
			CriteriaContext criteriaContext = queryContext.criteria();
			PullRequestCriteria requestCriteria;
			if (criteriaContext != null) {
				requestCriteria = new PullRequestQueryBaseVisitor<PullRequestCriteria>() {

					@Override
					public PullRequestCriteria visitOperatorCriteria(OperatorCriteriaContext ctx) {
						switch (ctx.operator.getType()) {
						case PullRequestQueryLexer.Open:
							return new OpenCriteria();
						case PullRequestQueryLexer.Merged:
							return new MergedCriteria();
						case PullRequestQueryLexer.Discarded:
							return new DiscardedCriteria();
						case PullRequestQueryLexer.SubmittedByMe:
							return new SubmittedByMeCriteria();
						case PullRequestQueryLexer.ToBeReviewedByMe:
							return new ToBeReviewedByMeCriteria();
						case PullRequestQueryLexer.RequestedForChangesByMe:
							return new RequestForChangesByMeCriteria();
						case PullRequestQueryLexer.ApprovedByMe:
							return new ApprovedByMeCriteria();
						case PullRequestQueryLexer.DiscardedByMe:
							return new DiscardedByMeCriteria();
						case PullRequestQueryLexer.SomeoneRequestedForChanges:
							return new SomeoneRequestedForChangesCriteria();
						case PullRequestQueryLexer.HasFailedBuilds:
							return new HasFailedBuildsCriteria();
						case PullRequestQueryLexer.HasMergeConflicts:
							return new HasMergeConflictsCriteria();
						case PullRequestQueryLexer.HasPendingBuilds:
							return new HasPendingBuildsCriteria();
						case PullRequestQueryLexer.HasPendingReviews:
							return new HasPendingReviewsCriteria();
						default:
							throw new OneException("Unexpected operator: " + ctx.operator.getText());
						}
					}
					
					private User getUser(String value) {
						User user = OneDev.getInstance(UserManager.class).findByName(value);
						if (user == null)
							throw new OneException("Unable to find user with login: " + value);
						return user;
					}
					
					@Override
					public PullRequestCriteria visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						String value = getValue(ctx.Quoted().getText());
						switch (ctx.operator.getType()) {
						case PullRequestQueryLexer.ToBeReviewedBy:
							return new ToBeReviewedByCriteria(getUser(value));
						case PullRequestQueryLexer.ApprovedBy:
							return new ApprovedByCriteria(getUser(value));
						case PullRequestQueryLexer.RequestedForChangesBy:
							return new RequestForChangesByCriteria(getUser(value));
						case PullRequestQueryLexer.SubmittedBy:
							return new SubmittedByCriteria(getUser(value));
						case PullRequestQueryLexer.DiscardedBy:
							return new DiscardedByCriteria(getUser(value));
						case PullRequestQueryLexer.ContainsCommit:
							try {
								ObjectId commitId = project.getRepository().resolve(value);
								if (commitId == null)
									throw new RevisionSyntaxException("");
								return new ContainsCommitCriteria(commitId);								
							} catch (RevisionSyntaxException | IOException e) {
								throw new OneException("Invalid revision string: " + value);
							}
						default:
							throw new OneException("Unexpected operator: " + ctx.operator.getText());
						}
					}
					
					@Override
					public PullRequestCriteria visitParensCriteria(ParensCriteriaContext ctx) {
						return visit(ctx.criteria());
					}

					@Override
					public PullRequestCriteria visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted(0).getText());
						String value = getValue(ctx.Quoted(1).getText());
						int operator = ctx.operator.getType();
						if (validate)
							checkField(project, fieldName, operator);
						
						switch (operator) {
						case PullRequestQueryLexer.IsBefore:
						case PullRequestQueryLexer.IsAfter:
							Date dateValue = DateUtils.parseRelaxed(value);
							if (dateValue == null)
								throw new OneException("Unrecognized date: " + value);
							switch (fieldName) {
							case PullRequest.FIELD_SUBMIT_DATE:
								return new SubmitDateCriteria(dateValue, value, operator);
							case PullRequest.FIELD_UPDATE_DATE:
								return new UpdateDateCriteria(dateValue, value, operator);
							case PullRequest.FIELD_CLOSE_DATE:
								return new CloseDateCriteria(dateValue, value, operator);
							default:
								throw new IllegalStateException();
							}
						case PullRequestQueryLexer.Contains:
							switch (fieldName) {
							case PullRequest.FIELD_TITLE:
								return new TitleCriteria(value);
							case PullRequest.FIELD_DESCRIPTION:
								return new DescriptionCriteria(value);
							default:
								throw new IllegalStateException();
							}
						case PullRequestQueryLexer.Is:
							switch (fieldName) {
							case PullRequest.FIELD_STATE:
								return new StateCriteria(value);
							case PullRequest.FIELD_NUMBER:
								return new NumberCriteria(getIntValue(value), operator);
							case PullRequest.FIELD_MERGE_STRATEGY:
								return new MergeStrategyCriteria(MergeStrategy.from(value));
							case PullRequest.FIELD_SOURCE_BRANCH:
								return new SourceBranchCriteria(value);
							case PullRequest.FIELD_SOURCE_PROJECT:
								Project project = OneDev.getInstance(ProjectManager.class).find(value);
								if (project == null)
									throw new OneException("Unable to find project: " + value);
								return new SourceProjectCriteria(project);
							case PullRequest.FIELD_TARGET_BRANCH:
								return new TargetBranchCriteria(value);
							default: 
								throw new IllegalStateException();
							}
						case PullRequestQueryLexer.IsLessThan:
						case PullRequestQueryLexer.IsGreaterThan:
							switch (fieldName) {
							case PullRequest.FIELD_NUMBER:
								return new NumberCriteria(getIntValue(value), operator);
							default:
								throw new IllegalStateException();
							}
						default:
							throw new IllegalStateException();
						}
					}
					
					@Override
					public PullRequestCriteria visitOrCriteria(OrCriteriaContext ctx) {
						List<PullRequestCriteria> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrCriteria(childCriterias);
					}

					@Override
					public PullRequestCriteria visitAndCriteria(AndCriteriaContext ctx) {
						List<PullRequestCriteria> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndCriteria(childCriterias);
					}

					@Override
					public PullRequestCriteria visitNotCriteria(NotCriteriaContext ctx) {
						return new NotCriteria(visit(ctx.criteria()));
					}

				}.visit(criteriaContext);
			} else {
				requestCriteria = null;
			}

			List<EntitySort> requestSorts = new ArrayList<>();
			for (OrderContext order: queryContext.order()) {
				String fieldName = getValue(order.Quoted().getText());
				if (validate 
						&& !fieldName.equals(PullRequest.FIELD_SUBMIT_DATE) 
						&& !fieldName.equals(PullRequest.FIELD_UPDATE_DATE) 
						&& !fieldName.equals(PullRequest.FIELD_CLOSE_DATE) 
						&& !fieldName.equals(PullRequest.FIELD_NUMBER)) {
					throw new OneException("Can not order by field: " + fieldName);
				}
				
				EntitySort pullRequestSort = new EntitySort();
				pullRequestSort.setField(fieldName);
				if (order.direction != null && order.direction.getText().equals("asc"))
					pullRequestSort.setDirection(Direction.ASCENDING);
				else
					pullRequestSort.setDirection(Direction.DESCENDING);
				requestSorts.add(pullRequestSort);
			}
			
			return new PullRequestQuery(requestCriteria, requestSorts);
		} else {
			return new PullRequestQuery();
		}
	}
	
	public static void checkField(Project project, String fieldName, int operator) {
		if (!PullRequest.FIELD_PATHS.containsKey(fieldName))
			throw new OneException("Field not found: " + fieldName);
		switch (operator) {
		case PullRequestQueryLexer.IsBefore:
		case PullRequestQueryLexer.IsAfter:
			if (!fieldName.equals(PullRequest.FIELD_SUBMIT_DATE) 
					&& !fieldName.equals(PullRequest.FIELD_UPDATE_DATE) 
					&& !fieldName.equals(PullRequest.FIELD_CLOSE_DATE)) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		case PullRequestQueryLexer.Contains:
			if (!fieldName.equals(PullRequest.FIELD_TITLE) && !fieldName.equals(PullRequest.FIELD_DESCRIPTION))
				throw newOperatorException(fieldName, operator);
			break;
		case PullRequestQueryLexer.Is:
			if (!fieldName.equals(PullRequest.FIELD_STATE) 
					&& !fieldName.equals(PullRequest.FIELD_NUMBER)
					&& !fieldName.equals(PullRequest.FIELD_TARGET_BRANCH)
					&& !fieldName.equals(PullRequest.FIELD_SOURCE_PROJECT)
					&& !fieldName.equals(PullRequest.FIELD_SOURCE_BRANCH)) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		case PullRequestQueryLexer.IsLessThan:
		case PullRequestQueryLexer.IsGreaterThan:
			if (!fieldName.equals(PullRequest.FIELD_NUMBER))
				throw newOperatorException(fieldName, operator);
			break;
		}
	}
	
	private static OneException newOperatorException(String fieldName, int operator) {
		return new OneException("Field '" + fieldName + "' is not applicable for operator '" + getRuleName(operator) + "'");
	}
	
	public static String getRuleName(int rule) {
		return getRuleName(PullRequestQueryLexer.ruleNames, rule);
	}
	
	public static int getOperator(String operatorName) {
		return getOperator(PullRequestQueryLexer.ruleNames, operatorName);
	}
	
	public static PullRequestQuery merge(PullRequestQuery query1, PullRequestQuery query2) {
		List<PullRequestCriteria> criterias = new ArrayList<>();
		if (query1.getCriteria() != null)
			criterias.add(query1.getCriteria());
		if (query2.getCriteria() != null)
			criterias.add(query2.getCriteria());
		List<EntitySort> sorts = new ArrayList<>();
		sorts.addAll(query1.getSorts());
		sorts.addAll(query2.getSorts());
		return new PullRequestQuery(PullRequestCriteria.of(criterias), sorts);
	}

	@Override
	public PullRequestCriteria getCriteria() {
		return criteria;
	}

	@Override
	public List<EntitySort> getSorts() {
		return sorts;
	}
	
}
