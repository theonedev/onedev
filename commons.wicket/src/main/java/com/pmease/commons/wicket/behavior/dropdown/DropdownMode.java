package com.pmease.commons.wicket.behavior.dropdown;

import java.io.Serializable;

public interface DropdownMode extends Serializable {

	public static class Hover implements DropdownMode {
		
		private static final long serialVersionUID = 1L;
		
		private final int delay;
		
		public Hover(int delay) {
			this.delay = delay;
		}
		
		public Hover() {
			this(350);
		}

		public int getDelay() {
			return delay;
		}
		
	}
	
	public static class Click implements DropdownMode {

		private static final long serialVersionUID = 1L;
		
	}
	
}
