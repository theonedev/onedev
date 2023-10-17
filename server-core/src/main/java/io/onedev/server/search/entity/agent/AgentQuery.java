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
						case AgentQueryLexer.Online:
							return new OnlineCriteria();
						case AgentQueryLexer.Offline:
							return new OfflineCriteria();
						case AgentQueryLexer.Paused:
							return new PausedCriteria();
						case AgentQueryLexer.HasRunningBuilds:
							return new HasRunningBuildsCriteria();
						default:
							throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
					}
					
					@Override
					public Criteria<Agent> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
						if (forExecutor && ctx.HasAttribute() == null)
							throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
						
						String value = getValue(ctx.Quoted().getText());
						if (ctx.HasAttribute() != null) 
							return new HasAttributeCriteria(value);
						else if (ctx.NotUsedSince() != null) 
							return new NotUsedSinceCriteria(value);
						else if (ctx.EverUsedSince() != null) 
							return new EverUsedSinceCriteria(value);
						else if (ctx.RanBuild() != null) 
							return new RanBuildCriteria(value);
						else 
							throw new RuntimeException("Unexpected criteria: " + ctx.operator.getText());
					}
					
					@Override
					public Criteria<Agent> visitParensCriteria(ParensCriteriaContext ctx) {
						return (Criteria<Agent>) visit(ctx.criteria()).withParens(true);
					}

					@Override
					public Criteria<Agent> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted(0).getText());
						String value = getValue(ctx.Quoted(1).getText());
						int operator = ctx.operator.getType();
						checkField(fieldName, operator);
						
						switch (operator) {
						case AgentQueryLexer.Is:
							switch (fieldName) {
							case NAME_NAME:
								return new NameCriteria(value);
							case NAME_OS_NAME:
								return new OsCriteria(value);
							case NAME_OS_VERSION:
								return new OsVersionCriteria(value);
							case NAME_OS_ARCH:
								return new OsArchCriteria(value);
							case NAME_IP_ADDRESS:
								return new IpAddressCriteria(value);
							default: 
								return new AttributeCriteria(fieldName, value);
							}
						default:
							throw new IllegalStateException();
						}
					}
					
					@Override
					public Criteria<Agent> visitOrCriteria(OrCriteriaContext ctx) {
						List<Criteria<Agent>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrCriteria<Agent>(childCriterias);
					}

					@Override
					public Criteria<Agent> visitAndCriteria(AndCriteriaContext ctx) {
						List<Criteria<Agent>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndCriteria<Agent>(childCriterias);
					}

					@Override
					public Criteria<Agent> visitNotCriteria(NotCriteriaContext ctx) {
						return new NotCriteria<Agent>(visit(ctx.criteria()));
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
