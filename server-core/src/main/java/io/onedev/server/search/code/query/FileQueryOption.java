package io.onedev.server.search.code.query;

import io.onedev.server.search.code.insidecommit.query.BlobQuery;
import io.onedev.server.search.code.insidecommit.query.FileQuery;
import io.onedev.server.web.component.codequeryoption.FileQueryOptionPanel;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.Model;

public class FileQueryOption implements QueryOption {

	private static final long serialVersionUID = 1L;
	
	private String term;

	private boolean caseSensitive;

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	@Override
	public BlobQuery.Builder newInsideCommitQueryBuilder() {
		return new FileQuery.Builder()
				.fileNames(term.toLowerCase())
				.caseSensitive(caseSensitive);
	}

	@Override
	public FormComponentPanel<? extends QueryOption> newOptionEditor(String componentId) {
		return new FileQueryOptionPanel(componentId, Model.of(this));
	}

}
