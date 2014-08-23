package com.pmease.gitplex.core.model;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;

@SuppressWarnings("serial")
@Embeddable
public class CommentPosition implements Serializable {

	private String filePath;
	
	private Integer lineNo;

	public CommentPosition() {
		
	}
	
	public CommentPosition(String filePath, Integer lineNo) {
		this.filePath = filePath;
		this.lineNo = lineNo;
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Nullable
	public Integer getLineNo() {
		return lineNo;
	}

	public void setLineNo(Integer lineNo) {
		this.lineNo = lineNo;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof CommentPosition)) 
			return false;
		if (this == other)
			return true;
		CommentPosition otherPosition = (CommentPosition) other;
		return new EqualsBuilder()
			.append(filePath, otherPosition.getFilePath())
			.append(lineNo, otherPosition.getLineNo())
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(filePath)
			.append(lineNo)
			.toHashCode();
	}		
	
	@Override
	public String toString() {
		return Objects.toStringHelper(CommentPosition.class)
				.add("filePath", filePath)
				.add("lineNo", lineNo)
				.toString(); 
	}
}