package io.onedev.server.model.support;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable
public class CodeAnalysisSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private String analysisFiles;
	
	@Editable(order=100, name="Files to Be Analyzed", placeholder="Inherit from parent", rootProjectPlaceholder="All files", description="OneDev analyzes repository files for code search, "
			+ "line statistics, and code contribution statistics. This setting tells which files should be analyzed, and expects space-separated "
			+ "<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path patterns</a>. A pattern can be excluded by prefixing with '-', for "
			+ "instance <code>-**/vendors/**</code> will exclude all files with vendors in path. <b>NOTE: </b> Changing this setting only affects new "
			+ "commits. To apply the change to history commits, please stop the server and delete folder <code>index</code> and "
			+ "<code>info/commit</code> under <a href='https://docs.onedev.io/concepts#project-storage' target='_blank'>project's storage directory</a>. The "
			+ "repository will be re-analyzed when server is started"
		)
	@Patterns
	public String getAnalyzeFiles() {
		return analysisFiles;
	}

	public void setAnalyzeFiles(String analyzeFiles) {
		this.analysisFiles = analyzeFiles;
	}
	
}
