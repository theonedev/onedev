package io.onedev.server.mail;

import java.util.*;

class Attachments {

	final Map<String, Attachment> identifiable = new LinkedHashMap<>();

	final Collection<Attachment> nonIdentifiable = new ArrayList<>();

	final Collection<String> referenced = new HashSet<>();

}
