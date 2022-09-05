package io.onedev.server.search.entity.codecomment;

import static io.onedev.server.model.CodeComment.NAME_CONTENT;
import static io.onedev.server.model.CodeComment.NAME_CREATE_DATE;
import static io.onedev.server.model.CodeComment.NAME_PATH;
import static io.onedev.server.model.CodeComment.NAME_REPLY;
import static io.onedev.server.model.CodeComment.NAME_REPLY_COUNT;
import static io.onedev.server.model.CodeComment.NAME_UPDATE_DATE;
import static io.onedev.server.model.CodeComment.ORDER_FIELDS;
import static io.onedev.server.model.CodeComment.QUERY_FIELDS;

import java.util.ArrayList;
import java.util.Date;
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
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
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
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;

public class CodeCommentQuery extends EntityQuery<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final Criteria<CodeComment> criteria;
	
	private final List<EntitySort> sorts;
	
	public CodeCommentQuery(@Nullable Criteria<CodeComment> criteria, List<EntitySort> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	public CodeCommentQuery(@Nullable Criteria<CodeComment> criteria) {
		this(criteria, new ArrayList<>());
	}
	
	public CodeCommentQuery() {
		this(null);
	}
	
	public static CodeCommentQuery parse(Project project, @Nullable String queryString, 
			boolean withCurrentUserCriteria) {
		if (queryString != null) {
			CharStream is = CharStreams.fromString(queryString); 
			CodeCommentQueryLexer lexer = new CodeCommentQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new RuntimeException("Malformed code comment query", e);
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			CodeCommentQueryParser parser = new CodeCommentQueryParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			QueryContext queryContext = parser.query();
			CriteriaContext criteriaContext = queryContext.criteria();
			Criteria<CodeComment> commentCriteria;
			if (criteriaContext != null) {
				commentCriteria = new CodeCommentQueryBaseVisitor<Criteria<CodeComment>>() {

					@Override
					public Criteria<CodeComment> visitOperatorCriteria(OperatorCriteriaContext ctx) {
						switch (ctx.operator.getType()) {
						case CodeCommentQueryLexer.Resolved:
							return new ResolvedCriteria();
						case CodeCommentQueryLexer.Unresolved:
							return new UnresolvedCriteria();
						default:
							if (!withCurrentUserCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new CreatedByMeCriteria();
						}
					}
					
					@Override
					public Criteria<CodeComment> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						int operator = ctx.operator.getType();
						String value = getValue(ctx.Quoted().getText());
						if (operator == CodeCommentQueryLexer.CreatedBy) {
							return new CreatedByCriteria(getUser(value));
						} else {
							ProjectScopedCommit commitId = getCommitId(project, value); 
							return new OnCommitCriteria(commitId.getProject(), commitId.getCommitId());
						}
					}
					
					@Override
					public Criteria<CodeComment> visitParensCriteria(ParensCriteriaContext ctx) {
						return (Criteria<CodeComment>) visit(ctx.criteria()).withParens(true);
					}

					@Override
					public Criteria<CodeComment> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted(0).getText());
						String value = getValue(ctx.Quoted(1).getText());
						int operator = ctx.operator.getType();
						checkField(project, fieldName, operator);
						
						switch (operator) {
						case CodeCommentQueryLexer.IsUntil:
						case CodeCommentQueryLexer.IsSince:
							Date dateValue = getDateValue(value);
							switch (fieldName) {
							case NAME_CREATE_DATE:
								return new CreateDateCriteria(dateValue, value, operator);
							case NAME_UPDATE_DATE:
								return new UpdateDateCriteria(dateValue, value, operator);
							default:
								throw new IllegalStateException();
							}
						case CodeCommentQueryLexer.IsLessThan:
						case CodeCommentQueryLexer.IsGreaterThan:
							return new ReplyCountCriteria(getIntValue(value), operator);
						case CodeCommentQueryLexer.Contains:
							switch (fieldName) {
							case NAME_CONTENT:
								return new ContentCriteria(value);
							case NAME_REPLY:
								return new ReplyCriteria(value);
							default:
								throw new IllegalStateException();
							}
						case CodeCommentQueryLexer.Is:
							switch (fieldName) {
							case NAME_PATH:
								return new PathCriteria(value);
							case NAME_REPLY_COUNT:
								return new ReplyCountCriteria(getIntValue(value), operator);
							default: 
								throw new IllegalStateException();
							}
						default:
							throw new IllegalStateException();
						}
					}
					
					@Override
					public Criteria<CodeComment> visitOrCriteria(OrCriteriaContext ctx) {
						List<Criteria<CodeComment>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrCriteria<CodeComment>(childCriterias);
					}

					@Override
					public Criteria<CodeComment> visitAndCriteria(AndCriteriaContext ctx) {
						List<Criteria<CodeComment>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndCriteria<CodeComment>(childCriterias);
					}

					@Override
					public Criteria<CodeComment> visitNotCriteria(NotCriteriaContext ctx) {
						return new NotCriteria<CodeComment>(visit(ctx.criteria()));
					}

				}.visit(criteriaContext);
			} else {
				commentCriteria = null;
			}

			List<EntitySort> commentSorts = new ArrayList<>();
			for (OrderContext order: queryContext.order()) {
				String fieldName = getValue(order.Quoted().getText());
				if (!ORDER_FIELDS.containsKey(fieldName)) 
					throw new ExplicitException("Can not order by field: " + fieldName);
				
				EntitySort commentSort = new EntitySort();
				commentSort.setField(fieldName);
				if (order.direction != null && order.direction.getText().equals("desc"))
					commentSort.setDirection(Direction.DESCENDING);
				else
					commentSort.setDirection(Direction.ASCENDING);
				commentSorts.add(commentSort);
			}
			
			return new CodeCommentQuery(commentCriteria, commentSorts);
		} else {
			return new CodeCommentQuery();
		}
	}
	
	public static void checkField(Project project, String fieldName, int operator) {
		if (!QUERY_FIELDS.contains(fieldName))
			throw new ExplicitException("Field not found: " + fieldName);
		switch (operator) {
		case CodeCommentQueryLexer.IsUntil:
		case CodeCommentQueryLexer.IsSince:
			if (!fieldName.equals(NAME_CREATE_DATE) && !fieldName.equals(NAME_UPDATE_DATE)) 
				throw newOperatorException(fieldName, operator);
			break;
		case CodeCommentQueryLexer.IsGreaterThan:
		case CodeCommentQueryLexer.IsLessThan:
			if (!fieldName.equals(NAME_REPLY_COUNT))
				throw newOperatorException(fieldName, operator);
			break;
		case CodeCommentQueryLexer.Contains:
			if (!fieldName.equals(NAME_CONTENT) && !fieldName.equals(NAME_REPLY))
				throw newOperatorException(fieldName, operator);
			break;
		case CodeCommentQueryLexer.Is:
			if (!fieldName.equals(NAME_REPLY_COUNT) && !fieldName.equals(NAME_PATH)) 
				throw newOperatorException(fieldName, operator);
			break;
		}
	}
	
	private static ExplicitException newOperatorException(String fieldName, int operator) {
		return new ExplicitException("Field '" + fieldName + "' is not applicable for operator '" + getRuleName(operator) + "'");
	}
	
	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(CodeCommentQueryLexer.ruleNames, rule);
	}
	
	public static int getOperator(String operatorName) {
		return AntlrUtils.getLexerRule(CodeCommentQueryLexer.ruleNames, operatorName);
	}
	
	@Override
	public Criteria<CodeComment> getCriteria() {
		return criteria;
	}

	@Override
	public List<EntitySort> getSorts() {
		return sorts;
	}
	
}
