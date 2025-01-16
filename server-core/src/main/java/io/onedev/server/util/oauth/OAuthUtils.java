package io.onedev.server.util.oauth;

import com.nimbusds.oauth2.sdk.ErrorObject;
import io.onedev.commons.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class OAuthUtils {

	public static String getErrorMessage(ErrorObject error) {
		if ("redirect_uri_mismatch".equals(error.getCode())) {
			return "Redirect uri mismatch: make sure the server url specified in system setting is the same as "
					+ "root part of the authorization callback url at authorization server side";
		} else {
			List<String> details = new ArrayList<>();
			if (error.getCode() != null) 
				details.add("code: " + error.getCode());
			if (error.getDescription() != null)
				details.add("description: " + error.getDescription());
			if (error.getHTTPStatusCode() != 0)
				details.add("http status code: " + error.getHTTPStatusCode());
			
			return StringUtils.join(details, ", ");
		}
	}

}
