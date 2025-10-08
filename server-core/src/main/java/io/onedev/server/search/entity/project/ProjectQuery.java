package io.onedev.server.search.entity.project;

import static io.onedev.server.model.Project.SORT_FIELDS;
import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;
import static io.onedev.server.search.entity.EntitySort.Direction.DESCENDING;
import static io.onedev.server.search.entity.project.ProjectQueryParser.ChildrenOf;
import static io.onedev.server.search.entity.project.ProjectQueryParser.Contains;
import static io.onedev.server.search.entity.project.ProjectQueryParser.ForkRoots;
import static io.onedev.server.search.entity.project.ProjectQueryParser.ForksOf;
import static io.onedev.server.search.entity.project.ProjectQueryParser.HasOutdatedReplicas;
import static io.onedev.server.search.entity.project.ProjectQueryParser.Is;
import static io.onedev.server.search.entity.project.ProjectQueryParser.IsNot;
import static io.onedev.server.search.entity.project.ProjectQueryParser.IsSince;
import static io.onedev.server.search.entity.project.ProjectQueryParser.IsUntil;
import static io.onedev.server.search.entity.project.ProjectQueryParser.Leafs;
import static io.onedev.server.search.entity.project.ProjectQueryParser.MissingStorage;
import static io.onedev.server.search.entity.project.ProjectQueryParser.OwnedByMe;
import static io.onedev.server.search.entity.project.ProjectQueryParser.OwnedByNone;
import static io.onedev.server.search.entity.project.ProjectQueryParser.Roots;
import static io.onedev.server.search.entity.project.ProjectQueryParser.WithoutEnoughReplicas;

import java.util.ArrayList;
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
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.project.ProjectQueryParser.AndCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.CriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.FuzzyCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.NotCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.OperatorCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.OperatorValueCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.OrCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.OrderContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.ParensCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.QueryContext;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;

public class ProjectQuery extends EntityQuery<Project> {

	private static final long serialVersionUID = 1L;

	public ProjectQuery(@Nullable Criteria<Project> criteria, List<EntitySort> sorts, List<EntitySort> baseSorts) {
		super(criteria, sorts, baseSorts);
	}

	public ProjectQuery(@Nullable Criteria<Project> criteria, List<EntitySort> sorts) {
		this(criteria, sorts, new ArrayList<>());
	}

	public ProjectQuery(@Nullable Criteria<Project> criteria) {
		this(criteria, new ArrayList<>());
	}
	
	public ProjectQuery() {
		this(null);
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
								return new NoOwnerCriteria();
							case WithoutEnoughReplicas:
								return new WithoutEnoughReplicasCriteria();
							case HasOutdatedReplicas:
								return new HasOutdatedReplicasCriteria();
							case MissingStorage:
								return new MissingStorageCriteria();
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
								criterias.add(new OwnedByUserCriteria(getUser(value)));
						}
						return Criteria.orCriterias(criterias);
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
										case Project.NAME_SERVICE_DESK_EMAIL_ADDRESS:
											criterias.add(new ServiceDeskEmailAddressCriteria(value, operator));
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
									criterias.add(new LastActivityDateCriteria(value, operator));
									break;
								default:
									throw new ExplicitException("Unexpected operator " + getRuleName(operator));
							}
						}
						return operator==IsNot? Criteria.andCriterias(criterias): Criteria.orCriterias(criterias);
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
				var fieldName = getValue(order.Quoted().getText());
				var sortField = SORT_FIELDS.get(fieldName);
				if (sortField == null)
					throw new ExplicitException("Can not order by field: " + fieldName);
				
				EntitySort projectSort = new EntitySort();
				projectSort.setField(fieldName);
				if (order.direction != null) {
					if (order.direction.getText().equals("desc"))
						projectSort.setDirection(DESCENDING);
					else
						projectSort.setDirection(ASCENDING);
				} else {
					projectSort.setDirection(sortField.getDefaultDirection());
				}
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
						&& !fieldName.equals(Project.NAME_SERVICE_DESK_EMAIL_ADDRESS)
						&& !fieldName.equals(Project.NAME_PATH)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
			case IsUntil:
			case IsSince:
				if (!fieldName.equals(Project.NAME_LAST_ACTIVITY_DATE)
						&& !fieldName.equals(Project.NAME_LAST_ACTIVITY_DATE)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
		}
	}
	
	public static ProjectQuery merge(ProjectQuery baseQuery, ProjectQuery query) {
		List<Criteria<Project>> criterias = new ArrayList<>();
		if (baseQuery.getCriteria() != null)
			criterias.add(baseQuery.getCriteria());
		if (query.getCriteria() != null)
			criterias.add(query.getCriteria());
		return new ProjectQuery(Criteria.andCriterias(criterias), query.getSorts(), baseQuery.getSorts());
	}
	
	@Override
	public boolean matches(Project project) {
		return getCriteria() == null || getCriteria().matches(project);
	}
	
	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(ProjectQueryLexer.ruleNames, rule);
	}
	
	public static int getOperator(String operatorName) {
		return AntlrUtils.getLexerRule(ProjectQueryLexer.ruleNames, operatorName);
	}
	
}
