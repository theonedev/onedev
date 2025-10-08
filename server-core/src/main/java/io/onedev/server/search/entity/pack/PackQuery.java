package io.onedev.server.search.entity.pack;

import static io.onedev.server.model.Pack.NAME_LABEL;
import static io.onedev.server.model.Pack.NAME_NAME;
import static io.onedev.server.model.Pack.NAME_PROJECT;
import static io.onedev.server.model.Pack.NAME_PUBLISH_DATE;
import static io.onedev.server.model.Pack.NAME_TYPE;
import static io.onedev.server.model.Pack.NAME_VERSION;
import static io.onedev.server.model.Pack.QUERY_FIELDS;
import static io.onedev.server.model.Pack.SORT_FIELDS;
import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;
import static io.onedev.server.search.entity.EntitySort.Direction.DESCENDING;
import static io.onedev.server.search.entity.pack.PackQueryParser.Is;
import static io.onedev.server.search.entity.pack.PackQueryParser.IsNot;
import static io.onedev.server.search.entity.pack.PackQueryParser.IsSince;
import static io.onedev.server.search.entity.pack.PackQueryParser.IsUntil;
import static io.onedev.server.search.entity.pack.PackQueryParser.PublishedByMe;

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
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.pack.PackQueryParser.AndCriteriaContext;
import io.onedev.server.search.entity.pack.PackQueryParser.CriteriaContext;
import io.onedev.server.search.entity.pack.PackQueryParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.search.entity.pack.PackQueryParser.FuzzyCriteriaContext;
import io.onedev.server.search.entity.pack.PackQueryParser.NotCriteriaContext;
import io.onedev.server.search.entity.pack.PackQueryParser.OrCriteriaContext;
import io.onedev.server.search.entity.pack.PackQueryParser.OrderContext;
import io.onedev.server.search.entity.pack.PackQueryParser.ParensCriteriaContext;
import io.onedev.server.search.entity.pack.PackQueryParser.QueryContext;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;

public class PackQuery extends EntityQuery<Pack> {

	private static final long serialVersionUID = 1L;

	public PackQuery(@Nullable Criteria<Pack> criteria, List<EntitySort> sorts) {
		super(criteria, sorts);
	}

	public PackQuery(@Nullable Criteria<Pack> criteria) {
		super(criteria, new ArrayList<>());
	}
	
	public PackQuery() {
		super(null, new ArrayList<>());
	}
	
	public static PackQuery parse(@Nullable Project project, @Nullable String queryString, boolean withCurrentUserCriteria) {
		if (queryString != null) {
			CharStream is = CharStreams.fromString(queryString); 
			PackQueryLexer lexer = new PackQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new RuntimeException("Malformed query", e);
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			PackQueryParser parser = new PackQueryParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			QueryContext queryContext = parser.query();
			CriteriaContext criteriaContext = queryContext.criteria();
			Criteria<Pack> packCriteria;
			if (criteriaContext != null) {
				packCriteria = new PackQueryBaseVisitor<Criteria<Pack>>() {

					@Override
					public Criteria<Pack> visitFuzzyCriteria(FuzzyCriteriaContext ctx) {
						return new FuzzyCriteria(getValue(ctx.getText()));
					}
					
					public Criteria<Pack> visitOperatorCriteria(PackQueryParser.OperatorCriteriaContext ctx) {
						switch (ctx.operator.getType()) {
							case PublishedByMe:
								if (!withCurrentUserCriteria)
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new PublishedByMeCriteria();
							default:
								throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
					}

					@Override
					public Criteria<Pack> visitOperatorValueCriteria(PackQueryParser.OperatorValueCriteriaContext ctx) {
						var criterias = new ArrayList<Criteria<Pack>>();
						for (var quoted: ctx.criteriaValue.Quoted()) {
							String value = getValue(quoted.getText());
							if (ctx.PublishedByUser() != null)
								criterias.add(new PublishedByUserCriteria(getUser(value)));
							else if (ctx.PublishedByBuild() != null)
								criterias.add(new PublishedViaBuildCriteria(project, value));
							else if (ctx.PublishedByProject() != null)
								criterias.add(new PublishedViaProjectCriteria(value));
							else
								throw new ExplicitException("Unexpected criteria: " + ctx.operator.getText());
						}	
						return Criteria.orCriterias(criterias);
					}
					
					@Override
					public Criteria<Pack> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.criteriaField.getText());
						int operator = ctx.operator.getType();
						checkField(project, fieldName, operator);

						var criterias = new ArrayList<Criteria<Pack>>();
						for (var quoted: ctx.criteriaValue.Quoted()) {
							String value = getValue(quoted.getText());
							switch (operator) {
								case IsUntil:
								case IsSince:
									if (fieldName.equals(NAME_PUBLISH_DATE))
										criterias.add(new PublishDateCriteria(value, operator));
									else
										throw new IllegalStateException();
									break;
								case Is:
								case IsNot:
									switch (fieldName) {
										case NAME_PROJECT:
											criterias.add(new ProjectCriteria(value, operator));
											break;
										case NAME_NAME:
											criterias.add(new NameCriteria(value, operator));
											break;
										case NAME_VERSION:
											criterias.add(new VersionCriteria(value, operator));
											break;
										case NAME_TYPE:
											criterias.add(new TypeCriteria(value, operator));
											break;
										case NAME_LABEL:
											criterias.add(new LabelCriteria(getLabelSpec(value), operator));
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
					public Criteria<Pack> visitParensCriteria(ParensCriteriaContext ctx) {
						return visit(ctx.criteria()).withParens(true);
					}
					
					@Override
					public Criteria<Pack> visitOrCriteria(OrCriteriaContext ctx) {
						List<Criteria<Pack>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new OrCriteria<>(childCriterias);
					}

					@Override
					public Criteria<Pack> visitAndCriteria(AndCriteriaContext ctx) {
						List<Criteria<Pack>> childCriterias = new ArrayList<>();
						for (CriteriaContext childCtx: ctx.criteria())
							childCriterias.add(visit(childCtx));
						return new AndCriteria<>(childCriterias);
					}

					@Override
					public Criteria<Pack> visitNotCriteria(NotCriteriaContext ctx) {
						return new NotCriteria<>(visit(ctx.criteria()));
					}

				}.visit(criteriaContext);
			} else {
				packCriteria = null;
			}

			List<EntitySort> buildSorts = new ArrayList<>();
			for (OrderContext order: queryContext.order()) {
				var fieldName = getValue(order.Quoted().getText());
				var sortField = SORT_FIELDS.get(fieldName);
				if (sortField == null)
					throw new ExplicitException("Can not order by field: " + fieldName);
				
				EntitySort packSort = new EntitySort();
				packSort.setField(fieldName);
				if (order.direction != null) {
					if (order.direction.getText().equals("desc"))
						packSort.setDirection(DESCENDING);
					else
						packSort.setDirection(ASCENDING);
				} else {
					packSort.setDirection(sortField.getDefaultDirection());
				}
				buildSorts.add(packSort);
			}
			
			return new PackQuery(packCriteria, buildSorts);
		} else {
			return new PackQuery();
		}
	}
	
	public static void checkField(Project project, String fieldName, int operator) {
		if (!QUERY_FIELDS.contains(fieldName))
			throw new ExplicitException("Field not found: " + fieldName);
		switch (operator) {
			case IsUntil:
			case IsSince:
				if (!fieldName.equals(NAME_PUBLISH_DATE))
					throw newOperatorException(fieldName, operator);
				break;
			case Is:
			case IsNot:
				if (!fieldName.equals(NAME_PROJECT) && !fieldName.equals(NAME_TYPE)
						&& !fieldName.equals(NAME_NAME) && !fieldName.equals(NAME_VERSION)
						&& !fieldName.equals(NAME_LABEL)) {
					throw newOperatorException(fieldName, operator);
				}
				break;
		}
	}
	
	private static ExplicitException newOperatorException(String fieldName, int operator) {
		return new ExplicitException("Field '" + fieldName + "' is not applicable for operator '" + getRuleName(operator) + "'");
	}
	
	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(PackQueryLexer.ruleNames, rule);
	}
	
	public static int getOperator(String operatorName) {
		return AntlrUtils.getLexerRule(PackQueryLexer.ruleNames, operatorName);
	}
	
	public static PackQuery merge(PackQuery baseQuery, PackQuery query) {
		List<Criteria<Pack>> criterias = new ArrayList<>();
		if (baseQuery.getCriteria() != null)
			criterias.add(baseQuery.getCriteria());
		if (query.getCriteria() != null)
			criterias.add(query.getCriteria());
		List<EntitySort> sorts = new ArrayList<>(query.getSorts());
		sorts.addAll(baseQuery.getSorts());
		return new PackQuery(Criteria.andCriterias(criterias), sorts);
	}
	
}
