package io.onedev.server.git;

public class UseOursOnConflict {
	
	private static ThreadLocal<Boolean> holder =  new ThreadLocal<Boolean>() {

		@Override
		protected Boolean initialValue() {
			return false;
		}
	
	};
	
	public static boolean get() {
		return holder.get();
	}
	
	public static void set(boolean useOursOnConflict) {
		holder.set(useOursOnConflict);
	}
	
}

