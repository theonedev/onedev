package io.onedev.server.search.entity.workspace;

import static io.onedev.server.model.AbstractEntity.NAME_NUMBER;
import static io.onedev.server.model.Workspace.NAME_ACTIVE_DATE;
import static io.onedev.server.model.Workspace.NAME_BRANCH;
import static io.onedev.server.model.Workspace.NAME_CREATE_DATE;
import static io.onedev.server.model.Workspace.NAME_PROJECT;
import static io.onedev.server.model.Workspace.NAME_SPEC;
import static io.onedev.server.model.Workspace.QUERY_FIELDS;
import static io.onedev.server.model.Workspace.SORT_FIELDS;
import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;
import static io.onedev.server.search.entity.EntitySort.Direction.DESCENDING;
import static io.onedev.server.search.entity.workspace.WorkspaceQueryParser.Active;
import static io.onedev.server.search.entity.workspace.WorkspaceQueryParser.Inactive;
import static io.onedev.server.search.entity.workspace.WorkspaceQueryParser.Is;
import static io.onedev.server.search.entity.workspace.WorkspaceQueryParser.IsGreaterThan;
import static io.onedev.server.search.entity.workspace.WorkspaceQueryParser.IsLessThan;
import static io.onedev.server.search.entity.workspace.WorkspaceQueryParser.IsNot;
import static io.onedev.server.search.entity.workspace.WorkspaceQueryParser.IsSince;
import static io.onedev.server.search.entity.workspace.WorkspaceQueryParser.IsUntil;
import static io.onedev.server.search.entity.workspace.WorkspaceQueryParser.Pending;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.jspecify.annotations.Nullable;

import io.onedev.commons.codeassist.AntlrUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.workspace.WorkspaceQueryParser.AndCriteriaContext;
import io.onedev.server.search.entity.workspace.WorkspaceQueryParser.CriteriaContext;
import io.onedev.server.search.entity.workspace.WorkspaceQueryParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.search.entity.workspace.WorkspaceQueryParser.FuzzyCriteriaContext;
import io.onedev.server.search.entity.workspace.WorkspaceQueryParser.NotCriteriaContext;
import io.onedev.server.search.entity.workspace.WorkspaceQueryParser.OperatorCriteriaContext;
import io.onedev.server.search.entity.workspace.WorkspaceQueryParser.OperatorValueCriteriaContext;
import io.onedev.server.search.entity.workspace.WorkspaceQueryParser.OrCriteriaContext;
import io.onedev.server.search.entity.workspace.WorkspaceQueryParser.OrderContext;
import io.onedev.server.search.entity.workspace.WorkspaceQueryParser.ParensCriteriaContext;
import io.onedev.server.search.entity.workspace.WorkspaceQueryParser.QueryContext;
import io.onedev.server.search.entity.workspace.WorkspaceQueryParser.ReferenceCriteriaContext;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;

public class WorkspaceQuery extends EntityQuery<Workspace> {

	private static final long serialVersionUID = 1L;

	public WorkspaceQuery(@Nullable Criteria<Workspace> criteria, List<EntitySort> sorts) {
		super(criteria, sorts);
	}

	public WorkspaceQuery(@Nullable Criteria<Workspace> criteria) {
		super(criteria, new ArrayList<>());
	}

	public WorkspaceQuery() {
		super(null, new ArrayList<>());
	}

	public static WorkspaceQuery parse(@Nullable String queryString) {
		return parse(null, queryString, false);
	}

	public static WorkspaceQuery parse(@Nullable Project project, @Nullable String queryString) {
		return parse(project, queryString, false);
	}

	public static WorkspaceQuery parse(@Nullable Project project, @Nullable String queryString, boolean withCurrentUserCriteria) {
		if (queryString != null) {
			CharStream is = CharStreams.fromString(queryString);
			WorkspaceQueryLexer lexer = new WorkspaceQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new RuntimeException("Malformed query", e);
				}

			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			WorkspaceQueryParser parser = new WorkspaceQueryParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			QueryContext queryContext = parser.query();
			CriteriaContext criteriaContext = queryContext.criteria();
			Criteria<Workspace> workspaceCriteria;
			if (criteriaContext != null) {
				workspaceCriteria = new WorkspaceQueryBaseVisitor<Criteria<Workspace>>() {

					@Override
					public Criteria<Workspace> visitOperatorCriteria(OperatorCriteriaContext ctx) {
						switch (ctx.operator.getType()) {
						case Pending:
							return new PendingCriteria();
						case Active:
							return new ActiveCriteria();
						case Inactive:
							return new InactiveCriteria();
						case WorkspaceQueryLexer.CreatedByMe:
							if (!withCurrentUserCriteria)
								throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
							return new CreatedByMeCriteria();
						default:
							throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
					}

					@Override
					public Criteria<Workspace> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						var criterias = new ArrayList<Criteria<Workspace>>();
						for (var quoted : ctx.criteriaValue.Quoted()) {
							String value = getValue(quoted.getText());
							if (ctx.CreatedBy() != null)
								criterias.add(new CreatedByUserCriteria(getUser(value)));
							else
								throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
						return Criteria.orCriterias(criterias);
					}

					@Override
					public Criteria<Workspace> visitFuzzyCriteria(FuzzyCriteriaContext ctx) {
						return new FuzzyCriteria(getValue(ctx.getText()));
					}

					@Override
					public Criteria<Workspace> visitReferenceCriteria(ReferenceCriteriaContext ctx) {
						return new NumberCriteria(project, ctx.getText(), WorkspaceQueryParser.Is);
					}

					@Override
					public Criteria<Workspace> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.criteriaField.getText());
						int operator = ctx.operator.getType();
						checkField(fieldName, operator);

						var criterias = new ArrayList<Criteria<Workspace>>();
						for (var quoted : ctx.criteriaValue.Quoted()) {
							String value = getValue(quoted.getText());
						switch (fieldName) {
							case NAME_NUMBER:
								criterias.add(new NumberCriteria(project, value, operator));
								break;
							case NAME_PROJECT:
								criterias.add(new ProjectCriteria(value, operator));
								break;
							case NAME_BRANCH:
								criterias.add(new BranchCriteria(value, operator));
								break;
							case NAME_SPEC:
								criterias.add(new SpecCriteria(value, operator));
								break;
							case NAME_CREATE_DATE:
								criterias.add(new CreateDateCriteria(value, operator));
								break;
							case NAME_ACTIVE_DATE:
								criterias.add(new ActiveDateCriteria(value, operator));
								break;
							default:
								throw new ExplicitException("Unexpected field: " + fieldName);
							}
						}
						return operator == IsNot ? Criteria.andCriterias(criterias) : Criteria.orCriterias(criterias);
					}

					@Override
					public Criteria<Workspace> visitParensCriteria(ParensCriteriaContext ctx) {
						return visit(ctx.criteria()).withParens(true);
					}

					@Override
					public Criteria<Workspace> visitOrCriteria(OrCriteriaContext ctx) {
						List<Criteria<Workspace>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx : ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrCriteria<>(childCriterias);
					}

					@Override
					public Criteria<Workspace> visitAndCriteria(AndCriteriaContext ctx) {
						List<Criteria<Workspace>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx : ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndCriteria<>(childCriterias);
					}

					@Override
					public Criteria<Workspace> visitNotCriteria(NotCriteriaContext ctx) {
						return new NotCriteria<>(visit(ctx.criteria()));
					}

				}.visit(criteriaContext);
			} else {
				workspaceCriteria = null;
			}

			List<EntitySort> workspaceSorts = new ArrayList<>();
			for (OrderContext order : queryContext.order()) {
				var fieldName = getValue(order.Quoted().getText());
				var sortField = SORT_FIELDS.get(fieldName);
				if (sortField == null)
					throw new ExplicitException("Can not order by field: " + fieldName);

				EntitySort workspaceSort = new EntitySort();
				workspaceSort.setField(fieldName);
				if (order.direction != null) {
					if (order.direction.getText().equals("desc"))
						workspaceSort.setDirection(DESCENDING);
					else
						workspaceSort.setDirection(ASCENDING);
				} else {
					workspaceSort.setDirection(sortField.getDefaultDirection());
				}
				workspaceSorts.add(workspaceSort);
			}

			return new WorkspaceQuery(workspaceCriteria, workspaceSorts);
		} else {
			return new WorkspaceQuery();
		}
	}

	public static void checkField(String fieldName, int operator) {
		if (!QUERY_FIELDS.contains(fieldName))
			throw new ExplicitException("Field not found: " + fieldName);
		switch (operator) {
			case Is:
			case IsNot:
				if (!fieldName.equals(NAME_NUMBER) && !fieldName.equals(NAME_PROJECT)
						&& !fieldName.equals(NAME_BRANCH) && !fieldName.equals(NAME_SPEC)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case IsGreaterThan:
			case IsLessThan:
				if (!fieldName.equals(NAME_NUMBER))
					throw newOperatorException(fieldName, operator);
				break;
			case IsSince:
			case IsUntil:
				if (!fieldName.equals(NAME_CREATE_DATE) && !fieldName.equals(NAME_ACTIVE_DATE))
					throw newOperatorException(fieldName, operator);
				break;
			default:
				throw newOperatorException(fieldName, operator);
		}
	}

	private static ExplicitException newOperatorException(String fieldName, int operator) {
		return new ExplicitException("Field '" + fieldName + "' is not applicable for operator '" + getRuleName(operator) + "'");
	}

	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(WorkspaceQueryLexer.ruleNames, rule);
	}

	public static int getOperator(String operatorName) {
		return AntlrUtils.getLexerRule(WorkspaceQueryLexer.ruleNames, operatorName);
	}

	public static WorkspaceQuery merge(WorkspaceQuery baseQuery, WorkspaceQuery query) {
		List<Criteria<Workspace>> criterias = new ArrayList<>();
		if (baseQuery.getCriteria() != null)
			criterias.add(baseQuery.getCriteria());
		if (query.getCriteria() != null)
			criterias.add(query.getCriteria());
		List<EntitySort> sorts = new ArrayList<>();
		sorts.addAll(query.getSorts());
		sorts.addAll(baseQuery.getSorts());
		return new WorkspaceQuery(Criteria.andCriterias(criterias), sorts);
	}

}
