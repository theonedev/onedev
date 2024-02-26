package io.onedev.server.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Credit to https://raccoon.onyxbits.de/blog/single-class-java-semantic-versioning-parser-implementation/
 */
public final class SemanticVersion implements Comparable<SemanticVersion> {

	/**
	 * Major version number
	 */
	public int major;

	/**
	 * Minor version number
	 */
	public int minor;

	/**
	 * Patch level
	 */
	public int patch;
	
	public int revision;

	/**
	 * Pre-release tags (potentially empty, but never null). This is private to
	 * ensure read only access.
	 */
	private String[] preRelease;

	/**
	 * Build meta data tags (potentially empty, but never null). This is private
	 * to ensure read only access.
	 */
	private String[] buildMeta;

	/**
	 * Construct a new plain version object
	 * 
	 * @param major
	 *          major version number. Must not be negative
	 * @param minor
	 *          minor version number. Must not be negative
	 * @param patch
	 *          patchlevel. Must not be negative.
	 */
	public SemanticVersion(int major, int minor, int patch, int revision) {
		this(major, minor, patch, revision, new String[0], new String[0]);
	}

	/**
	 * Construct a fully featured version object with all bells and whistles.
	 * 
	 * @param major
	 *          major version number (must not be negative)
	 * @param minor
	 *          minor version number (must not be negative)
	 * @param patch
	 *          patch level (must not be negative).
	 * @param preRelease
	 *          pre release identifiers. Must not be null, all parts must match
	 *          "[0-9A-Za-z-]+".
	 * @param buildMeta
	 *          build meta identifiers. Must not be null, all parts must match
	 *          "[0-9A-Za-z-]+".
	 */
	public SemanticVersion(int major, int minor, int patch, int revision, 
						   String[] preRelease, String[] buildMeta) {
		if (major < 0 || minor < 0 || patch < 0 || revision < 0) {
			throw new IllegalArgumentException("Versionnumbers must be positive!");
		}
		this.buildMeta = new String[buildMeta.length];
		this.preRelease = new String[preRelease.length];
		Pattern p = Pattern.compile("[0-9A-Za-z-]+");
		for (int i = 0; i < preRelease.length; i++) {
			if (preRelease[i] == null || !p.matcher(preRelease[i]).matches()) {
				throw new IllegalArgumentException("Pre Release tag: " + i);
			}
			this.preRelease[i] = preRelease[i];
		}
		for (int i = 0; i < buildMeta.length; i++) {
			if (buildMeta[i] == null || !p.matcher(buildMeta[i]).matches()) {
				throw new IllegalArgumentException("Build Meta tag: " + i);
			}
			this.buildMeta[i] = buildMeta[i];
		}

		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.revision = revision;
	}

	/**
	 * Convenience constructor for creating a Version object from the
	 * "Implementation-Version:" property of the Manifest file.
	 * 
	 * @param clazz
	 *          a class in the JAR file (or that otherwise has its
	 *          implementationVersion attribute set).
	 * @throws ParseException
	 *           if the versionstring does not conform to the semver specs.
	 */
	public SemanticVersion(Class<?> clazz) throws ParseException {
		this(clazz.getPackage().getImplementationVersion());
	}

	/**
	 * Construct a version object by parsing a string.
	 * 
	 * @param version
	 *          version in flat string format
	 * @throws ParseException
	 *           if the version string does not conform to the semver specs.
	 */
	public SemanticVersion(String version) throws ParseException {
		vParts = new int[4];
		vParts[0] = vParts[1] = vParts[2] = vParts[3] = 0;
		preParts = new ArrayList<String>(5);
		metaParts = new ArrayList<String>(5);
		input = version.toCharArray();
		if (!stateMajor()) { // Start recursive descend
			throw new ParseException(version, errPos);
		}
		major = vParts[0];
		minor = vParts[1];
		patch = vParts[2];
		revision = vParts[3];
		preRelease = preParts.toArray(new String[preParts.size()]);
		buildMeta = metaParts.toArray(new String[metaParts.size()]);
	}

	/**
	 * Check if this version has a given build Meta tags.
	 * 
	 * @param tag
	 *          the tag to check for.
	 * @return true if the tag is found in {@link SemanticVersion#buildMeta}.
	 */
	public boolean hasBuildMeta(String tag) {
		for (String s : buildMeta) {
			if (s.equals(tag)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if this version has a given pre release tag.
	 * 
	 * @param tag
	 *          the tag to check for
	 * @return true if the tag is found in {@link SemanticVersion#preRelease}.
	 */
	public boolean hasPreRelease(String tag) {
		for (String s : preRelease) {
			if (s.equals(tag)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the pre release tags
	 * 
	 * @return a potentially empty array, but never null.
	 */
	public String[] getPreRelease() {
		String[] ret = new String[preRelease.length];
		System.arraycopy(preRelease, 0, ret, 0, ret.length);
		return ret;
	}

	/**
	 * Get the build meta tags
	 * 
	 * @return a potentially empty array, but never null.
	 */
	public String[] getBuildMeta() {
		String ret[] = new String[buildMeta.length];
		System.arraycopy(buildMeta, 0, ret, 0, ret.length);
		return ret;
	}

	/**
	 * Convenience method to check if this version is an update.
	 * 
	 * @param v
	 *          the other version object
	 * @return true if this version is newer than the other one.
	 */
	public boolean isUpdateFor(SemanticVersion v) {
		return compareTo(v) > 0;
	}

	/**
	 * Convenience method to check if this version is a compatible update.
	 * 
	 * @param v
	 *          the other version object.
	 * @return true if this version is newer and both have the same major version.
	 */
	public boolean isCompatibleUpdateFor(SemanticVersion v) {
		return isUpdateFor(v) && (major == v.major) && (major != 0 || v.major != 0);
	}

	/**
	 * Convenience method to check if this is a stable version.
	 * 
	 * @return true if the major version number is greater than zero and there are
	 *         no pre release tags.
	 */
	public boolean isStable() {
		return major > 0 && preRelease.length == 0;
	}
	
	public SemanticVersion clearBuildMeta() {
		buildMeta = new String[0];
		return this;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(major);
		ret.append('.');
		ret.append(minor);
		ret.append('.');
		ret.append(patch);
		if (revision != 0) {
			ret.append('.');
			ret.append(revision);
		}
		if (preRelease.length > 0) {
			ret.append('-');
			for (int i = 0; i < preRelease.length; i++) {
				ret.append(preRelease[i]);
				if (i < preRelease.length - 1) {
					ret.append('.');
				}
			}
		}
		if (buildMeta.length > 0) {
			ret.append('+');
			for (int i = 0; i < buildMeta.length; i++) {
				ret.append(buildMeta[i]);
				if (i < buildMeta.length - 1) {
					ret.append('.');
				}
			}
		}
		return ret.toString();
	}

	@Override
	public int hashCode() {
		return toString().hashCode(); // Lazy
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SemanticVersion)) {
			return false;
		}
		SemanticVersion ov = (SemanticVersion) other;
		if (ov.major != major || ov.minor != minor || ov.patch != patch || ov.revision != revision) {
			return false;
		}
		if (ov.preRelease.length != preRelease.length) {
			return false;
		}
		for (int i = 0; i < preRelease.length; i++) {
			if (!preRelease[i].equals(ov.preRelease[i])) {
				return false;
			}
		}
		if (ov.buildMeta.length != buildMeta.length) {
			return false;
		}
		for (int i = 0; i < buildMeta.length; i++) {
			if (!buildMeta[i].equals(ov.buildMeta[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int compareTo(SemanticVersion v) {
		int result = major - v.major;
		if (result == 0) { // Same major
			result = minor - v.minor;
			if (result == 0) { // Same minor
				result = patch - v.patch;
				if (result == 0) { // Same patch
					result = revision - v.revision;
					if (result == 0) { // Same revision
						if (preRelease.length == 0 && v.preRelease.length > 0) {
							result = 1; // No pre release wins over pre release
						}
						if (v.preRelease.length == 0 && preRelease.length > 0) {
							result = -1; // No pre release wins over pre release
						}
						if (preRelease.length > 0 && v.preRelease.length > 0) {
							int len = Math.min(preRelease.length, v.preRelease.length);
							int count = 0;
							for (count = 0; count < len; count++) {
								result = comparePreReleaseTag(count, v);
								if (result != 0) {
									break;
								}
							}
							if (result == 0 && count == len) { // Longer version wins.
								result = preRelease.length - v.preRelease.length;
							}
						}
					}
				}
			}
		}
		return result;
	}

	private int comparePreReleaseTag(int pos, SemanticVersion ov) {
		Integer here = null;
		Integer there = null;
		try {
			here = Integer.parseInt(preRelease[pos], 10);
		}
		catch (NumberFormatException e) {
		}
		try {
			there = Integer.parseInt(ov.preRelease[pos], 10);
		}
		catch (NumberFormatException e) {
		}
		if (here != null && there == null) {
			return -1; // Strings take precedence over numbers
		}
		if (here == null && there != null) {
			return 1; // Strings take precedence over numbers
		}
		if (here == null && there == null) {
			return (preRelease[pos].compareTo(ov.preRelease[pos])); // ASCII compare
		}
		return here.compareTo(there); // Number compare
	}

	// Parser implementation below

	private int[] vParts;
	private ArrayList<String> preParts, metaParts;
	private int errPos;
	private char[] input;

	private boolean stateMajor() {
		int pos = 0;
		while (pos < input.length && input[pos] >= '0' && input[pos] <= '9') {
			pos++; // match [0..9]+
		}
		if (pos == 0) { // Empty String -> Error
			return false;
		}
		if (input[0] == '0' && pos > 1) { // Leading zero
			return false;
		}

		vParts[0] = Integer.parseInt(new String(input, 0, pos), 10);

		if (pos == input.length)
			return true;
		
		if (input[pos] == '.') {
			return stateMinor(pos + 1);
		}

		if (input[pos] == '+') { // We have build meta tags -> descend
			return stateMeta(pos + 1);
		}

		if (input[pos] == '-') { // We have pre release tags -> descend
			return stateRelease(pos + 1);
		}
		
		return false;
	}

	private boolean stateMinor(int index) {
		int pos = index;
		while (pos < input.length && input[pos] >= '0' && input[pos] <= '9') {
			pos++;// match [0..9]+
		}
		if (pos == index) { // Empty String -> Error
			errPos = index;
			return false;
		}
		if (input[index] == '0' && pos - index > 1) { // Leading zero
			errPos = index;
			return false;
		}
		vParts[1] = Integer.parseInt(new String(input, index, pos - index), 10);

		if (pos == input.length)
			return true;
		
		if (input[pos] == '.') {
			return statePatch(pos + 1);
		}

		if (input[pos] == '+') { // We have build meta tags -> descend
			return stateMeta(pos + 1);
		}

		if (input[pos] == '-') { // We have pre release tags -> descend
			return stateRelease(pos + 1);
		}
		
		errPos = pos;
		return false;
	}

	private boolean statePatch(int index) {
		int pos = index;
		while (pos < input.length && input[pos] >= '0' && input[pos] <= '9') {
			pos++;// match [0..9]+
		}
		if (pos == index) { // Empty String -> Error
			errPos = index;
			return false;
		}
		if (input[index] == '0' && pos - index > 1) { // Leading zero
			errPos = index;
			return false;
		}
		vParts[2] = Integer.parseInt(new String(input, index, pos - index), 10);

		if (pos == input.length)
			return true;
		
		if (input[pos] == '.') {
			return stateRevision(pos + 1);
		}

		if (input[pos] == '+') { // We have build meta tags -> descend
			return stateMeta(pos + 1);
		}

		if (input[pos] == '-') { // We have pre release tags -> descend
			return stateRelease(pos + 1);
		}
		
		errPos = pos;
		return false;
	}
	
	private boolean stateRevision(int index) {
		int pos = index;
		while (pos < input.length && input[pos] >= '0' && input[pos] <= '9') {
			pos++; // match [0..9]+
		}
		if (pos == index) { // Empty String -> Error
			errPos = index;
			return false;
		}
		if (input[index] == '0' && pos - index > 1) { // Leading zero
			errPos = index;
			return false;
		}

		vParts[3] = Integer.parseInt(new String(input, index, pos - index), 10);

		if (pos == input.length) { // We have a clean version string
			return true;
		}

		if (input[pos] == '+') { // We have build meta tags -> descend
			return stateMeta(pos + 1);
		}

		if (input[pos] == '-') { // We have pre release tags -> descend
			return stateRelease(pos + 1);
		}

		errPos = pos; // We have junk
		return false;
	}

	private boolean stateRelease(int index) {
		int pos = index;
		while ((pos < input.length)
				&& ((input[pos] >= '0' && input[pos] <= '9')
						|| (input[pos] >= 'a' && input[pos] <= 'z')
						|| (input[pos] >= 'A' && input[pos] <= 'Z') || input[pos] == '-')) {
			pos++; // match [0..9a-zA-Z-]+
		}
		if (pos == index) { // Empty String -> Error
			errPos = index;
			return false;
		}

		preParts.add(new String(input, index, pos - index));
		if (pos == input.length) { // End of input
			return true;
		}
		if (input[pos] == '.') { // More parts -> descend
			return stateRelease(pos + 1);
		}
		if (input[pos] == '+') { // Build meta -> descend
			return stateMeta(pos + 1);
		}

		errPos = pos;
		return false;
	}

	private boolean stateMeta(int index) {
		int pos = index;
		while ((pos < input.length)
				&& ((input[pos] >= '0' && input[pos] <= '9')
						|| (input[pos] >= 'a' && input[pos] <= 'z')
						|| (input[pos] >= 'A' && input[pos] <= 'Z') || input[pos] == '-')) {
			pos++; // match [0..9a-zA-Z-]+
		}
		if (pos == index) { // Empty String -> Error
			errPos = index;
			return false;
		}

		metaParts.add(new String(input, index, pos - index));
		if (pos == input.length) { // End of input
			return true;
		}
		if (input[pos] == '.') { // More parts -> descend
			return stateMeta(pos + 1);
		}
		errPos = pos;
		return false;
	}
}