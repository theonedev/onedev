package io.onedev.server.search.entity.issue;

import static io.onedev.server.util.IssueConstants.FIELD_COMMENT;
import static io.onedev.server.util.IssueConstants.FIELD_COMMENT_COUNT;
import static io.onedev.server.util.IssueConstants.FIELD_DESCRIPTION;
import static io.onedev.server.util.IssueConstants.FIELD_MILESTONE;
import static io.onedev.server.util.IssueConstants.FIELD_NUMBER;
import static io.onedev.server.util.IssueConstants.FIELD_STATE;
import static io.onedev.server.util.IssueConstants.FIELD_SUBMITTER;
import static io.onedev.server.util.IssueConstants.FIELD_SUBMIT_DATE;
import static io.onedev.server.util.IssueConstants.FIELD_TITLE;
import static io.onedev.server.util.IssueConstants.FIELD_UPDATE_DATE;
import static io.onedev.server.util.IssueConstants.FIELD_VOTE_COUNT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.exception.OneException;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.setting.GlobalIssueSetting;
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
import io.onedev.server.util.IssueConstants;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.util.inputspec.BuildChoiceInput;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.IssueChoiceInput;
import io.onedev.server.util.inputspec.PullRequestChoiceInput;
import io.onedev.server.util.inputspec.booleaninput.BooleanInput;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.dateinput.DateInput;
import io.onedev.server.util.inputspec.groupchoiceinput.GroupChoiceInput;
import io.onedev.server.util.inputspec.numberinput.NumberInput;
import io.onedev.server.util.inputspec.textinput.TextInput;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.web.page.admin.issuesetting.GlobalIssueSettingPage;
import io.onedev.server.web.page.project.issueworkflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.util.WicketUtils;

public class IssueQuery extends EntityQuery<Issue> {

	private static final long serialVersionUID = 1L;

	private final IssueCriteria criteria;
	
	private final List<EntitySort> sorts;
	
	public IssueQuery(@Nullable IssueCriteria criteria, List<EntitySort> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	public IssueQuery() {
		this(null, new ArrayList<>());
	}
	
	@Nullable
	public IssueCriteria getCriteria() {
		return criteria;
	}

	public List<EntitySort> getSorts() {
		return sorts;
	}

	public static IssueQuery parse(@Nullable Project project, @Nullable String queryString, boolean validate) {
		if (queryString != null) {
			CharStream is = CharStreams.fromString(queryString); 
			IssueQueryLexer lexer = new IssueQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new OneException("Malformed query syntax", e);
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			IssueQueryParser parser = new IssueQueryParser(tokens);
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
			IssueCriteria issueCriteria;
			if (criteriaContext != null) {
				issueCriteria = new IssueQueryBaseVisitor<IssueCriteria>() {

					private long getValueOrdinal(ChoiceInput field, String value) {
						List<String> choices = new ArrayList<>(field.getChoiceProvider().getChoices(true).keySet());
						return choices.indexOf(value);
					}
					
					@Override
					public IssueCriteria visitOperatorCriteria(OperatorCriteriaContext ctx) {
						switch (ctx.operator.getType()) {
						case IssueQueryLexer.Mine:
							return new MineCriteria();
						case IssueQueryLexer.Outstanding:
							return new OutstandingCriteria();
						case IssueQueryLexer.Closed:
							return new ClosedCriteria();
						case IssueQueryLexer.SubmittedByMe:
							return new SubmittedByMeCriteria();
						default:
							throw new OneException("Unexpected operator: " + ctx.operator.getText());
						}
					}
					
					@Override
					public IssueCriteria visitFieldOperatorCriteria(FieldOperatorCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted().getText());
						int operator = ctx.operator.getType();
						if (validate)
							checkField(fieldName, operator);
						if (fieldName.equals(FIELD_MILESTONE))
							return new MilestoneCriteria(null);
						else if (fieldName.equals(FIELD_DESCRIPTION))
							return new DescriptionCriteria(null);
						else if (fieldName.equals(FIELD_SUBMITTER))
							return new SubmittedByMeCriteria();
						else
							return new FieldOperatorCriteria(fieldName, operator);
					}
					
					public IssueCriteria visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						String value = getValue(ctx.Quoted().getText());
						if (ctx.SubmittedBy() != null) {
							return new SubmittedByCriteria(getUser(value), value);
						} else if (ctx.FixedInBuild() != null) {
							return new FixedInCriteria(getBuild(project, value));
						} else {
							throw new RuntimeException("Unexpected operator: " + ctx.operator.getText());
						}
					}
					
					private ObjectId getCommitId(RevisionCriteriaContext revision) {
						String value = getValue(revision.Quoted().getText());
						if (revision.Build() != null) {
							return ObjectId.fromString(getBuild(project, value).getCommitHash());
						} else {
							try {
								return project.getRepository().resolve(value);
							} catch (RevisionSyntaxException | IOException e) {
								throw new OneException("Invalid revision: " + value);
							}
						}
					}
					
					public IssueCriteria visitFixedBetweenCriteria(FixedBetweenCriteriaContext ctx) {
						RevisionCriteriaContext sinceRevision = ctx.revisionCriteria(0);
						int sinceType = sinceRevision.revisionType.getType();
						String sinceValue = getValue(sinceRevision.Quoted().getText());
						ObjectId sinceCommitId = getCommitId(sinceRevision);
						
						RevisionCriteriaContext untilRevision = ctx.revisionCriteria(1);
						int untilType = untilRevision.revisionType.getType();
						String untilValue = getValue(untilRevision.Quoted().getText());
						ObjectId untilCommitId = getCommitId(untilRevision);
						return new FixedBetweenCriteria(sinceType, sinceValue, sinceCommitId, untilType, untilValue, untilCommitId);
					}
					
					@Override
					public IssueCriteria visitParensCriteria(ParensCriteriaContext ctx) {
						return visit(ctx.criteria());
					}

					@Override
					public IssueCriteria visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted(0).getText());
						String value = getValue(ctx.Quoted(1).getText());
						int operator = ctx.operator.getType();
						if (validate)
							checkField(fieldName, operator);
						
						switch (operator) {
						case IssueQueryLexer.IsBefore:
						case IssueQueryLexer.IsAfter:
							Date dateValue = getDateValue(value);
							if (fieldName.equals(FIELD_SUBMIT_DATE)) 
								return new SubmitDateCriteria(dateValue, value, operator);
							else if (fieldName.equals(FIELD_UPDATE_DATE))
								return new UpdateDateCriteria(dateValue, value, operator);
							else 
								return new DateFieldCriteria(fieldName, dateValue, value, operator);
						case IssueQueryLexer.Contains:
							if (fieldName.equals(FIELD_TITLE)) {
								return new TitleCriteria(value);
							} else if (fieldName.equals(FIELD_DESCRIPTION)) {
								return new DescriptionCriteria(value);
							} else if (fieldName.equals(FIELD_COMMENT)) {
								return new CommentCriteria(value);
							} else {
								InputSpec fieldSpec = getIssueSetting().getFieldSpec(fieldName);
								if (fieldSpec instanceof TextInput) {
									return new StringFieldCriteria(fieldName, value, operator);
								} else {
									long ordinal;
									if (validate)
										ordinal = getValueOrdinal((ChoiceInput) fieldSpec, value);
									else
										ordinal = 0;
									return new ChoiceFieldCriteria(fieldName, value, ordinal, operator, true);
								}
							}
						case IssueQueryLexer.Is:
							if (fieldName.equals(FIELD_MILESTONE)) {
								return new MilestoneCriteria(value);
							} else if (fieldName.equals(FIELD_STATE)) {
								return new StateCriteria(value);
							} else if (fieldName.equals(FIELD_VOTE_COUNT)) {
								return new VoteCountCriteria(getIntValue(value), operator);
							} else if (fieldName.equals(FIELD_COMMENT_COUNT)) {
								return new CommentCountCriteria(getIntValue(value), operator);
							} else if (fieldName.equals(FIELD_NUMBER)) {
								return new NumberCriteria(getIntValue(value), operator);
							} else {
								InputSpec field = getIssueSetting().getFieldSpec(fieldName);
								if (field instanceof IssueChoiceInput || field instanceof BuildChoiceInput 
										|| field instanceof PullRequestChoiceInput) {
									value = value.trim();
									if (value.startsWith("#"))
										value = value.substring(1);
									return new ReferenceableFieldCriteria(fieldName, getIntValue(value));
								} else if (field instanceof BooleanInput) {
									return new BooleanFieldCriteria(fieldName, getBooleanValue(value));
								} else if (field instanceof NumberInput) {
									return new NumberFieldCriteria(fieldName, getIntValue(value), operator);
								} else if (field instanceof ChoiceInput) { 
									long ordinal = getValueOrdinal((ChoiceInput) field, value);
									return new ChoiceFieldCriteria(fieldName, value, ordinal, operator, false);
								} else if (field instanceof UserChoiceInput 
										|| field instanceof GroupChoiceInput) {
									return new ChoiceFieldCriteria(fieldName, value, -1, operator, false);
								} else {
									return new StringFieldCriteria(fieldName, value, operator);
								}
							}
						case IssueQueryLexer.IsLessThan:
						case IssueQueryLexer.IsGreaterThan:
							if (fieldName.equals(FIELD_VOTE_COUNT)) {
								return new VoteCountCriteria(getIntValue(value), operator);
							} else if (fieldName.equals(FIELD_COMMENT_COUNT)) {
								return new CommentCountCriteria(getIntValue(value), operator);
							} else if (fieldName.equals(FIELD_NUMBER)) {
								return new NumberCriteria(getIntValue(value), operator);
							} else {
								InputSpec field = getIssueSetting().getFieldSpec(fieldName);
								if (field instanceof NumberInput) {
									return new NumberFieldCriteria(fieldName, getIntValue(value), operator);
								} else {
									long ordinal;
									if (validate)
										ordinal = getValueOrdinal((ChoiceInput) field, value);
									else
										ordinal = 0;
									return new ChoiceFieldCriteria(fieldName, value, ordinal, operator, false);
								}
							}
						default:
							throw new OneException("Unexpected operator " + getRuleName(operator));
						}
					}
					
					@Override
					public IssueCriteria visitOrCriteria(OrCriteriaContext ctx) {
						List<IssueCriteria> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrCriteria(childCriterias);
					}

					@Override
					public IssueCriteria visitAndCriteria(AndCriteriaContext ctx) {
						List<IssueCriteria> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndCriteria(childCriterias);
					}

					@Override
					public IssueCriteria visitNotCriteria(NotCriteriaContext ctx) {
						return new NotCriteria(visit(ctx.criteria()));
					}
					
				}.visit(criteriaContext);
			} else {
				issueCriteria = null;
			}

			List<EntitySort> issueSorts = new ArrayList<>();
			for (OrderContext order: queryContext.order()) {
				String fieldName = getValue(order.Quoted().getText());
				if (validate && !IssueConstants.ORDER_FIELDS.containsKey(fieldName)) {
					InputSpec fieldSpec = getIssueSetting().getFieldSpec(fieldName);
					if (!(fieldSpec instanceof ChoiceInput) && !(fieldSpec instanceof DateInput) 
							&& !(fieldSpec instanceof NumberInput)) {
						throw new OneException("Can not order by field: " + fieldName);
					}
				}
				
				EntitySort issueSort = new EntitySort();
				issueSort.setField(fieldName);
				if (order.direction != null && order.direction.getText().equals("asc"))
					issueSort.setDirection(Direction.ASCENDING);
				else
					issueSort.setDirection(Direction.DESCENDING);
				issueSorts.add(issueSort);
			}
			
			return new IssueQuery(issueCriteria, issueSorts);
		} else {
			return new IssueQuery();
		}
	}
	
	private static GlobalIssueSetting getIssueSetting() {
		if (WicketUtils.getPage() instanceof GlobalIssueSettingPage) 
			return ((GlobalIssueSettingPage)WicketUtils.getPage()).getSetting();
		else
			return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	private static OneException newOperatorException(String fieldName, int operator) {
		return new OneException("Field '" + fieldName + "' is not applicable for operator '" + getRuleName(operator) + "'");
	}
	
	public static void checkField(String fieldName, int operator) {
		InputSpec fieldSpec = getIssueSetting().getFieldSpec(fieldName);
		if (fieldSpec == null && !IssueConstants.QUERY_FIELDS.contains(fieldName))
			throw new OneException("Field not found: " + fieldName);
		switch (operator) {
		case IssueQueryLexer.IsEmpty:
			if (IssueConstants.QUERY_FIELDS.contains(fieldName) && !fieldName.equals(FIELD_DESCRIPTION) && !fieldName.equals(FIELD_MILESTONE)) 
				throw newOperatorException(fieldName, operator);
			break;
		case IssueQueryLexer.IsMe:
			if (!(fieldSpec instanceof UserChoiceInput))
				throw newOperatorException(fieldName, operator);
			break;
		case IssueQueryLexer.IsBefore:
		case IssueQueryLexer.IsAfter:
			if (!fieldName.equals(FIELD_SUBMIT_DATE) && !fieldName.equals(FIELD_UPDATE_DATE) && !(fieldSpec instanceof DateInput))
				throw newOperatorException(fieldName, operator);
			break;
		case IssueQueryLexer.Contains:
			if (!fieldName.equals(FIELD_TITLE) 
					&& !fieldName.equals(FIELD_DESCRIPTION)
					&& !fieldName.equals(FIELD_COMMENT)
					&& !(fieldSpec instanceof TextInput) 
					&& !(fieldSpec != null && fieldSpec.isAllowMultiple())) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		case IssueQueryLexer.Is:
			if (!fieldName.equals(FIELD_STATE) 
					&& !fieldName.equals(FIELD_VOTE_COUNT) 
					&& !fieldName.equals(FIELD_COMMENT_COUNT) 
					&& !fieldName.equals(FIELD_NUMBER)
					&& !fieldName.equals(FIELD_MILESTONE)
					&& !(fieldSpec instanceof IssueChoiceInput)
					&& !(fieldSpec instanceof PullRequestChoiceInput)
					&& !(fieldSpec instanceof BuildChoiceInput)
					&& !(fieldSpec instanceof BooleanInput)
					&& !(fieldSpec instanceof NumberInput) 
					&& !(fieldSpec instanceof ChoiceInput) 
					&& !(fieldSpec instanceof UserChoiceInput)
					&& !(fieldSpec instanceof GroupChoiceInput)
					&& !(fieldSpec instanceof TextInput)) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		case IssueQueryLexer.IsLessThan:
		case IssueQueryLexer.IsGreaterThan:
			if (!fieldName.equals(FIELD_VOTE_COUNT)
					&& !fieldName.equals(FIELD_COMMENT_COUNT)
					&& !fieldName.equals(FIELD_NUMBER)
					&& !(fieldSpec instanceof NumberInput) 
					&& !(fieldSpec instanceof ChoiceInput))
				throw newOperatorException(fieldName, operator);
			break;
		}
	}
	
	@Override
	public boolean needsLogin() {
		return criteria != null && criteria.needsLogin();
	}
	
	@Override
	public boolean matches(Issue issue, User user) {
		return criteria == null || criteria.matches(issue, user);
	}
	
	public static String getRuleName(int rule) {
		return getLexerRuleName(IssueQueryLexer.ruleNames, rule);
	}
	
	public static int getOperator(String operatorName) {
		return getLexerRule(IssueQueryLexer.ruleNames, operatorName);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (criteria != null) 
			builder.append(criteria.toString()).append(" ");
		else
			builder.append("all");
		if (!sorts.isEmpty()) {
			builder.append("order by ");
			for (EntitySort sort: sorts)
				builder.append(sort.toString()).append(" ");
		}
		return builder.toString().trim();
	}
	
	public Collection<String> getUndefinedStates() {
		if (criteria != null) 
			return criteria.getUndefinedStates();
		else
			return new ArrayList<>();
	}
	
	public void onRenameState(String oldName, String newName) {
		if (criteria != null)
			criteria.onRenameState(oldName, newName);
	}

	public boolean onDeleteState(String stateName) {
		return criteria != null && criteria.onDeleteState(stateName);
	}
	
	public Collection<String> getUndefinedFields() {
		Set<String> undefinedFields = new HashSet<>();
		if (criteria != null)
			undefinedFields.addAll(criteria.getUndefinedFields());
		for (EntitySort sort: sorts) {
			if (!IssueConstants.QUERY_FIELDS.contains(sort.getField()) 
					&& getIssueSetting().getFieldSpec(sort.getField()) == null) {
				undefinedFields.add(sort.getField());
			}
		}
		return undefinedFields;
	}

	public void onRenameField(String oldName, String newName) {
		if (criteria != null)
			criteria.onRenameField(oldName, newName);
		for (EntitySort sort: sorts) {
			if (sort.getField().equals(oldName))
				sort.setField(newName);
		}
	}

	public boolean onDeleteField(String fieldName) {
		if (criteria != null && criteria.onDeleteField(fieldName))
			return true;
		for (Iterator<EntitySort> it = sorts.iterator(); it.hasNext();) {
			if (it.next().getField().equals(fieldName))
				it.remove();
		}
		return false;
	}
	
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		if (criteria != null)
			return criteria.getUndefinedFieldValues();
		else
			return new HashSet<>();
	}

	public boolean onEditFieldValues(String fieldName, ValueSetEdit valueSetEdit) {
		return criteria != null && criteria.onEditFieldValues(fieldName, valueSetEdit);
	}
	
	public boolean fixUndefinedFieldValues(Map<String, ValueSetEdit> valueSetEdits) {
		for (Map.Entry<String, ValueSetEdit> entry: valueSetEdits.entrySet()) {
			if (onEditFieldValues(entry.getKey(), entry.getValue()))
				return true;
		}
		return false;
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
		return new IssueQuery(IssueCriteria.of(criterias), sorts);
	}

}
