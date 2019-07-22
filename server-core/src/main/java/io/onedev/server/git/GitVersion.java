package io.onedev.server.git;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import io.onedev.commons.utils.StringUtils;

public class GitVersion implements Comparable<GitVersion> {

	private List<Integer> parts = new ArrayList<Integer>(); 
			
	private boolean msysgit = false;
	
	public GitVersion(String versionStr) {
		for (String each: StringUtils.splitAndTrim(versionStr, ".")) {
			if (NumberUtils.isDigits(each))
				parts.add(Integer.valueOf(each));
			else if (each.equals("msysgit"))
				msysgit = true;
		}
	}

	public List<Integer> getParts() {
		return Collections.unmodifiableList(parts);
	}
	
	public boolean isMsysgit() {
		return msysgit;
	}
	
	@Override
	public int compareTo(GitVersion version) {
		List<Integer> otherParts = version.getParts();
		int index = 0;
		for (int part: parts) {
			int otherPart = 0;
			if (index < otherParts.size())
				otherPart = otherParts.get(index);
			if (part < otherPart)
				return -1;
			else if (part > otherPart)
				return 1;
			index++;
		}
		
		for (int i=index; i<otherParts.size(); i++) {
			int otherPart = otherParts.get(i);
			if (otherPart > 0)
				return -1;
			else if (otherPart < 0)
				return 1;
		}
		
		return 0;
	}
	
	public boolean isNotOlderThan(GitVersion version) {
		return compareTo(version) >= 0;
	}
	
	public boolean isNewerThan(GitVersion version) {
		return compareTo(version) > 0;
	}

	public boolean isNotNewerThan(GitVersion version) {
		return compareTo(version) <= 0;
	}

	public boolean isOlderThan(GitVersion version) {
		return compareTo(version) < 0;
	}

	@Override
	public String toString() {
		List<String> parts = new ArrayList<String>();
		for (Integer each: this.parts)
			parts.add(each.toString());
		
		return StringUtils.join(parts, ".");
	}
}
