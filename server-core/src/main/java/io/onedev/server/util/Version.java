package io.onedev.server.util;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.StringUtils;

/**
 * A version composed of major, minor, patch, qualifier and build. All fields except 
 * major is optional, and optional fields are denoted using negative value (in case 
 * of integer type or empty string in case of string type).
 * @author robin
 *
 */
public class Version implements Comparable<Version> {
	
	private int major = 0;
	
	private int minor = -1;
	
	private int patch = -1;
	
	private String qualifier = "";
	
	private int build = -1;

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getPatch() {
		return patch;
	}

	public String getQualifier() {
		return qualifier;
	}

	public int getBuild() {
		return build;
	}

	public Version(String versionStr) {
		versionStr = StringUtils.deleteWhitespace(versionStr);
		String[] fields = StringUtils.split(versionStr, '-');
		Preconditions.checkState(fields.length >= 1);
		String[] parts = StringUtils.split(fields[0], '.');

		major = Integer.parseInt(parts[0]);
		Preconditions.checkArgument(major>=0);
		
		if (parts.length >= 2) {
			minor = Integer.parseInt(parts[1]);
			Preconditions.checkArgument(minor>=0);
		}
		if (parts.length >= 3) {
			patch = Integer.parseInt(parts[2]);
			Preconditions.checkArgument(patch>=0);
		}
		
		if (parts.length >= 4)
			throw new RuntimeException("Invalid version string '" + versionStr + "'.");
		
		if (fields.length >= 2) 
			qualifier = fields[1];
		if (fields.length >= 3) {
			build = Integer.parseInt(fields[2]);
			Preconditions.checkArgument(build>=0);
		}
		
		if (fields.length >= 4) 
			throw new RuntimeException("Invalid version string '" + versionStr + "'.");
	}
	
	public Version(int major, int minor, int patch, String qualifier, int build) {
		Preconditions.checkArgument(major>=0, "Major version should be a positive integer.");
		this.major = major;
		
		if (minor >= 0) {
			this.minor = minor;
			this.patch = patch;
		} else {
			Preconditions.checkArgument(patch<0, 
					"Minor version should also be specified if patch version is specified.");
		}

		if (qualifier == null)
			qualifier = "";
		else
			qualifier = StringUtils.deleteWhitespace(qualifier);

		if (qualifier.length() != 0) {
			this.qualifier = qualifier;
			this.build = build;
		} else {
			Preconditions.checkArgument(build<0, 
					"Qualifier should also be specified if build number is specified.");
		}
	}
	
	public Version(int major, int minor, int patch) {
		this(major, minor, patch, "", -1);
	}
	
	public Version(int major, int minor) {
		this(major, minor, -1);
	}
	
	public Version(int major) {
		this(major, -1);
	}
	
	public boolean isCompatible(Version version) {
		return major == version.getMajor() && (minor<0?0:minor) == (version.getMinor()<0?0:version.getMinor());
	}

	@Override
	public int compareTo(Version version) {
		if (major < version.getMajor()) 
			return -1;
		else if (major > version.getMajor()) 
			return 1;
		else if ((minor<0?0:minor) < (version.getMinor()<0?0:version.getMinor())) 
			return -1;
		else if ((minor<0?0:minor) > (version.getMinor()<0?0:version.getMinor())) 
			return 1;
		else if ((patch<0?0:patch) < (version.getPatch()<0?0:version.getPatch())) 
			return -1;
		else if ((patch<0?0:patch) > (version.getPatch()<0?0:version.getPatch())) 
			return 1;
		else if (!qualifier.equals(version.getQualifier())) 
			return qualifier.compareTo(version.getQualifier());
		else
			return (build<0?0:build) - (version.getBuild()<0?0:version.getBuild());
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(major);
		if (minor >= 0) {
			buffer.append(".").append(minor);
			if (patch >= 0)
				buffer.append(".").append(patch);
		}
		if (qualifier.length() != 0) {
			buffer.append("-").append(qualifier);
			if (build >= 0)
				buffer.append("-").append(build);
		}
		return buffer.toString();
	}
	
}
