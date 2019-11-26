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
import io.onedev.server.OneException;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.project.ProjectQueryParser.AndCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.CriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.FieldOperatorCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.NotCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.OperatorValueCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.OrCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.OrderContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.ParensCriteriaContext;
import io.onedev.server.search.entity.project.ProjectQueryParser.QueryContext;
import io.onedev.server.util.ProjectConstants;

public class ProjectQuery extends EntityQuery<Project> {

	private static final long serialVersionUID = 1L;

	private final EntityCriteria<Project> criteria;
	
	private final List<EntitySort> sorts;
	
	public ProjectQuery(@Nullable EntityCriteria<Project> criteria, List<EntitySort> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	public ProjectQuery() {
		this(null, new ArrayList<>());
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
					throw new OneException("Malformed query syntax", e);
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ProjectQueryParser parser = new ProjectQueryParser(tokens);
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
			EntityCriteria<Project> projectCriteria;
			if (criteriaContext != null) {
				projectCriteria = new ProjectQueryBaseVisitor<EntityCriteria<Project>>() {

					@Override
					public EntityCriteria<Project> visitFieldOperatorCriteria(FieldOperatorCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted().getText());
						int operator = ctx.operator.getType();
						checkField(fieldName, operator);
						return new OwnerIsMeCriteria();
					}
					
					public EntityCriteria<Project> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						String value = getValue(ctx.Quoted().getText());
						return new ForksOfCriteria(value);
					}
					
					@Override
					public EntityCriteria<Project> visitParensCriteria(ParensCriteriaContext ctx) {
						return visit(ctx.criteria());
					}

					@Override
					public EntityCriteria<Project> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted(0).getText());
						String value = getValue(ctx.Quoted(1).getText());
						int operator = ctx.operator.getType();
						checkField(fieldName, operator);
						
						switch (operator) {
						case ProjectQueryLexer.Is:
							if (fieldName.equals(ProjectConstants.FIELD_NAME)) 
								return new NameCriteria(value);
							else 
								return new OwnerCriteria(value);
						case ProjectQueryLexer.Contains:
							return new DescriptionCriteria(value);
						default:
							throw new OneException("Unexpected operator " + getRuleName(operator));
						}
					}
					
					@Override
					public EntityCriteria<Project> visitOrCriteria(OrCriteriaContext ctx) {
						List<EntityCriteria<Project>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrCriteria(childCriterias);
					}

					@Override
					public EntityCriteria<Project> visitAndCriteria(AndCriteriaContext ctx) {
						List<EntityCriteria<Project>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndCriteria(childCriterias);
					}

					@Override
					public EntityCriteria<Project> visitNotCriteria(NotCriteriaContext ctx) {
						return new NotCriteria(visit(ctx.criteria()));
					}
					
				}.visit(criteriaContext);
			} else {
				projectCriteria = null;
			}

			List<EntitySort> projectSorts = new ArrayList<>();
			for (OrderContext order: queryContext.order()) {
				String fieldName = getValue(order.Quoted().getText());
				if (!ProjectConstants.ORDER_FIELDS.containsKey(fieldName)) 
					throw new OneException("Can not order by field: " + fieldName);
				
				EntitySort projectSort = new EntitySort();
				projectSort.setField(fieldName);
				if (order.direction != null && order.direction.getText().equals("asc"))
					projectSort.setDirection(Direction.ASCENDING);
				else
					projectSort.setDirection(Direction.DESCENDING);
				projectSorts.add(projectSort);
			}
			
			return new ProjectQuery(projectCriteria, projectSorts);
		} else {
			return new ProjectQuery();
		}
	}
	
	private static OneException newOperatorException(String fieldName, int operator) {
		return new OneException("Field '" + fieldName + "' is not applicable for operator '" + getRuleName(operator) + "'");
	}
	
	public static void checkField(String fieldName, int operator) {
		if (!ProjectConstants.QUERY_FIELDS.contains(fieldName))
			throw new OneException("Field not found: " + fieldName);
		switch (operator) {
		case ProjectQueryLexer.Contains:
			if (!fieldName.equals(ProjectConstants.FIELD_DESCRIPTION))
				throw newOperatorException(fieldName, operator);
			break;
		case ProjectQueryLexer.Is:
			if (!fieldName.equals(ProjectConstants.FIELD_OWNER) && !fieldName.equals(ProjectConstants.FIELD_NAME)) 
				throw newOperatorException(fieldName, operator);
			break;
		case ProjectQueryLexer.IsMe:
			if (!fieldName.equals(ProjectConstants.FIELD_OWNER)) 
				throw newOperatorException(fieldName, operator);
			break;
		}
	}
	
	@Override
	public boolean needsLogin() {
		return criteria != null && criteria.needsLogin();
	}
	
	@Override
	public boolean matches(Project project, User user) {
		return criteria == null || criteria.matches(project, user);
	}
	
	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(ProjectQueryLexer.ruleNames, rule);
	}
	
	public static int getOperator(String operatorName) {
		return AntlrUtils.getLexerRule(ProjectQueryLexer.ruleNames, operatorName);
	}
	
}
