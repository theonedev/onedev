package com.pmease.gitplex.web.component.diff.revision;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.component.Mark;

public class DiffMark extends Mark {
	
	private static final long serialVersionUID = 1L;

	private final String path;
	
	private final boolean leftSide;

	public DiffMark(String path, boolean leftSide, int beginLine, int beginChar, 
			int endLine, int endChar) {
		super(beginLine, beginChar, endLine, endChar);
		this.path = path;
		this.leftSide = leftSide;
	}
	
	public DiffMark(String path, boolean leftSide, Mark mark) {
		super(mark);
		this.path = path;
		this.leftSide = leftSide;
	}

	public String getPath() {
		return path;
	}

	public boolean isLeftSide() {
		return leftSide;
	}

	public DiffMark(String str) {
		super(str.substring(getMarkIndex(str)+1));
		String tempStr = str.substring(0, getMarkIndex(str));
		path = StringUtils.substringBeforeLast(tempStr, "-");
		leftSide = Boolean.valueOf(StringUtils.substringAfterLast(tempStr, "-"));
	}
	
	private static int getMarkIndex(String str) {
		return str.substring(0, str.lastIndexOf('-')).lastIndexOf('-');
	}
	
	@Override
	public String toString() {
		return path + "-" + leftSide + "-" + super.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof DiffMark))
			return false;
		if (this == other)
			return true;
		DiffMark otherMark = (DiffMark) other;
		return new EqualsBuilder()
				.append(path, otherMark.path)
				.append(leftSide, otherMark.leftSide)
				.append(beginLine, otherMark.beginLine)
				.append(beginChar, otherMark.beginChar)
				.append(endLine, otherMark.endLine)
				.append(endChar, otherMark.endChar)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(path)
				.append(leftSide)
				.append(beginLine)
				.append(beginChar)
				.append(endLine)
				.append(endChar)
				.toHashCode();
	}
	
	@Override
	public String toJson() {
		try {
			return GitPlex.getInstance(ObjectMapper.class).writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		} 
	}
	
}
