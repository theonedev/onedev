package io.onedev.server.search.entity.agent;

import io.onedev.commons.codeassist.AntlrUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AgentAttributeManager;
import io.onedev.server.model.Agent;
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

import static io.onedev.server.model.Agent.*;
import static io.onedev.server.search.entity.agent.AgentQueryParser.*;

public class AgentQuery extends EntityQuery<Agent> {

	private static final long serialVersionUID = 1L;

	private final Criteria<Agent> criteria;
	
	private final List<EntitySort> sorts;
	
	public AgentQuery(@Nullable Criteria<Agent> criteria, List<EntitySort> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	public AgentQuery(@Nullable Criteria<Agent> criteria) {
		this(criteria, new ArrayList<>());
	}
	
	public AgentQuery() {
		this(null);
	}
	
	public static AgentQuery parse(@Nullable String queryString, boolean forExecutor) {
		if (queryString != null) {
			CharStream is = CharStreams.fromString(queryString); 
			AgentQueryLexer lexer = new AgentQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new RuntimeException("Malformed query", e);
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			AgentQueryParser parser = new AgentQueryParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			QueryContext queryContext = parser.query();
			CriteriaContext criteriaContext = queryContext.criteria();
			Criteria<Agent> agentCriteria;
			if (criteriaContext != null) {
				agentCriteria = new AgentQueryBaseVisitor<Criteria<Agent>>() {

					@Override
					public Criteria<Agent> visitFuzzyCriteria(FuzzyCriteriaContext ctx) {
						return new FuzzyCriteria(getValue(ctx.getText()));
					}
					
					@Override
					public Criteria<Agent> visitOperatorCriteria(OperatorCriteriaContext ctx) {
						if (forExecutor)
							throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
						
						switch (ctx.operator.getType()) {
						case Online:
							return new OnlineCriteria();
						case Offline:
							return new OfflineCriteria();
						case Paused:
							return new PausedCriteria();
						case HasRunningBuilds:
							return new HasRunningBuildsCriteria();
						default:
							throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
					}
					
					@Override
					public Criteria<Agent> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						if (forExecutor && ctx.HasAttribute() == null)
							throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
						
						var criterias = new ArrayList<Criteria<Agent>>();
						for (var quoted: ctx.criteriaValue.Quoted()) {
							String value = getValue(quoted.getText());
							if (ctx.HasAttribute() != null)
								criterias.add(new HasAttributeCriteria(value));
							else if (ctx.NotUsedSince() != null)
								criterias.add(new NotUsedSinceCriteria(value));
							else if (ctx.EverUsedSince() != null)
								criterias.add(new EverUsedSinceCriteria(value));
							else if (ctx.RanBuild() != null)
								criterias.add(new RanBuildCriteria(value));
							else
								throw new RuntimeException("Unexpected criteria: " + ctx.operator.getText());
						}
						return new OrCriteria<>(criterias);
					}
					
					@Override
					public Criteria<Agent> visitParensCriteria(ParensCriteriaContext ctx) {
						return visit(ctx.criteria()).withParens(true);
					}

					@Override
					public Criteria<Agent> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.criteriaField.getText());
						int operator = ctx.operator.getType();
						checkField(fieldName, operator);

						var criterias = new ArrayList<Criteria<Agent>>();
						for (var quoted: ctx.criteriaValue.Quoted()) {
							String value = getValue(quoted.getText());
							if (operator == Is || operator == IsNot) {
								switch (fieldName) {
									case NAME_NAME:
										criterias.add(new NameCriteria(value, operator));
										break;
									case NAME_OS_NAME:
										criterias.add(new OsCriteria(value, operator));
										break;
									case NAME_OS_VERSION:
										criterias.add(new OsVersionCriteria(value, operator));
										break;
									case NAME_OS_ARCH:
										criterias.add(new OsArchCriteria(value, operator));
										break;
									case NAME_IP_ADDRESS:
										criterias.add(new IpAddressCriteria(value, operator));
										break;
									default:
										criterias.add(new AttributeCriteria(fieldName, value, operator));
								}
							} else {
								throw new IllegalStateException();
							}
						}
						return operator==IsNot? new AndCriteria<>(criterias): new OrCriteria<>(criterias);
					}
					
					@Override
					public Criteria<Agent> visitOrCriteria(OrCriteriaContext ctx) {
						List<Criteria<Agent>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrCriteria<>(childCriterias);
					}

					@Override
					public Criteria<Agent> visitAndCriteria(AndCriteriaContext ctx) {
						List<Criteria<Agent>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndCriteria<>(childCriterias);
					}

					@Override
					public Criteria<Agent> visitNotCriteria(NotCriteriaContext ctx) {
						return new NotCriteria<>(visit(ctx.criteria()));
					}

				}.visit(criteriaContext);
			} else {
				agentCriteria = null;
			}

			List<EntitySort> agentSorts = new ArrayList<>();
			for (OrderContext order: queryContext.order()) {
				String fieldName = getValue(order.Quoted().getText());
				if (!ORDER_FIELDS.containsKey(fieldName)) 
					throw new ExplicitException("Can not order by field: " + fieldName);
				
				EntitySort agentSort = new EntitySort();
				agentSort.setField(fieldName);
				if (order.direction != null && order.direction.getText().equals("desc"))
					agentSort.setDirection(Direction.DESCENDING);
				else
					agentSort.setDirection(Direction.ASCENDING);
				agentSorts.add(agentSort);
			}
			
			return new AgentQuery(agentCriteria, agentSorts);
		} else {
			return new AgentQuery();
		}
	}

	public static void checkField(String fieldName, int operator) {
		var attributeNames = OneDev.getInstance(AgentAttributeManager.class).getAttributeNames();
		if (!QUERY_FIELDS.contains(fieldName) && !attributeNames.contains(fieldName))
			throw new ExplicitException("Attribute not found: " + fieldName);
	}
	
	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(AgentQueryLexer.ruleNames, rule);
	}
	
	public static int getOperator(String operatorName) {
		return AntlrUtils.getLexerRule(AgentQueryLexer.ruleNames, operatorName);
	}
	
	@Override
	public Criteria<Agent> getCriteria() {
		return criteria;
	}

	@Override
	public List<EntitySort> getSorts() {
		return sorts;
	}
	
	public static AgentQuery merge(AgentQuery query1, AgentQuery query2) {
		List<Criteria<Agent>> criterias = new ArrayList<>();
		if (query1.getCriteria() != null)
			criterias.add(query1.getCriteria());
		if (query2.getCriteria() != null)
			criterias.add(query2.getCriteria());
		List<EntitySort> sorts = new ArrayList<>();
		sorts.addAll(query1.getSorts());
		sorts.addAll(query2.getSorts());
		return new AgentQuery(Criteria.andCriterias(criterias), sorts);
	}
	
}
