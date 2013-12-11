package com.pmease.gitop.web.util;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.tika.mime.MimeType;

import com.google.common.collect.ImmutableMap;

public class MimeTypeUtils {

	private MimeTypeUtils() {}
	
	public static boolean isTextType(MimeType type) {
		return type.getType().getType().equalsIgnoreCase("text")
                || type.getType().getSubtype().equalsIgnoreCase("text")
                || type.getType().getSubtype().equalsIgnoreCase("x-sh");
	}
	
	public static boolean isXMLType(final MimeType type) {
        return type.getType().getType().equalsIgnoreCase("xml")
                || type.getType().getSubtype().endsWith("xml");
    }
	
	public static boolean isImageType(final MimeType type) {
    	return type.getType().getType().equalsIgnoreCase("image")
    			|| type.getType().getSubtype().endsWith("image");
    			
    }
	
	static Map<String, String> sourceTypes = ImmutableMap.<String, String>builder()
			.put("text/x-actionscript", "actionscript")
			.put("text/x-ada", "ada")
			.put("text/x-applescript", "applescript")
			.put("text/asp", "asp")
			.put("text/aspdotnet", "asp")
			.put("text/x-aspectj", "aspectj")
			.put("text/x-assembly", "assembly")
			.put("text/calendar", "calendar")
			.put("text/css", "css")
			.put("text/csv", "csv")
			.put("text/html", "html")
			.put("text/x-awk", "awk")
			.put("text/x-basic", "basic")
			.put("text/x-c++hdr", "cpp")
			.put("text/x-c++src", "cpp")
			.put("text/x-cgi", "cgi")
			.put("text/x-chdr", "cpp") // c header
			.put("text/x-csrc", "cpp") // c source
			.put("text/x-clojure", "clojure")
			.put("text/x-coffeescript", "coffeescript")
			.put("text/x-csharp", "cs")
			.put("text/x-cobol", "cobol")
			.put("text/x-coldfusion", "coldfusion")
			.put("text/x-common-lisp", "lisp")
			.put("text/x-diff", "diff") // diff
			.put("text/x-eiffel", "eiffel")
			.put("text/x-emacs-lisp", "lisp")
			.put("text/x-erlang", "erlang")
			.put("text/x-expect", "expect")
			.put("text/x-forth", "forth")
			.put("text/x-fortran", "fortran")
			.put("text/x-go", "go")
			.put("text/x-groovy", "groovy")
			.put("text/x-haskell", "haskell")
			.put("text/x-idl", "idl")
			.put("text/x-ini", "ini")
			.put("text/x-java-source", "java")
			.put("text/x-jsp", "jsp")
			.put("text/x-less", "less")
			.put("text/x-lex", "lex")
			.put("text/x-log", "log")
			.put("text/x-lua", "lua")
			.put("text/x-ml", "ml")
			.put("text/x-matlab", "matlab")
			.put("text/x-modula", "modula")
			.put("text/x-objcsrc", "objectivec") // object c source
			.put("text/x-ocaml", "ocaml")
			.put("text/x-pascal", "pascal")
			.put("text/x-perl", "perl")
			.put("text/x-php", "php")
			.put("text/x-prolog", "prolog")
			.put("text/x-python", "python")
			.put("text/x-rst", "rst")
			.put("text/x-rexx", "rexx")
			.put("text/x-ruby", "ruby")
			.put("text/x-scala", "scala")
			.put("text/x-scheme", "scheme")
			.put("text/x-sed", "sed")
			.put("text/x-sql", "sql")
			.put("text/x-setext", "setext")
			.put("text/x-stsrc", "smalltalk") // smalltalk
			.put("text/x-tcl", "tcl")
			.put("text/x-vbasic", "basic")
			.put("text/x-vbdotnet", "vbnet")
			.put("text/x-vbscript", "vbscript")
			.put("text/x-vcalendar", "vcalendar")
			.put("text/x-vcard", "vcard")
			.put("text/x-verilog", "verilog")
			.put("text/x-vhdl", "vhdl")
			.put("text/x-web-markdown", "markdown")
			.put("text/x-yacc", "yacc")
			.put("text/x-yaml", "yaml")
			
			.put("application/json", "json")
			.put("application/javascript", "javascript")
			.put("application/x-sh", "sh")
			.put("application/x-httpd-jsp", "jsp")
			.build();
	
	public static @Nullable String guessSourceType(MimeType mime) {
		String type = mime.getType().toString();
		if (sourceTypes.containsKey(type)) {
			return sourceTypes.get(type);
		}
		
		if (isXMLType(mime)) {
			return "xml";
		}
		
		return null;
	}
}
