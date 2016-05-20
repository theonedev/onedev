package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.compare;

import java.io.Serializable;

import com.pmease.commons.lang.diff.WhitespaceOption;
import com.pmease.gitplex.web.component.diff.revision.DiffMark;

public class HistoryState implements Serializable {

	private static final long serialVersionUID = 1L;

	public String oldRev;
	
	public String newRev;
	
	public WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;
	
	public String pathFilter;
	
	public Long commentId;
	
	public DiffMark mark;
	
}
