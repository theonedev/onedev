package io.onedev.server.oem;

public interface OemService {

	String DEFAULT_WEBSITE = "https://onedev.io";

	String DEFAULT_DOCSITE = "https://docs.onedev.io";

	String DEFAULT_CONTACT_EMAIL = "support@onedev.io";

	String getProductName();

	String getWebsite();

	String getDocsite();

	String getContactEmail();

}
