package io.onedev.server.web.component.markdown;

import java.io.Serializable;

import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.web.upload.FileUpload;

/** Repository uploads shared by Markdown editors. Upload directories are relative to the repository root. */
public interface BlobUploadSupport extends Serializable {
	Project getProject();
	BlobIdent getBlobIdent();
	String getDefaultDirectory();

	/** Commit the upload, update the editor revision, and return the URL to insert. */
	String upload(FileUpload upload, String directory, String commitMessage);

	/** Return a wiki page reference when the uploaded URL points to a wiki page. */
	default @org.jspecify.annotations.Nullable String getPageReference(String url, String text) {
		return null;
	}
}
