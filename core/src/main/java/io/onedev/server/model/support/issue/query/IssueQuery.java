package io.onedev.server.model.support.issue.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.java.JavaEscape;

import com.google.common.base.Splitter;

import io.onedev.server.OneDev;
import io.onedev.server.exception.OneException;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.query.IssueQueryParser.AndCriteriaContext;
import io.onedev.server.model.support.issue.query.IssueQueryParser.BracedCriteriaContext;
import io.onedev.server.model.support.issue.query.IssueQueryParser.CriteriaContext;
import io.onedev.server.model.support.issue.query.IssueQueryParser.MineCriteriaContext;
import io.onedev.server.model.support.issue.query.IssueQueryParser.OrCriteriaContext;
import io.onedev.server.model.support.issue.query.IssueQueryParser.OrderContext;
import io.onedev.server.model.support.issue.query.IssueQueryParser.QueryContext;
import io.onedev.server.model.support.issue.query.IssueQueryParser.UnaryCriteriaContext;
import io.onedev.server.model.support.issue.query.IssueQueryParser.ValueCriteriaContext;
import io.onedev.server.model.support.issue.query.IssueSort.Direction;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.booleaninput.BooleanInput;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.dateinput.DateInput;
import io.onedev.server.util.inputspec.groupchoiceinput.GroupChoiceInput;
import io.onedev.server.util.inputspec.issuechoiceinput.IssueChoiceInput;
import io.onedev.server.util.inputspec.numberinput.NumberInput;
import io.onedev.server.util.inputspec.textinput.TextInput;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.utils.WordUtils;

public class IssueQuery implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(IssueQuery.class);
	
	private final IssueCriteria criteria;
	
	private final List<IssueSort> sorts;
	
	public IssueQuery(@Nullable IssueCriteria criteria, List<IssueSort> sorts) {
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

	public List<IssueSort> getSorts() {
		return sorts;
	}

	public static String getValue(String tokenText) {
		String value = tokenText.substring(1);
		return JavaEscape.unescapeJava(value.substring(0, value.length()-1));
	}

	public static int getIntValue(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new OneException("Invalid number: " + value);
		}
	}
	
	public static boolean getBooleanValue(String value) {
		if (value.equals("true"))
			return true;
		else if (value.equals("false"))
			return false;
		else
			throw new OneException("Invalid boolean: " + value);
	}
	
	public static IssueQuery parse(Project project, @Nullable String queryString, boolean allowSort, boolean validate) {
		if (queryString != null) {
			ANTLRInputStream is = new ANTLRInputStream(queryString); 
			IssueQueryLexer lexer = new IssueQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					if (e != null) {
						logger.error("Error lexing issue query", e);
					} else if (msg != null) {
						logger.error("Error lexing issue query: " + msg);
					}
					throw new RuntimeException("Malformed issue query");
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
					public IssueCriteria visitMineCriteria(MineCriteriaContext ctx) {
						IssueCriteria submitterCriteria = new SubmitterCriteria(SecurityUtils.getUser(), IssueQueryLexer.Is);
						List<IssueCriteria> fieldCriterias = new ArrayList<>();
						for (InputSpec field: project.getIssueWorkflow().getFieldSpecs()) {
							if (field instanceof UserChoiceInput) {
								IssueCriteria fieldCriteria = new FieldUnaryCriteria(field.getName(), IssueQueryLexer.IsMe);
								fieldCriterias.add(fieldCriteria);
							} 
						}
						if (!fieldCriterias.isEmpty()) {
							fieldCriterias.add(0, submitterCriteria);
							return new OrCriteria(fieldCriterias);
						} else {
							return submitterCriteria;
						}
					}

					@Override
					public IssueCriteria visitUnaryCriteria(UnaryCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted().getText());
						int operator = ctx.operator.getType();
						if (validate)
							checkField(project, fieldName, operator);
						if (fieldName.equals(Issue.MILESTONE))
							return new MilestoneUnaryCriteria(operator);
						else if (fieldName.equals(Issue.DESCRIPTION))
							return new DescriptionUnaryCriteria(operator);
						else if (fieldName.equals(Issue.SUBMITTER))
							return new SubmitterUnaryCriteria(operator);
						else
							return new FieldUnaryCriteria(fieldName, operator);
					}
					
					@Override
					public IssueCriteria visitBracedCriteria(BracedCriteriaContext ctx) {
						return visit(ctx.criteria());
					}

					@Override
					public IssueCriteria visitValueCriteria(ValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted(0).getText());
						String value = getValue(ctx.Quoted(1).getText());
						int operator = ctx.operator.getType();
						if (validate)
							checkField(project, fieldName, operator);
						
						switch (operator) {
						case IssueQueryLexer.IsBefore:
						case IssueQueryLexer.IsAfter:
							Date dateValue = DateUtils.parseRelaxed(value);
							if (dateValue == null)
								throw new OneException("Unrecognized date: " + value);
							if (fieldName.equals(Issue.SUBMIT_DATE)) 
								return new SubmitDateCriteria(dateValue, value, operator);
							else if (fieldName.equals(Issue.UPDATE_DATE))
								return new UpdateDateCriteria(dateValue, value, operator);
							else 
								return new DateFieldCriteria(fieldName, dateValue, value, operator);
						case IssueQueryLexer.Contains:
						case IssueQueryLexer.DoesNotContain:
							if (fieldName.equals(Issue.TITLE)) {
								return new TitleCriteria(value, operator);
							} else if (fieldName.equals(Issue.DESCRIPTION)) {
								return new DescriptionCriteria(value, operator);
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
						case IssueQueryLexer.IsNot:
							if (fieldName.equals(Issue.MILESTONE)) {
								return new MilestoneCriteria(value, operator);
							} else if (fieldName.equals(Issue.STATE)) {
								return new StateCriteria(value, operator);
							} else if (fieldName.equals(Issue.VOTES)) {
								return new VotesCriteria(getIntValue(value), operator);
							} else if (fieldName.equals(Issue.COMMENTS)) {
								return new CommentsCriteria(getIntValue(value), operator);
							} else if (fieldName.equals(Issue.NUMBER)) {
								return new NumberCriteria(getIntValue(value), operator);
							} else if (fieldName.equals(Issue.SUBMITTER)) {
								User user = OneDev.getInstance(UserManager.class).findByName(value);
								if (user == null)
									throw new OneException("Unable to find user with login: " + value);
								return new SubmitterCriteria(user, operator);
							} else {
								InputSpec field = project.getIssueWorkflow().getFieldSpec(fieldName);
								if (field instanceof IssueChoiceInput) {
									value = value.trim();
									if (value.startsWith("#"))
										value = value.substring(1);
									return new IssueFieldCriteria(fieldName, getIntValue(value), operator);
								} else if (field instanceof BooleanInput) {
									return new BooleanFieldCriteria(fieldName, getBooleanValue(value), operator);
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
							if (fieldName.equals(Issue.VOTES)) {
								return new VotesCriteria(getIntValue(value), operator);
							} else if (fieldName.equals(Issue.COMMENTS)) {
								return new CommentsCriteria(getIntValue(value), operator);
							} else if (fieldName.equals(Issue.NUMBER)) {
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
							throw new OneException("Unexpected operator " + getOperatorName(operator));
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

				}.visit(criteriaContext);
			} else {
				issueCriteria = null;
			}

			List<IssueSort> issueSorts = new ArrayList<>();
			if (allowSort) {
				for (OrderContext order: queryContext.order()) {
					String fieldName = getValue(order.Quoted().getText());
					if (validate 
							&& !fieldName.equals(Issue.SUBMIT_DATE) 
							&& !fieldName.equals(Issue.UPDATE_DATE) 
							&& !fieldName.equals(Issue.VOTES) 
							&& !fieldName.equals(Issue.COMMENTS) 
							&& !fieldName.equals(Issue.NUMBER)) {
						InputSpec fieldSpec = project.getIssueWorkflow().getFieldSpec(fieldName);
						if (!(fieldSpec instanceof ChoiceInput) && !(fieldSpec instanceof DateInput) 
								&& !(fieldSpec instanceof NumberInput)) {
							throw new OneException("Can not order by field: " + fieldName);
						}
					}
					
					IssueSort issueSort = new IssueSort();
					issueSort.setField(fieldName);
					if (order.direction != null && order.direction.getText().equals("asc"))
						issueSort.setDirection(Direction.ASCENDING);
					else
						issueSort.setDirection(Direction.DESCENDING);
					issueSorts.add(issueSort);
				}
			} else if (validate && queryContext.OrderBy() != null) {
				throw new OneException("Issue order is not supported here");
			}
			
			return new IssueQuery(issueCriteria, issueSorts);
		} else {
			return new IssueQuery();
		}
	}
	
	private static OneException newOperatorException(String fieldName, int operator) {
		return new OneException("Field '" + fieldName + "' is not applicable for operator '" + getOperatorName(operator) + "'");
	}
	
	public static void checkField(Project project, String fieldName, int operator) {
		InputSpec fieldSpec = project.getIssueWorkflow().getFieldSpec(fieldName);
		if (fieldSpec == null && !Issue.BUILTIN_FIELDS.containsKey(fieldName))
			throw new OneException("Field not found: " + fieldName);
		switch (operator) {
		case IssueQueryLexer.IsEmpty:
		case IssueQueryLexer.IsNotEmpty:
			if (Issue.BUILTIN_FIELDS.containsKey(fieldName) && !fieldName.equals(Issue.DESCRIPTION)
					&& !fieldName.equals(Issue.MILESTONE)) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		case IssueQueryLexer.IsMe:
		case IssueQueryLexer.IsNotMe:
			if (!fieldName.equals(Issue.SUBMITTER) && !(fieldSpec instanceof UserChoiceInput))
				throw newOperatorException(fieldName, operator);
			break;
		case IssueQueryLexer.IsBefore:
		case IssueQueryLexer.IsAfter:
			if (!fieldName.equals(Issue.SUBMIT_DATE) && !fieldName.equals(Issue.UPDATE_DATE) && !(fieldSpec instanceof DateInput))
				throw newOperatorException(fieldName, operator);
			break;
		case IssueQueryLexer.Contains:
		case IssueQueryLexer.DoesNotContain:
			if (!fieldName.equals(Issue.TITLE) 
					&& !fieldName.equals(Issue.DESCRIPTION)
					&& !(fieldSpec instanceof TextInput) 
					&& !(fieldSpec != null && fieldSpec.isAllowMultiple())) {
				throw newOperatorException(fieldName, operator);
			}
			break;
		case IssueQueryLexer.Is:
		case IssueQueryLexer.IsNot:
			if (!fieldName.equals(Issue.STATE) 
					&& !fieldName.equals(Issue.VOTES) 
					&& !fieldName.equals(Issue.COMMENTS) 
					&& !fieldName.equals(Issue.SUBMITTER)
					&& !fieldName.equals(Issue.NUMBER)
					&& !fieldName.equals(Issue.MILESTONE)
					&& !(fieldSpec instanceof IssueChoiceInput)
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
			if (!fieldName.equals(Issue.VOTES)
					&& !fieldName.equals(Issue.COMMENTS)
					&& !fieldName.equals(Issue.NUMBER)
					&& !(fieldSpec instanceof NumberInput) 
					&& !(fieldSpec instanceof ChoiceInput))
				throw newOperatorException(fieldName, operator);
			break;
		}
	}
	
	public boolean needsLogin() {
		return criteria != null && criteria.needsLogin();
	}
	
	public boolean matches(Issue issue) {
		return criteria == null || criteria.matches(issue);
	}
	
	public static String getOperatorName(int operator) {
		return WordUtils.uncamel(IssueQueryLexer.ruleNames[operator-1]).toLowerCase();
	}
	
	public static int getOperator(String operatorName) {
		for (int i=0; i<IssueQueryLexer.ruleNames.length; i++) {
			String ruleName = IssueQueryLexer.ruleNames[i];
			if (WordUtils.uncamel(ruleName).toLowerCase().equals(operatorName))
				return i+1;
		}
		throw new OneException("Unable to find operator: " + operatorName);
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
			for (IssueSort sort: sorts)
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
		for (IssueSort sort: sorts) {
			if (!Issue.BUILTIN_FIELDS.containsKey(sort.getField()) 
					&& project.getIssueWorkflow().getFieldSpec(sort.getField()) == null) {
				undefinedFields.add(sort.getField());
			}
		}
		return undefinedFields;
	}

	public void onRenameField(String oldField, String newField) {
		if (criteria != null)
			criteria.onRenameField(oldField, newField);
		for (IssueSort sort: sorts) {
			if (sort.getField().equals(oldField))
				sort.setField(newField);
		}
	}

	public boolean onDeleteField(String fieldName) {
		if (criteria != null && criteria.onDeleteField(fieldName))
			return true;
		for (Iterator<IssueSort> it = sorts.iterator(); it.hasNext();) {
			if (it.next().getField().equals(fieldName))
				it.remove();
		}
		return false;
	}
	
	public Map<String, String> getUndefinedFieldValues(Project project) {
		if (criteria != null)
			return criteria.getUndefinedFieldValues(project);
		else
			return new HashMap<>();
	}

	public void onRenameFieldValue(String fieldName, String oldValue, String newValue) {
		if (criteria != null)
			criteria.onRenameFieldValue(fieldName, oldValue, newValue);
	}
	
	public boolean onDeleteFieldValue(String fieldName, String fieldValue) {
		return criteria != null && criteria.onDeleteFieldValue(fieldName, fieldValue);
	}
	
	public static String quote(String value) {
		return "\"" + JavaEscape.escapeJava(value) + "\"";
	}
	
	public static <T> Path<T> getPath(Root<Issue> root, String pathName) {
		int index = pathName.indexOf('.');
		if (index != -1) {
			Path<T> path = root.get(pathName.substring(0, index));
			for (String field: Splitter.on(".").split(pathName.substring(index+1))) 
				path = path.get(field);
			return path;
		} else {
			return root.get(pathName);
		}
	}
	
}
