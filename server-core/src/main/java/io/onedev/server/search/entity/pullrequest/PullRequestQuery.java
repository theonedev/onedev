package io.onedev.server.search.entity.pullrequest;

import java.util.ArrayList;
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
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryParser.AndCriteriaContext;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryParser.CriteriaContext;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryParser.NotCriteriaContext;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryParser.OperatorCriteriaContext;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryParser.OperatorValueCriteriaContext;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryParser.OrCriteriaContext;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryParser.OrderContext;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryParser.ParensCriteriaContext;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryParser.QueryContext;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;

public class PullRequestQuery extends EntityQuery<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final Criteria<PullRequest> criteria;
	
	private final List<EntitySort> sorts;
	
	public PullRequestQuery(@Nullable Criteria<PullRequest> criteria, List<EntitySort> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	public PullRequestQuery(@Nullable Criteria<PullRequest> criteria) {
		this(criteria, new ArrayList<>());
	}
	
	public PullRequestQuery() {
		this(null);
	}
	
	public static PullRequestQuery parse(@Nullable Project project, @Nullable String queryString, boolean withCurrentUserCriteria) {
		if (queryString != null) {
			CharStream is = CharStreams.fromString(queryString); 
			PullRequestQueryLexer lexer = new PullRequestQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new RuntimeException("Malformed pull request query", e);
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			PullRequestQueryParser parser = new PullRequestQueryParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			QueryContext queryContext = parser.query();
			CriteriaContext criteriaContext = queryContext.criteria();
			Criteria<PullRequest> requestCriteria;
			if (criteriaContext != null) {
				requestCriteria = new PullRequestQueryBaseVisitor<Criteria<PullRequest>>() {

					@Override
					public Criteria<PullRequest> visitOperatorCriteria(OperatorCriteriaContext ctx) {
						switch (ctx.operator.getType()) {
						case PullRequestQueryLexer.Open:
							return new OpenCriteria();
						case PullRequestQueryLexer.Merged:
							return new MergedCriteria();
						case PullRequestQueryLexer.Discarded:
							return new DiscardedCriteria();
						case PullRequestQueryLexer.SubmittedByMe:
							if (!withCurrentUserCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new SubmittedByMeCriteria();
						case PullRequestQueryLexer.ToBeReviewedByMe:
							if (!withCurrentUserCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new ToBeReviewedByMeCriteria();
						case PullRequestQueryLexer.RequestedForChangesByMe:
							if (!withCurrentUserCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new RequestedForChangesByMeCriteria();
						case PullRequestQueryLexer.ApprovedByMe:
							if (!withCurrentUserCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new ApprovedByMeCriteria();
						case PullRequestQueryLexer.AssignedToMe:
							if (!withCurrentUserCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new AssignedToMeCriteria();
						case PullRequestQueryLexer.SomeoneRequestedForChanges:
							return new SomeoneRequestedForChangesCriteria();
						case PullRequestQueryLexer.HasFailedBuilds:
							return new HasFailedBuildsCriteria();
						case PullRequestQueryLexer.HasMergeConflicts:
							return new HasMergeConflictsCriteria();
						case PullRequestQueryLexer.ToBeVerifiedByBuilds:
							return new ToBeVerifiedByBuildsCriteria();
						case PullRequestQueryLexer.HasPendingReviews:
							return new HasPendingReviewsCriteria();
						default:
							throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
					}
					
					@Override
					public Criteria<PullRequest> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						String value = getValue(ctx.Quoted().getText());
						switch (ctx.operator.getType()) {
						case PullRequestQueryLexer.ToBeReviewedBy:
							return new ToBeReviewedByCriteria(getUser(value));
						case PullRequestQueryLexer.ApprovedBy:
							return new ApprovedByCriteria(getUser(value));
						case PullRequestQueryLexer.AssignedTo:
							return new AssignedToCriteria(getUser(value));
						case PullRequestQueryLexer.RequestedForChangesBy:
							return new RequestedForChangesByCriteria(getUser(value));
						case PullRequestQueryLexer.SubmittedBy:
							return new SubmittedByCriteria(getUser(value));
						case PullRequestQueryLexer.IncludesCommit:
							return new IncludesCommitCriteria(project, value);
						case PullRequestQueryLexer.IncludesIssue:
							return new IncludesIssueCriteria(project, value);
						default:
							throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
					}
					
					@Override
					public Criteria<PullRequest> visitParensCriteria(ParensCriteriaContext ctx) {
						return (Criteria<PullRequest>) visit(ctx.criteria()).withParens(true);
					}

					@Override
					public Criteria<PullRequest> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted(0).getText());
						String value = getValue(ctx.Quoted(1).getText());
						int operator = ctx.operator.getType();
						checkField(fieldName, operator);
						
						switch (operator) {
						case PullRequestQueryLexer.IsUntil:
						case PullRequestQueryLexer.IsSince:
							switch (fieldName) {
							case PullRequest.NAME_SUBMIT_DATE:
								return new SubmitDateCriteria(value, operator);
							case PullRequest.NAME_UPDATE_DATE:
								return new UpdateDateCriteria(value, operator);
							default:
								throw new IllegalStateException();
							}
						case PullRequestQueryLexer.Contains:
							switch (fieldName) {
							case PullRequest.NAME_TITLE:
								return new TitleCriteria(value);
							case PullRequest.NAME_DESCRIPTION:
								return new DescriptionCriteria(value);
							case PullRequest.NAME_COMMENT:
								return new CommentCriteria(value);
							default:
								throw new IllegalStateException();
							}
						case PullRequestQueryLexer.Is:
							switch (fieldName) {
							case PullRequest.NAME_NUMBER:
								return new NumberCriteria(project, value, operator);
							case PullRequest.NAME_STATUS:
								try {
									return new StatusCriteria(PullRequest.Status.valueOf(value.toUpperCase()));
								} catch (IllegalArgumentException e) {
									throw new ExplicitException("Invalid status: " + value);
								}
							case PullRequest.NAME_MERGE_STRATEGY:
								return new MergeStrategyCriteria(MergeStrategy.fromString(value));
							case PullRequest.NAME_SOURCE_BRANCH:
								return new SourceBranchCriteria(value);
							case PullRequest.NAME_SOURCE_PROJECT:
								return new SourceProjectCriteria(value);
							case PullRequest.NAME_TARGET_BRANCH:
								return new TargetBranchCriteria(value);
							case PullRequest.NAME_TARGET_PROJECT:
								return new TargetProjectCriteria(value);
							case PullRequest.NAME_COMMENT_COUNT:
								return new CommentCountCriteria(getIntValue(value), operator);
							default: 
								throw new IllegalStateException();
							}
						case PullRequestQueryLexer.IsLessThan:
						case PullRequestQueryLexer.IsGreaterThan:
							switch (fieldName) {
							case PullRequest.NAME_NUMBER:
								return new NumberCriteria(project, value, operator);
							case PullRequest.NAME_COMMENT_COUNT:
								return new CommentCountCriteria(getIntValue(value), operator);
							default:
								throw new IllegalStateException();
							}
						default:
							throw new IllegalStateException();
						}
					}
					
					@Override
					public Criteria<PullRequest> visitOrCriteria(OrCriteriaContext ctx) {
						List<Criteria<PullRequest>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrCriteria<PullRequest>(childCriterias);
					}

					@Override
					public Criteria<PullRequest> visitAndCriteria(AndCriteriaContext ctx) {
						List<Criteria<PullRequest>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndCriteria<PullRequest>(childCriterias);
					}

					@Override
					public Criteria<PullRequest> visitNotCriteria(NotCriteriaContext ctx) {
						return new NotCriteria<PullRequest>(visit(ctx.criteria()));
					}

				}.visit(criteriaContext);
			} else {
				requestCriteria = null;
			}

			List<EntitySort> requestSorts = new ArrayList<>();
			for (OrderContext order: queryContext.order()) {
				String fieldName = getValue(order.Quoted().getText());
				if (!PullRequest.ORDER_FIELDS.containsKey(fieldName))
					throw new ExplicitException("Can not order by field: " + fieldName);
				
				EntitySort requestSort = new EntitySort();
				requestSort.setField(fieldName);
				if (order.direction != null && order.direction.getText().equals("desc"))
					requestSort.setDirection(Direction.DESCENDING);
				else
					requestSort.setDirection(Direction.ASCENDING);
				requestSorts.add(requestSort);
			}
			
			return new PullRequestQuery(requestCriteria, requestSorts);
		} else {
			return new PullRequestQuery();
		}
	}
	
	public static void checkField(String fieldName, int operator) {
		if (!PullRequest.QUERY_FIELDS.contains(fieldName))
			throw new ExplicitException("Field not found: " + fieldName);
		switch (operator) {
		case PullRequestQueryLexer.IsUntil:
		case PullRequestQueryLexer.IsSince:
			if (!fieldName.equals(PullRequest.NAME_SUBMIT_DATE) 
					&& !fieldName.equals(PullRequest.NAME_UPDATE_DATE) 
					&& !fieldName.equals(PullRequest.NAME_CLOSE_DATE)) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		case PullRequestQueryLexer.Contains:
			if (!fieldName.equals(PullRequest.NAME_TITLE) 
					&& !fieldName.equals(PullRequest.NAME_DESCRIPTION)
					&& !fieldName.equals(PullRequest.NAME_COMMENT)) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		case PullRequestQueryLexer.Is:
			if (!fieldName.equals(PullRequest.NAME_NUMBER)
					&& !fieldName.equals(PullRequest.NAME_STATUS)
					&& !fieldName.equals(PullRequest.NAME_MERGE_STRATEGY)
					&& !fieldName.equals(PullRequest.NAME_TARGET_PROJECT)
					&& !fieldName.equals(PullRequest.NAME_TARGET_BRANCH)
					&& !fieldName.equals(PullRequest.NAME_SOURCE_PROJECT)
					&& !fieldName.equals(PullRequest.NAME_SOURCE_BRANCH)
					&& !fieldName.equals(PullRequest.NAME_COMMENT_COUNT)) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		case PullRequestQueryLexer.IsLessThan:
		case PullRequestQueryLexer.IsGreaterThan:
			if (!fieldName.equals(PullRequest.NAME_NUMBER) 
					&& !fieldName.equals(PullRequest.NAME_COMMENT_COUNT)) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		}
	}
	
	private static ExplicitException newOperatorException(String fieldName, int operator) {
		return new ExplicitException("Field '" + fieldName + "' is not applicable for operator '" + getRuleName(operator) + "'");
	}
	
	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(PullRequestQueryLexer.ruleNames, rule);
	}
	
	public static int getOperator(String operatorName) {
		return AntlrUtils.getLexerRule(PullRequestQueryLexer.ruleNames, operatorName);
	}
	
	@Override
	public Criteria<PullRequest> getCriteria() {
		return criteria;
	}

	@Override
	public List<EntitySort> getSorts() {
		return sorts;
	}
	
	public static PullRequestQuery merge(PullRequestQuery query1, PullRequestQuery query2) {
		List<Criteria<PullRequest>> criterias = new ArrayList<>();
		if (query1.getCriteria() != null)
			criterias.add(query1.getCriteria());
		if (query2.getCriteria() != null)
			criterias.add(query2.getCriteria());
		List<EntitySort> sorts = new ArrayList<>();
		sorts.addAll(query1.getSorts());
		sorts.addAll(query2.getSorts());
		return new PullRequestQuery(Criteria.andCriterias(criterias), sorts);
	}
	
}
