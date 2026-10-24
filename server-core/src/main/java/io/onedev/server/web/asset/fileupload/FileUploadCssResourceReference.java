package io.onedev.server.web.asset.fileupload;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class FileUploadCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public FileUploadCssResourceReference() {
		super(FileUploadCssResourceReference.class, "file-upload.css");
	}

}
