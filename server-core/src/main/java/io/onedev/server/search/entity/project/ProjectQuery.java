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

import static io.onedev.server.search.entity.project.ProjectQueryLexer.ChildrenOf;
import static io.onedev.server.search.entity.project.ProjectQueryLexer.Contains;
import static io.onedev.server.search.entity.project.ProjectQueryLexer.ForkRoots;
import static io.onedev.server.search.entity.project.ProjectQueryLexer.ForksOf;
import static io.onedev.server.search.entity.project.ProjectQueryLexer.HasOutdatedReplicas;
import static io.onedev.server.search.entity.project.ProjectQueryLexer.Is;
import static io.onedev.server.search.entity.project.ProjectQueryLexer.IsSince;
import static io.onedev.server.search.entity.project.ProjectQueryLexer.IsUntil;
import static io.onedev.server.search.entity.project.ProjectQueryLexer.Leafs;
import static io.onedev.server.search.entity.project.ProjectQueryLexer.MissingStorage;
import static io.onedev.server.search.entity.project.ProjectQueryLexer.OwnedByMe;
import static io.onedev.server.search.entity.project.ProjectQueryLexer.OwnedByNone;
import static io.onedev.server.search.entity.project.ProjectQueryLexer.Roots;
import static io.onedev.server.search.entity.project.ProjectQueryLexer.WithoutEnoughReplicas;
import static io.onedev.server.search.entity.project.ProjectQueryLexer.ruleNames;
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
							default:
								throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
					}
					
					public Criteria<Project> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						String value = getValue(ctx.Quoted().getText());
						if (ctx.operator.getType() == ChildrenOf)
							return new ChildrenOfCriteria(value);
						else if (ctx.operator.getType() == ForksOf)
							return new ForksOfCriteria(value);
						else
							return new OwnedByCriteria(getUser(value));
					}
					
					@Override
					public Criteria<Project> visitParensCriteria(ParensCriteriaContext ctx) {
						return (Criteria<Project>) visit(ctx.criteria()).withParens(true);
					}

					@Override
					public Criteria<Project> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted(0).getText());
						String value = getValue(ctx.Quoted(1).getText());
						int operator = ctx.operator.getType();
						checkField(fieldName, operator);
						
						switch (operator) {
						case Is:
							if (fieldName.equals(Project.NAME_NAME))
								return new NameCriteria(value);
							else if (fieldName.equals(Project.NAME_SERVICE_DESK_NAME))
								return new ServiceDeskNameCriteria(value);
							else if (fieldName.equals(Project.NAME_PATH))
								return new PathCriteria(value);
							else
								return new LabelCriteria(getLabelSpec(value));
						case Contains:
							return new DescriptionCriteria(value);
						case IsUntil:
						case IsSince:
							if (fieldName.equals(Project.NAME_LAST_ACTIVITY_DATE))
								return new LastActivityDateCriteria(value, operator);
							else 
								return new CommitDateCriteria(value, operator);
						default:
							throw new ExplicitException("Unexpected operator " + getRuleName(operator));
						}
					}
					
					@Override
					public Criteria<Project> visitOrCriteria(OrCriteriaContext ctx) {
						List<Criteria<Project>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrCriteria<Project>(childCriterias);
					}

					@Override
					public Criteria<Project> visitAndCriteria(AndCriteriaContext ctx) {
						List<Criteria<Project>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndCriteria<Project>(childCriterias);
					}

					@Override
					public Criteria<Project> visitNotCriteria(NotCriteriaContext ctx) {
						return new NotCriteria<Project>(visit(ctx.criteria()));
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
			if (!fieldName.equals(Project.NAME_NAME) 
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
		return AntlrUtils.getLexerRuleName(ruleNames, rule);
	}
	
	public static int getOperator(String operatorName) {
		return AntlrUtils.getLexerRule(ruleNames, operatorName);
	}
	
}
