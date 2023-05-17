package io.onedev.server.search.code.query;

import com.google.common.base.Preconditions;
import io.onedev.commons.utils.LinearRange;
import io.onedev.server.util.match.WildcardUtils;
import io.onedev.server.web.component.codequeryoption.FileQueryOptionEditor;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.Model;

import javax.annotation.Nullable;
import java.util.Optional;

import static io.onedev.server.search.code.FieldConstants.BLOB_NAME;
import static io.onedev.server.util.match.WildcardUtils.rangeOfMatch;
import static java.util.Optional.ofNullable;

public class FileQueryOption implements QueryOption {

	private static final long serialVersionUID = 1L;
	
	private final String term;

	private final boolean caseSensitive;

	public FileQueryOption(@Nullable String term, boolean caseSensitive) {
		this.term = term;
		this.caseSensitive = caseSensitive;
	}
	
	public FileQueryOption() {
		this(null, false);
	}
	
	@Nullable
	public String getTerm() {
		return term;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void applyConstraints(BooleanQuery.Builder builder) {
		Preconditions.checkNotNull(term);
		
		boolean tooGeneral = true;
		for (char ch: term.toCharArray()) {
			if (ch != '?' && ch != '*' && ch != ',' && ch != '.') {
				tooGeneral = false;
				break;
			}
		}
		if (tooGeneral)
			throw new TooGeneralQueryException();

		builder.add(new WildcardQuery(new Term(BLOB_NAME.name(), term.toLowerCase())), BooleanClause.Occur.MUST);
	}

	@Nullable
	public Optional<LinearRange> matches(String blobName, @Nullable String excludeFileName) {
		Preconditions.checkNotNull(term);
		
		var normalizedTerm = term;
		var normalizedBlobName = blobName;
		var normalizedExcludeFileName = excludeFileName;
		if (!caseSensitive) {
			normalizedBlobName = normalizedBlobName.toLowerCase();
			normalizedTerm = normalizedTerm.toLowerCase();
			if (normalizedExcludeFileName != null)
				normalizedExcludeFileName = normalizedExcludeFileName.toLowerCase();
		}
		if (WildcardUtils.matchString(normalizedTerm, normalizedBlobName)
				&& (normalizedExcludeFileName == null || !normalizedExcludeFileName.equals(normalizedBlobName))) {
			return ofNullable(rangeOfMatch(normalizedTerm, normalizedBlobName));
		} else {
			return null;
		}
	}

	@Override
	public FormComponentPanel<? extends QueryOption> newOptionEditor(String componentId) {
		return new FileQueryOptionEditor(componentId, Model.of(this));
	}
	
}
