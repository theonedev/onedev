package com.pmease.commons.util.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordSplitter implements PartialSplitter {

	private static final Pattern pattern = Pattern.compile("\\w+");
	
	@Override
	public List<String> split(String line) {
		List<String> partials = new ArrayList<>();
		Matcher matcher = pattern.matcher(line);
		int lastEnd = 0;
		while (matcher.find()) {
			int start = matcher.start();
			if (start > lastEnd)
				partials.add(line.substring(lastEnd, start));
            partials.add(matcher.group());
            lastEnd = matcher.end();
        }
		if (lastEnd < line.length())
			partials.add(line.substring(lastEnd));
		return partials;
	}

}
