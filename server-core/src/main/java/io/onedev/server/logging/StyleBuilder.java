package io.onedev.server.logging;

import java.io.Serializable;

public class StyleBuilder implements Serializable {

	private static final long serialVersionUID = 1L;

	private String color = Style.FOREGROUND_COLOR_DEFAULT;
	
	private String backgroundColor = Style.BACKGROUND_COLOR_DEFAULT;
	
	private boolean bold = false;
	
	void setColor(String color) {
		this.color = color;
	}

	void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	void setBold(boolean bold) {
		this.bold = bold;
	}
	
	void reset() {
		color = Style.FOREGROUND_COLOR_DEFAULT;
		backgroundColor = Style.BACKGROUND_COLOR_DEFAULT;
		bold = false;
	}
	
	Style build() {
		return new Style(color, backgroundColor, bold);
	}

	void swapForegroundAndBackGround() {
		String temp = color;
		color = backgroundColor;
		backgroundColor = temp;
	}
	
}
