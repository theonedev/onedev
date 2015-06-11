package com.pmease.gitplex.search.query;

import static com.pmease.gitplex.search.IndexConstants.NGRAM_SIZE;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.CharUtils;
import org.apache.lucene.search.Query;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.pmease.commons.lang.TokenPosition;
import com.pmease.commons.util.Charsets;
import com.pmease.gitplex.search.FieldConstants;
import com.pmease.gitplex.search.IndexConstants;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.hit.TextHit;
import com.pmease.gitplex.search.query.regex.RegexLiterals;

public class TextQuery extends BlobQuery {

	private static int MAX_LINE_LEN = 1024;
	
	public TextQuery(String term, boolean regex, boolean matchWord, boolean caseSensitive, 
			@Nullable String pathPrefix, @Nullable Collection<String> pathSuffixes, int count) {
		super(FieldConstants.BLOB_TEXT.name(), term, regex, matchWord, caseSensitive, 
				pathPrefix, pathSuffixes, count);
	}

	public TextQuery(String term, boolean regex, boolean matchWord, boolean caseSensitive, int count) {
		this(term, regex, matchWord, caseSensitive, null, null, count);
	}
	
	@Override
	public void collect(TreeWalk treeWalk, List<QueryHit> hits) {
		ObjectLoader objectLoader;
		try {
			objectLoader = treeWalk.getObjectReader().open(treeWalk.getObjectId(0));
			if (objectLoader.getSize() <= IndexConstants.MAX_INDEXABLE_SIZE) {
				byte[] bytes = objectLoader.getCachedBytes();
				Charset charset = Charsets.detectFrom(bytes);
				if (charset != null) {
					String blobPath = treeWalk.getPathString();
					String content = new String(bytes, charset);

					Pattern pattern = getPattern();
					if (pattern != null) {
						int lineNo = 0;
						for (String line: Splitter.on(CharMatcher.anyOf("\n\r")).split(content)) {
							if (line.length() <= MAX_LINE_LEN) {
								Matcher matcher = pattern.matcher(line);
								while (matcher.find()) {
									TokenPosition.Range range = new TokenPosition.Range(matcher.start(), matcher.end());
									hits.add(new TextHit(blobPath, line, new TokenPosition(lineNo, range)));
									if (hits.size() >= getCount())
										break;
								}
								if (hits.size() >= getCount())
									break;
							}
							lineNo++;
						}
					} else {
						String casedTerm = getCasedTerm();
						
						int lineNo = 0;
						for (String line: Splitter.on("\n").split(content)) {
							String casedLine = getCasedText(line);
							int start = casedLine.indexOf(casedTerm, 0);
							while (start != -1) {
								int end = start + casedTerm.length();
								if (isWordMatch()) {
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
										TokenPosition.Range range = new TokenPosition.Range(start, end);
										hits.add(new TextHit(blobPath, line, new TokenPosition(lineNo, range)));
										if (hits.size() >= getCount())
											break;
									}
								} else {
									TokenPosition.Range range = new TokenPosition.Range(start, end);
									hits.add(new TextHit(blobPath, line, new TokenPosition(lineNo, range)));
									if (hits.size() >= getCount())
										break;
								}
								start = casedLine.indexOf(casedTerm, end);
							}
							if (hits.size() >= getCount())
								break;
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
	protected Query asLuceneQuery(String term) {
		if (isRegex()) 
			return new RegexLiterals(term).asNGramQuery(getFieldName(), NGRAM_SIZE);
		else if (term.length() >= NGRAM_SIZE)  
			return new NGramLuceneQuery(getFieldName(), term, NGRAM_SIZE);
		else 
			throw new TooGeneralQueryException();
	}
	
}
