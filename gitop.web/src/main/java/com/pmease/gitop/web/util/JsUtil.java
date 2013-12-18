package com.pmease.gitop.web.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Throwables;

public class JsUtil {

	public static String formatOptions(Object option) {
		try {
			return StandardObjectMapper.getInstance().writeValueAsString(option);
		} catch (JsonProcessingException e) {
			throw Throwables.propagate(e);
		}
	}
}
