package io.onedev.server.search.code.query;

import static io.onedev.server.search.code.FieldConstants.BLOB_NAME;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.WildcardQuery;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.LinearRange;
import io.onedev.server.search.code.hit.FileHit;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.util.match.WildcardUtils;

public class FileQuery extends BlobQuery {

	private final String fileNames;
	
	private final String excludeFileName;
	
	private final boolean caseSensitive;
	
	private FileQuery(String fileNames, @Nullable String excludeFileName, boolean caseSensitive,  
			@Nullable String directory, int count) {
		super(directory, count);
		
		this.fileNames = fileNames;
		this.excludeFileName = excludeFileName;
		this.caseSensitive = caseSensitive;
	}

	@Override
	public void collect(IndexSearcher searcher, TreeWalk treeWalk, List<QueryHit> hits) {
		String blobPath = treeWalk.getPathString();
		String blobName = blobPath.substring(blobPath.lastIndexOf('/')+1);
		if (caseSensitive) {
			if (WildcardUtils.matchString(fileNames, blobName) 
					&& (excludeFileName == null || !excludeFileName.equals(blobName))) {
				LinearRange match = WildcardUtils.rangeOfMatch(fileNames, blobName);
				hits.add(new FileHit(blobPath, match));
			}
		} else {
			if (WildcardUtils.matchString(fileNames, blobName.toLowerCase()) 
					&& (excludeFileName == null || !excludeFileName.equalsIgnoreCase(blobName))) {
				LinearRange match = WildcardUtils.rangeOfMatch(fileNames, blobName.toLowerCase());
				hits.add(new FileHit(blobPath, match));
			}
		}
	}

	@Override
	protected void applyConstraints(BooleanQuery.Builder builder) {
		boolean tooGeneral = true;
		for (char ch: fileNames.toCharArray()) {
			if (ch != '?' && ch != '*' && ch != ',' && ch != '.') {
				tooGeneral = false;
				break;
			}
		}
		if (tooGeneral)
			throw new TooGeneralQueryException();
		
		builder.add(new WildcardQuery(new Term(BLOB_NAME.name(), fileNames.toLowerCase())), Occur.MUST);
	}

	public static class Builder {

		private int count;
		
		private String directory;

		private String excludeFileName;
		
		private boolean caseSensitive;
		
		private String fileNames;
		
		public Builder count(int count) {
			this.count = count;
			return this;
		}
		
		public Builder directory(String directory) {
			this.directory = directory;
			return this;
		}
		
		public Builder excludeFileName(String excludeFileName) {
			this.excludeFileName = excludeFileName;
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
		
		public FileQuery build() {
			Preconditions.checkArgument(fileNames!=null, "File names should be specified");
			Preconditions.checkArgument(count!=0, "Query count should be specified");
			
			return new FileQuery(fileNames, excludeFileName, caseSensitive, 
					directory, count);
		}
		
	}

}
