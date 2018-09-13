package io.onedev.server.search.entity.issue;

import static io.onedev.server.model.support.issue.IssueConstants.FIELD_COMMENT;
import static io.onedev.server.model.support.issue.IssueConstants.FIELD_COMMENT_COUNT;
import static io.onedev.server.model.support.issue.IssueConstants.FIELD_DESCRIPTION;
import static io.onedev.server.model.support.issue.IssueConstants.FIELD_MILESTONE;
import static io.onedev.server.model.support.issue.IssueConstants.FIELD_NUMBER;
import static io.onedev.server.model.support.issue.IssueConstants.FIELD_STATE;
import static io.onedev.server.model.support.issue.IssueConstants.FIELD_SUBMITTER;
import static io.onedev.server.model.support.issue.IssueConstants.FIELD_SUBMIT_DATE;
import static io.onedev.server.model.support.issue.IssueConstants.FIELD_TITLE;
import static io.onedev.server.model.support.issue.IssueConstants.FIELD_UPDATE_DATE;
import static io.onedev.server.model.support.issue.IssueConstants.FIELD_VOTE_COUNT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import io.onedev.server.exception.OneException;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.IssueConstants;
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
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.BuildChoiceInput;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.IssueChoiceInput;
import io.onedev.server.util.inputspec.PullRequestChoiceInput;
import io.onedev.server.util.inputspec.booleaninput.BooleanInput;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.dateinput.DateInput;
import io.onedev.server.util.inputspec.numberinput.NumberInput;
import io.onedev.server.util.inputspec.teamchoiceinput.TeamChoiceInput;
import io.onedev.server.util.inputspec.textinput.TextInput;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedFieldValue;

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

	public static IssueQuery parse(Project project, @Nullable String queryString, boolean validate) {
		if (queryString != null) {
			ANTLRInputStream is = new ANTLRInputStream(queryString); 
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
						OneContext.push(new OneContext() {

							@Override
							public Project getProject() {
								return project;
							}

							@Override
							public EditContext getEditContext(int level) {
								return new EditContext() {

									@Override
									public Object getInputValue(String name) {
										return null;
									}
									
								};
							}

							@Override
							public InputContext getInputContext() {
								throw new UnsupportedOperationException();
							}
							
						});
						try {
							List<String> choices = new ArrayList<>(field.getChoiceProvider().getChoices(true).keySet());
							return choices.indexOf(value);
						} finally {
							OneContext.pop();
						}								
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
							checkField(project, fieldName, operator);
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
							User user = OneDev.getInstance(UserManager.class).findByName(value);
							if (user == null)
								throw new OneException("Unable to find user with login: " + value);
							return new SubmittedByCriteria(user);
						} else if (ctx.FixedInBuild() != null) {
							Build build = OneDev.getInstance(BuildManager.class).findByFQN(project, value);
							if (build != null)
								return new FixedInCriteria(build);
							else
								throw new OneException("Unable to find build with FQN: " + value);
						} else {
							throw new RuntimeException("Unexpected operator: " + ctx.operator.getText());
						}
					}
					
					private ObjectId getCommitId(RevisionCriteriaContext revision) {
						String value = getValue(revision.Quoted().getText());
						if (revision.Build() != null) {
							Build build = OneDev.getInstance(BuildManager.class).findByFQN(project, value);
							if (build != null)
								return ObjectId.fromString(build.getCommit());
							else
								throw new OneException("Unable to find build with FQN: " + value);
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
							checkField(project, fieldName, operator);
						
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
								if (validate) {
									InputSpec fieldSpec = project.getIssueWorkflow().getFieldSpec(fieldName);
									if (fieldSpec instanceof TextInput) {
										return new StringFieldCriteria(fieldName, value, operator);
									} else {
										long ordinal = getValueOrdinal((ChoiceInput) fieldSpec, value);
										return new ChoiceFieldCriteria(fieldName, value, ordinal, operator, true);
									}
								} else {
									return new StringFieldCriteria(fieldName, value, operator);
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
								InputSpec field = project.getIssueWorkflow().getFieldSpec(fieldName);
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
										|| field instanceof TeamChoiceInput) {
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
								if (validate) {
									InputSpec field = project.getIssueWorkflow().getFieldSpec(fieldName);
									if (field instanceof NumberInput) {
										return new NumberFieldCriteria(fieldName, getIntValue(value), operator);
									} else {
										long ordinal = getValueOrdinal((ChoiceInput) field, value);
										return new ChoiceFieldCriteria(fieldName, value, ordinal, operator, false);
									}
								} else {
									return new NumberFieldCriteria(fieldName, 0, operator);
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
					InputSpec fieldSpec = project.getIssueWorkflow().getFieldSpec(fieldName);
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
	
	private static OneException newOperatorException(String fieldName, int operator) {
		return new OneException("Field '" + fieldName + "' is not applicable for operator '" + getRuleName(operator) + "'");
	}
	
	public static void checkField(Project project, String fieldName, int operator) {
		InputSpec fieldSpec = project.getIssueWorkflow().getFieldSpec(fieldName);
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
					&& !(fieldSpec instanceof TeamChoiceInput)
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
	
	public Collection<String> getUndefinedStates(Project project) {
		if (criteria != null) 
			return criteria.getUndefinedStates(project);
		else
			return new ArrayList<>();
	}
	
	public void onRenameState(String oldState, String newState) {
		if (criteria != null)
			criteria.onRenameState(oldState, newState);
	}

	public Collection<String> getUndefinedFields(Project project) {
		Set<String> undefinedFields = new HashSet<>();
		if (criteria != null)
			undefinedFields.addAll(criteria.getUndefinedFields(project));
		for (EntitySort sort: sorts) {
			if (!IssueConstants.QUERY_FIELDS.contains(sort.getField()) 
					&& project.getIssueWorkflow().getFieldSpec(sort.getField()) == null) {
				undefinedFields.add(sort.getField());
			}
		}
		return undefinedFields;
	}

	public void onRenameField(String oldField, String newField) {
		if (criteria != null)
			criteria.onRenameField(oldField, newField);
		for (EntitySort sort: sorts) {
			if (sort.getField().equals(oldField))
				sort.setField(newField);
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
	
	public Collection<UndefinedFieldValue> getUndefinedFieldValues(Project project) {
		if (criteria != null)
			return criteria.getUndefinedFieldValues(project);
		else
			return new HashSet<>();
	}

	public void onRenameFieldValue(String fieldName, String oldValue, String newValue) {
		if (criteria != null)
			criteria.onRenameFieldValue(fieldName, oldValue, newValue);
	}
	
	public boolean onDeleteFieldValue(String fieldName, String fieldValue) {
		return criteria != null && criteria.onDeleteFieldValue(fieldName, fieldValue);
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
