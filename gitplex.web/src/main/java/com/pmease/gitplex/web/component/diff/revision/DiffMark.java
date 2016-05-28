package com.pmease.gitplex.web.component.diff.revision;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.component.Mark;

public class DiffMark extends Mark {
	
	private static final long serialVersionUID = 1L;

	private final String commit;
	
	private final String path;
	
	public DiffMark(String commit, String path, int beginLine, int beginChar, 
			int endLine, int endChar) {
		super(beginLine, beginChar, endLine, endChar);
		this.commit = commit;
		this.path = path;
	}
	
	public DiffMark(String commit, String path, Mark mark) {
		super(mark);
		this.commit = commit;
		this.path = path;
	}
	
	public DiffMark(CodeComment comment) {
		super(comment.getMark());
		commit = comment.getCommit();
		path = comment.getPath();
	}

	public String getCommit() {
		return commit;
	}

	public String getPath() {
		return path;
	}

	public DiffMark(String str) {
		super(str.substring(getMarkIndex(str)+1));
		String tempStr = str.substring(0, getMarkIndex(str));
		commit = StringUtils.substringBefore(tempStr, "-");
		path = StringUtils.substringAfter(tempStr, "-");
	}
	
	private static int getMarkIndex(String str) {
		return str.substring(0, str.lastIndexOf('-')).lastIndexOf('-');
	}
	
	@Override
	public String toString() {
		return commit + "-" + path + "-" + super.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof DiffMark))
			return false;
		if (this == other)
			return true;
		DiffMark otherMark = (DiffMark) other;
		return new EqualsBuilder()
				.append(commit, otherMark.commit)
				.append(path, otherMark.path)
				.append(beginLine, otherMark.beginLine)
				.append(beginChar, otherMark.beginChar)
				.append(endLine, otherMark.endLine)
				.append(endChar, otherMark.endChar)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(commit)
				.append(path)
				.append(beginLine)
				.append(beginChar)
				.append(endLine)
				.append(endChar)
				.toHashCode();
	}
	
	public static DiffMark of(@Nullable String markStr) {
		if (markStr != null)
			return new DiffMark(markStr);
		else
			return null;
	}
}
