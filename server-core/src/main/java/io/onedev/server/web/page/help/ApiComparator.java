package io.onedev.server.web.page.help;

import io.onedev.server.rest.annotation.Api;

import java.lang.reflect.AnnotatedElement;
import java.util.Comparator;

public class ApiComparator implements Comparator<AnnotatedElement> {

	@Override
	public int compare(AnnotatedElement o1, AnnotatedElement o2) {
		Api api1 = o1.getAnnotation(Api.class);
		Api api2 = o2.getAnnotation(Api.class);
		int order1 = Integer.MAX_VALUE;
		if (api1 != null)
			order1 = api1.order();
		int order2 = Integer.MAX_VALUE;
		if (api2 != null)
			order2 = api2.order();
		
		return order1 - order2;
	}

}
