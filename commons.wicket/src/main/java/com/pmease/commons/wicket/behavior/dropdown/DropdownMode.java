package com.pmease.commons.wicket.behavior.dropdown;

public interface DropdownMode {

	public static class Hover implements DropdownMode {
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
		
	}
	
}
