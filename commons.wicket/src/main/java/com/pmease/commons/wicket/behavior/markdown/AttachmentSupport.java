package com.pmease.commons.wicket.behavior.markdown;

import java.io.File;
import java.io.Serializable;

public interface AttachmentSupport extends Serializable {
	File getStoreDir();
	
	String getAttachmentUrl(String attachment);
}
