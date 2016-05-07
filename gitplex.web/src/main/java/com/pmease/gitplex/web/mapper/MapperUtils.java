package com.pmease.gitplex.web.mapper;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.Url;

import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.util.validation.AccountNameValidator;
import com.pmease.gitplex.core.util.validation.DepotNameValidator;

public class MapperUtils {

	public static List<String> normalize(List<String> urlSegments) {
		List<String> normalized = new ArrayList<String>();
		for (String each: urlSegments) {
			each = StringUtils.remove(each, '/');
			if (each.length() != 0)
				normalized.add(each);
		}
		return normalized;
	}
	
	public static int getAccountSegments(Url url) {
		List<String> urlSegments = normalize(url.getSegments());
		if (urlSegments.size() < 1)
			return 0;
		String accountName = urlSegments.get(0);
		
		if (AccountNameValidator.getReservedNames().contains(accountName))
			return 0;
		else
			return urlSegments.size();
	}
	
	public static int getDepotSegments(Url url) {
		List<String> urlSegments = normalize(url.getSegments());
		if (urlSegments.size() < 2)
			return 0;
		
		String accountName = urlSegments.get(0);
		if (AccountNameValidator.getReservedNames().contains(accountName))
			return 0;

		String depotName = urlSegments.get(1);
		if (DepotNameValidator.getReservedNames().contains(depotName))
			return 0;
		else
			return urlSegments.size();
	}
	
}
