package com.gitplex.server.web.component.diff.revision;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.gitplex.server.model.support.MarkPos;

public interface MarkSupport extends Serializable {
	
	@Nullable MarkPos getMark();
	
	String getMarkUrl(MarkPos mark);

}
