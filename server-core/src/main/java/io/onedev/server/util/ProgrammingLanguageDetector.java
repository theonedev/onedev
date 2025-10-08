package io.onedev.server.util;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

public class ProgrammingLanguageDetector {
	private static final Map<String, String> PROGRAMMING_LANGUAGES = new HashMap<>();

	static {
		PROGRAMMING_LANGUAGES.put("java", "Java");
		
		PROGRAMMING_LANGUAGES.put("h", "C");
		PROGRAMMING_LANGUAGES.put("c", "C");
		
		PROGRAMMING_LANGUAGES.put("h++", "C++");
		PROGRAMMING_LANGUAGES.put("c++", "C++");
		PROGRAMMING_LANGUAGES.put("hpp", "C++");
		PROGRAMMING_LANGUAGES.put("cpp", "C++");
		PROGRAMMING_LANGUAGES.put("hxx", "C++");
		PROGRAMMING_LANGUAGES.put("cxx", "C++");
		PROGRAMMING_LANGUAGES.put("hh", "C++");
		PROGRAMMING_LANGUAGES.put("cc", "C++");
		
		PROGRAMMING_LANGUAGES.put("cob", "Cobol");
		PROGRAMMING_LANGUAGES.put("cpy", "Cobol");
		
		PROGRAMMING_LANGUAGES.put("cs", "CSharp");
		
		PROGRAMMING_LANGUAGES.put("clj", "Clojure");
		PROGRAMMING_LANGUAGES.put("cljc", "Clojure");
		PROGRAMMING_LANGUAGES.put("cljx", "Clojure");
		PROGRAMMING_LANGUAGES.put("cljs", "ClojureScript");
		PROGRAMMING_LANGUAGES.put("gss", "Closure Stylesheets");
		PROGRAMMING_LANGUAGES.put("coffee", "CoffeeScript");
		
		PROGRAMMING_LANGUAGES.put("cl", "Common Lisp");
		PROGRAMMING_LANGUAGES.put("lisp", "Common Lisp");
		PROGRAMMING_LANGUAGES.put("el", "Common Lisp");
		
		PROGRAMMING_LANGUAGES.put("css", "CSS");
		PROGRAMMING_LANGUAGES.put("d", "D");
		PROGRAMMING_LANGUAGES.put("dart", "Dart");
		PROGRAMMING_LANGUAGES.put("dtd", "DTD");
		
		PROGRAMMING_LANGUAGES.put("erl", "Erlang");
		PROGRAMMING_LANGUAGES.put("f", "Fortran");
		PROGRAMMING_LANGUAGES.put("for", "Fortran");
		PROGRAMMING_LANGUAGES.put("f77", "Fortran");
		PROGRAMMING_LANGUAGES.put("f90", "Fortran");
		
		PROGRAMMING_LANGUAGES.put("go", "Go");
		PROGRAMMING_LANGUAGES.put("groovy", "Groovy");
		PROGRAMMING_LANGUAGES.put("gradle", "Groovy");
		
		PROGRAMMING_LANGUAGES.put("hs", "Haskell");
		PROGRAMMING_LANGUAGES.put("aspx", "ASP.NET");
		PROGRAMMING_LANGUAGES.put("html", "HTML");
		PROGRAMMING_LANGUAGES.put("htm", "HTML");
		PROGRAMMING_LANGUAGES.put("jsp", "Java Server Pages");
		PROGRAMMING_LANGUAGES.put("js", "JavaScript");
		
		PROGRAMMING_LANGUAGES.put("json", "JSON");
		PROGRAMMING_LANGUAGES.put("jsx", "JSX");
		
		PROGRAMMING_LANGUAGES.put("kt", "Kotlin");
		PROGRAMMING_LANGUAGES.put("less", "LESS");
		PROGRAMMING_LANGUAGES.put("lua", "Lua");
		PROGRAMMING_LANGUAGES.put("md", "Markdown");
		PROGRAMMING_LANGUAGES.put("mkd", "Markdown");
		PROGRAMMING_LANGUAGES.put("markdown", "Markdown");
		PROGRAMMING_LANGUAGES.put("m", "Objective-C");
		PROGRAMMING_LANGUAGES.put("mm", "Objective-C");
		
		PROGRAMMING_LANGUAGES.put("p", "Pascal");
		PROGRAMMING_LANGUAGES.put("pas", "Pascal");
		PROGRAMMING_LANGUAGES.put("pl", "Perl");
		PROGRAMMING_LANGUAGES.put("pm", "Perl");
		
		PROGRAMMING_LANGUAGES.put("php", "PHP");
		PROGRAMMING_LANGUAGES.put("php3", "PHP");
		PROGRAMMING_LANGUAGES.put("php4", "PHP");
		PROGRAMMING_LANGUAGES.put("php5", "PHP");
		PROGRAMMING_LANGUAGES.put("php7", "PHP");
		PROGRAMMING_LANGUAGES.put("phtml", "PHP");
		
		PROGRAMMING_LANGUAGES.put("sql", "SQL");
		PROGRAMMING_LANGUAGES.put("ps1", "PowerShell");
		PROGRAMMING_LANGUAGES.put("psd1", "PowerShell");
		PROGRAMMING_LANGUAGES.put("psm1", "PowerShell");
		
		PROGRAMMING_LANGUAGES.put("properties", "Properties");
		PROGRAMMING_LANGUAGES.put("ini", "INI");
		PROGRAMMING_LANGUAGES.put("in", "INI");
		PROGRAMMING_LANGUAGES.put("proto", "ProtoBuf");
		
		PROGRAMMING_LANGUAGES.put("BUILD", "Python");
		PROGRAMMING_LANGUAGES.put("py", "Python");
		PROGRAMMING_LANGUAGES.put("pyw", "Python");
		PROGRAMMING_LANGUAGES.put("bzl", "Python");
		
		PROGRAMMING_LANGUAGES.put("pp", "Puppet");
		PROGRAMMING_LANGUAGES.put("r", "R");
		PROGRAMMING_LANGUAGES.put("R", "R");
		PROGRAMMING_LANGUAGES.put("rb", "Ruby");
		PROGRAMMING_LANGUAGES.put("rs", "Rust");
		
		PROGRAMMING_LANGUAGES.put("sas", "SAS");
		PROGRAMMING_LANGUAGES.put("sass", "Sass");
		PROGRAMMING_LANGUAGES.put("scala", "Scala");
		PROGRAMMING_LANGUAGES.put("scm", "Scheme");
		PROGRAMMING_LANGUAGES.put("ss", "Scheme");
		PROGRAMMING_LANGUAGES.put("scss", "Scss");
		
		PROGRAMMING_LANGUAGES.put("sh", "Shell");
		PROGRAMMING_LANGUAGES.put("ksh", "Shell");
		PROGRAMMING_LANGUAGES.put("bash", "Shell");
		
		PROGRAMMING_LANGUAGES.put("st", "SmallTalk");
		PROGRAMMING_LANGUAGES.put("soy", "Soy");
		PROGRAMMING_LANGUAGES.put("styl", "Stylus");
		PROGRAMMING_LANGUAGES.put("swift", "Swift");
		
		PROGRAMMING_LANGUAGES.put("tex", "LaTeX");
		PROGRAMMING_LANGUAGES.put("tcl", "TCL");
		PROGRAMMING_LANGUAGES.put("ts", "TypeScript");
		PROGRAMMING_LANGUAGES.put("tsx", "TypeScript-JSX");
		PROGRAMMING_LANGUAGES.put("vb", "VB.NET");
		PROGRAMMING_LANGUAGES.put("vbs", "VBScript");
		
		PROGRAMMING_LANGUAGES.put("vue", "Vue.js Component");
		PROGRAMMING_LANGUAGES.put("xml", "XML");
		PROGRAMMING_LANGUAGES.put("xsl", "XML");
		PROGRAMMING_LANGUAGES.put("xsd", "XML");
		PROGRAMMING_LANGUAGES.put("svg", "XML");
		PROGRAMMING_LANGUAGES.put("yaml", "Yaml");
		PROGRAMMING_LANGUAGES.put("yml", "Yaml");
	}

	@Nullable
	public static String getLanguageForExtension(@Nullable String extension) {
		if(extension != null) {
			return PROGRAMMING_LANGUAGES.get(extension.toLowerCase());
		}
		return null;
	}
}
