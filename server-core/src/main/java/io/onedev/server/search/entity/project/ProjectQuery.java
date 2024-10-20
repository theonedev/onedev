package io.onedev.server.search.entity.project;

import io.onedev.commons.codeassist.AntlrUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;
import org.antlr.v4.runtime.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static io.onedev.server.search.entity.project.ProjectQueryParser.*;

public class ProjectQuery extends EntityQuery<Project> {

	private static final long serialVersionUID = 1L;

	private final Criteria<Project> criteria;
	
	private final List<EntitySort> sorts;
	
	public ProjectQuery(@Nullable Criteria<Project> criteria, List<EntitySort> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	public ProjectQuery(@Nullable Criteria<Project> criteria) {
		this(criteria, new ArrayList<>());
	}
	
	public ProjectQuery() {
		this(null);
	}
	
	@Nullable
	public Criteria<Project> getCriteria() {
		return criteria;
	}

	public List<EntitySort> getSorts() {
		return sorts;
	}

	public static ProjectQuery parse(@Nullable String queryString) {
		if (queryString != null) {
			CharStream is = CharStreams.fromString(queryString); 
			ProjectQueryLexer lexer = new ProjectQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new RuntimeException("Malformed query", e);
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ProjectQueryParser parser = new ProjectQueryParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			QueryContext queryContext = parser.query();
			CriteriaContext criteriaContext = queryContext.criteria();
			Criteria<Project> projectCriteria;
			if (criteriaContext != null) {
				projectCriteria = new ProjectQueryBaseVisitor<Criteria<Project>>() {

					@Override
					public Criteria<Project> visitFuzzyCriteria(FuzzyCriteriaContext ctx) {
						return new FuzzyCriteria(getValue(ctx.getText()));
					}
					
					@Override
					public Criteria<Project> visitOperatorCriteria(OperatorCriteriaContext ctx) {
						switch (ctx.operator.getType()) {
							case Roots:
								return new RootsCriteria();
							case Leafs:
								return new LeafsCriteria();
							case ForkRoots:
								return new ForkRootsCriteria();
							case OwnedByMe:
								return new OwnedByMeCriteria();
							case OwnedByNone:
								return new OwnedByNoneCriteria();
							case WithoutEnoughReplicas:
								return new WithoutEnoughReplicasCriteria();
							case HasOutdatedReplicas:
								return new HasOutdatedReplicasCriteria();
							case MissingStorage:
								return new MissingStorageCriteria();
							case PendingDelete:
								return new PendingDeleteCriteria();
							default:
								throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
					}
					
					public Criteria<Project> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						var criterias = new ArrayList<Criteria<Project>>();
						for (var quoted: ctx.criteriaValue.Quoted()) {
							String value = getValue(quoted.getText());
							if (ctx.operator.getType() == ChildrenOf)
								criterias.add(new ChildrenOfCriteria(value));
							else if (ctx.operator.getType() == ForksOf)
								criterias.add(new ForksOfCriteria(value));
							else
								criterias.add(new OwnedByCriteria(getUser(value)));
						}
						return new OrCriteria<>(criterias);
					}
					
					@Override
					public Criteria<Project> visitParensCriteria(ParensCriteriaContext ctx) {
						return visit(ctx.criteria()).withParens(true);
					}

					@Override
					public Criteria<Project> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.criteriaField.getText());
						int operator = ctx.operator.getType();
						checkField(fieldName, operator);
							
						var criterias = new ArrayList<Criteria<Project>>();
						for (var quoted: ctx.criteriaValue.Quoted()) {
							String value = getValue(quoted.getText());
							switch (operator) {
								case Is:
								case IsNot:
									switch (fieldName) {
										case Project.NAME_ID:
											criterias.add(new IdCriteria(getLongValue(value), operator));
											break;
										case Project.NAME_NAME:
											criterias.add(new NameCriteria(value, operator));
											break;
										case Project.NAME_KEY:
											criterias.add(new KeyCriteria(value, operator));
											break;
										case Project.NAME_SERVICE_DESK_NAME:
											criterias.add(new ServiceDeskNameCriteria(value, operator));
											break;
										case Project.NAME_PATH:
											criterias.add(new PathCriteria(value, operator));
											break;
										default:
											criterias.add(new LabelCriteria(getLabelSpec(value), operator));
									}
									break;
								case Contains:
									criterias.add(new DescriptionCriteria(value));
									break;
								case IsUntil:
								case IsSince:
									if (fieldName.equals(Project.NAME_LAST_ACTIVITY_DATE))
										criterias.add(new LastActivityDateCriteria(value, operator));
									else
										criterias.add(new CommitDateCriteria(value, operator));
									break;
								default:
									throw new ExplicitException("Unexpected operator " + getRuleName(operator));
							}
						}
						return operator==IsNot? new AndCriteria<>(criterias): new OrCriteria<>(criterias);
					}
					
					@Override
					public Criteria<Project> visitOrCriteria(OrCriteriaContext ctx) {
						List<Criteria<Project>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrCriteria<>(childCriterias);
					}

					@Override
					public Criteria<Project> visitAndCriteria(AndCriteriaContext ctx) {
						List<Criteria<Project>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndCriteria<>(childCriterias);
					}

					@Override
					public Criteria<Project> visitNotCriteria(NotCriteriaContext ctx) {
						return new NotCriteria<>(visit(ctx.criteria()));
					}
					
				}.visit(criteriaContext);
			} else {
				projectCriteria = null;
			}

			List<EntitySort> projectSorts = new ArrayList<>();
			for (OrderContext order: queryContext.order()) {
				String fieldName = getValue(order.Quoted().getText());
				if (!Project.ORDER_FIELDS.containsKey(fieldName)) 
					throw new ExplicitException("Can not order by field: " + fieldName);
				
				EntitySort projectSort = new EntitySort();
				projectSort.setField(fieldName);
				if (order.direction != null && order.direction.getText().equals("desc"))
					projectSort.setDirection(Direction.DESCENDING);
				else
					projectSort.setDirection(Direction.ASCENDING);
				projectSorts.add(projectSort);
			}
			
			return new ProjectQuery(projectCriteria, projectSorts);
		} else {
			return new ProjectQuery();
		}
	}
	
	private static ExplicitException newOperatorException(String fieldName, int operator) {
		return new ExplicitException("Field '" + fieldName + "' is not applicable for operator '" + getRuleName(operator) + "'");
	}
	
	public static void checkField(String fieldName, int operator) {
		if (!Project.QUERY_FIELDS.contains(fieldName))
			throw new ExplicitException("Field not found: " + fieldName);
		switch (operator) {
			case Contains:
				if (!fieldName.equals(Project.NAME_DESCRIPTION))
					throw newOperatorException(fieldName, operator);
				break;
			case Is:
			case IsNot:
				if (!fieldName.equals(Project.NAME_NAME)
						&& !fieldName.equals(Project.NAME_ID)
						&& !fieldName.equals(Project.NAME_KEY)
						&& !fieldName.equals(Project.NAME_LABEL)
						&& !fieldName.equals(Project.NAME_SERVICE_DESK_NAME)
						&& !fieldName.equals(Project.NAME_PATH)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case IsUntil:
			case IsSince:
				if (!fieldName.equals(Project.NAME_LAST_ACTIVITY_DATE)
						&& !fieldName.equals(Project.NAME_LAST_COMMIT_DATE)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
		}
	}
	
	public static ProjectQuery merge(ProjectQuery query1, ProjectQuery query2) {
		List<Criteria<Project>> criterias = new ArrayList<>();
		if (query1.getCriteria() != null)
			criterias.add(query1.getCriteria());
		if (query2.getCriteria() != null)
			criterias.add(query2.getCriteria());
		List<EntitySort> sorts = new ArrayList<>();
		sorts.addAll(query1.getSorts());
		sorts.addAll(query2.getSorts());
		return new ProjectQuery(Criteria.andCriterias(criterias), sorts);
	}
	
	@Override
	public boolean matches(Project project) {
		return criteria == null || criteria.matches(project);
	}
	
	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(ProjectQueryLexer.ruleNames, rule);
	}
	
	public static int getOperator(String operatorName) {
		return AntlrUtils.getLexerRule(ProjectQueryLexer.ruleNames, operatorName);
	}
	
}
