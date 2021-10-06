package io.onedev.server.util;

import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

public class XmlUtils {
	
	// Prevent XXE attack as the xml might be provided by malicious users
	public static void disallowDocTypeDecl(SAXReader reader) {
		try {
			reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String stripDoctype(String xml) {
		return xml.replaceFirst("<!DOCTYPE\\s.*?>", "");
	}

}
