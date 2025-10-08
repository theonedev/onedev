package io.onedev.server.search.entity.issue;

import static io.onedev.server.model.Issue.NAME_COMMENT;
import static io.onedev.server.model.Issue.NAME_COMMENT_COUNT;
import static io.onedev.server.model.Issue.NAME_CONFUSED_COUNT;
import static io.onedev.server.model.Issue.NAME_DESCRIPTION;
import static io.onedev.server.model.Issue.NAME_ESTIMATED_TIME;
import static io.onedev.server.model.Issue.NAME_EYES_COUNT;
import static io.onedev.server.model.Issue.NAME_HEART_COUNT;
import static io.onedev.server.model.Issue.NAME_LAST_ACTIVITY_DATE;
import static io.onedev.server.model.Issue.NAME_PROGRESS;
import static io.onedev.server.model.Issue.NAME_PROJECT;
import static io.onedev.server.model.Issue.NAME_ROCKET_COUNT;
import static io.onedev.server.model.Issue.NAME_SMILE_COUNT;
import static io.onedev.server.model.Issue.NAME_SPENT_TIME;
import static io.onedev.server.model.Issue.NAME_STATE;
import static io.onedev.server.model.Issue.NAME_SUBMIT_DATE;
import static io.onedev.server.model.Issue.NAME_TADA_COUNT;
import static io.onedev.server.model.Issue.NAME_THUMBS_DOWN_COUNT;
import static io.onedev.server.model.Issue.NAME_THUMBS_UP_COUNT;
import static io.onedev.server.model.Issue.NAME_TITLE;
import static io.onedev.server.model.Issue.NAME_VOTE_COUNT;
import static io.onedev.server.model.Issue.SORT_FIELDS;
import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;
import static io.onedev.server.search.entity.EntitySort.Direction.DESCENDING;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.IgnoredByMe;
import static io.onedev.server.search.entity.issue.IssueQueryParser.CommentedByMe;
import static io.onedev.server.search.entity.issue.IssueQueryParser.Confidential;
import static io.onedev.server.search.entity.issue.IssueQueryParser.Contains;
import static io.onedev.server.search.entity.issue.IssueQueryParser.CurrentIssue;
import static io.onedev.server.search.entity.issue.IssueQueryParser.FixedInCurrentBuild;
import static io.onedev.server.search.entity.issue.IssueQueryParser.FixedInCurrentCommit;
import static io.onedev.server.search.entity.issue.IssueQueryParser.FixedInCurrentPullRequest;
import static io.onedev.server.search.entity.issue.IssueQueryParser.Is;
import static io.onedev.server.search.entity.issue.IssueQueryParser.IsAfter;
import static io.onedev.server.search.entity.issue.IssueQueryParser.IsBefore;
import static io.onedev.server.search.entity.issue.IssueQueryParser.IsEmpty;
import static io.onedev.server.search.entity.issue.IssueQueryParser.IsGreaterThan;
import static io.onedev.server.search.entity.issue.IssueQueryParser.IsLessThan;
import static io.onedev.server.search.entity.issue.IssueQueryParser.IsMe;
import static io.onedev.server.search.entity.issue.IssueQueryParser.IsNot;
import static io.onedev.server.search.entity.issue.IssueQueryParser.IsNotEmpty;
import static io.onedev.server.search.entity.issue.IssueQueryParser.IsNotMe;
import static io.onedev.server.search.entity.issue.IssueQueryParser.IsPrevious;
import static io.onedev.server.search.entity.issue.IssueQueryParser.IsSince;
import static io.onedev.server.search.entity.issue.IssueQueryParser.IsUntil;
import static io.onedev.server.search.entity.issue.IssueQueryParser.MentionedMe;
import static io.onedev.server.search.entity.issue.IssueQueryParser.SubmittedByMe;
import static io.onedev.server.search.entity.issue.IssueQueryParser.WatchedByMe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import io.onedev.server.service.SettingService;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.spec.BooleanField;
import io.onedev.server.model.support.issue.field.spec.BuildChoiceField;
import io.onedev.server.model.support.issue.field.spec.CommitField;
import io.onedev.server.model.support.issue.field.spec.DateField;
import io.onedev.server.model.support.issue.field.spec.DateTimeField;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.GroupChoiceField;
import io.onedev.server.model.support.issue.field.spec.IntegerField;
import io.onedev.server.model.support.issue.field.spec.IssueChoiceField;
import io.onedev.server.model.support.issue.field.spec.IterationChoiceField;
import io.onedev.server.model.support.issue.field.spec.PullRequestChoiceField;
import io.onedev.server.model.support.issue.field.spec.TextField;
import io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.UserChoiceField;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.issue.IssueQueryParser.AndCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.CriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.FieldOperatorCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.FixedBetweenCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.FuzzyCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.LinkMatchCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.NotCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.OperatorCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.OperatorValueCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.OrCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.OrderContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.ParensCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.QueryContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.ReferenceCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.RevisionCriteriaContext;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution.FixType;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;
import io.onedev.server.web.util.WicketUtils;

public class IssueQuery extends EntityQuery<Issue> {

	private static final long serialVersionUID = 1L;

	public IssueQuery(@Nullable Criteria<Issue> criteria, List<EntitySort> sorts, List<EntitySort> baseSorts) {
		super(criteria, sorts, baseSorts);
	}

	public IssueQuery(@Nullable Criteria<Issue> criteria, List<EntitySort> sorts) {
		this(criteria, sorts, new ArrayList<>());
	}

	public IssueQuery(@Nullable Criteria<Issue> criteria) {
		this(criteria, new ArrayList<>());
	}

	public IssueQuery() {
		this(null);
	}

	public static IssueQuery parse(@Nullable Project project, @Nullable String queryString,
								   IssueQueryParseOption option, boolean validate) {
		if (queryString != null) {
			CharStream is = CharStreams.fromString(queryString);
			IssueQueryLexer lexer = new IssueQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
										int charPositionInLine, String msg, RecognitionException e) {
					throw new RuntimeException("Malformed query", e);
				}

			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			IssueQueryParser parser = new IssueQueryParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());

			QueryContext queryContext = parser.query();
			CriteriaContext criteriaContext = queryContext.criteria();
			Criteria<Issue> issueCriteria;
			if (criteriaContext != null) {
				issueCriteria = new IssueQueryBaseVisitor<Criteria<Issue>>() {

					private long getValueOrdinal(ChoiceField field, String value) {
						List<String> choices = new ArrayList<>(field.getChoiceProvider().getChoices(true).keySet());
						return choices.indexOf(value);
					}

					@Override
					public Criteria<Issue> visitReferenceCriteria(ReferenceCriteriaContext ctx) {
						return new ReferenceCriteria(project, ctx.getText(), Is);
					}

					@Override
					public Criteria<Issue> visitFuzzyCriteria(FuzzyCriteriaContext ctx) {
						return new FuzzyCriteria(getValue(ctx.getText()));
					}

					@Override
					public Criteria<Issue> visitOperatorCriteria(OperatorCriteriaContext ctx) {
						switch (ctx.operator.getType()) {
							case Confidential:
								return new ConfidentialCriteria();
							case MentionedMe:
								if (!option.withCurrentUserCriteria())
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new MentionedMeCriteria();
							case SubmittedByMe:
								if (!option.withCurrentUserCriteria())
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new SubmittedByMeCriteria();
							case WatchedByMe:
								if (!option.withCurrentUserCriteria())
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new WatchedByMeCriteria();
							case IgnoredByMe:
								if (!option.withCurrentUserCriteria())
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new IgnoredByMeCriteria();
							case CommentedByMe:
								if (!option.withCurrentUserCriteria())
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new CommentedByMeCriteria();
							case FixedInCurrentBuild:
								if (!option.withCurrentBuildCriteria())
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new FixedInCurrentBuildCriteria();
							case FixedInCurrentPullRequest:
								if (!option.withCurrentPullRequestCriteria())
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new FixedInCurrentPullRequestCriteria();
							case FixedInCurrentCommit:
								if (!option.withCurrentCommitCriteria())
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new FixedInCurrentCommitCriteria();
							case CurrentIssue:
								if (!option.withCurrentIssueCriteria())
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new CurrentIssueCriteria();
							default:
								throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
					}

					@Override
					public Criteria<Issue> visitFieldOperatorCriteria(FieldOperatorCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted().getText());
						int operator = ctx.operator.getType();
						if (validate)
							checkField(fieldName, operator, option);
						if (fieldName.equals(NAME_PROJECT)) {
							return new ProjectIsCurrentCriteria();
						} else if (fieldName.equals(IssueSchedule.NAME_ITERATION)) {
							return new IterationEmptyCriteria(operator);
						} else {
							FieldSpec fieldSpec = getGlobalIssueSetting().getFieldSpec(fieldName);
							if (fieldSpec != null)
								return new FieldOperatorCriteria(fieldName, operator, fieldSpec.isAllowMultiple());
							else
								return new FieldOperatorCriteria(fieldName, operator, false);
						}
					}

					@Override
					public Criteria<Issue> visitLinkMatchCriteria(LinkMatchCriteriaContext ctx) {
						String linkName = getValue(ctx.Quoted().getText());
						boolean allMatch = ctx.All() != null;
						Criteria<Issue> criteria = visit(ctx.criteria());
						return new LinkMatchCriteria(linkName, criteria, allMatch);
					}

					public Criteria<Issue> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						var criterias = new ArrayList<Criteria<Issue>>();
						for (var quoted: ctx.criteriaValue.Quoted()) {
							String value = getValue(quoted.getText());
							if (ctx.Mentioned() != null)
								criterias.add(new MentionedUserCriteria(getUser(value)));
							else if (ctx.SubmittedBy() != null)
								criterias.add(new SubmittedByUserCriteria(getUser(value)));
							else if (ctx.WatchedBy() != null)
								criterias.add(new WatchedByUserCriteria(getUser(value)));
							else if (ctx.IgnoredBy() != null)
								criterias.add(new IgnoredByUserCriteria(getUser(value)));
							else if (ctx.CommentedBy() != null)
								criterias.add(new CommentedByUserCriteria(getUser(value)));
							else if (ctx.FixedInBuild() != null)
								criterias.add(new FixedInBuildCriteria(project, value));
							else if (ctx.FixedInPullRequest() != null)
								criterias.add(new FixedInPullRequestCriteria(project, value));
							else if (ctx.FixedInCommit() != null)
								criterias.add(new FixedInCommitCriteria(project, value));
							else if (ctx.HasAny() != null)
								criterias.add(new HasLinkCriteria(value));
							else
								throw new RuntimeException("Unexpected operator: " + ctx.operator.getText());
						}
						return Criteria.orCriterias(criterias);
					}

					@Override
					public Criteria<Issue> visitFixedBetweenCriteria(FixedBetweenCriteriaContext ctx) {
						RevisionCriteriaContext firstRevision = ctx.revisionCriteria(0);
						int firstType = firstRevision.revisionType.getType();
						String firstValue = getValue(firstRevision.Quoted().getText());

						RevisionCriteriaContext secondRevision = ctx.revisionCriteria(1);
						int secondType = secondRevision.revisionType.getType();
						String secondValue = getValue(secondRevision.Quoted().getText());

						return new FixedBetweenCriteria(project, firstType, firstValue, secondType, secondValue);
					}

					@Override
					public Criteria<Issue> visitParensCriteria(ParensCriteriaContext ctx) {
						return visit(ctx.criteria()).withParens(true);
					}

					@Override
					public Criteria<Issue> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.criteriaField.getText());
						int operator = ctx.operator.getType();
						if (validate)
							checkField(fieldName, operator, option);
						
						List<Criteria<Issue>> criterias = new ArrayList<>();
						for (var quoted: ctx.criteriaValue.Quoted()) {
							String value = getValue(quoted.getText());
							var timeTrackingSetting = OneDev.getInstance(SettingService.class)
									.getIssueSetting().getTimeTrackingSetting();
							switch (operator) {
								case IsUntil:
								case IsSince:
									if (fieldName.equals(NAME_SUBMIT_DATE))
										criterias.add(new SubmitDateCriteria(value, operator));
									else if (fieldName.equals(NAME_LAST_ACTIVITY_DATE))
										criterias.add(new LastActivityDateCriteria(value, operator));
									else
										criterias.add(new DateFieldCriteria(fieldName, value, operator));
									break;
								case Contains:
									switch (fieldName) {
										case NAME_TITLE:
											criterias.add(new TitleCriteria(value));
											break;
										case NAME_DESCRIPTION:
											criterias.add(new DescriptionCriteria(value));
											break;
										case NAME_COMMENT:
											criterias.add(new CommentCriteria(value));
											break;
										default:
											criterias.add(new StringFieldCriteria(fieldName, value, operator));
									}
									break;
								case Is:
								case IsNot:
									if (fieldName.equals(NAME_PROJECT)) {
										criterias.add(new ProjectCriteria(value, operator));
									} else if (fieldName.equals(IssueSchedule.NAME_ITERATION)) {
										criterias.add(new IterationCriteria(value, operator));
									} else if (fieldName.equals(NAME_STATE)) {
										criterias.add(new StateCriteria(value, operator));
									} else if (fieldName.equals(NAME_VOTE_COUNT)) {
										criterias.add(new VoteCountCriteria(getIntValue(value), operator));
									} else if (fieldName.equals(NAME_COMMENT_COUNT)) {
										criterias.add(new CommentCountCriteria(getIntValue(value), operator));
									} else if (fieldName.equals(NAME_THUMBS_UP_COUNT)) {
										criterias.add(new ThumbsUpCountCriteria(getIntValue(value), operator));
									} else if (fieldName.equals(NAME_THUMBS_DOWN_COUNT)) {
										criterias.add(new ThumbsDownCountCriteria(getIntValue(value), operator));
									} else if (fieldName.equals(NAME_SMILE_COUNT)) {
										criterias.add(new SmileCountCriteria(getIntValue(value), operator));
									} else if (fieldName.equals(NAME_TADA_COUNT)) {
										criterias.add(new TadaCountCriteria(getIntValue(value), operator));
									} else if (fieldName.equals(NAME_CONFUSED_COUNT)) {
										criterias.add(new ConfusedCountCriteria(getIntValue(value), operator));
									} else if (fieldName.equals(NAME_HEART_COUNT)) {
										criterias.add(new HeartCountCriteria(getIntValue(value), operator));
									} else if (fieldName.equals(NAME_ROCKET_COUNT)) {
										criterias.add(new RocketCountCriteria(getIntValue(value), operator));
									} else if (fieldName.equals(NAME_EYES_COUNT)) {
										criterias.add(new EyesCountCriteria(getIntValue(value), operator));
									} else if (fieldName.equals(Issue.NAME_NUMBER)) {
										criterias.add(new ReferenceCriteria(project, value, operator));
									} else if (fieldName.equals(NAME_ESTIMATED_TIME)) {
										int intValue = timeTrackingSetting.parseWorkingPeriod(value);
										criterias.add(new EstimatedTimeCriteria(intValue, operator));
									} else if (fieldName.equals(NAME_SPENT_TIME)) {
										int intValue = timeTrackingSetting.parseWorkingPeriod(value);
										criterias.add(new SpentTimeCriteria(intValue, operator));
									} else {
										FieldSpec field = getGlobalIssueSetting().getFieldSpec(fieldName);
										if (field instanceof IssueChoiceField) {
											criterias.add(new IssueFieldCriteria(fieldName, project, value, operator));
										} else if (field instanceof BuildChoiceField) {
											criterias.add(new BuildFieldCriteria(fieldName, project, value, field.isAllowMultiple(), operator));
										} else if (field instanceof PullRequestChoiceField) {
											criterias.add(new PullRequestFieldCriteria(fieldName, project, value, operator));
										} else if (field instanceof CommitField) {
											criterias.add(new CommitFieldCriteria(fieldName, project, value, operator));
										} else if (field instanceof BooleanField) {
											criterias.add(new BooleanFieldCriteria(fieldName, getBooleanValue(value), operator));
										} else if (field instanceof IntegerField) {
											criterias.add(new NumericFieldCriteria(fieldName, getIntValue(value), operator));
										} else if (field instanceof ChoiceField) {
											long ordinal = getValueOrdinal((ChoiceField) field, value);
											criterias.add(new ChoiceFieldCriteria(fieldName, value, ordinal, operator, field.isAllowMultiple()));
										} else if (field instanceof UserChoiceField
												|| field instanceof GroupChoiceField) {
											criterias.add(new ChoiceFieldCriteria(fieldName, value, -1, operator, field.isAllowMultiple()));
										} else if (field instanceof IterationChoiceField) {
											criterias.add(new IterationFieldCriteria(fieldName, value, operator, field.isAllowMultiple()));
										} else {
											criterias.add(new StringFieldCriteria(fieldName, value, operator));
										}
									}
									break;
								case IsLessThan:
								case IsGreaterThan:
									switch (fieldName) {
										case NAME_VOTE_COUNT:
											criterias.add(new VoteCountCriteria(getIntValue(value), operator));
											break;
										case NAME_COMMENT_COUNT:
											criterias.add(new CommentCountCriteria(getIntValue(value), operator));
											break;
										case NAME_THUMBS_UP_COUNT:
											criterias.add(new ThumbsUpCountCriteria(getIntValue(value), operator));
											break;
										case NAME_THUMBS_DOWN_COUNT:
											criterias.add(new ThumbsDownCountCriteria(getIntValue(value), operator));
											break;
										case NAME_SMILE_COUNT:
											criterias.add(new SmileCountCriteria(getIntValue(value), operator));
											break;
										case NAME_TADA_COUNT:
											criterias.add(new TadaCountCriteria(getIntValue(value), operator));
											break;
										case NAME_CONFUSED_COUNT:
											criterias.add(new ConfusedCountCriteria(getIntValue(value), operator));
											break;
										case NAME_HEART_COUNT:
											criterias.add(new HeartCountCriteria(getIntValue(value), operator));
											break;
										case NAME_ROCKET_COUNT:
											criterias.add(new RocketCountCriteria(getIntValue(value), operator));
											break;
										case NAME_EYES_COUNT:
											criterias.add(new EyesCountCriteria(getIntValue(value), operator));
											break;
										case NAME_ESTIMATED_TIME: 
											int intValue = value.equals(NAME_SPENT_TIME) ? -1 : timeTrackingSetting.parseWorkingPeriod(value);
											criterias.add(new EstimatedTimeCriteria(intValue, operator));
											break;
										case NAME_SPENT_TIME: 
											intValue = value.equals(NAME_ESTIMATED_TIME) ? -1 : timeTrackingSetting.parseWorkingPeriod(value);
											criterias.add(new SpentTimeCriteria(intValue, operator));
											break;
										case NAME_PROGRESS:
											var floatValue = getFloatValue(value);
											criterias.add(new ProgressCriteria(floatValue, operator));
											break;
										default:
											FieldSpec field = getGlobalIssueSetting().getFieldSpec(fieldName);
											if (field instanceof IntegerField) {
												criterias.add(new NumericFieldCriteria(fieldName, getIntValue(value), operator));
											} else {
												long ordinal;
												if (validate)
													ordinal = getValueOrdinal((ChoiceField) field, value);
												else
													ordinal = 0;
												criterias.add(new ChoiceFieldCriteria(fieldName, value, ordinal, operator, false));
											}
									}
									break;
								case IsBefore:
								case IsAfter:
									criterias.add(new StateCriteria(value, operator));
									break;
								default:
									throw new ExplicitException("Unexpected operator " + getRuleName(operator));
							}
						}
						return operator==IsNot? Criteria.andCriterias(criterias): Criteria.orCriterias(criterias);
					}

					@Override
					public Criteria<Issue> visitOrCriteria(OrCriteriaContext ctx) {
						List<Criteria<Issue>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx : ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrCriteria<>(childCriterias);
					}

					@Override
					public Criteria<Issue> visitAndCriteria(AndCriteriaContext ctx) {
						List<Criteria<Issue>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx : ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndCriteria<>(childCriterias);
					}

					@Override
					public Criteria<Issue> visitNotCriteria(NotCriteriaContext ctx) {
						return new NotCriteria<>(visit(ctx.criteria()));
					}

				}.visit(criteriaContext);
			} else {
				issueCriteria = null;
			}

			List<EntitySort> issueSorts = new ArrayList<>();
			for (OrderContext order : queryContext.order()) {
				var fieldName = getValue(order.Quoted().getText());
				var sortField = SORT_FIELDS.get(fieldName);
				if (validate && sortField == null) {
					FieldSpec fieldSpec = getGlobalIssueSetting().getFieldSpec(fieldName);
					if (validate && !(fieldSpec instanceof ChoiceField) && !(fieldSpec instanceof DateField)
							&& !(fieldSpec instanceof DateTimeField) && !(fieldSpec instanceof IntegerField)
							&& !(fieldSpec instanceof IterationChoiceField)) {
						throw new ExplicitException("Can not order by field: " + fieldName);
					}
				}

				EntitySort issueSort = new EntitySort();
				issueSort.setField(fieldName);
				if (order.direction != null) {
					if (order.direction.getText().equals("desc"))
						issueSort.setDirection(DESCENDING);
					else
						issueSort.setDirection(ASCENDING);
				} else if (sortField != null) {
					issueSort.setDirection(sortField.getDefaultDirection());
				} else {
					issueSort.setDirection(ASCENDING);
				}
				issueSorts.add(issueSort);
			}

			return new IssueQuery(issueCriteria, issueSorts);
		} else {
			return new IssueQuery();
		}
	}

	private static GlobalIssueSetting getGlobalIssueSetting() {
		if (WicketUtils.getPage() instanceof IssueSettingPage)
			return ((IssueSettingPage) WicketUtils.getPage()).getSetting();
		else
			return OneDev.getInstance(SettingService.class).getIssueSetting();
	}

	private static ExplicitException newOperatorException(String fieldName, int operator) {
		return new ExplicitException("Field '" + fieldName + "' is not applicable for operator '" + getRuleName(operator) + "'");
	}

	public static void checkField(String fieldName, int operator, IssueQueryParseOption option) {
		FieldSpec fieldSpec = getGlobalIssueSetting().getFieldSpec(fieldName);
		if (fieldSpec == null && !Issue.QUERY_FIELDS.contains(fieldName))
			throw new ExplicitException("Field not found: " + fieldName);
		switch (operator) {
			case IsEmpty:
			case IsNotEmpty:
				if (Issue.QUERY_FIELDS.contains(fieldName)
						&& !fieldName.equals(IssueSchedule.NAME_ITERATION)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case IsMe:
			case IsNotMe:
				if (!(fieldSpec instanceof UserChoiceField && option.withCurrentUserCriteria()))
					throw newOperatorException(fieldName, operator);
				break;
			case IssueQueryLexer.IsCurrent:
				if (!(fieldName.equals(Issue.NAME_PROJECT) && option.withCurrentProjectCriteria()
						|| fieldSpec instanceof BuildChoiceField && option.withCurrentBuildCriteria()
						|| fieldSpec instanceof PullRequestChoiceField && option.withCurrentPullRequestCriteria()
						|| fieldSpec instanceof CommitField && option.withCurrentCommitCriteria()))
					throw newOperatorException(fieldName, operator);
				break;
			case IsPrevious:
				if (!(fieldSpec instanceof BuildChoiceField && option.withCurrentBuildCriteria()))
					throw newOperatorException(fieldName, operator);
				break;
			case IsUntil:
			case IsSince:
				if (!fieldName.equals(Issue.NAME_SUBMIT_DATE)
						&& !fieldName.equals(Issue.NAME_LAST_ACTIVITY_DATE)
						&& !(fieldSpec instanceof DateField)
						&& !(fieldSpec instanceof DateTimeField)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case Contains:
				if (!fieldName.equals(Issue.NAME_TITLE)
						&& !fieldName.equals(Issue.NAME_DESCRIPTION)
						&& !fieldName.equals(Issue.NAME_COMMENT)
						&& !(fieldSpec instanceof TextField)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case Is:
			case IsNot:
				if (!fieldName.equals(Issue.NAME_PROJECT)
						&& !fieldName.equals(Issue.NAME_ESTIMATED_TIME)
						&& !fieldName.equals(NAME_SPENT_TIME)
						&& !fieldName.equals(Issue.NAME_STATE)
						&& !fieldName.equals(Issue.NAME_VOTE_COUNT)
						&& !fieldName.equals(Issue.NAME_COMMENT_COUNT)
						&& !fieldName.equals(Issue.NAME_THUMBS_UP_COUNT)
						&& !fieldName.equals(Issue.NAME_THUMBS_DOWN_COUNT)
						&& !fieldName.equals(Issue.NAME_SMILE_COUNT)
						&& !fieldName.equals(Issue.NAME_TADA_COUNT)
						&& !fieldName.equals(Issue.NAME_CONFUSED_COUNT)
						&& !fieldName.equals(Issue.NAME_HEART_COUNT)
						&& !fieldName.equals(Issue.NAME_ROCKET_COUNT)
						&& !fieldName.equals(Issue.NAME_EYES_COUNT)
						&& !fieldName.equals(Issue.NAME_NUMBER)
						&& !fieldName.equals(IssueSchedule.NAME_ITERATION)
						&& !(fieldSpec instanceof IssueChoiceField)
						&& !(fieldSpec instanceof PullRequestChoiceField)
						&& !(fieldSpec instanceof BuildChoiceField)
						&& !(fieldSpec instanceof BooleanField)
						&& !(fieldSpec instanceof IntegerField)
						&& !(fieldSpec instanceof CommitField)
						&& !(fieldSpec instanceof ChoiceField)
						&& !(fieldSpec instanceof UserChoiceField)
						&& !(fieldSpec instanceof GroupChoiceField)
						&& !(fieldSpec instanceof IterationChoiceField)
						&& !(fieldSpec instanceof TextField)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case IsBefore:
			case IsAfter:
				if (!fieldName.equals(Issue.NAME_STATE))
					throw newOperatorException(fieldName, operator);
				break;
			case IsLessThan:
			case IsGreaterThan:
				if (!fieldName.equals(Issue.NAME_VOTE_COUNT)
						&& !fieldName.equals(Issue.NAME_ESTIMATED_TIME)
						&& !fieldName.equals(NAME_SPENT_TIME)
						&& !fieldName.equals(NAME_PROGRESS)
						&& !fieldName.equals(Issue.NAME_COMMENT_COUNT)
						&& !fieldName.equals(Issue.NAME_THUMBS_UP_COUNT)
						&& !fieldName.equals(Issue.NAME_THUMBS_DOWN_COUNT)
						&& !fieldName.equals(Issue.NAME_SMILE_COUNT)
						&& !fieldName.equals(Issue.NAME_TADA_COUNT)
						&& !fieldName.equals(Issue.NAME_CONFUSED_COUNT)
						&& !fieldName.equals(Issue.NAME_HEART_COUNT)
						&& !fieldName.equals(Issue.NAME_ROCKET_COUNT)
						&& !fieldName.equals(Issue.NAME_EYES_COUNT)
						&& !fieldName.equals(Issue.NAME_NUMBER)
						&& !(fieldSpec instanceof IntegerField)
						&& !(fieldSpec instanceof ChoiceField && !fieldSpec.isAllowMultiple()))
					throw newOperatorException(fieldName, operator);
				break;
		}
	}

	@Override
	public boolean matches(Issue issue) {
		return getCriteria() == null || getCriteria().matches(issue);
	}

	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(IssueQueryLexer.ruleNames, rule);
	}

	public static int getOperator(String operatorName) {
		return AntlrUtils.getLexerRule(IssueQueryLexer.ruleNames, operatorName);
	}

	public IssueQuery onRenameLink(String oldName, String newName) {
		if (getCriteria() != null)
			getCriteria().onRenameLink(oldName, newName);
		return this;
	}

	public boolean isUsingLink(String linkName) {
		if (getCriteria() != null)
			return getCriteria().isUsingLink(linkName);
		else
			return false;
	}

	public Collection<String> getUndefinedStates() {
		if (getCriteria() != null)
			return getCriteria().getUndefinedStates();
		else
			return new ArrayList<>();
	}

	public Collection<String> getUndefinedFields() {
		Set<String> undefinedFields = new HashSet<>();
		if (getCriteria() != null) {
			undefinedFields.addAll(getCriteria().getUndefinedFields());
		}
		for (EntitySort sort : getSorts()) {
			if (!Issue.QUERY_FIELDS.contains(sort.getField())
					&& getGlobalIssueSetting().getFieldSpec(sort.getField()) == null) {
				undefinedFields.add(sort.getField());
			}
		}
		return undefinedFields;
	}

	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		if (getCriteria() != null)
			return getCriteria().getUndefinedFieldValues();
		else
			return new HashSet<>();
	}

	public boolean fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		if (getCriteria() != null)
			return getCriteria().fixUndefinedStates(resolutions);
		else
			return true;
	}

	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		if (getCriteria() != null && !getCriteria().fixUndefinedFields(resolutions))
			return false;
		for (Iterator<EntitySort> it = getSorts().iterator(); it.hasNext(); ) {
			EntitySort sort = it.next();
			UndefinedFieldResolution resolution = resolutions.get(sort.getField());
			if (resolution != null) {
				if (resolution.getFixType() == FixType.CHANGE_TO_ANOTHER_FIELD)
					sort.setField(resolution.getNewField());
				else
					it.remove();
			}
		}
		return true;
	}

	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		if (getCriteria() != null)
			return getCriteria().fixUndefinedFieldValues(resolutions);
		else
			return true;
	}

	public static IssueQuery merge(IssueQuery baseQuery, IssueQuery query) {
		List<Criteria<Issue>> criterias = new ArrayList<>();
		if (baseQuery.getCriteria() != null)
			criterias.add(baseQuery.getCriteria());
		if (query.getCriteria() != null)
			criterias.add(query.getCriteria());
		return new IssueQuery(Criteria.andCriterias(criterias), query.getSorts(), baseQuery.getSorts());
	}

}
