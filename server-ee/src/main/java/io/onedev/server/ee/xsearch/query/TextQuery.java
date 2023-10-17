package io.onedev.server.ee.xsearch.query;

import com.google.common.base.Preconditions;
import io.onedev.server.OneDev;
import io.onedev.server.ee.xsearch.match.BlobMatch;
import io.onedev.server.ee.xsearch.match.ContentMatch;
import io.onedev.server.ee.xsearch.match.TextMatch;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.search.code.query.TextQueryOption;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanQuery;
import org.eclipse.jgit.lib.ObjectId;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static io.onedev.server.search.code.FieldConstants.*;

public class TextQuery extends BlobQuery {

	private static final long serialVersionUID = 1L;

	private final String term;
	
	private final boolean regex;

	private final boolean wholeWord;

	private final boolean caseSensitive;
	
	private final String fileNames;
	
	private transient Pattern pattern;
	
	private TextQuery(String term, boolean regex, boolean wholeWord, boolean caseSensitive,
					  @Nullable String fileNames, @Nullable String projects, int count) {
		super(projects, count);
		
		this.term = term;
		this.regex = regex;
		this.wholeWord = wholeWord;
		this.caseSensitive = caseSensitive;
		this.fileNames = fileNames;
	}
	
	@Override
	protected void applyConstraints(BooleanQuery.Builder builder) {
		getOption().applyConstraints(builder);
	}

	@Override
	public BlobMatch matches(Document document) {
		var projectId = document.getField(PROJECT_ID.name()).numericValue().longValue();
		var blobHash = document.getField(BLOB_HASH.name()).stringValue();
		var blobPath = document.getField(BLOB_PATH.name()).stringValue();
		var repository = OneDev.getInstance(ProjectManager.class).getRepository(projectId);
		try (var objectReader = repository.newObjectReader()) {
			try {
				var objectLoader = objectReader.open(ObjectId.fromString(blobHash));
				var textMatches = new ArrayList<ContentMatch>();
				for (var match: getOption().matches(blobPath, objectLoader, Integer.MAX_VALUE)) 
					textMatches.add(new TextMatch(match.getPosition(), match.getLine()));
				if (!textMatches.isEmpty())
					return new BlobMatch(projectId, blobPath, textMatches);
				else 
					return null;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private TextQueryOption getOption() {
		return new TextQueryOption(term, regex, wholeWord, caseSensitive, fileNames);
	}

	public static class Builder {

		private final String term;

		private boolean regex;
		
		private boolean wholeWord;
		
		private boolean caseSensitive;
		
		private String fileNames;
		
		private String projects;
		
		private int count;
		
		public Builder(String term) {
			this.term = term;
		}
		
		public Builder(TextQueryOption option) {
			this(option.getTerm());
			regex(option.isRegex());
			wholeWord(option.isWholeWord());
			caseSensitive(option.isCaseSensitive());
			fileNames(option.getFileNames());
		}
		
		public Builder regex(boolean regex) {
			this.regex = regex;
			return this;
		}

		public Builder wholeWord(boolean wholeWord) {
			this.wholeWord = wholeWord;
			return this;
		}
		
		public Builder caseSensitive(boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
			return this;
		}
		
		public Builder fileNames(String fileNames) {
			this.fileNames = fileNames;
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
			Preconditions.checkState(term != null);
			Preconditions.checkState(count != 0);
			
			return new TextQuery(term, regex, wholeWord, caseSensitive, fileNames, projects, count);
		}
		
	}
	
}
