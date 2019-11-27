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
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.ProjectManager;
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
import io.onedev.server.util.PullRequestConstants;

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
	
	public static PullRequestQuery parse(@Nullable Project project, @Nullable String queryString) {
		if (queryString != null) {
			CharStream is = CharStreams.fromString(queryString); 
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
							return new RequestedForChangesByMeCriteria();
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
						case PullRequestQueryLexer.ToBeVerifiedByBuilds:
							return new ToBeVerifiedByBuildsCriteria();
						case PullRequestQueryLexer.HasPendingReviews:
							return new HasPendingReviewsCriteria();
						default:
							throw new OneException("Unexpected operator: " + ctx.operator.getText());
						}
					}
					
					@Override
					public PullRequestCriteria visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						String value = getValue(ctx.Quoted().getText());
						switch (ctx.operator.getType()) {
						case PullRequestQueryLexer.ToBeReviewedBy:
							return new ToBeReviewedByCriteria(value);
						case PullRequestQueryLexer.ApprovedBy:
							return new ApprovedByCriteria(value);
						case PullRequestQueryLexer.RequestedForChangesBy:
							return new RequestedForChangesByCriteria(value);
						case PullRequestQueryLexer.SubmittedBy:
							return new SubmittedByCriteria(value);
						case PullRequestQueryLexer.DiscardedBy:
							return new DiscardedByCriteria(value);
						case PullRequestQueryLexer.IncludesCommit:
							return new IncludesCommitCriteria(project, value);
						case PullRequestQueryLexer.IncludesIssue:
							return new IncludesIssueCriteria(project, value);
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
						checkField(fieldName, operator);
						
						switch (operator) {
						case PullRequestQueryLexer.IsBefore:
						case PullRequestQueryLexer.IsAfter:
							switch (fieldName) {
							case PullRequestConstants.FIELD_SUBMIT_DATE:
								return new SubmitDateCriteria(value, operator);
							case PullRequestConstants.FIELD_UPDATE_DATE:
								return new UpdateDateCriteria(value, operator);
							case PullRequestConstants.FIELD_CLOSE_DATE:
								return new CloseDateCriteria(value, operator);
							default:
								throw new IllegalStateException();
							}
						case PullRequestQueryLexer.Contains:
							switch (fieldName) {
							case PullRequestConstants.FIELD_TITLE:
								return new TitleCriteria(value);
							case PullRequestConstants.FIELD_DESCRIPTION:
								return new DescriptionCriteria(value);
							case PullRequestConstants.FIELD_COMMENT:
								return new CommentCriteria(value);
							default:
								throw new IllegalStateException();
							}
						case PullRequestQueryLexer.Is:
							switch (fieldName) {
							case PullRequestConstants.FIELD_NUMBER:
								return new NumberCriteria(getIntValue(value), operator);
							case PullRequestConstants.FIELD_MERGE_STRATEGY:
								return new MergeStrategyCriteria(MergeStrategy.fromString(value));
							case PullRequestConstants.FIELD_SOURCE_BRANCH:
								return new SourceBranchCriteria(value);
							case PullRequestConstants.FIELD_SOURCE_PROJECT:
								Project project = OneDev.getInstance(ProjectManager.class).find(value);
								if (project == null)
									throw new OneException("Unable to find project: " + value);
								return new SourceProjectCriteria(project);
							case PullRequestConstants.FIELD_TARGET_BRANCH:
								return new TargetBranchCriteria(value);
							case PullRequestConstants.FIELD_COMMENT_COUNT:
								return new CommentCountCriteria(getIntValue(value), operator);
							default: 
								throw new IllegalStateException();
							}
						case PullRequestQueryLexer.IsLessThan:
						case PullRequestQueryLexer.IsGreaterThan:
							switch (fieldName) {
							case PullRequestConstants.FIELD_NUMBER:
								return new NumberCriteria(getIntValue(value), operator);
							case PullRequestConstants.FIELD_COMMENT_COUNT:
								return new CommentCountCriteria(getIntValue(value), operator);
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
				if (!PullRequestConstants.ORDER_FIELDS.containsKey(fieldName))
					throw new OneException("Can not order by field: " + fieldName);
				
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
		if (!PullRequestConstants.QUERY_FIELDS.contains(fieldName))
			throw new OneException("Field not found: " + fieldName);
		switch (operator) {
		case PullRequestQueryLexer.IsBefore:
		case PullRequestQueryLexer.IsAfter:
			if (!fieldName.equals(PullRequestConstants.FIELD_SUBMIT_DATE) 
					&& !fieldName.equals(PullRequestConstants.FIELD_UPDATE_DATE) 
					&& !fieldName.equals(PullRequestConstants.FIELD_CLOSE_DATE)) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		case PullRequestQueryLexer.Contains:
			if (!fieldName.equals(PullRequestConstants.FIELD_TITLE) 
					&& !fieldName.equals(PullRequestConstants.FIELD_DESCRIPTION)
					&& !fieldName.equals(PullRequestConstants.FIELD_COMMENT)) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		case PullRequestQueryLexer.Is:
			if (!fieldName.equals(PullRequestConstants.FIELD_NUMBER)
					&& !fieldName.equals(PullRequestConstants.FIELD_MERGE_STRATEGY)
					&& !fieldName.equals(PullRequestConstants.FIELD_TARGET_BRANCH)
					&& !fieldName.equals(PullRequestConstants.FIELD_SOURCE_PROJECT)
					&& !fieldName.equals(PullRequestConstants.FIELD_SOURCE_BRANCH)
					&& !fieldName.equals(PullRequestConstants.FIELD_COMMENT_COUNT)) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		case PullRequestQueryLexer.IsLessThan:
		case PullRequestQueryLexer.IsGreaterThan:
			if (!fieldName.equals(PullRequestConstants.FIELD_NUMBER) && !fieldName.equals(PullRequestConstants.FIELD_COMMENT_COUNT))
				throw newOperatorException(fieldName, operator);
			break;
		}
	}
	
	private static OneException newOperatorException(String fieldName, int operator) {
		return new OneException("Field '" + fieldName + "' is not applicable for operator '" + getRuleName(operator) + "'");
	}
	
	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(PullRequestQueryLexer.ruleNames, rule);
	}
	
	public static int getOperator(String operatorName) {
		return AntlrUtils.getLexerRule(PullRequestQueryLexer.ruleNames, operatorName);
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
