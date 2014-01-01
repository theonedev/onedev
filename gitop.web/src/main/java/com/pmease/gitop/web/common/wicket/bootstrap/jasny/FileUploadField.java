package com.pmease.gitop.web.common.wicket.bootstrap.jasny;

import java.util.List;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;

/**
 * An extension of Wicket's default FileUploadField that contributes <a
 * href="http://jasny.github.com/bootstrap/javascript.html#fileupload"
 * >Jasny</a>'s special CSS and JS resources for making &lt;input type="file"
 * /&gt; looking nicer.
 * 
 * <p>
 * As markup use any of the recommended markup snippets at <a
 * href="http://jasny.github.com/bootstrap/javascript.html#fileupload">Jasny
 * File Upload</a>
 * </p>
 */
@SuppressWarnings("serial")
public class FileUploadField extends
		org.apache.wicket.markup.html.form.upload.FileUploadField {
	public FileUploadField(String id) {
		super(id);
	}

	public FileUploadField(String id, IModel<List<FileUpload>> model) {
		super(id, model);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem
				.forReference(JasnyJsReference.INSTANCE));
	}
}