package io.onedev.server.web.component.markdown;

import java.io.Serializable;
import org.apache.wicket.ajax.AjaxRequestTarget;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;

/** Customizes repository selection without requiring a full file editor context. */
public interface BlobSelectionSupport extends Serializable {
	Project getProject();
	BlobIdent getBlobIdent();
	default boolean accepts(BlobIdent blob, boolean image) {
		return true;
	}
	void onSelect(AjaxRequestTarget target, String path, boolean image, String text);
}
