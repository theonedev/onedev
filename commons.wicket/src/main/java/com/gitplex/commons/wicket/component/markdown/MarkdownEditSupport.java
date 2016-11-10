package com.gitplex.commons.wicket.component.markdown;

import java.io.Serializable;

public interface MarkdownEditSupport extends Serializable {
	
	void setContent(String content);
	
	long getVersion();
	
}
