package io.onedev.server.search.entity.codecomment;

import static io.onedev.server.model.support.codecomment.CodeCommentConstants.FIELD_COMMIT;
import static io.onedev.server.model.support.codecomment.CodeCommentConstants.FIELD_CONTENT;
import static io.onedev.server.model.support.codecomment.CodeCommentConstants.FIELD_CREATE_DATE;
import static io.onedev.server.model.support.codecomment.CodeCommentConstants.FIELD_PATH;
import static io.onedev.server.model.support.codecomment.CodeCommentConstants.FIELD_REPLY;
import static io.onedev.server.model.support.codecomment.CodeCommentConstants.FIELD_REPLY_COUNT;
import static io.onedev.server.model.support.codecomment.CodeCommentConstants.FIELD_UPDATE_DATE;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import io.onedev.server.OneDev;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryBaseVisitor;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryLexer;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryParser;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.AndCriteriaContext;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.CriteriaContext;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.NotCriteriaContext;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.OperatorCriteriaContext;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.OperatorValueCriteriaContext;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.OrCriteriaContext;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.OrderContext;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.ParensCriteriaContext;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.QueryContext;
import io.onedev.server.exception.OneException;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.codecomment.CodeCommentConstants;
import io.onedev.server.search.entity.AndCriteriaHelper;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.NotCriteriaHelper;
import io.onedev.server.search.entity.OrCriteriaHelper;
import io.onedev.server.search.entity.EntitySort.Direction;

public class CodeCommentQuery extends EntityQuery<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final EntityCriteria<CodeComment> criteria;
	
	private final List<EntitySort> sorts;
	
	public CodeCommentQuery(@Nullable EntityCriteria<CodeComment> criteria, List<EntitySort> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	public CodeCommentQuery() {
		this(null, new ArrayList<>());
	}
	
	public static CodeCommentQuery parse(Project project, @Nullable String queryString, boolean validate) {
		if (queryString != null) {
			ANTLRInputStream is = new ANTLRInputStream(queryString); 
			CodeCommentQueryLexer lexer = new CodeCommentQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new OneException("Malformed query syntax", e);
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			CodeCommentQueryParser parser = new CodeCommentQueryParser(tokens);
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
			EntityCriteria<CodeComment> commentCriteria;
			if (criteriaContext != null) {
				commentCriteria = new CodeCommentQueryBaseVisitor<EntityCriteria<CodeComment>>() {

					@Override
					public EntityCriteria<CodeComment> visitOperatorCriteria(OperatorCriteriaContext ctx) {
						return new CreatedByMeCriteria();
					}
					
					private User getUser(String value) {
						User user = OneDev.getInstance(UserManager.class).findByName(value);
						if (user == null)
							throw new OneException("Unable to find user with login: " + value);
						return user;
					}
					
					@Override
					public EntityCriteria<CodeComment> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						String value = getValue(ctx.Quoted().getText());
						return new CreatedByCriteria(getUser(value));
					}
					
					@Override
					public EntityCriteria<CodeComment> visitParensCriteria(ParensCriteriaContext ctx) {
						return visit(ctx.criteria());
					}

					@Override
					public EntityCriteria<CodeComment> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted(0).getText());
						String value = getValue(ctx.Quoted(1).getText());
						int operator = ctx.operator.getType();
						if (validate)
							checkField(project, fieldName, operator);
						
						switch (operator) {
						case CodeCommentQueryLexer.IsBefore:
						case CodeCommentQueryLexer.IsAfter:
							Date dateValue = getDateValue(value);
							switch (fieldName) {
							case FIELD_CREATE_DATE:
								return new CreateDateCriteria(dateValue, value, operator);
							case FIELD_UPDATE_DATE:
								return new UpdateDateCriteria(dateValue, value, operator);
							default:
								throw new IllegalStateException();
							}
						case CodeCommentQueryLexer.IsLessThan:
						case CodeCommentQueryLexer.IsGreaterThan:
							return new ReplyCountCriteria(getIntValue(value), operator);
						case CodeCommentQueryLexer.Contains:
							switch (fieldName) {
							case FIELD_CONTENT:
								return new ContentCriteria(value);
							case FIELD_REPLY:
								return new ReplyCriteria(value);
							default:
								throw new IllegalStateException();
							}
						case CodeCommentQueryLexer.Is:
							switch (fieldName) {
							case FIELD_COMMIT:
								return new CommitCriteria(getCommitId(project, value));
							case FIELD_PATH:
								return new PathCriteria(value);
							case FIELD_REPLY_COUNT:
								return new ReplyCountCriteria(getIntValue(value), operator);
							default: 
								throw new IllegalStateException();
							}
						default:
							throw new IllegalStateException();
						}
					}
					
					@Override
					public EntityCriteria<CodeComment> visitOrCriteria(OrCriteriaContext ctx) {
						List<EntityCriteria<CodeComment>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrCriteriaHelper<CodeComment>(childCriterias);
					}

					@Override
					public EntityCriteria<CodeComment> visitAndCriteria(AndCriteriaContext ctx) {
						List<EntityCriteria<CodeComment>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndCriteriaHelper<CodeComment>(childCriterias);
					}

					@Override
					public EntityCriteria<CodeComment> visitNotCriteria(NotCriteriaContext ctx) {
						return new NotCriteriaHelper<CodeComment>(visit(ctx.criteria()));
					}

				}.visit(criteriaContext);
			} else {
				commentCriteria = null;
			}

			List<EntitySort> commentSorts = new ArrayList<>();
			for (OrderContext order: queryContext.order()) {
				String fieldName = getValue(order.Quoted().getText());
				if (validate && !CodeCommentConstants.ORDER_FIELDS.containsKey(fieldName)) 
					throw new OneException("Can not order by field: " + fieldName);
				
				EntitySort commentSort = new EntitySort();
				commentSort.setField(fieldName);
				if (order.direction != null && order.direction.getText().equals("asc"))
					commentSort.setDirection(Direction.ASCENDING);
				else
					commentSort.setDirection(Direction.DESCENDING);
				commentSorts.add(commentSort);
			}
			
			return new CodeCommentQuery(commentCriteria, commentSorts);
		} else {
			return new CodeCommentQuery();
		}
	}
	
	public static void checkField(Project project, String fieldName, int operator) {
		if (!CodeCommentConstants.QUERY_FIELDS.contains(fieldName))
			throw new OneException("Field not found: " + fieldName);
		switch (operator) {
		case CodeCommentQueryLexer.IsBefore:
		case CodeCommentQueryLexer.IsAfter:
			if (!fieldName.equals(FIELD_CREATE_DATE) && !fieldName.equals(FIELD_UPDATE_DATE)) 
				throw newOperatorException(fieldName, operator);
			break;
		case CodeCommentQueryLexer.IsGreaterThan:
		case CodeCommentQueryLexer.IsLessThan:
			if (!fieldName.equals(FIELD_REPLY_COUNT))
				throw newOperatorException(fieldName, operator);
			break;
		case CodeCommentQueryLexer.Contains:
			if (!fieldName.equals(FIELD_CONTENT) && !fieldName.equals(FIELD_REPLY))
				throw newOperatorException(fieldName, operator);
			break;
		case CodeCommentQueryLexer.Is:
			if (!fieldName.equals(FIELD_COMMIT) && !fieldName.equals(FIELD_REPLY_COUNT) && !fieldName.equals(FIELD_PATH)) 
				throw newOperatorException(fieldName, operator);
			break;
		}
	}
	
	private static OneException newOperatorException(String fieldName, int operator) {
		return new OneException("Field '" + fieldName + "' is not applicable for operator '" + getRuleName(operator) + "'");
	}
	
	public static String getRuleName(int rule) {
		return getRuleName(CodeCommentQueryLexer.ruleNames, rule);
	}
	
	public static int getOperator(String operatorName) {
		return getOperator(CodeCommentQueryLexer.ruleNames, operatorName);
	}
	
	@Override
	public EntityCriteria<CodeComment> getCriteria() {
		return criteria;
	}

	@Override
	public List<EntitySort> getSorts() {
		return sorts;
	}
	
}
