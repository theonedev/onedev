package io.onedev.server.search.code.query;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import io.onedev.commons.jsymbol.Symbol;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.component.codequeryoption.SymbolQueryOptionEditor;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.Model;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static io.onedev.server.search.code.FieldConstants.*;
import static io.onedev.server.util.match.WildcardUtils.matchString;
import static io.onedev.server.util.match.WildcardUtils.rangeOfMatch;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;
import static org.apache.lucene.search.BooleanClause.Occur.SHOULD;

public class SymbolQueryOption implements QueryOption {

	private static final long serialVersionUID = 1L;
	
	private final String term;
	
	private final boolean caseSensitive;
	
	private final String fileNames;
	
	public SymbolQueryOption(@Nullable String term, boolean caseSensitive, @Nullable String fileNames) {
		this.term = term;
		this.caseSensitive = caseSensitive;
		this.fileNames = fileNames;
	}
	
	public SymbolQueryOption() {
		this(null, false, null);
	}

	@Nullable
	public String getTerm() {
		return term;
	}

	public String getFileNames() {
		return fileNames;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}
	
	public List<Match> matches(String blobPath, List<Symbol> symbols, @Nullable String excludeTerm, 
							   @Nullable String excludeBlobPath, @Nullable Boolean primary, 
							   @Nullable Boolean local, int count) {
		Preconditions.checkNotNull(term);
		
		var matches = new ArrayList<Match>();
		for (Symbol symbol: symbols) {
			if (matches.size() < count) {
				if ((primary == null || primary.booleanValue() == symbol.isPrimary())
						&& symbol.getName() != null
						&& symbol.isSearchable()
						&& (local == null || local.booleanValue() == symbol.isLocalInHierarchy())) {
					String normalizedTerm;
					if (!caseSensitive)
						normalizedTerm = term.toLowerCase();
					else
						normalizedTerm = term;

					String normalizedSymbolName;
					if (!caseSensitive)
						normalizedSymbolName = symbol.getName().toLowerCase();
					else
						normalizedSymbolName = symbol.getName();

					String normalizedExcludeTerm;
					if (excludeTerm != null) {
						if (!caseSensitive)
							normalizedExcludeTerm = excludeTerm.toLowerCase();
						else
							normalizedExcludeTerm = excludeTerm;
					} else {
						normalizedExcludeTerm = null;
					}
					if (matchString(normalizedTerm, normalizedSymbolName)
							&& !normalizedSymbolName.equals(normalizedExcludeTerm)
							&& !blobPath.equals(excludeBlobPath)) {
						matches.add(new Match(symbol, rangeOfMatch(normalizedTerm, normalizedSymbolName)));
					}
				}
			} else {
				break;
			}
		}
		return matches;
	}

	public void applyConstraints(BooleanQuery.Builder builder, @Nullable Boolean primary) {
		Preconditions.checkNotNull(term);
		
		if (fileNames != null) {
			BooleanQuery.Builder subQueryBuilder = new BooleanQuery.Builder();
			for (String pattern: Splitter.on(",").omitEmptyStrings().trimResults().split(fileNames.toLowerCase()))
				subQueryBuilder.add(new WildcardQuery(new Term(BLOB_NAME.name(), pattern)), SHOULD);
			BooleanQuery subQuery = subQueryBuilder.build();
			if (subQuery.clauses().size() != 0)
				builder.add(subQuery, MUST);
		}

		boolean tooGeneral = true;
		for (char ch: term.toCharArray()) {
			if (ch != '?' && ch != '*') {
				tooGeneral = false;
				break;
			}
		}
		if (tooGeneral)
			throw new TooGeneralQueryException();

		if (primary != null) {
			String fieldName;
			if (primary)
				fieldName = BLOB_PRIMARY_SYMBOLS.name();
			else
				fieldName = BLOB_SECONDARY_SYMBOLS.name();

			builder.add(new WildcardQuery(new Term(fieldName, term.toLowerCase())), MUST);
		} else {
			BooleanQuery.Builder termQueryBuilder = new BooleanQuery.Builder();
			termQueryBuilder.add(new WildcardQuery(new Term(BLOB_PRIMARY_SYMBOLS.name(), term.toLowerCase())), SHOULD);
			termQueryBuilder.add(new WildcardQuery(new Term(BLOB_SECONDARY_SYMBOLS.name(), term.toLowerCase())), SHOULD);
			builder.add(termQueryBuilder.build(), MUST);
		}
	}

	@Override
	public FormComponentPanel<? extends QueryOption> newOptionEditor(String componentId) {
		return new SymbolQueryOptionEditor(componentId, Model.of(this));
	}

	public static class Match implements Serializable {

		private static final long serialVersionUID = 1L;

		private final Symbol symbol;

		private final LinearRange position;

		public Match(Symbol symbol, @Nullable LinearRange position) {
			this.symbol = symbol;
			this.position = position;
		}

		public Symbol getSymbol() {
			return symbol;
		}

		@Nullable
		public LinearRange getPosition() {
			return position;
		}
	}

}
