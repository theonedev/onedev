package com.gitplex.commons.wicket.page;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitplex.calla.loader.AppLoader;

public class ViewState implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Cursor cursor;
	
	private final Scroll scroll;
	
	public ViewState(Cursor cursor, Scroll scroll) {
		this.cursor = cursor;
		this.scroll = scroll;
	}

	public Cursor getCursor() {
		return cursor;
	}

	public Scroll getScroll() {
		return scroll;
	}
	
	public static class Cursor implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private final int line;
		
		private final int ch;
		
		public Cursor(int line, int ch) {
			this.line = line;
			this.ch = ch;
		}

		public int getLine() {
			return line;
		}

		public int getCh() {
			return ch;
		}
		
	}
	
	public static class Scroll implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private int left;
		
		private int top;
		
		public Scroll(int left, int top) {
			this.left = left;
			this.top = top;
		}

		public int getLeft() {
			return left;
		}

		public void setLeft(int left) {
			this.left = left;
		}

		public int getTop() {
			return top;
		}

		public void setTop(int top) {
			this.top = top;
		}
		
	}

	public String toJSON() {
		try {
			return AppLoader.getInstance(ObjectMapper.class).writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
