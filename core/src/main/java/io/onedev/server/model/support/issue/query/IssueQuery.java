package io.onedev.server.model.support.issue.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.java.JavaEscape;

import io.onedev.server.OneDev;
import io.onedev.server.exception.OneException;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldUnary;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.query.IssueQueryParser.AllCriteriaContext;
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
	
	public CriteriaQuery<Issue> buildCriteriaQuery(Session session) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Issue> query = builder.createQuery(Issue.class);
		Root<Issue> root = query.from(Issue.class);

		if (criteria != null) 
			query.where(criteria.getPredicate(new QueryBuildContext(root, builder)));

		List<Order> orders = new ArrayList<>();
		for (IssueSort sort: sorts) {
			if (Issue.BUILTIN_FIELDS.containsKey(sort.getField())) {
				if (sort.getDirection() == Direction.ASCENDING)
					orders.add(builder.asc(root.get(Issue.BUILTIN_FIELDS.get(sort.getField()))));
				else
					orders.add(builder.desc(root.get(Issue.BUILTIN_FIELDS.get(sort.getField()))));
			} else {
				Join<Issue, IssueFieldUnary> join = root.join("fieldUnaries", JoinType.LEFT);
				join.on(builder.equal(join.get(IssueFieldUnary.NAME), sort.getField()));
				if (sort.getDirection() == Direction.ASCENDING)
					orders.add(builder.asc(join.get(IssueFieldUnary.ORDINAL)));
				else
					orders.add(builder.desc(join.get(IssueFieldUnary.ORDINAL)));
			}
		}

		Path<String> idPath = root.get("id");
		if (orders.isEmpty())
			orders.add(builder.desc(idPath));
		query.orderBy(orders);
		
		return query;
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
	
	public static IssueQuery parse(Project project, @Nullable String queryString) {
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
							List<String> choices = new ArrayList<>(((ChoiceInput)field).getChoiceProvider().getChoices(true).keySet());
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
					public IssueCriteria visitAllCriteria(AllCriteriaContext ctx) {
						return null;
					}
					
					@Override
					public IssueCriteria visitUnaryCriteria(UnaryCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted().getText());
						int operator = ctx.operator.getType();
						checkField(project, fieldName, operator);
						if (fieldName.equals(Issue.SUBMITTER))
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
						checkField(project, fieldName, operator);
						
						switch (operator) {
						case IssueQueryLexer.IsBefore:
						case IssueQueryLexer.IsAfter:
							Date dateValue = DateUtils.parseRelaxed(value);
							if (dateValue == null)
								throw new OneException("Unrecognized date: " + value);
							if (fieldName.equals(Issue.SUBMIT_DATE)) 
								return new SubmitDateCriteria(dateValue, operator);
							else if (fieldName.equals(Issue.UPDATE_DATE))
								return new UpdateDateCriteria(dateValue, operator);
							else 
								return new DateFieldCriteria(fieldName, dateValue, operator);
						case IssueQueryLexer.Contains:
						case IssueQueryLexer.DoesNotContain:
							if (fieldName.equals(Issue.TITLE)) {
								return new TitleCriteria(value, operator);
							} else if (fieldName.equals(Issue.DESCRIPTION)) {
								return new DescriptionCriteria(value, operator);
							} else {
								InputSpec fieldSpec = project.getIssueWorkflow().getFieldSpec(fieldName);
								if (fieldSpec instanceof TextInput) 
									return new StringFieldCriteria(fieldName, value, operator);
								else 
									return new MultiChoiceFieldCriteria(fieldName, value, operator);
							}
						case IssueQueryLexer.Is:
						case IssueQueryLexer.IsNot:
							if (fieldName.equals(Issue.STATE)) {
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
									return new ChoiceFieldCriteria(fieldName, value, ordinal, operator);
								} else if (field instanceof UserChoiceInput 
										|| field instanceof GroupChoiceInput) {
									return new ChoiceFieldCriteria(fieldName, value, -1, operator);
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
								InputSpec field = project.getIssueWorkflow().getFieldSpec(fieldName);
								if (field instanceof NumberInput)
									return new NumberFieldCriteria(fieldName, getIntValue(value), operator);
								else
									return new ChoiceFieldCriteria(fieldName, value, getValueOrdinal((ChoiceInput) field, value), operator);
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
			for (OrderContext order: queryContext.order()) {
				String fieldName = getValue(order.Quoted().getText());
				if (!fieldName.equals(Issue.SUBMIT_DATE) && !fieldName.equals(Issue.UPDATE_DATE) 
						&& !fieldName.equals(Issue.VOTES) && !fieldName.equals(Issue.COMMENTS) 
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
			if (Issue.BUILTIN_FIELDS.containsKey(fieldName))
				throw newOperatorException(fieldName, operator);
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
					&& !fieldSpec.isAllowMultiple()) {
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
}
