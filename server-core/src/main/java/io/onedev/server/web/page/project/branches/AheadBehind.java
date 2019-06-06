package io.onedev.server.web.page.project.branches;

import java.io.Serializable;

@SuppressWarnings("serial")
public class AheadBehind implements Serializable {

	private final int ahead;
	
	private final int behind;

	public AheadBehind(int ahead, int behind) {
		this.ahead = ahead;
		this.behind = behind;
	}
	
	public int getAhead() {
		return ahead;
	}

	public int getBehind() {
		return behind;
	}

}
