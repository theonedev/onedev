package io.onedev.server.search.code.query;

import static io.onedev.server.search.code.FieldConstants.BLOB_NAME;
import static io.onedev.server.search.code.FieldConstants.BLOB_TEXT;
import static io.onedev.server.search.code.IndexConstants.NGRAM_SIZE;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.CharUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.WildcardQuery;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.search.code.IndexConstants;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.search.code.hit.TextHit;
import io.onedev.server.search.code.query.regex.RegexLiterals;
import io.onedev.server.util.ContentDetector;

public class TextQuery extends BlobQuery {

	private static int MAX_LINE_LEN = 1024;

	private final String term;
	
	private final boolean regex;
	
	private final boolean caseSensitive;
	
	private final boolean wholeWord;
	
	private final String fileNames;
	
	private transient Pattern pattern;
	
	private TextQuery(String term, boolean regex, boolean caseSensitive, boolean wholeWord, 
			@Nullable String directory, @Nullable String fileNames, int count) {
		super(directory, count);
		
		this.term = term;
		this.regex = regex;
		this.caseSensitive = caseSensitive;
		this.wholeWord = wholeWord;
		this.fileNames = fileNames;
	}

	@Nullable
	private Pattern getPattern() {
		if (regex) {
			if (pattern == null) {
				String expression = term;
				if (wholeWord) {
					if (!expression.startsWith("\\b"))
						expression = "\\b" + expression;
					if (!expression.endsWith("\\b"))
						expression = expression + "\\b";
				}
				if (caseSensitive)
					pattern = Pattern.compile(expression);
				else
					pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
			}
			return pattern;
		} else {
			return null;
		}
	}
	
	@Override
	public void collect(IndexSearcher searcher, TreeWalk treeWalk, List<QueryHit> hits) {
		ObjectLoader objectLoader;
		try {
			objectLoader = treeWalk.getObjectReader().open(treeWalk.getObjectId(0));
			if (objectLoader.getSize() <= IndexConstants.MAX_INDEXABLE_SIZE) {
				String blobPath = treeWalk.getPathString();
				String content = ContentDetector.convertToText(objectLoader.getCachedBytes(), blobPath);
				if (content != null) {
					Pattern pattern = getPattern();
					if (pattern != null) {
						int lineNo = 0;
						for (String line: Splitter.on('\n').split(content)) {
							if (line.length() <= MAX_LINE_LEN) {
								Matcher matcher = pattern.matcher(line);
								while (matcher.find()) {
									LinearRange range = new LinearRange(matcher.start(), matcher.end());
									PlanarRange position = new PlanarRange(lineNo, range.getFrom(), lineNo, range.getTo());
									hits.add(new TextHit(blobPath, line, position));
									if (hits.size() >= getCount())
										break;
								}
								if (hits.size() >= getCount())
									break;
							}
							lineNo++;
						}
					} else {
						String normalizedTerm;
						if (!caseSensitive)
							normalizedTerm = term.toLowerCase();
						else
							normalizedTerm = term;
						
						int lineNo = 0;
						for (String line: Splitter.on('\n').split(content)) {
							if (line.length() <= MAX_LINE_LEN) {
								String normalizedLine;
								if (!caseSensitive)
									normalizedLine = line.toLowerCase();
								else
									normalizedLine = line;
								
								int start = normalizedLine.indexOf(normalizedTerm, 0);
								while (start != -1) {
									int end = start + normalizedTerm.length();
									if (wholeWord) {
										char beforeChar;
										if (start == 0)
											beforeChar = ' ';
										else 
											beforeChar = line.charAt(start-1);
										
										char afterChar;
										if (end == line.length())
											afterChar = ' ';
										else
											afterChar = line.charAt(end);
										
										if (!isWordChar(beforeChar) && !isWordChar(afterChar)) {
											LinearRange range = new LinearRange(start, end);
											PlanarRange position = new PlanarRange(lineNo, range.getFrom(), lineNo, range.getTo());
											hits.add(new TextHit(blobPath, line, position));
											if (hits.size() >= getCount())
												break;
										}
									} else {
										LinearRange range = new LinearRange(start, end);
										PlanarRange position = new PlanarRange(lineNo, range.getFrom(), lineNo, range.getTo());
										hits.add(new TextHit(blobPath, line, position));
										if (hits.size() >= getCount())
											break;
									}
									start = normalizedLine.indexOf(normalizedTerm, end);
								}
								if (hits.size() >= getCount())
									break;
							}
							lineNo++;
						}
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isWordChar(char ch) {
		return CharUtils.isAsciiAlphanumeric(ch) || ch == '_';
	}

	@Override
	protected void applyConstraints(BooleanQuery.Builder builder) {
		if (fileNames != null) {
			BooleanQuery.Builder subQueryBuilder = new BooleanQuery.Builder();
			for (String pattern: Splitter.on(",").omitEmptyStrings().trimResults().split(fileNames.toLowerCase()))
				subQueryBuilder.add(new WildcardQuery(new Term(BLOB_NAME.name(), pattern)), Occur.SHOULD);
			BooleanQuery subQuery = subQueryBuilder.build();
			if (subQuery.clauses().size() != 0)
				builder.add(subQuery, Occur.MUST);
		}

		if (regex) 
			builder.add(new RegexLiterals(term).asNGramQuery(BLOB_TEXT.name(), NGRAM_SIZE), Occur.MUST);
		else if (term.length() >= NGRAM_SIZE)
			builder.add(new NGramLuceneQuery(BLOB_TEXT.name(), term, NGRAM_SIZE), Occur.MUST);
		else 
			throw new TooGeneralQueryException();
	}
	
	public static class Builder {

		private String term;
		
		private int count;

		private boolean regex;
		
		private boolean wholeWord;
		
		private boolean caseSensitive;
		
		private String directory;
		
		private String fileNames;
		
		public Builder term(String term) {
			this.term = term;
			return this;
		}
		
		public Builder count(int count) {
			this.count = count;
			return this;
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
		
		public Builder directory(String directory) {
			this.directory = directory;
			return this;
		}
		
		public Builder fileNames(String fileNames) {
			this.fileNames = fileNames;
			return this;
		}
		
		public TextQuery build() {
			Preconditions.checkArgument(term!=null, "Query term should be specified");
			Preconditions.checkArgument(count!=0, "Query count should be specified");
			return new TextQuery(term, regex, caseSensitive, wholeWord, directory, fileNames, count);
		}
		
	}
	
}
