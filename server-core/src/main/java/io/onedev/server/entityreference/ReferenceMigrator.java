package io.onedev.server.entityreference;

import java.util.Map;

import io.onedev.commons.utils.WordUtils;

public class ReferenceMigrator {

	private final String referenceType;
	
	private final Map<Long, Long> numberMappings;
	
	public ReferenceMigrator(Class<?> referenceClass, Map<Long, Long> numberMappings) {
		referenceType = WordUtils.uncamel(referenceClass.getSimpleName()).toLowerCase();
		this.numberMappings = numberMappings;
	}
	
	public String migratePrefixed(String content, String prefix) {
		StringBuilder builder = new StringBuilder();
		int start = 0;
		while (true) {
			int index = content.indexOf(prefix, start);
			if (index == -1) {
				builder.append(content.substring(start));
				break;
			} else {
				builder.append(content.substring(start, index));
				start = index + prefix.length();
				if (index > 0 && Character.isLetterOrDigit(content.charAt(index-1))) {
					builder.append(prefix);
				} else {
					StringBuilder numberBuilder = new StringBuilder();
					while (start < content.length()) {
						char ch = content.charAt(start);
						if (Character.isDigit(ch)) {
							numberBuilder.append(ch);
							start++;
						} else {
							if (Character.isAlphabetic(ch)) 
								numberBuilder.setLength(0);
							break;
						}
					}
					if (numberBuilder.length() != 0) {
						Long newNumber = numberMappings.get(Long.valueOf(numberBuilder.toString()));
						if (newNumber != null)
							builder.append(referenceType).append(" #").append(newNumber);
						else 
							builder.append(content.substring(index, start));
					} else {
						builder.append(content.substring(index, start));
					}
				}
			}
		}
		return builder.toString();
	}
	
}
