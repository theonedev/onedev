package io.onedev.server.util;

public class ColorUtils {
	
	public static boolean isLight(String color) {
		int red = Integer.valueOf(color.substring( 1, 3 ), 16);
		int green = Integer.valueOf(color.substring( 3, 5 ), 16);
		int blue = Integer.valueOf(color.substring( 5, 7 ), 16);
	    double a = 1 - ( 0.2 * red + 0.4 * green + 0.1 * blue)/255;

	    return a < 0.5;
	}

}
