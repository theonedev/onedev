package io.onedev.server.util;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;

import io.onedev.commons.utils.StringUtils;

public class JerseyUtils {

	@Nullable
	public static String checkStatus(Response response) {
		int status = response.getStatus();
		if (status != 200) {
			String errorMessage = response.readEntity(String.class);
			if (StringUtils.isNotBlank(errorMessage)) {
				return String.format("Http request failed (status code: %d, error message: %s)", 
						status, errorMessage);
			} else {
				return String.format("Http request failed (status code: %d)", status);
			}
		} else {
			return null;
		}
	}
	
}
