package io.onedev.server.web.avatar;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The Gravatar class is a utility for generating Gravatar URLs.
 * 
 * @author Finn Kuusisto
 */
public class Gravatar {

	public static final String PRE = "https://secure.gravatar.com/avatar/";

	// default images
	public static final int DFLT_DEFAULT = 2;
	public static final int DFLT_FOUR_O_FOUR = 0;
	public static final int DFLT_MYSTERY_MAN = 1;
	public static final int DFLT_IDENTICON = 2;
	public static final int DFLT_MONSTERID = 3;
	public static final int DFLT_WAVATAR = 4;
	public static final int DFLT_RETRO = 5;
	public static final int DFLT_BLANK = 6;
	private static final String[] dfltImages = { "404", "mm", "identicon",
			"monsterid", "wavatar", "retro", "blank" };

	// ratings
	public static final int RTNG_NONE = -1;
	public static final int RTNG_G = 0;
	public static final int RTNG_PG = 1;
	public static final int RTNG_R = 2;
	public static final int RTNG_X = 3;
	private static final String[] ratings = { "g", "pg", "r", "x" };

	private String email;
	private String hash;
	private int size;
	private int dflt = DFLT_DEFAULT;
	private boolean forceDefault;
	private String customDefault;
	private int rating;

	/**
	 * Create a new Gravatar object for a particular email address.
	 * 
	 * @param email
	 *            the email for this Gravatar
	 */
	public Gravatar(String email) {
		this.email = email;
		byte[] buf = email.trim().toLowerCase().getBytes();
		try {
			this.hash = Gravatar.toHexString(MessageDigest.getInstance("MD5")
					.digest(buf));
			this.hash = this.hash.toLowerCase();
		} catch (NoSuchAlgorithmException e) {
			// all java implementations must implement MD5
		}
	}

	/**
	 * Get the email associated with this Gravatar.
	 * 
	 * @return the email associated with this Gravatar
	 */
	public String getEmail() {
		return this.email;
	}

	/**
	 * Set the image size of this Gravatar in pixels.
	 * 
	 * @param size
	 *            size of image in pixels [1-512], 0 for default size
	 */
	public void setSize(int size) {
		if (size >= 0 && size < 512) {
			this.size = size;
		}
	}

	/**
	 * Get the size set for this Gravatar.
	 * 
	 * @return the size set for this Gravatar [1-512], 0 for default size
	 */
	public int getSize() {
		return this.size;
	}

	/**
	 * Set the image returned if there is no Gravatar for the email.
	 * 
	 * @param dflt
	 *            must be one of the DFLT_XXX constants
	 */
	public void setDefault(int dflt) {
		if (dflt >= DFLT_DEFAULT && dflt <= DFLT_BLANK) {
			// blank out the custom if one was set
			this.customDefault = null;
			this.dflt = dflt;
		}
	}

	/**
	 * Get the default image setting of this Gravatar.
	 * 
	 * @return the default image setting of this Gravatar
	 */
	public int getDefault() {
		return this.dflt;
	}

	/**
	 * Sets whether the default Gravatar should always be returned.
	 * 
	 * @param forceDefault
	 *            true if the default Gravatar should always be returned
	 */
	public void setForceDefault(boolean forceDefault) {
		this.forceDefault = forceDefault;
	}

	/**
	 * Get whether this Gravatar is set to always return the default image.
	 * 
	 * @return true if this Gravatar is set to always return the default image
	 */
	public boolean getForceDefault() {
		return this.forceDefault;
	}

	/**
	 * Set the URL of a custom default image to use.
	 * 
	 * @param url
	 *            the URL of the custom default image to use
	 */
	public void setCustomDefault(String url) {
		try {
			if (url != null) {
				// UTF-8 should always be supported
				this.customDefault = URLEncoder.encode(url, "UTF-8");
				this.dflt = DFLT_DEFAULT;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the URL of the custom default image set for this Gravatar.
	 * 
	 * @return the URL of the custom default image set, null if not set
	 */
	public String getCustomDefault() {
		try {
			// UTF-8 should always be supported
			return this.customDefault == null ? null : URLDecoder.decode(
					this.customDefault, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Set the maximum allowed rating of the image to return.
	 * 
	 * @param rating
	 *            must be one of the RTNG_XXX constants
	 */
	public void setRating(int rating) {
		if (rating >= RTNG_NONE && rating <= RTNG_X) {
			this.rating = rating;
		}
	}

	/**
	 * Get the maximum allowed rating of image set for this Gravatar.
	 * 
	 * @return the rating set for this Gravatar
	 */
	public int getRating() {
		return this.rating;
	}

	/**
	 * Get the URL for this Gravatar, HTTPS if specified.
	 * 
	 * @param secure
	 *            true if HTTPS is desired
	 * @return URL for this Gravatar
	 */
	public String getURL() {
		StringBuilder str = new StringBuilder();
		str.append(PRE);
		str.append(this.hash);
		str.append(this.getOptions());
		return str.toString();
	}

	/**
	 * Build the options string to append to the URL.
	 * 
	 * @return the options portion of the URL
	 */
	private String getOptions() {
		StringBuilder opts = new StringBuilder();
		opts.append("?");
		// size
		if (this.size != 0) {
			opts.append("s=");
			opts.append(this.size);
		}
		// default
		if (this.dflt >= 0) {
			// check if we just added an option
			opts.append(opts.length() > 1 ? "&" : "");
			opts.append("d=");
			opts.append(dfltImages[this.dflt]);
		}
		// custom default
		if (this.customDefault != null) {
			// check if we just added an option
			opts.append(opts.length() > 1 ? "&" : "");
			opts.append("d=");
			opts.append(this.customDefault);
		}
		// force default
		if (this.forceDefault) {
			// check if we just added an option
			opts.append(opts.length() > 1 ? "&" : "");
			opts.append("f=y");
		}
		// rating
		if (this.rating != RTNG_NONE) {
			// check if we just added an option
			opts.append(opts.length() > 1 ? "&" : "");
			opts.append("r=");
			opts.append(ratings[this.rating]);
		}
		return (opts.length() > 1) ? opts.toString() : "";
	}

	/**
	 * Get the optionally HTTPS Gravatar URL for a particular email.
	 * 
	 * @param email
	 *            the email for this Gravatar
	 * @return the URL of the Gravatar
	 */
	public static String getURL(String email) {
		return Gravatar.getURL(email, 0, DFLT_DEFAULT, false, RTNG_NONE);
	}

	/**
	 * Get the Gravatar URL for an email and with a specified size
	 * 
	 * @param email
	 *            the email for this Gravatar
	 * @param size
	 *            the desired size for this Gravatar [1-512]
	 * @return the URL of the Gravatar
	 */
	public static String getURL(String email, int size) {
		return Gravatar.getURL(email, size, DFLT_DEFAULT);
	}

	/**
	 * Get the Gravatar for an email with a specified size and default image.
	 * 
	 * @param email
	 *            the email for this Gravatar
	 * @param size
	 *            the desired size for this Gravatar [1-512]
	 * @param dflt
	 *            must be one of the DFLT_XXX constants
	 * @return the URL of the Gravatar
	 */
	public static String getURL(String email, int size, int dflt) {
		return Gravatar.getURL(email, size, dflt, RTNG_NONE);
	}

	/**
	 * Get the Gravatar for an email with a specified size, default image, and
	 * maximum rating.
	 * 
	 * @param email
	 *            the email for this Gravatar
	 * @param size
	 *            the desired size for this Gravatar [1-512]
	 * @param dflt
	 *            must be one of the DFLT_XXX constants
	 * @param rating
	 *            must be one of the RTNG_XXX constants
	 * @return the URL of the Gravatar
	 */
	public static String getURL(String email, int size, int dflt, int rating) {
		return Gravatar.getURL(email, size, dflt, false, rating);
	}

	/**
	 * Get the Gravatar for an email with a specified size, custom default,
	 * maximum rating, and specified force default option, and HTTPS preference.
	 * 
	 * @param email
	 *            the email for this Gravatar
	 * @param size
	 *            the desired size for this Gravatar [1-512]
	 * @param customDefault
	 *            URL of custom default image
	 * @param forceDefault
	 *            true if the default should always be returned
	 * @param rating
	 *            must be one of the RTNG_XXX constants
	 * @param secure
	 *            true if HTTPS desired
	 * @return the URL of the Gravatar
	 */
	public static String getURL(String email, int size, int dflt,
			boolean forceDefault, int rating) {
		Gravatar g = new Gravatar(email);
		g.setSize(size);
		g.setDefault(dflt);
		g.setForceDefault(forceDefault);
		g.setRating(rating);
		return g.getURL();
	}

	/**
	 * Get the Gravatar for an email with a specified size, custom default,
	 * maximum rating, specified force default option, and HTTPS preference.
	 * 
	 * @param email
	 *            the email for this Gravatar
	 * @param size
	 *            the desired size for this Gravatar [1-512]
	 * @param customDefault
	 *            URL of custom default image
	 * @param forceDefault
	 *            true if the default should always be returned
	 * @param rating
	 *            must be one of the RTNG_XXX constants
	 * @param secure
	 *            true if HTTPS desired
	 * @return the URL of the Gravatar
	 */
	public static String getURL(String email, int size, String customDefault,
			boolean forceDefault, int rating) {
		Gravatar g = new Gravatar(email);
		g.setSize(size);
		g.setCustomDefault(customDefault);
		g.setForceDefault(forceDefault);
		g.setRating(rating);
		return g.getURL();
	}

	/**
	 * Convert an array of bytes to a Hex String
	 * 
	 * @param buf
	 *            the bytes to convert
	 * @return the Hex String of the bytes
	 */
	private static String toHexString(byte[] buf) {
		BigInteger bi = new BigInteger(1, buf);
		return String.format("%0" + (buf.length << 1) + "X", bi);
	}
}
