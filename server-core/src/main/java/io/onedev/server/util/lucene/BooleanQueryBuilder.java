package io.onedev.server.util.lucene;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;

public class BooleanQueryBuilder extends BooleanQuery.Builder {

	@Override
	public Builder add(Query query, Occur occur) {
		if (query != null && !LuceneUtils.isEmpty(query)) 
			super.add(query, occur);
		return this;
	}

	@Override
	public Builder add(BooleanClause clause) {
		if (clause.getQuery() != null && !LuceneUtils.isEmpty(clause.getQuery()))
			super.add(clause);
		return this;
	}

	@Override
	public BooleanQuery build() {
		BooleanQuery query = super.build();
		if (!LuceneUtils.isEmpty(query))
			return query;
		else
			return null;
	}

	@Override
	public Builder setMinimumNumberShouldMatch(int min) {
		throw new UnsupportedOperationException();
	}

}
