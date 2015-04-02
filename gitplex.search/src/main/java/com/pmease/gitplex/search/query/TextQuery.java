package com.pmease.gitplex.search.query;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.CharUtils;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Splitter;
import com.pmease.commons.util.Charsets;
import com.pmease.gitplex.search.FieldConstants;
import com.pmease.gitplex.search.IndexConstants;
import com.pmease.gitplex.search.hit.TextHit;
import com.pmease.gitplex.search.hit.QueryHit;

public class TextQuery extends BlobQuery {

	public TextQuery(String searchFor, boolean regex, boolean matchWord, boolean caseSensitive, 
			@Nullable String pathPrefix, @Nullable Collection<String> pathSuffixes, int count) {
		super(FieldConstants.BLOB_TEXT.name(), searchFor, regex, matchWord, caseSensitive, 
				pathPrefix, pathSuffixes, count);
	}

	public TextQuery(String searchFor, boolean regex, boolean matchWord, boolean caseSensitive, int count) {
		this(searchFor, regex, matchWord, caseSensitive, null, null, count);
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
						for (String line: Splitter.on("\n").split(content)) {
							List<TextHit.Range> matches = new ArrayList<>();
							Matcher matcher = pattern.matcher(line);
							while (matcher.find())
								matches.add(new TextHit.Range(matcher.start(), matcher.end()));
							
							if (!matches.isEmpty()) {
								TextHit hit = new TextHit(blobPath, line, lineNo, matches);
								hits.add(hit);
								if (hits.size() >= getCount())
									break;
							}
							lineNo++;
						}
					} else {
						String searchFor = getCasedSearchFor();
						
						int lineNo = 0;
						for (String line: Splitter.on("\n").split(content)) {
							List<TextHit.Range> matches = new ArrayList<>();
							String casedLine = getCasedText(line);
							int start = casedLine.indexOf(searchFor, 0);
							while (start != -1) {
								int end = start + searchFor.length();
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
									
									if (!isWordChar(beforeChar) && !isWordChar(afterChar))
										matches.add(new TextHit.Range(start, end));
								} else {
									matches.add(new TextHit.Range(start, end));
								}
								start = casedLine.indexOf(searchFor, end);
							}
							if (!matches.isEmpty()) {
								TextHit hit = new TextHit(blobPath, line, lineNo, matches);
								hits.add(hit);
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
	
}
