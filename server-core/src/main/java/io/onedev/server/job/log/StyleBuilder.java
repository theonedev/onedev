package io.onedev.server.job.log;

import java.io.Serializable;

import io.onedev.server.buildspec.job.log.Style;

public class StyleBuilder implements Serializable {

	private static final long serialVersionUID = 1L;

	private String color = Style.FOREGROUND_COLOR_DEFAULT;
	
	private String backgroundColor = Style.BACKGROUND_COLOR_DEFAULT;
	
	private boolean bold = false;
	
	public void setColor(String color) {
		this.color = color;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public void setBold(boolean bold) {
		this.bold = bold;
	}
	
	public void reset() {
		color = Style.FOREGROUND_COLOR_DEFAULT;
		backgroundColor = Style.BACKGROUND_COLOR_DEFAULT;
		bold = false;
	}
	
	public Style build() {
		return new Style(color, backgroundColor, bold);
	}

	public void swapForegroundAndBackGround() {
		String temp = color;
		color = backgroundColor;
		backgroundColor = temp;
	}
	
}
