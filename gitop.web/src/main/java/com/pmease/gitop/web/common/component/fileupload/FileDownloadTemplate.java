package com.pmease.gitop.web.common.component.fileupload;

import org.apache.wicket.markup.html.panel.Panel;

/**
 * This panel contributes the JavaScript template that is used by
 * jQuery-File-Upload to show which files were just uploaded
 * and can be downloaded.
 */
@SuppressWarnings("serial")
public class FileDownloadTemplate extends Panel {
	public FileDownloadTemplate(String id) {
        super(id);
    }
}
