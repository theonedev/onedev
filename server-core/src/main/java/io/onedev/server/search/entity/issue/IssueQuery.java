package io.onedev.server.search.entity.issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.spec.BooleanField;
import io.onedev.server.model.support.issue.field.spec.BuildChoiceField;
import io.onedev.server.model.support.issue.field.spec.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.CommitField;
import io.onedev.server.model.support.issue.field.spec.DateField;
import io.onedev.server.model.support.issue.field.spec.DateTimeField;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.GroupChoiceField;
import io.onedev.server.model.support.issue.field.spec.IntegerField;
import io.onedev.server.model.support.issue.field.spec.IssueChoiceField;
import io.onedev.server.model.support.issue.field.spec.MilestoneChoiceField;
import io.onedev.server.model.support.issue.field.spec.PullRequestChoiceField;
import io.onedev.server.model.support.issue.field.spec.TextField;
import io.onedev.server.model.support.issue.field.spec.UserChoiceField;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.issue.IssueQueryParser.AndCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.CriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.FieldOperatorCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.FixedBetweenCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.LinkMatchCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.NotCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.OperatorCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.OperatorValueCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.OrCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.OrderContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.ParensCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.QueryContext;
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

public class IssueQuery extends EntityQuery<Issue> implements Comparator<Issue> {

	private static final long serialVersionUID = 1L;

	private final Criteria<Issue> criteria;

	private final List<EntitySort> sorts;

	public IssueQuery(@Nullable Criteria<Issue> criteria, List<EntitySort> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	public IssueQuery(@Nullable Criteria<Issue> criteria) {
		this(criteria, new ArrayList<>());
	}

	public IssueQuery() {
		this(null);
	}

	@Nullable
	public Criteria<Issue> getCriteria() {
		return criteria;
	}

	public List<EntitySort> getSorts() {
		return sorts;
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
					throw new RuntimeException("Malformed issue query", e);
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
					public Criteria<Issue> visitOperatorCriteria(OperatorCriteriaContext ctx) {
						switch (ctx.operator.getType()) {
							case IssueQueryLexer.Confidential:
								return new ConfidentialCriteria();
							case IssueQueryLexer.MentionedMe:
								if (!option.withCurrentUserCriteria())
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new MentionedMeCriteria();
							case IssueQueryLexer.SubmittedByMe:
								if (!option.withCurrentUserCriteria())
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new SubmittedByMeCriteria();
							case IssueQueryLexer.FixedInCurrentBuild:
								if (!option.withCurrentBuildCriteria())
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new FixedInCurrentBuildCriteria();
							case IssueQueryLexer.FixedInCurrentPullRequest:
								if (!option.withCurrentPullRequestCriteria())
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new FixedInCurrentPullRequestCriteria();
							case IssueQueryLexer.FixedInCurrentCommit:
								if (!option.withCurrentCommitCriteria())
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new FixedInCurrentCommitCriteria();
							case IssueQueryLexer.CurrentIssue:
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
						if (fieldName.equals(Issue.NAME_PROJECT)) {
							return new ProjectIsCurrentCriteria();
						} else if (fieldName.equals(IssueSchedule.NAME_MILESTONE)) {
							return new MilestoneIsEmptyCriteria();
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
						String value = getValue(ctx.Quoted().getText());
						if (ctx.Mentioned() != null)
							return new MentionedCriteria(getUser(value));
						if (ctx.SubmittedBy() != null)
							return new SubmittedByCriteria(getUser(value));
						else if (ctx.FixedInBuild() != null)
							return new FixedInBuildCriteria(project, value);
						else if (ctx.FixedInPullRequest() != null)
							return new FixedInPullRequestCriteria(project, value);
						else if (ctx.FixedInCommit() != null)
							return new FixedInCommitCriteria(project, value);
						else if (ctx.HasAny() != null)
							return new HasLinkCriteria(value);
						else
							throw new RuntimeException("Unexpected operator: " + ctx.operator.getText());
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
						return (Criteria<Issue>) visit(ctx.criteria()).withParens(true);
					}

					@Override
					public Criteria<Issue> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted(0).getText());
						String value = getValue(ctx.Quoted(1).getText());
						int operator = ctx.operator.getType();
						if (validate)
							checkField(fieldName, operator, option);

						switch (operator) {
							case IssueQueryLexer.IsUntil:
							case IssueQueryLexer.IsSince:
								if (fieldName.equals(Issue.NAME_SUBMIT_DATE))
									return new SubmitDateCriteria(value, operator);
								else if (fieldName.equals(Issue.NAME_LAST_ACTIVITY_DATE))
									return new LastActivityDateCriteria(value, operator);
								else
									return new DateFieldCriteria(fieldName, value, operator);
							case IssueQueryLexer.Contains:
								if (fieldName.equals(Issue.NAME_TITLE)) {
									return new TitleCriteria(value);
								} else if (fieldName.equals(Issue.NAME_DESCRIPTION)) {
									return new DescriptionCriteria(value);
								} else if (fieldName.equals(Issue.NAME_COMMENT)) {
									return new CommentCriteria(value);
								} else {
									return new StringFieldCriteria(fieldName, value, operator);
								}
							case IssueQueryLexer.Is:
								if (fieldName.equals(Issue.NAME_PROJECT)) {
									return new ProjectCriteria(value);
								} else if (fieldName.equals(IssueSchedule.NAME_MILESTONE)) {
									return new MilestoneCriteria(value);
								} else if (fieldName.equals(Issue.NAME_STATE)) {
									return new StateCriteria(value, operator);
								} else if (fieldName.equals(Issue.NAME_VOTE_COUNT)) {
									return new VoteCountCriteria(getIntValue(value), operator);
								} else if (fieldName.equals(Issue.NAME_COMMENT_COUNT)) {
									return new CommentCountCriteria(getIntValue(value), operator);
								} else if (fieldName.equals(Issue.NAME_NUMBER)) {
									return new NumberCriteria(project, value, operator);
								} else {
									FieldSpec field = getGlobalIssueSetting().getFieldSpec(fieldName);
									if (field instanceof IssueChoiceField) {
										return new IssueFieldCriteria(fieldName, project, value);
									} else if (field instanceof BuildChoiceField) {
										return new BuildFieldCriteria(fieldName, project, value, field.isAllowMultiple());
									} else if (field instanceof PullRequestChoiceField) {
										return new PullRequestFieldCriteria(fieldName, project, value);
									} else if (field instanceof CommitField) {
										return new CommitFieldCriteria(fieldName, project, value);
									} else if (field instanceof BooleanField) {
										return new BooleanFieldCriteria(fieldName, getBooleanValue(value));
									} else if (field instanceof IntegerField) {
										return new NumericFieldCriteria(fieldName, getIntValue(value), operator);
									} else if (field instanceof ChoiceField) {
										long ordinal = getValueOrdinal((ChoiceField) field, value);
										return new ChoiceFieldCriteria(fieldName, value, ordinal, operator, field.isAllowMultiple());
									} else if (field instanceof UserChoiceField
											|| field instanceof GroupChoiceField) {
										return new ChoiceFieldCriteria(fieldName, value, -1, operator, field.isAllowMultiple());
									} else {
										return new StringFieldCriteria(fieldName, value, operator);
									}
								}
							case IssueQueryLexer.IsLessThan:
							case IssueQueryLexer.IsGreaterThan:
								if (fieldName.equals(Issue.NAME_VOTE_COUNT)) {
									return new VoteCountCriteria(getIntValue(value), operator);
								} else if (fieldName.equals(Issue.NAME_COMMENT_COUNT)) {
									return new CommentCountCriteria(getIntValue(value), operator);
								} else if (fieldName.equals(Issue.NAME_NUMBER)) {
									return new NumberCriteria(project, value, operator);
								} else {
									FieldSpec field = getGlobalIssueSetting().getFieldSpec(fieldName);
									if (field instanceof IntegerField) {
										return new NumericFieldCriteria(fieldName, getIntValue(value), operator);
									} else {
										long ordinal;
										if (validate)
											ordinal = getValueOrdinal((ChoiceField) field, value);
										else
											ordinal = 0;
										return new ChoiceFieldCriteria(fieldName, value, ordinal, operator, false);
									}
								}
							case IssueQueryLexer.IsBefore:
							case IssueQueryLexer.IsAfter:
								return new StateCriteria(value, operator);
							default:
								throw new ExplicitException("Unexpected operator " + getRuleName(operator));
						}
					}

					@Override
					public Criteria<Issue> visitOrCriteria(OrCriteriaContext ctx) {
						List<Criteria<Issue>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx : ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrCriteria<Issue>(childCriterias);
					}

					@Override
					public Criteria<Issue> visitAndCriteria(AndCriteriaContext ctx) {
						List<Criteria<Issue>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx : ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndCriteria<Issue>(childCriterias);
					}

					@Override
					public Criteria<Issue> visitNotCriteria(NotCriteriaContext ctx) {
						return new NotCriteria<Issue>(visit(ctx.criteria()));
					}

				}.visit(criteriaContext);
			} else {
				issueCriteria = null;
			}

			List<EntitySort> issueSorts = new ArrayList<>();
			for (OrderContext order : queryContext.order()) {
				String fieldName = getValue(order.Quoted().getText());
				if (validate && !Issue.ORDER_FIELDS.containsKey(fieldName)) {
					FieldSpec fieldSpec = getGlobalIssueSetting().getFieldSpec(fieldName);
					if (validate && !(fieldSpec instanceof ChoiceField) && !(fieldSpec instanceof DateField)
							&& !(fieldSpec instanceof DateTimeField) && !(fieldSpec instanceof IntegerField)
							&& !(fieldSpec instanceof MilestoneChoiceField)) {
						throw new ExplicitException("Can not order by field: " + fieldName);
					}
				}

				EntitySort issueSort = new EntitySort();
				issueSort.setField(fieldName);
				if (order.direction != null && order.direction.getText().equals("desc"))
					issueSort.setDirection(Direction.DESCENDING);
				else
					issueSort.setDirection(Direction.ASCENDING);
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
			return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}

	private static ExplicitException newOperatorException(String fieldName, int operator) {
		return new ExplicitException("Field '" + fieldName + "' is not applicable for operator '" + getRuleName(operator) + "'");
	}

	public static void checkField(String fieldName, int operator, IssueQueryParseOption option) {
		FieldSpec fieldSpec = getGlobalIssueSetting().getFieldSpec(fieldName);
		if (fieldSpec == null && !Issue.QUERY_FIELDS.contains(fieldName))
			throw new ExplicitException("Field not found: " + fieldName);
		switch (operator) {
			case IssueQueryLexer.IsEmpty:
				if (Issue.QUERY_FIELDS.contains(fieldName)
						&& !fieldName.equals(IssueSchedule.NAME_MILESTONE)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case IssueQueryLexer.IsMe:
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
			case IssueQueryLexer.IsPrevious:
				if (!(fieldSpec instanceof BuildChoiceField && option.withCurrentBuildCriteria()))
					throw newOperatorException(fieldName, operator);
				break;
			case IssueQueryLexer.IsUntil:
			case IssueQueryLexer.IsSince:
				if (!fieldName.equals(Issue.NAME_SUBMIT_DATE)
						&& !fieldName.equals(Issue.NAME_LAST_ACTIVITY_DATE)
						&& !(fieldSpec instanceof DateField)
						&& !(fieldSpec instanceof DateTimeField)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case IssueQueryLexer.Contains:
				if (!fieldName.equals(Issue.NAME_TITLE)
						&& !fieldName.equals(Issue.NAME_DESCRIPTION)
						&& !fieldName.equals(Issue.NAME_COMMENT)
						&& !(fieldSpec instanceof TextField)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case IssueQueryLexer.Is:
				if (!fieldName.equals(Issue.NAME_PROJECT)
						&& !fieldName.equals(Issue.NAME_STATE)
						&& !fieldName.equals(Issue.NAME_VOTE_COUNT)
						&& !fieldName.equals(Issue.NAME_COMMENT_COUNT)
						&& !fieldName.equals(Issue.NAME_NUMBER)
						&& !fieldName.equals(IssueSchedule.NAME_MILESTONE)
						&& !(fieldSpec instanceof IssueChoiceField)
						&& !(fieldSpec instanceof PullRequestChoiceField)
						&& !(fieldSpec instanceof BuildChoiceField)
						&& !(fieldSpec instanceof BooleanField)
						&& !(fieldSpec instanceof IntegerField)
						&& !(fieldSpec instanceof CommitField)
						&& !(fieldSpec instanceof ChoiceField)
						&& !(fieldSpec instanceof UserChoiceField)
						&& !(fieldSpec instanceof GroupChoiceField)
						&& !(fieldSpec instanceof TextField)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case IssueQueryLexer.IsBefore:
			case IssueQueryLexer.IsAfter:
				if (!fieldName.equals(Issue.NAME_STATE))
					throw newOperatorException(fieldName, operator);
				break;
			case IssueQueryLexer.IsLessThan:
			case IssueQueryLexer.IsGreaterThan:
				if (!fieldName.equals(Issue.NAME_VOTE_COUNT)
						&& !fieldName.equals(Issue.NAME_COMMENT_COUNT)
						&& !fieldName.equals(Issue.NAME_NUMBER)
						&& !(fieldSpec instanceof IntegerField)
						&& !(fieldSpec instanceof ChoiceField && !fieldSpec.isAllowMultiple()))
					throw newOperatorException(fieldName, operator);
				break;
		}
	}

	@Override
	public boolean matches(Issue issue) {
		return criteria == null || criteria.matches(issue);
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
		if (criteria != null)
			return criteria.getUndefinedStates();
		else
			return new ArrayList<>();
	}

	public Collection<String> getUndefinedFields() {
		Set<String> undefinedFields = new HashSet<>();
		if (criteria != null) {
			undefinedFields.addAll(criteria.getUndefinedFields());
		}
		for (EntitySort sort : sorts) {
			if (!Issue.QUERY_FIELDS.contains(sort.getField())
					&& getGlobalIssueSetting().getFieldSpec(sort.getField()) == null) {
				undefinedFields.add(sort.getField());
			}
		}
		return undefinedFields;
	}

	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		if (criteria != null)
			return criteria.getUndefinedFieldValues();
		else
			return new HashSet<>();
	}

	public boolean fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		if (criteria != null)
			return criteria.fixUndefinedStates(resolutions);
		else
			return true;
	}

	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		if (criteria != null && !criteria.fixUndefinedFields(resolutions))
			return false;
		for (Iterator<EntitySort> it = sorts.iterator(); it.hasNext(); ) {
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
		if (criteria != null)
			return criteria.fixUndefinedFieldValues(resolutions);
		else
			return true;
	}

	public static IssueQuery merge(IssueQuery query1, IssueQuery query2) {
		List<Criteria<Issue>> criterias = new ArrayList<>();
		if (query1.getCriteria() != null)
			criterias.add(query1.getCriteria());
		if (query2.getCriteria() != null)
			criterias.add(query2.getCriteria());
		List<EntitySort> sorts = new ArrayList<>();
		sorts.addAll(query1.getSorts());
		sorts.addAll(query2.getSorts());
		return new IssueQuery(Criteria.andCriterias(criterias), sorts);
	}

	@Override
	public int compare(Issue o1, Issue o2) {
		for (EntitySort sort : getSorts()) {
			int result = Issue.ORDER_FIELDS.get(sort.getField()).getComparator().compare(o1, o2);
			if (sort.getDirection() == Direction.DESCENDING)
				result *= -1;
			if (result != 0)
				return result;
		}
		return 0;
	}

}
