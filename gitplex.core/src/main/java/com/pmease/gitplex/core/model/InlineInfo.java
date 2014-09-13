package com.pmease.gitplex.core.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Embeddable;
import javax.persistence.Lob;

import com.pmease.commons.util.diff.DiffLine;

@SuppressWarnings("serial")
@Embeddable
public class InlineInfo implements Serializable {

	private String commit;
	
	private String file;
	
	private Integer line;

	@Lob
	private List<DiffLine> context;
	
	private String compareCommit;

	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public List<DiffLine> getContext() {
		return context;
	}

	public void setContext(List<DiffLine> context) {
		this.context = context;
	}

	public String getCompareCommit() {
		return compareCommit;
	}

	public void setCompareCommit(String compareCommit) {
		this.compareCommit = compareCommit;
	}

}