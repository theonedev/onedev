package io.onedev.server.ee.xsearch.query;

import com.google.common.base.Preconditions;
import io.onedev.commons.jsymbol.Symbol;
import io.onedev.server.ee.xsearch.hit.QueryHit;
import io.onedev.server.ee.xsearch.hit.SymbolHit;
import io.onedev.server.search.code.query.SymbolQueryOption;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;

import static io.onedev.server.search.code.FieldConstants.*;

public class SymbolQuery extends BlobQuery {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(SymbolQuery.class);

	private final String term;
	
	private final boolean caseSensitive;
	
	private final String fileNames;
	
	private final Boolean primary;
	
	private SymbolQuery(String term, boolean caseSensitive, @Nullable String fileNames,
						@Nullable Boolean primary,  @Nullable String projects, int count) {
		super(projects, count);
		
		this.term = term;
		this.caseSensitive = caseSensitive;
		this.fileNames = fileNames;
		this.primary = primary;
	}
 
	@Override
	protected void applyConstraints(BooleanQuery.Builder builder) {
		getOption().applyConstraints(builder, primary);
	}

	@Override
	public void collect(Document document, List<QueryHit> hits) {
		var projectId = document.getField(PROJECT_ID.name()).numericValue().longValue();
		var blobPath = document.getField(BLOB_PATH.name()).stringValue();
		List<Symbol> symbols = null;
		BytesRef bytesRef = document.getBinaryValue(BLOB_SYMBOL_LIST.name());
		if (bytesRef != null) {
			try {
				symbols = SerializationUtils.deserialize(bytesRef.bytes);
			} catch (Exception e) {
				logger.error("Error deserializing symbols", e);
			}
		}
		
		for (var match: getOption().matches(blobPath, symbols, null, null, true, null, getCount())) 
			hits.add(new SymbolHit(projectId, blobPath, match.getSymbol(), match.getPosition()));				
		
		if (hits.size() < getCount()) {
			for (var match: getOption().matches(blobPath, symbols, null, null, false, null, getCount() - hits.size()))
				hits.add(new SymbolHit(projectId, blobPath, match.getSymbol(), match.getPosition()));
		}
	}
	
	private SymbolQueryOption getOption() {
		return new SymbolQueryOption(term, caseSensitive, fileNames);
	}

	public static class Builder {

		private String term;
		
		private boolean caseSensitive;
		
		private String fileNames;
		
		private Boolean primary;
		
		private String projects;
		
		private int count;
		
		public Builder(String term) {
			this.term = term;
		}
		
		public Builder(SymbolQueryOption option) {
			this(option.getTerm());
			caseSensitive(option.isCaseSensitive());
			fileNames(option.getFileNames());
		}

		public Builder caseSensitive(boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
			return this;
		}
		
		public Builder fileNames(String fileNames) {
			this.fileNames = fileNames;
			return this;
		}

		public Builder primary(@Nullable Boolean primary) {
			this.primary = primary;
			return this;
		}

		public Builder projects(@Nullable String projects) {
			this.projects = projects;
			return this;
		}

		public Builder count(int count) {
			this.count = count;
			return this;
		}
		
		public BlobQuery build() {
			Preconditions.checkState(term!=null);
			Preconditions.checkState(count!=0);
			return new SymbolQuery(term, caseSensitive, fileNames, primary, projects, count);
		}
		
	}
}
