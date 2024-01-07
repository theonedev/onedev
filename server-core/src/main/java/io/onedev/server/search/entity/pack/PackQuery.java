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
							case PackQueryLexer.PublishedByMe:
								if (!withCurrentUserCriteria)
									throw new ExplicitException("Criteria '" + ctx.operator.getText() + "' is not supported here");
								return new PublishedByMeCriteria();
							default:
								throw new ExplicitException("Unexpected operator: " + ctx.operator.getText());
						}
					}

					@Override
					public Criteria<Pack> visitOperatorValueCriteria(PackQueryParser.OperatorValueCriteriaContext ctx) {
						String value = getValue(ctx.Quoted().getText());
						if (ctx.PublishedByUser() != null)
							return new PublishedByUserCriteria(getUser(value));
						else if (ctx.PublishedByBuild() != null)
							return new PublishedByBuildCriteria(project, value);
						else if (ctx.PublishedByProject() != null)
							return new PublishedByProjectCriteria(value);
						else
							throw new RuntimeException("Unexpected criteria: " + ctx.operator.getText());
					}
					
					@Override
					public Criteria<Pack> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
						String fieldName = getValue(ctx.Quoted(0).getText());
						String value = getValue(ctx.Quoted(1).getText());
						int operator = ctx.operator.getType();
						checkField(project, fieldName, operator);

						switch (operator) {
							case PackQueryLexer.IsUntil:
							case PackQueryLexer.IsSince:
								if (fieldName.equals(NAME_PUBLISH_DATE))
									return new PublishDateCriteria(value, operator);
								else
									throw new IllegalStateException();
							case PackQueryLexer.Is:
							case PackQueryLexer.IsNot:
								switch (fieldName) {
									case NAME_PROJECT:
										return new ProjectCriteria(value, operator);
									case NAME_TAG:
										return new TagCriteria(value, operator);
									case NAME_GROUP_ID:
										return new GroupIdCriteria(value, operator);
									case NAME_ARTIFACT_ID:
										return new ArtiractIdCriteria(value, operator);
									case NAME_NAME:
										return new NameCriteria(value, operator);
									case NAME_VERSION:
										return new VersionCriteria(value, operator);
									case NAME_TYPE:
										return new TypeCriteria(value, operator);
									case NAME_LABEL:
										return new LabelCriteria(getLabelSpec(value), operator);
									default:
										throw new IllegalStateException();
								}
							default:
								throw new IllegalStateException();
						}
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
			case PackQueryLexer.IsUntil:
			case PackQueryLexer.IsSince:
				if (!fieldName.equals(NAME_PUBLISH_DATE))
					throw newOperatorException(fieldName, operator);
				break;
			case PackQueryLexer.Is:
			case PackQueryLexer.IsNot:
				if (!fieldName.equals(NAME_PROJECT) && !fieldName.equals(NAME_TYPE)
						&& !fieldName.equals(NAME_TAG) && !fieldName.equals(NAME_GROUP_ID)
						&& !fieldName.equals(NAME_ARTIFACT_ID) && !fieldName.equals(NAME_VERSION)
						&& !fieldName.equals(NAME_NAME) && !fieldName.equals(NAME_LABEL)) {
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
