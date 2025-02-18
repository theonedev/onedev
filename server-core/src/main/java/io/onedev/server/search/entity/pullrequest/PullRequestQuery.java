package io.onedev.server.search.entity.pullrequest;

import io.onedev.commons.codeassist.AntlrUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryParser.*;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;
import org.antlr.v4.runtime.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static io.onedev.server.model.PullRequest.SORT_FIELDS;
import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;
import static io.onedev.server.search.entity.EntitySort.Direction.DESCENDING;
import static io.onedev.server.search.entity.pullrequest.PullRequestQueryParser.HasUnsuccessfulBuilds;
import static io.onedev.server.search.entity.pullrequest.PullRequestQueryParser.*;

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
					throw new RuntimeException("Malformed query", e);
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
					public Criteria<PullRequest> visitReferenceCriteria(ReferenceCriteriaContext ctx) {
						return new ReferenceCriteria(project, ctx.getText(), Is);
					}

					@Override
					public Criteria<PullRequest> visitFuzzyCriteria(FuzzyCriteriaContext ctx) {
						return new FuzzyCriteria(getValue(ctx.getText()));
					}
					
					@Override
					public Criteria<PullRequest> visitOperatorCriteria(OperatorCriteriaContext ctx) {
						switch (ctx.operator.getType()) {
							case Open:
								return new OpenCriteria();
							case Merged:
								return new MergedCriteria();
							case Discarded:
								return new DiscardedCriteria();
							case ReadyToMerge:
								return new ReadyToMergeCriteria();
							case NeedMyAction:
								if (!withCurrentUserCriteria)
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new NeedMyActionCriteria();
							case ToBeMergedByMe:
								if (!withCurrentUserCriteria)
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new ToBeMergedByMeCriteria();
							case ToBeChangedByMe:
								if (!withCurrentUserCriteria)
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new ToBeChangedByMeCriteria();
							case MentionedMe:
								if (!withCurrentUserCriteria)
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new MentionedMeCriteria();
							case SubmittedByMe:
								if (!withCurrentUserCriteria)
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new SubmittedByMeCriteria();
							case WatchedByMe:
								if (!withCurrentUserCriteria)
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new WatchedByMeCriteria();
							case CommentedByMe:
								if (!withCurrentUserCriteria)
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new CommentedByMeCriteria();
							case ToBeReviewedByMe:
								if (!withCurrentUserCriteria)
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new ToBeReviewedByMeCriteria();
							case RequestedForChangesByMe:
								if (!withCurrentUserCriteria)
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new RequestedForChangesByMeCriteria();
							case ApprovedByMe:
								if (!withCurrentUserCriteria)
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new ApprovedByMeCriteria();
							case AssignedToMe:
								if (!withCurrentUserCriteria)
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new AssignedToMeCriteria();
							case SomeoneRequestedForChanges:
								return new SomeoneRequestedForChangesCriteria();
							case HasUnsuccessfulBuilds:
								return new HasUnsuccessfulBuilds();
							case HasMergeConflicts:
								return new HasMergeConflictsCriteria();
							case HasUnfinishedBuilds:
								return new HasUnfinishedBuildsCriteria();
							case HasPendingReviews:
								return new HasPendingReviewsCriteria();
							default:
								throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
					}

					@Override
					public Criteria<PullRequest> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						List<Criteria<PullRequest>> criterias = new ArrayList<>();
						for (var quoted: ctx.criteriaValue.Quoted()) {
							String value = getValue(quoted.getText());
							switch (ctx.operator.getType()) {
								case ToBeReviewedBy:
									criterias.add(new ToBeReviewedByCriteria(getUser(value)));
									break;
								case ToBeChangedBy:
									criterias.add(new ToBeChangedByCriteria(getUser(value)));
									break;
								case ToBeMergedBy:
									criterias.add(new ToBeMergedByCriteria(getUser(value)));
									break;
								case ApprovedBy:
									criterias.add(new ApprovedByCriteria(getUser(value)));
									break;
								case AssignedTo:
									criterias.add(new AssignedToCriteria(getUser(value)));
									break;
								case RequestedForChangesBy:
									criterias.add(new RequestedForChangesByCriteria(getUser(value)));
									break;
								case Mentioned:
									criterias.add(new MentionedCriteria(getUser(value)));
									break;
								case SubmittedBy:
									criterias.add(new SubmittedByCriteria(getUser(value)));
									break;
								case WatchedBy:
									criterias.add(new WatchedByCriteria(getUser(value)));
									break;
								case NeedActionOf:
									criterias.add(new NeedActionOfCriteria(getUser(value)));
									break;
								case CommentedBy:
									criterias.add(new CommentedByCriteria(getUser(value)));
									break;
								case IncludesCommit:
									criterias.add(new IncludesCommitCriteria(project, value));
									break;
								case IncludesIssue:
									criterias.add(new IncludesIssueCriteria(project, value));
									break;
								default:
									throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
							}
						} 
						return new OrCriteria<>(criterias);
					}

					@Override
					public Criteria<PullRequest> visitParensCriteria(ParensCriteriaContext ctx) {
						return visit(ctx.criteria()).withParens(true);
					}

					@Override
					public Criteria<PullRequest> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.criteriaField.getText());
						int operator = ctx.operator.getType();
						checkField(fieldName, operator);

						var criterias = new ArrayList<Criteria<PullRequest>>();
						for (var quoted: ctx.criteriaValue.Quoted()) {
							String value = getValue(quoted.getText());
							switch (operator) {
								case PullRequestQueryLexer.IsUntil:
								case PullRequestQueryLexer.IsSince:
									switch (fieldName) {
										case PullRequest.NAME_SUBMIT_DATE:
											criterias.add(new SubmitDateCriteria(value, operator));
											break;
										case PullRequest.NAME_CLOSE_DATE:
											criterias.add(new CloseDateCriteria(value, operator));
											break;
										case PullRequest.NAME_LAST_ACTIVITY_DATE:
											criterias.add(new LastActivityDateCriteria(value, operator));
											break;
										default:
											throw new IllegalStateException();
									}
									break;
								case PullRequestQueryLexer.Contains:
									switch (fieldName) {
										case PullRequest.NAME_TITLE:
											criterias.add(new TitleCriteria(value));
											break;
										case PullRequest.NAME_DESCRIPTION:
											criterias.add(new DescriptionCriteria(value));
											break;
										case PullRequest.NAME_COMMENT:
											criterias.add(new CommentCriteria(value));
											break;
										default:
											throw new IllegalStateException();
									}
									break;
								case Is:
								case IsNot:
									switch (fieldName) {
										case PullRequest.NAME_NUMBER:
											criterias.add(new ReferenceCriteria(project, value, operator));
											break;
										case PullRequest.NAME_STATUS:
											try {
												criterias.add(new StatusCriteria(PullRequest.Status.valueOf(value.toUpperCase()), operator));
												break;
											} catch (IllegalArgumentException e) {
												throw new ExplicitException("Invalid status: " + value);
											}
										case PullRequest.NAME_MERGE_STRATEGY:
											criterias.add(new MergeStrategyCriteria(MergeStrategy.fromString(value), operator));
											break;
										case PullRequest.NAME_SOURCE_BRANCH:
											criterias.add(new SourceBranchCriteria(value, operator));
											break;
										case PullRequest.NAME_SOURCE_PROJECT:
											criterias.add(new SourceProjectCriteria(value, operator));
											break;
										case PullRequest.NAME_TARGET_BRANCH:
											criterias.add(new TargetBranchCriteria(value, operator));
											break;
										case PullRequest.NAME_TARGET_PROJECT:
											criterias.add(new TargetProjectCriteria(value, operator));
											break;
										case PullRequest.NAME_LABEL:
											criterias.add(new LabelCriteria(getLabelSpec(value), operator));
											break;
										case PullRequest.NAME_COMMENT_COUNT:
											criterias.add(new CommentCountCriteria(getIntValue(value), operator));
											break;
										case PullRequest.NAME_THUMBS_UP_COUNT:
											criterias.add(new ThumbsUpCountCriteria(getIntValue(value), operator));
											break;
										case PullRequest.NAME_THUMBS_DOWN_COUNT:
											criterias.add(new ThumbsDownCountCriteria(getIntValue(value), operator));
											break;
										case PullRequest.NAME_SMILE_COUNT:
											criterias.add(new SmileCountCriteria(getIntValue(value), operator));
											break;
										case PullRequest.NAME_TADA_COUNT:
											criterias.add(new TadaCountCriteria(getIntValue(value), operator));
											break;
										case PullRequest.NAME_CONFUSED_COUNT:
											criterias.add(new ConfusedCountCriteria(getIntValue(value), operator));
											break;
										case PullRequest.NAME_HEART_COUNT:
											criterias.add(new HeartCountCriteria(getIntValue(value), operator));
											break;
										case PullRequest.NAME_ROCKET_COUNT:
											criterias.add(new RocketCountCriteria(getIntValue(value), operator));
											break;
										case PullRequest.NAME_EYES_COUNT:
											criterias.add(new EyesCountCriteria(getIntValue(value), operator));
											break;
										default:
											throw new IllegalStateException();
									}
									break;
								case IsLessThan:
								case IsGreaterThan:
									switch (fieldName) {
										case PullRequest.NAME_NUMBER:
											criterias.add(new ReferenceCriteria(project, value, operator));
											break;
										case PullRequest.NAME_COMMENT_COUNT:
											criterias.add(new CommentCountCriteria(getIntValue(value), operator));
											break;
										case PullRequest.NAME_THUMBS_UP_COUNT:
											criterias.add(new ThumbsUpCountCriteria(getIntValue(value), operator));
											break;
										case PullRequest.NAME_THUMBS_DOWN_COUNT:
											criterias.add(new ThumbsDownCountCriteria(getIntValue(value), operator));
											break;
										case PullRequest.NAME_SMILE_COUNT:
											criterias.add(new SmileCountCriteria(getIntValue(value), operator));
											break;
										case PullRequest.NAME_TADA_COUNT:
											criterias.add(new TadaCountCriteria(getIntValue(value), operator));
											break;
										case PullRequest.NAME_CONFUSED_COUNT:
											criterias.add(new ConfusedCountCriteria(getIntValue(value), operator));
											break;
										case PullRequest.NAME_HEART_COUNT:
											criterias.add(new HeartCountCriteria(getIntValue(value), operator));
											break;
										case PullRequest.NAME_ROCKET_COUNT:
											criterias.add(new RocketCountCriteria(getIntValue(value), operator));
											break;
										case PullRequest.NAME_EYES_COUNT:
											criterias.add(new EyesCountCriteria(getIntValue(value), operator));
											break;
									}
									break;
								default:
									throw new IllegalStateException();
							}
						}
						return operator==IsNot? new AndCriteria<>(criterias): new OrCriteria<>(criterias);
					}

					@Override
					public Criteria<PullRequest> visitOrCriteria(OrCriteriaContext ctx) {
						List<Criteria<PullRequest>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx : ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrCriteria<>(childCriterias);
					}

					@Override
					public Criteria<PullRequest> visitAndCriteria(AndCriteriaContext ctx) {
						List<Criteria<PullRequest>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx : ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndCriteria<>(childCriterias);
					}

					@Override
					public Criteria<PullRequest> visitNotCriteria(NotCriteriaContext ctx) {
						return new NotCriteria<>(visit(ctx.criteria()));
					}

				}.visit(criteriaContext);
			} else {
				requestCriteria = null;
			}

			List<EntitySort> requestSorts = new ArrayList<>();
			for (OrderContext order : queryContext.order()) {
				String fieldName = getValue(order.Quoted().getText());
				var sortField = SORT_FIELDS.get(fieldName);
				if (sortField == null)
					throw new ExplicitException("Can not order by field: " + fieldName);

				EntitySort requestSort = new EntitySort();
				requestSort.setField(fieldName);
				if (order.direction != null) {
					if (order.direction.getText().equals("desc"))
						requestSort.setDirection(DESCENDING);
					else
						requestSort.setDirection(ASCENDING);
				} else {
					requestSort.setDirection(sortField.getDefaultDirection());
				}
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
			case IsUntil:
			case IsSince:
				if (!fieldName.equals(PullRequest.NAME_SUBMIT_DATE)
						&& !fieldName.equals(PullRequest.NAME_LAST_ACTIVITY_DATE)
						&& !fieldName.equals(PullRequest.NAME_CLOSE_DATE)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case Contains:
				if (!fieldName.equals(PullRequest.NAME_TITLE)
						&& !fieldName.equals(PullRequest.NAME_DESCRIPTION)
						&& !fieldName.equals(PullRequest.NAME_COMMENT)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case Is:
			case IsNot:
				if (!fieldName.equals(PullRequest.NAME_NUMBER)
						&& !fieldName.equals(PullRequest.NAME_STATUS)
						&& !fieldName.equals(PullRequest.NAME_MERGE_STRATEGY)
						&& !fieldName.equals(PullRequest.NAME_TARGET_PROJECT)
						&& !fieldName.equals(PullRequest.NAME_TARGET_BRANCH)
						&& !fieldName.equals(PullRequest.NAME_SOURCE_PROJECT)
						&& !fieldName.equals(PullRequest.NAME_SOURCE_BRANCH)
						&& !fieldName.equals(PullRequest.NAME_LABEL)
						&& !fieldName.equals(PullRequest.NAME_COMMENT_COUNT)
						&& !fieldName.equals(PullRequest.NAME_THUMBS_UP_COUNT)
						&& !fieldName.equals(PullRequest.NAME_THUMBS_DOWN_COUNT)
						&& !fieldName.equals(PullRequest.NAME_SMILE_COUNT)
						&& !fieldName.equals(PullRequest.NAME_TADA_COUNT)
						&& !fieldName.equals(PullRequest.NAME_CONFUSED_COUNT)
						&& !fieldName.equals(PullRequest.NAME_HEART_COUNT)
						&& !fieldName.equals(PullRequest.NAME_ROCKET_COUNT)
						&& !fieldName.equals(PullRequest.NAME_EYES_COUNT)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case IsLessThan:
			case IsGreaterThan:
				if (!fieldName.equals(PullRequest.NAME_NUMBER)
						&& !fieldName.equals(PullRequest.NAME_COMMENT_COUNT)
						&& !fieldName.equals(PullRequest.NAME_THUMBS_UP_COUNT)
						&& !fieldName.equals(PullRequest.NAME_THUMBS_DOWN_COUNT)
						&& !fieldName.equals(PullRequest.NAME_SMILE_COUNT)
						&& !fieldName.equals(PullRequest.NAME_TADA_COUNT)
						&& !fieldName.equals(PullRequest.NAME_CONFUSED_COUNT)
						&& !fieldName.equals(PullRequest.NAME_HEART_COUNT)
						&& !fieldName.equals(PullRequest.NAME_ROCKET_COUNT)
						&& !fieldName.equals(PullRequest.NAME_EYES_COUNT)) {
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
