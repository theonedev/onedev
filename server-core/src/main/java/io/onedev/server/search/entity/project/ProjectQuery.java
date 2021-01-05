package io.onedev.server.search.entity.project;

import java.util.ArrayList;
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
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.AndEntityCriteria;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.NotEntityCriteria;
import io.onedev.server.search.entity.OrEntityCriteria;
import io.onedev.server.search.entity.project.ProjectQueryParser.AndCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.CriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.NotCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.OperatorCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.OperatorValueCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.OrCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.OrderContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.ParensCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.QueryContext;

public class ProjectQuery extends EntityQuery<Project> {

	private static final long serialVersionUID = 1L;

	private final EntityCriteria<Project> criteria;
	
	private final List<EntitySort> sorts;
	
	public ProjectQuery(@Nullable EntityCriteria<Project> criteria, List<EntitySort> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	public ProjectQuery(@Nullable EntityCriteria<Project> criteria) {
		this(criteria, new ArrayList<>());
	}
	
	public ProjectQuery() {
		this(null);
	}
	
	@Nullable
	public EntityCriteria<Project> getCriteria() {
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
					throw new RuntimeException("Malformed project query", e);
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ProjectQueryParser parser = new ProjectQueryParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			QueryContext queryContext = parser.query();
			CriteriaContext criteriaContext = queryContext.criteria();
			EntityCriteria<Project> projectCriteria;
			if (criteriaContext != null) {
				projectCriteria = new ProjectQueryBaseVisitor<EntityCriteria<Project>>() {

					@Override
					public EntityCriteria<Project> visitOperatorCriteria(OperatorCriteriaContext ctx) {
						if (ctx.operator.getType() == ProjectQueryLexer.OwnedByMe)
							return new OwnedByMeCriteria();
						else
							return new OwnedByNoneCriteria();
					}
					
					public EntityCriteria<Project> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						String value = getValue(ctx.Quoted().getText());
						if (ctx.operator.getType() == ProjectQueryLexer.ForksOf)
							return new ForksOfCriteria(value);
						else
							return new OwnedByCriteria(getUser(value));
					}
					
					@Override
					public EntityCriteria<Project> visitParensCriteria(ParensCriteriaContext ctx) {
						return (EntityCriteria<Project>) visit(ctx.criteria()).withParens(true);
					}

					@Override
					public EntityCriteria<Project> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted(0).getText());
						String value = getValue(ctx.Quoted(1).getText());
						int operator = ctx.operator.getType();
						checkField(fieldName, operator);
						
						switch (operator) {
						case ProjectQueryLexer.Is:
							return new NameCriteria(value);
						case ProjectQueryLexer.Contains:
							return new DescriptionCriteria(value);
						case ProjectQueryLexer.IsUntil:
						case ProjectQueryLexer.IsSince:
							return new UpdateDateCriteria(value, operator);
						default:
							throw new ExplicitException("Unexpected operator " + getRuleName(operator));
						}
					}
					
					@Override
					public EntityCriteria<Project> visitOrCriteria(OrCriteriaContext ctx) {
						List<EntityCriteria<Project>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrEntityCriteria<Project>(childCriterias);
					}

					@Override
					public EntityCriteria<Project> visitAndCriteria(AndCriteriaContext ctx) {
						List<EntityCriteria<Project>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndEntityCriteria<Project>(childCriterias);
					}

					@Override
					public EntityCriteria<Project> visitNotCriteria(NotCriteriaContext ctx) {
						return new NotEntityCriteria<Project>(visit(ctx.criteria()));
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
		case ProjectQueryLexer.Contains:
			if (!fieldName.equals(Project.NAME_DESCRIPTION))
				throw newOperatorException(fieldName, operator);
			break;
		case ProjectQueryLexer.Is:
			if (!fieldName.equals(Project.NAME_NAME)) 
				throw newOperatorException(fieldName, operator);
			break;
		case ProjectQueryLexer.IsUntil:
		case ProjectQueryLexer.IsSince:
			if (!fieldName.equals(Project.NAME_UPDATE_DATE)) 
				throw newOperatorException(fieldName, operator);
			break;
		}
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
