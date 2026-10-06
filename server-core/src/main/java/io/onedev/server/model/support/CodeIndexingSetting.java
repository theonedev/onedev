package io.onedev.server.model.support;

import io.onedev.server.rest.annotation.Api;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Patterns;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;

@Editable
public class CodeIndexingSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	@Api(description = "Files to analyze during code indexing. Leave empty to inherit from parent project, or all files if no parent")
	private String analysisFiles;

	@Api(description = "Leave empty to inherit from parent project, or false if no parent")
	private Boolean requireLoginForAutoIndexing;
	
	@Editable(order=100, name="Files to Be Indexed", placeholder="Inherit from parent", rootPlaceholder ="All files", description="""
		Specify which repository files OneDev should index. During indexing, OneDev analyzes these files for code search, line statistics, and code contribution statistics.
		<b>NOTE: </b> Changing this setting only affects new commits. To apply the change to history commits, please stop the server and delete folder 
		<code>index</code> and <code>info/commit</code> under <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>project's storage directory</a>. 
		The repository will be re-indexed when the server is started""")
	@Patterns(path=true)
	@Nullable
	public String getAnalyzeFiles() {
		return analysisFiles;
	}

	public void setAnalyzeFiles(@Nullable String analyzeFiles) {
		this.analysisFiles = analyzeFiles;
	}

	@Editable(order=200, name="Require Login for Automatic Indexing", placeholder="Inherit from parent", rootPlaceholder="No", description="""
		If enabled, a login session is required to automatically trigger code indexing when browsing repository files or viewing diffs""")
	@Nullable
	public Boolean getRequireLoginForAutoIndexing() {
		return requireLoginForAutoIndexing;
	}

	public void setRequireLoginForAutoIndexing(@Nullable Boolean requireLoginForAutoIndexing) {
		this.requireLoginForAutoIndexing = requireLoginForAutoIndexing;
	}
	
}
