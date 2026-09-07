package io.onedev.server.web.component.markdown;

import org.apache.wicket.model.IModel;
import org.jspecify.annotations.Nullable;

/**
 * A markdown viewer whose content comes from a repository blob.
 */
public class MarkdownBlobViewer extends MarkdownViewer {

	public MarkdownBlobViewer(String id, IModel<String> model,
			@Nullable ContentVersionSupport contentVersionSupport) {
		super(id, model, contentVersionSupport);
	}

}
