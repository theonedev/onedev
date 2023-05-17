package io.onedev.server.search.code.query;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.search.code.IndexConstants;
import io.onedev.server.search.code.query.regex.RegexLiterals;
import io.onedev.server.util.ContentDetector;
import io.onedev.server.web.component.codequeryoption.TextQueryOptionEditor;
import org.apache.commons.lang3.CharUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.ObjectLoader;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.onedev.server.search.code.FieldConstants.BLOB_NAME;
import static io.onedev.server.search.code.FieldConstants.BLOB_TEXT;
import static io.onedev.server.search.code.IndexConstants.MAX_INDEXABLE_LINE_LEN;
import static io.onedev.server.search.code.IndexConstants.NGRAM_SIZE;

public class TextQueryOption implements QueryOption {
	
	private static final long serialVersionUID = 1L;
	
	private final String term;

	private final boolean regex;

	private final boolean wholeWord;
	
	private final boolean caseSensitive;
	
	private final String fileNames;

	private transient Pattern pattern;
	
	public TextQueryOption(@Nullable String term, boolean regex, boolean wholeWord, 
						   boolean caseSensitive, @Nullable String fileNames) {
		this.term = term;
		this.regex = regex;
		this.wholeWord = wholeWord;
		this.caseSensitive = caseSensitive;
		this.fileNames = fileNames;
	}
	
	public TextQueryOption() {
		this(null, false, false, false, null);
	}

	@Nullable
	public String getTerm() {
		return term;
	}

	public boolean isRegex() {
		return regex;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public boolean isWholeWord() {
		return wholeWord;
	}

	public String getFileNames() {
		return fileNames;
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

	private boolean isWordChar(char ch) {
		return CharUtils.isAsciiAlphanumeric(ch) || ch == '_';
	}
	
	public List<Match> matches(String blobPath, ObjectLoader objectLoader, int count) {
		Preconditions.checkNotNull(term);
		
		var matches = new ArrayList<Match>();
		if (objectLoader.getSize() <= IndexConstants.MAX_INDEXABLE_BLOB_SIZE) {
			String content = ContentDetector.convertToText(objectLoader.getCachedBytes(), blobPath);
			if (content != null) {
				Pattern pattern = getPattern();
				if (pattern != null) {
					int lineNo = 0;
					for (String line: Splitter.on('\n').split(content)) {
						if (line.length() <= MAX_INDEXABLE_LINE_LEN) {
							Matcher matcher = pattern.matcher(line);
							while (matcher.find()) {
								LinearRange range = new LinearRange(matcher.start(), matcher.end());
								PlanarRange position = new PlanarRange(lineNo, range.getFrom(), lineNo, range.getTo());
								matches.add(new Match(line, position));
								if (matches.size() >= count)
									break;
							}
							if (matches.size() >= count)
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
						if (line.length() <= MAX_INDEXABLE_LINE_LEN) {
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
										matches.add(new Match(line, position));
										if (matches.size() >= count)
											break;
									}
								} else {
									LinearRange range = new LinearRange(start, end);
									PlanarRange position = new PlanarRange(lineNo, range.getFrom(), lineNo, range.getTo());
									matches.add(new Match(line, position));
									if (matches.size() >= count)
										break;
								}
								start = normalizedLine.indexOf(normalizedTerm, end);
							}
							if (matches.size() >= count)
								break;
						}
						lineNo++;
					}
				}
			}
		}
		return matches;
	}

	public void applyConstraints(BooleanQuery.Builder builder) {
		Preconditions.checkNotNull(term);
		
		if (fileNames != null) {
			BooleanQuery.Builder subQueryBuilder = new BooleanQuery.Builder();
			for (String pattern: Splitter.on(",").omitEmptyStrings().trimResults().split(fileNames.toLowerCase()))
				subQueryBuilder.add(new WildcardQuery(new Term(BLOB_NAME.name(), pattern)), BooleanClause.Occur.SHOULD);
			BooleanQuery subQuery = subQueryBuilder.build();
			if (subQuery.clauses().size() != 0)
				builder.add(subQuery, BooleanClause.Occur.MUST);
		}

		if (regex)
			builder.add(new RegexLiterals(term).asNGramQuery(BLOB_TEXT.name(), NGRAM_SIZE), BooleanClause.Occur.MUST);
		else if (term.length() >= NGRAM_SIZE)
			builder.add(new NGramLuceneQuery(BLOB_TEXT.name(), term, NGRAM_SIZE), BooleanClause.Occur.MUST);
		else
			throw new TooGeneralQueryException();
	}

	@Override
	public FormComponentPanel<? extends QueryOption> newOptionEditor(String componentId) {
		return new TextQueryOptionEditor(componentId, Model.of(this));
	}

	public static class Match implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private final String line;
		
		private final PlanarRange position;
		
		public Match(String line, PlanarRange position) {
			this.line = line;
			this.position = position;
		}

		public String getLine() {
			return line;
		}

		public PlanarRange getPosition() {
			return position;
		}

	}
	
}
