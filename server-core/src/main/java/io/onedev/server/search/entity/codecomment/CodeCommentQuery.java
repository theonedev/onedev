package io.onedev.server.search.entity.codecomment;

import static io.onedev.server.model.CodeComment.NAME_CONTENT;
import static io.onedev.server.model.CodeComment.NAME_CREATE_DATE;
import static io.onedev.server.model.CodeComment.NAME_LAST_ACTIVITY_DATE;
import static io.onedev.server.model.CodeComment.NAME_PATH;
import static io.onedev.server.model.CodeComment.NAME_REPLY;
import static io.onedev.server.model.CodeComment.NAME_REPLY_COUNT;
import static io.onedev.server.model.CodeComment.QUERY_FIELDS;
import static io.onedev.server.model.CodeComment.SORT_FIELDS;
import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;
import static io.onedev.server.search.entity.EntitySort.Direction.DESCENDING;
import static io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.Contains;
import static io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.CreatedBy;
import static io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.CreatedByMe;
import static io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.Is;
import static io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.IsGreaterThan;
import static io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.IsLessThan;
import static io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.IsNot;
import static io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.IsSince;
import static io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.IsUntil;
import static io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.Mentioned;
import static io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.MentionedMe;
import static io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.RepliedBy;
import static io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.RepliedByMe;
import static io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.Resolved;
import static io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.Unresolved;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.AndCriteriaContext;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.CriteriaContext;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryParser.FuzzyCriteriaContext;
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

	public CodeCommentQuery(@Nullable Criteria<CodeComment> criteria, List<EntitySort> sorts) {
		super(criteria, sorts);
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
					throw new RuntimeException("Malformed query", e);
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
					public Criteria<CodeComment> visitFuzzyCriteria(FuzzyCriteriaContext ctx) {
						return new FuzzyCriteria(getValue(ctx.getText()));
					}

					@Override
					public Criteria<CodeComment> visitOperatorCriteria(OperatorCriteriaContext ctx) {
						switch (ctx.operator.getType()) {
							case Resolved:
								return new ResolvedCriteria();
							case Unresolved:
								return new UnresolvedCriteria();
							case MentionedMe:
								if (!withCurrentUserCriteria)
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new MentionedMeCriteria();
							case CreatedByMe:
								if (!withCurrentUserCriteria)
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new CreatedByMeCriteria();
							case RepliedByMe:
								if (!withCurrentUserCriteria)
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new RepliedByMeCriteria();
							default:
								throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
					}

					@Override
					public Criteria<CodeComment> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						int operator = ctx.operator.getType();
						var criterias = new ArrayList<Criteria<CodeComment>>();
						for (var quoted: ctx.criteriaValue.Quoted()) {
							String value = getValue(quoted.getText());
							if (operator == Mentioned) {
								criterias.add(new MentionedUserCriteria(getUser(value)));
							} else if (operator == CreatedBy) {
								criterias.add(new CreatedByUserCriteria(getUser(value)));
							} else if (operator == RepliedBy) {
								criterias.add(new RepliedByUserCriteria(getUser(value)));
							} else {
								ProjectScopedCommit commitId = getCommitId(project, value);
								criterias.add(new OnCommitCriteria(commitId.getProject(), commitId.getCommitId()));
							}
						}
						return Criteria.orCriterias(criterias);
					}

					@Override
					public Criteria<CodeComment> visitParensCriteria(ParensCriteriaContext ctx) {
						return visit(ctx.criteria()).withParens(true);
					}

					@Override
					public Criteria<CodeComment> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.criteriaField.getText());
						int operator = ctx.operator.getType();
						checkField(project, fieldName, operator);

						var criterias = new ArrayList<Criteria<CodeComment>>();
						for (var quoted: ctx.criteriaValue.Quoted()) {
							String value = getValue(quoted.getText());
							switch (operator) {
								case IsUntil:
								case IsSince:
									Date dateValue = getDateValue(value);
									switch (fieldName) {
										case NAME_CREATE_DATE:
											criterias.add(new CreateDateCriteria(dateValue, value, operator));
											break;
										case NAME_LAST_ACTIVITY_DATE:
											criterias.add(new LastActivityDateCriteria(dateValue, value, operator));
											break;
										default:
											throw new IllegalStateException();
									}
									break;
								case IsLessThan:
								case IsGreaterThan:
									criterias.add(new ReplyCountCriteria(getIntValue(value), operator));
									break;
								case Contains:
									switch (fieldName) {
										case NAME_CONTENT:
											criterias.add(new ContentCriteria(value));
											break;
										case NAME_REPLY:
											criterias.add(new ReplyCriteria(value));
											break;
										default:
											throw new IllegalStateException();
									}
									break;
								case Is:
								case IsNot:
									switch (fieldName) {
										case NAME_PATH:
											criterias.add(new PathCriteria(value, operator));
											break;
										case NAME_REPLY_COUNT:
											criterias.add(new ReplyCountCriteria(getIntValue(value), operator));
											break;
										default:
											throw new IllegalStateException();
									}
									break;
								default:
									throw new IllegalStateException();
							}
						}
						return operator==IsNot? Criteria.andCriterias(criterias): Criteria.orCriterias(criterias);
					}

					@Override
					public Criteria<CodeComment> visitOrCriteria(OrCriteriaContext ctx) {
						List<Criteria<CodeComment>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx : ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrCriteria<>(childCriterias);
					}

					@Override
					public Criteria<CodeComment> visitAndCriteria(AndCriteriaContext ctx) {
						List<Criteria<CodeComment>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx : ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndCriteria<>(childCriterias);
					}

					@Override
					public Criteria<CodeComment> visitNotCriteria(NotCriteriaContext ctx) {
						return new NotCriteria<>(visit(ctx.criteria()));
					}

				}.visit(criteriaContext);
			} else {
				commentCriteria = null;
			}

			List<EntitySort> commentSorts = new ArrayList<>();
			for (OrderContext order : queryContext.order()) {
				var fieldName = getValue(order.Quoted().getText());
				var sortField = SORT_FIELDS.get(fieldName);
				if (sortField == null)
					throw new ExplicitException("Can not order by field: " + fieldName);

				EntitySort commentSort = new EntitySort();
				commentSort.setField(fieldName);
				if (order.direction != null) {
					if (order.direction.getText().equals("desc"))
						commentSort.setDirection(DESCENDING);
					else
						commentSort.setDirection(ASCENDING);
				} else {
					commentSort.setDirection(sortField.getDefaultDirection());
				}
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
			case IsUntil:
			case IsSince:
				if (!fieldName.equals(NAME_CREATE_DATE) && !fieldName.equals(NAME_LAST_ACTIVITY_DATE))
					throw newOperatorException(fieldName, operator);
				break;
			case IsGreaterThan:
			case IsLessThan:
				if (!fieldName.equals(NAME_REPLY_COUNT))
					throw newOperatorException(fieldName, operator);
				break;
			case Contains:
				if (!fieldName.equals(NAME_CONTENT) && !fieldName.equals(NAME_REPLY))
					throw newOperatorException(fieldName, operator);
				break;
			case Is:
			case IsNot:
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

}
