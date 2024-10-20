package io.onedev.server.search.entity.pack;

import io.onedev.commons.codeassist.AntlrUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Pack;
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

import static io.onedev.server.model.Pack.*;
import static io.onedev.server.search.entity.pack.PackQueryParser.*;

public class PackQuery extends EntityQuery<Pack> {

	private static final long serialVersionUID = 1L;

	private final Criteria<Pack> criteria;
	
	private final List<EntitySort> sorts;
	
	public PackQuery(@Nullable Criteria<Pack> criteria, List<EntitySort> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	public PackQuery(@Nullable Criteria<Pack> criteria) {
		this(criteria, new ArrayList<>());
	}
	
	public PackQuery() {
		this(null);
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
								criterias.add(new PublishedByBuildCriteria(project, value));
							else if (ctx.PublishedByProject() != null)
								criterias.add(new PublishedByProjectCriteria(value));
							else
								throw new ExplicitException("Unexpected criteria: " + ctx.operator.getText());
						}
						return new OrCriteria<>(criterias);
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
						return operator==IsNot? new AndCriteria<>(criterias): new OrCriteria<>(criterias);
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
				String fieldName = getValue(order.Quoted().getText());
				if (!ORDER_FIELDS.containsKey(fieldName)) 
					throw new ExplicitException("Can not order by field: " + fieldName);
				
				EntitySort buildSort = new EntitySort();
				buildSort.setField(fieldName);
				if (order.direction != null && order.direction.getText().equals("desc"))
					buildSort.setDirection(Direction.DESCENDING);
				else
					buildSort.setDirection(Direction.ASCENDING);
				buildSorts.add(buildSort);
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
	
	@Override
	public Criteria<Pack> getCriteria() {
		return criteria;
	}

	@Override
	public List<EntitySort> getSorts() {
		return sorts;
	}
	
	public static PackQuery merge(PackQuery query1, PackQuery query2) {
		List<Criteria<Pack>> criterias = new ArrayList<>();
		if (query1.getCriteria() != null)
			criterias.add(query1.getCriteria());
		if (query2.getCriteria() != null)
			criterias.add(query2.getCriteria());
		List<EntitySort> sorts = new ArrayList<>();
		sorts.addAll(query1.getSorts());
		sorts.addAll(query2.getSorts());
		return new PackQuery(Criteria.andCriterias(criterias), sorts);
	}
	
}
