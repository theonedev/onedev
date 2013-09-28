package com.pmease.gitop.web.common.component.fileupload;

import org.apache.wicket.markup.html.panel.Panel;

/**
 * This bar contributes the toolbar with "Add files", "Start upload",
 * "Cancel upload" and "Delete all" buttons.
 */
public class FileUploadBar extends Panel {

    private static final long serialVersionUID = 1L;

	public FileUploadBar(String id) {
        super(id);

        add(new FileUploadResourceBehavior());
    }
}