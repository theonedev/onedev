package com.pmease.gitop.web.git.command;

import java.io.Serializable;

import com.google.common.base.Objects;


@SuppressWarnings("serial")
public class AheadBehind implements Serializable {

	private int ahead;
	private int behind;

	public int getAhead() {
		return ahead;
	}

	public void setAhead(int ahead) {
		this.ahead = ahead;
	}

	public int getBehind() {
		return behind;
	}

	public void setBehind(int behind) {
		this.behind = behind;
	}
	
	@Override
	public String toString() {
		return "Ahead: " + ahead + ", behind: " + behind;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(ahead, behind);
	}
}
