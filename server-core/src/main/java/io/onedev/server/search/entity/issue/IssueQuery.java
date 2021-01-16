package io.onedev.server.search.entity.issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.fieldspec.BooleanField;
import io.onedev.server.model.support.issue.fieldspec.BuildChoiceField;
import io.onedev.server.model.support.issue.fieldspec.ChoiceField;
import io.onedev.server.model.support.issue.fieldspec.CommitField;
import io.onedev.server.model.support.issue.fieldspec.DateField;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.model.support.issue.fieldspec.GroupChoiceField;
import io.onedev.server.model.support.issue.fieldspec.IssueChoiceField;
import io.onedev.server.model.support.issue.fieldspec.NumberField;
import io.onedev.server.model.support.issue.fieldspec.PullRequestChoiceField;
import io.onedev.server.model.support.issue.fieldspec.TextField;
import io.onedev.server.model.support.issue.fieldspec.UserChoiceField;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.issue.IssueQueryParser.AndCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.CriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.FieldOperatorCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.FixedBetweenCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.NotCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.OperatorCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.OperatorValueCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.OrCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.OrderContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.ParensCriteriaContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.QueryContext;
import io.onedev.server.search.entity.issue.IssueQueryParser.RevisionCriteriaContext;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;
import io.onedev.server.web.util.WicketUtils;

public class IssueQuery extends EntityQuery<Issue> {

	private static final long serialVersionUID = 1L;

	private final IssueCriteria criteria;
	
	private final List<EntitySort> sorts;
	
	public IssueQuery(@Nullable IssueCriteria criteria, List<EntitySort> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	public IssueQuery(@Nullable IssueCriteria criteria) {
		this(criteria, new ArrayList<>());
	}
	
	public IssueQuery() {
		this(null);
	}
	
	@Nullable
	public IssueCriteria getCriteria() {
		return criteria;
	}

	public List<EntitySort> getSorts() {
		return sorts;
	}

	public static IssueQuery parse(@Nullable Project project, @Nullable String queryString, 
			boolean validate, boolean withCurrentUserCriteria, boolean withCurrentBuildCriteria, 
			boolean withCurrentPullRequestCriteria, boolean withCurrentCommitCriteria) {
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
			IssueCriteria issueCriteria;
			if (criteriaContext != null) {
				issueCriteria = new IssueQueryBaseVisitor<IssueCriteria>() {

					private long getValueOrdinal(ChoiceField field, String value) {
						List<String> choices = new ArrayList<>(field.getChoiceProvider().getChoices(true).keySet());
						return choices.indexOf(value);
					}
					
					@Override
					public IssueCriteria visitOperatorCriteria(OperatorCriteriaContext ctx) {
						switch (ctx.operator.getType()) {
						case IssueQueryLexer.SubmittedByMe:
							if (!withCurrentUserCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new SubmittedByMeCriteria();
						case IssueQueryLexer.FixedInCurrentBuild:
							if (!withCurrentBuildCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new FixedInCurrentBuildCriteria();
						case IssueQueryLexer.FixedInCurrentPullRequest:
							if (!withCurrentPullRequestCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new FixedInCurrentPullRequestCriteria();
						case IssueQueryLexer.FixedInCurrentCommit:
							if (!withCurrentCommitCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new FixedInCurrentCommitCriteria();
						default:
							throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
					}
					
					@Override
					public IssueCriteria visitFieldOperatorCriteria(FieldOperatorCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted().getText());
						int operator = ctx.operator.getType();
						if (validate) {
							checkField(fieldName, operator, withCurrentUserCriteria, withCurrentBuildCriteria, 
									withCurrentPullRequestCriteria, withCurrentCommitCriteria);
						}
						if (fieldName.equals(Issue.NAME_MILESTONE)) {
							return new MilestoneIsEmptyCriteria();
						} else {
							FieldSpec fieldSpec = getGlobalIssueSetting().getFieldSpec(fieldName);
							if (fieldSpec != null)
								return new FieldOperatorCriteria(fieldName, operator, fieldSpec.isAllowMultiple());
							else
								return new FieldOperatorCriteria(fieldName, operator, false);
						}
					}
					
					public IssueCriteria visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						String value = getValue(ctx.Quoted().getText());
						if (ctx.SubmittedBy() != null) 
							return new SubmittedByCriteria(getUser(value));
						else if (ctx.FixedInBuild() != null) 
							return new FixedInBuildCriteria(project, value);
						else if (ctx.FixedInPullRequest() != null) 
							return new FixedInPullRequestCriteria(project, value);
						else if (ctx.FixedInCommit() != null) 
							return new FixedInCommitCriteria(project, value);
						else 
							throw new RuntimeException("Unexpected operator: " + ctx.operator.getText());
					}
					
					@Override
					public IssueCriteria visitFixedBetweenCriteria(FixedBetweenCriteriaContext ctx) {
						RevisionCriteriaContext firstRevision = ctx.revisionCriteria(0);
						int firstType = firstRevision.revisionType.getType();
						String firstValue = getValue(firstRevision.Quoted().getText());
						
						RevisionCriteriaContext secondRevision = ctx.revisionCriteria(1);
						int secondType = secondRevision.revisionType.getType();
						String secondValue = getValue(secondRevision.Quoted().getText());
						
						return new FixedBetweenCriteria(project, firstType, firstValue, secondType, secondValue);
					}
					
					@Override
					public IssueCriteria visitParensCriteria(ParensCriteriaContext ctx) {
						return (IssueCriteria) visit(ctx.criteria()).withParens(true);
					}

					@Override
					public IssueCriteria visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted(0).getText());
						String value = getValue(ctx.Quoted(1).getText());
						int operator = ctx.operator.getType();
						if (validate) {
							checkField(fieldName, operator, withCurrentUserCriteria, withCurrentBuildCriteria, 
									withCurrentPullRequestCriteria, withCurrentCommitCriteria);
						}
						
						switch (operator) {
						case IssueQueryLexer.IsUntil:
						case IssueQueryLexer.IsSince:
							if (fieldName.equals(Issue.NAME_SUBMIT_DATE)) 
								return new SubmitDateCriteria(value, operator);
							else if (fieldName.equals(Issue.NAME_UPDATE_DATE))
								return new UpdateDateCriteria(value, operator);
							else 
								return new DateFieldCriteria(fieldName, value, operator);
						case IssueQueryLexer.Contains:
							if (fieldName.equals(Issue.NAME_TITLE)) {
								return new TitleCriteria(value);
							} else if (fieldName.equals(Issue.NAME_DESCRIPTION)) {
								return new DescriptionCriteria(value);
							} else {
								return new CommentCriteria(value);
							}
						case IssueQueryLexer.Is:
							if (fieldName.equals(Issue.NAME_PROJECT)) {
								return new ProjectCriteria(value);
							} else if (fieldName.equals(Issue.NAME_MILESTONE)) {
								return new MilestoneCriteria(value);
							} else if (fieldName.equals(Issue.NAME_STATE)) {
								return new StateCriteria(value);
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
								} else if (field instanceof NumberField) {
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
								if (field instanceof NumberField) {
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
						default:
							throw new ExplicitException("Unexpected operator " + getRuleName(operator));
						}
					}
					
					@Override
					public IssueCriteria visitOrCriteria(OrCriteriaContext ctx) {
						List<IssueCriteria> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrIssueCriteria(childCriterias);
					}

					@Override
					public IssueCriteria visitAndCriteria(AndCriteriaContext ctx) {
						List<IssueCriteria> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndIssueCriteria(childCriterias);
					}

					@Override
					public IssueCriteria visitNotCriteria(NotCriteriaContext ctx) {
						return new NotIssueCriteria(visit(ctx.criteria()));
					}
					
				}.visit(criteriaContext);
			} else {
				issueCriteria = null;
			}

			List<EntitySort> issueSorts = new ArrayList<>();
			for (OrderContext order: queryContext.order()) {
				String fieldName = getValue(order.Quoted().getText());
				if (validate && !Issue.ORDER_FIELDS.containsKey(fieldName)) {
					FieldSpec fieldSpec = getGlobalIssueSetting().getFieldSpec(fieldName);
					if (validate && !(fieldSpec instanceof ChoiceField) && !(fieldSpec instanceof DateField) 
							&& !(fieldSpec instanceof NumberField)) {
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
			return ((IssueSettingPage)WicketUtils.getPage()).getSetting();
		else
			return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	private static ExplicitException newOperatorException(String fieldName, int operator) {
		return new ExplicitException("Field '" + fieldName + "' is not applicable for operator '" + getRuleName(operator) + "'");
	}
	
	public static void checkField(String fieldName, int operator, 
			boolean withCurrentUserCriteria, boolean withCurrentBuildCriteria, 
			boolean withCurrentPullRequestCriteria, boolean withCurrentCommitCriteria) {
		FieldSpec fieldSpec = getGlobalIssueSetting().getFieldSpec(fieldName);
		if (fieldSpec == null && !Issue.QUERY_FIELDS.contains(fieldName))
			throw new ExplicitException("Field not found: " + fieldName);
		switch (operator) {
		case IssueQueryLexer.IsEmpty:
			if (Issue.QUERY_FIELDS.contains(fieldName) 
					&& !fieldName.equals(Issue.NAME_MILESTONE)) { 
				throw newOperatorException(fieldName, operator);
			}
			break;
		case IssueQueryLexer.IsMe:
			if (!(fieldSpec instanceof UserChoiceField && withCurrentUserCriteria))
				throw newOperatorException(fieldName, operator);
			break;
		case IssueQueryLexer.IsCurrent:
			if (!(fieldSpec instanceof BuildChoiceField && withCurrentBuildCriteria 
					|| fieldSpec instanceof PullRequestChoiceField && withCurrentPullRequestCriteria)
					|| fieldSpec instanceof CommitField && withCurrentCommitCriteria)
				throw newOperatorException(fieldName, operator);
			break;
		case IssueQueryLexer.IsPrevious:
			if (!(fieldSpec instanceof BuildChoiceField && withCurrentBuildCriteria)) 
				throw newOperatorException(fieldName, operator);
			break;
		case IssueQueryLexer.IsUntil:
		case IssueQueryLexer.IsSince:
			if (!fieldName.equals(Issue.NAME_SUBMIT_DATE) 
					&& !fieldName.equals(Issue.NAME_UPDATE_DATE) 
					&& !(fieldSpec instanceof DateField)) {
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
					&& !fieldName.equals(Issue.NAME_MILESTONE)
					&& !(fieldSpec instanceof IssueChoiceField)
					&& !(fieldSpec instanceof PullRequestChoiceField)
					&& !(fieldSpec instanceof BuildChoiceField)
					&& !(fieldSpec instanceof BooleanField)
					&& !(fieldSpec instanceof NumberField) 
					&& !(fieldSpec instanceof CommitField) 
					&& !(fieldSpec instanceof ChoiceField) 
					&& !(fieldSpec instanceof UserChoiceField)
					&& !(fieldSpec instanceof GroupChoiceField)
					&& !(fieldSpec instanceof TextField)) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		case IssueQueryLexer.IsLessThan:
		case IssueQueryLexer.IsGreaterThan:
			if (!fieldName.equals(Issue.NAME_VOTE_COUNT)
					&& !fieldName.equals(Issue.NAME_COMMENT_COUNT)
					&& !fieldName.equals(Issue.NAME_NUMBER)
					&& !(fieldSpec instanceof NumberField) 
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
		for (EntitySort sort: sorts) {
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
		if (criteria != null) 
			return criteria.fixUndefinedFields(resolutions);
		else 
			return true;
	}
	
	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		if (criteria != null) 
			return criteria.fixUndefinedFieldValues(resolutions);
		else 
			return true;
	}
	
	public static IssueQuery merge(IssueQuery query1, IssueQuery query2) {
		List<IssueCriteria> criterias = new ArrayList<>();
		if (query1.getCriteria() != null)
			criterias.add(query1.getCriteria());
		if (query2.getCriteria() != null)
			criterias.add(query2.getCriteria());
		List<EntitySort> sorts = new ArrayList<>();
		sorts.addAll(query1.getSorts());
		sorts.addAll(query2.getSorts());
		return new IssueQuery(IssueCriteria.and(criterias), sorts);
	}

}
