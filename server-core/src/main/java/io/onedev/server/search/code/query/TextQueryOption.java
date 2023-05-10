package io.onedev.server.search.code.query;

import io.onedev.server.search.code.insidecommit.query.BlobQuery;
import io.onedev.server.search.code.insidecommit.query.TextQuery;
import io.onedev.server.web.component.codequeryoption.TextQueryOptionPanel;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.Model;

public class TextQueryOption implements QueryOption {
	
	private static final long serialVersionUID = 1L;

	private String term;

	private boolean regex;

	private boolean caseSensitive;

	private boolean wholeWord;

	private String fileNames;

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public boolean isRegex() {
		return regex;
	}

	public void setRegex(boolean regex) {
		this.regex = regex;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public boolean isWholeWord() {
		return wholeWord;
	}

	public void setWholeWord(boolean wholeWord) {
		this.wholeWord = wholeWord;
	}

	public String getFileNames() {
		return fileNames;
	}

	public void setFileNames(String fileNames) {
		this.fileNames = fileNames;
	}
	
	@Override
	public BlobQuery.Builder newInsideCommitQueryBuilder() {
		return new TextQuery.Builder()
				.term(term)
				.regex(regex)
				.caseSensitive(caseSensitive)
				.wholeWord(wholeWord)
				.fileNames(fileNames);
	}

	@Override
	public FormComponentPanel<? extends QueryOption> newOptionEditor(String componentId) {
		return new TextQueryOptionPanel(componentId, Model.of(this));
	}

}
