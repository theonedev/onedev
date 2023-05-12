package io.onedev.server.search.code.query;

import io.onedev.server.web.component.codequeryoption.SymbolQueryOptionPanel;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.Model;

public class SymbolQueryOption implements QueryOption {

	private static final long serialVersionUID = 1L;
	
	private String term;

	private String fileNames;

	private boolean caseSensitive;

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getFileNames() {
		return fileNames;
	}

	public void setFileNames(String fileNames) {
		this.fileNames = fileNames;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	@Override
	public BlobQuery.Builder newInsideCommitQueryBuilder() {
		return new SymbolQuery.Builder()
				.term(term)
				.primary(true)
				.caseSensitive(caseSensitive)
				.fileNames(fileNames);
	}

	@Override
	public FormComponentPanel<? extends QueryOption> newOptionEditor(String componentId) {
		return new SymbolQueryOptionPanel(componentId, Model.of(this));
	}

}
