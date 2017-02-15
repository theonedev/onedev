package com.gitplex.server.web.component.markdown;

import java.io.Serializable;

public interface MarkdownEditSupport extends Serializable {
	
	void setContent(String content);
	
	long getVersion();
	
}
