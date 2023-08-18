package io.onedev.server.ee.xsearch.query;

import com.google.common.base.Preconditions;
import io.onedev.server.ee.xsearch.match.BlobMatch;
import io.onedev.server.search.code.query.FileQueryOption;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanQuery;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static io.onedev.server.git.GitUtils.getBlobName;
import static io.onedev.server.search.code.FieldConstants.*;

public class FileQuery extends BlobQuery {

	private static final long serialVersionUID = 1L;

	private final String term;
	
	private final boolean caseSensitive;
	
	private FileQuery(String term, boolean caseSensitive, @Nullable String projects, int count) {
		super(projects, count);
		
		this.term = term;
		this.caseSensitive = caseSensitive;
	}

	@Override
	protected void applyConstraints(BooleanQuery.Builder builder) {
		getOption().applyConstraints(builder);
	}

	@Override
	public BlobMatch matches(Document document) {
		var projectId = document.getField(PROJECT_ID.name()).numericValue().longValue();
		var blobPath = document.getField(BLOB_PATH.name()).stringValue();
		String blobName = getBlobName(blobPath);
		var match = getOption().matches(blobName, null);
		if (match != null)
			return new BlobMatch(projectId, blobPath, new ArrayList<>());
		else 
			return null;
	}
	
	private FileQueryOption getOption() {
		return new FileQueryOption(term, caseSensitive);
	}

	public static class Builder {
		
		private boolean caseSensitive;
		
		private final String term;

		protected String projects;

		protected int count;

		public Builder(String term) {
			this.term = term;
		}
		
		public Builder(FileQueryOption option) {
			this(option.getTerm());
			caseSensitive(option.isCaseSensitive());
		}
		
		public Builder caseSensitive(boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
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
			Preconditions.checkState(term !=null);
			Preconditions.checkState(count!=0);
			
			return new FileQuery(term, caseSensitive, projects, count);
		}
		
	}

}
