package io.onedev.server.web.asset.diffmatchpatch;

import java.io.Serial;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class DiffMatchPatchResourceReference extends JavaScriptResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public DiffMatchPatchResourceReference() {
		super(DiffMatchPatchResourceReference.class, "diff_match_patch.js");
	}

}
