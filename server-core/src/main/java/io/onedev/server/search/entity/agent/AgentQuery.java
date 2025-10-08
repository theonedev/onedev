package io.onedev.server.search.entity.agent;

import static io.onedev.server.model.Agent.NAME_IP_ADDRESS;
import static io.onedev.server.model.Agent.NAME_NAME;
import static io.onedev.server.model.Agent.NAME_OS_ARCH;
import static io.onedev.server.model.Agent.NAME_OS_NAME;
import static io.onedev.server.model.Agent.NAME_OS_VERSION;
import static io.onedev.server.model.Agent.QUERY_FIELDS;
import static io.onedev.server.model.Agent.SORT_FIELDS;
import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;
import static io.onedev.server.search.entity.EntitySort.Direction.DESCENDING;
import static io.onedev.server.search.entity.agent.AgentQueryParser.HasRunningBuilds;
import static io.onedev.server.search.entity.agent.AgentQueryParser.Is;
import static io.onedev.server.search.entity.agent.AgentQueryParser.IsNot;
import static io.onedev.server.search.entity.agent.AgentQueryParser.Offline;
import static io.onedev.server.search.entity.agent.AgentQueryParser.Online;
import static io.onedev.server.search.entity.agent.AgentQueryParser.Paused;

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
import io.onedev.server.OneDev;
import io.onedev.server.service.AgentAttributeService;
import io.onedev.server.model.Agent;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.agent.AgentQueryParser.AndCriteriaContext;
import io.onedev.server.search.entity.agent.AgentQueryParser.CriteriaContext;
import io.onedev.server.search.entity.agent.AgentQueryParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.search.entity.agent.AgentQueryParser.FuzzyCriteriaContext;
import io.onedev.server.search.entity.agent.AgentQueryParser.NotCriteriaContext;
import io.onedev.server.search.entity.agent.AgentQueryParser.OperatorCriteriaContext;
import io.onedev.server.search.entity.agent.AgentQueryParser.OperatorValueCriteriaContext;
import io.onedev.server.search.entity.agent.AgentQueryParser.OrCriteriaContext;
import io.onedev.server.search.entity.agent.AgentQueryParser.OrderContext;
import io.onedev.server.search.entity.agent.AgentQueryParser.ParensCriteriaContext;
import io.onedev.server.search.entity.agent.AgentQueryParser.QueryContext;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;

public class AgentQuery extends EntityQuery<Agent> {

	private static final long serialVersionUID = 1L;

	public AgentQuery(@Nullable Criteria<Agent> criteria, List<EntitySort> sorts) {
		super(criteria, sorts);
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
						return Criteria.orCriterias(criterias);
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
						return operator==IsNot? Criteria.andCriterias(criterias): Criteria.orCriterias(criterias);
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
				var fieldName = getValue(order.Quoted().getText());
				var sortField = SORT_FIELDS.get(fieldName);
				if (sortField == null)
					throw new ExplicitException("Can not order by field: " + fieldName);
				
				EntitySort agentSort = new EntitySort();
				agentSort.setField(fieldName);
				if (order.direction != null) {
					if (order.direction.getText().equals("desc"))
						agentSort.setDirection(DESCENDING);
					else
						agentSort.setDirection(ASCENDING);
				} else {
					agentSort.setDirection(sortField.getDefaultDirection());
				}
				agentSorts.add(agentSort);
			}
			
			return new AgentQuery(agentCriteria, agentSorts);
		} else {
			return new AgentQuery();
		}
	}

	public static void checkField(String fieldName, int operator) {
		var attributeNames = OneDev.getInstance(AgentAttributeService.class).getAttributeNames();
		if (!QUERY_FIELDS.contains(fieldName) && !attributeNames.contains(fieldName))
			throw new ExplicitException("Attribute not found: " + fieldName);
	}
	
	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(AgentQueryLexer.ruleNames, rule);
	}
	
	public static int getOperator(String operatorName) {
		return AntlrUtils.getLexerRule(AgentQueryLexer.ruleNames, operatorName);
	}
	
	public static AgentQuery merge(AgentQuery baseQuery, AgentQuery query) {
		List<Criteria<Agent>> criterias = new ArrayList<>();
		if (baseQuery.getCriteria() != null)
			criterias.add(baseQuery.getCriteria());
		if (query.getCriteria() != null)
			criterias.add(query.getCriteria());
		List<EntitySort> sorts = new ArrayList<>(query.getSorts());
		sorts.addAll(baseQuery.getSorts());
		return new AgentQuery(Criteria.andCriterias(criterias), sorts);
	}
	
}
