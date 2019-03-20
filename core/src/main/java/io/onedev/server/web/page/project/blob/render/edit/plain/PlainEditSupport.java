package io.onedev.server.web.page.project.blob.render.edit.plain;

import java.io.Serializable;

import org.apache.wicket.markup.html.form.FormComponentPanel;

public interface PlainEditSupport extends Serializable {

	FormComponentPanel<byte[]> newEditor(String componentId, byte[] initialContent);
	
}
