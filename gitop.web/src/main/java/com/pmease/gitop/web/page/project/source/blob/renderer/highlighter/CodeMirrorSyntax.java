package com.pmease.gitop.web.page.project.source.blob.renderer.highlighter;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.tika.mime.MimeType;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Language mappings to CodeMirror modes and mime types
 *
 */
public enum CodeMirrorSyntax {
	APL("text/apl", "apl"),
	ASTERISK("text/x-asterisk", "asterisk"),
	C(new String[] {"text/x-csrc", "text/x-c", "text/x-chdr"}, "clike"),
	CPP(new String[] { "text/x-c++src", "text/x-c++hdr" }, "clike"),
	JAVA(new String[] { "text/x-java", "text/x-java-source" }, "clike"),
	CSHARP(new String[] { "text/x-csharp", "text/x-cs" }, "clike"),
	SCALA(new String[] { "text/x-scala" }, "clike"),
	VERTEX(new String[] { "x-shader/x-vertex", "x-shader/x-fragment"}, "clike"),
	CLOJURE ("text/x-clojure", "clojure"),
	COBOL("text/x-cobol", "cobol"),
	COFFEESCRIPT("text/x-coffeescript", "coffeescript"),
	COMMONLISP("text/x-common-lisp", "commonlisp"),
	CSS(new String[] {"text/css", "text/x-scss"}, "css"),
	D("text/x-d", "d"),
	DIFF("text/x-diff", "diff"),
	DTD("application/xml-dtd", "dtd"),
	ECL("text/x-ecl", "ecl"),
	EIFFEL("text/x-eiffel", "eiffel"),
	ERLANG("text/x-erlang", "erlang"),
	FORTRAN("text/x-fortran", "fortran"),
	GAS("text/x-gas", "gas"), // TODO
	GFM("text/x-gfmBase", "gfm"), // TODO
	GHERKIN("text/x-feature", "gherkin"),
	GO("text/x-go", "go"),
	GROOVY("text/x-groovy", "groovy"),
	HAML("text/x-haml", "haml"),
	HASKELL("text/x-haskell", "haskell"),
	HAXE("text/x-haxe", "haxe"),
	HTTP("message/http", "http"),
	JADE("text/x-jade", "jade"),
	JAVASCRIPT(new String[] {"text/javascript", 
							 "text/ecmascript", 
							 "application/javascript", 
							 "application/ecmascript", 
							 "text/typescript", 
							 "application/typescript"}, 
				"javascript"),
	JSON(new String[] {"application/json", "application/x-json"}, "javascript"),
	JINJA2("text/x-jinja2", "jinja2"), // XXX
	JULIA("text/x-julia", "julia"),
	LESS("text/x-less", "less"),
	LIVESCRIPT("text/x-livescript", "livescript"),
	LUA("text/x-lua", "lua"),
	MARKDOWN("text/x-markdown", "markdown"),
	MIRC("text/mirc", "mirc"),
	NGINX(new String[] {"text/nginx", "text/x-nginx-conf"}, "nginx"),
	NTRIPLES("text/n-triples", "ntriples"),
	OCAML("text/x-ocaml", "ocaml"),
	OCTAVE("text/x-octave", "octave"),
	PASCAL("text/x-pascal", "pascal"),
	PEGJS("text/x-pegjs", "pegjs"), // XXX
	PERL("text/x-perl", "perl"),
	PHP(new String[] { "application/x-httpd-php",
						"application/x-httpd-php-open",
						"text/x-php"},
		"php"),
	PIG("text/x-pig", "pig"),
	PROPERTIES(new String[] { "text/x-properties", "text/x-ini" }, "properties"),
	PYTHON(new String[] { "text/x-python", "text/x-cython"}, "python"),
	Q("text/x-q", "q"),
	R(new String[] {"text/x-r", "text/x-rsrc"}, "r"),
	RPM_CHANGE(new String[] {"text/x-rpm-changes"}, "changes", "rpm"),
	RPM_SPEC(new String[] {"text/x-rpm-spec"}, "spec", "rpm"),
	RST("text/x-rst", "rst"),
	RUBY("text/x-ruby", "ruby"),
	RUST("text/x-rustsrc", "rust"),
	SASS("text/x-sass", "sass"),
	SCHEME("text/x-scheme", "scheme"),
	SHELL("text/x-sh", "shell"),
	SIEVE("application/sieve", "sieve"),
	SMALLTALK("text/x-stsrc", "smalltalk"),
	SMARTY("text/x-smarty", "smarty"),
	SMARTYMIXED("text/x-smarty", "smartymixed"),
	SPARQL("application/x-sparql-query", "sparql"),
	SQL(new String[] { "text/x-sql",
						"text/x-mssql",
						"text/x-mysql",
						"text/x-mariadb",
						"text/x-cassandra",
						"text/x-plsql"},
		"sql"),
	STEX(new String[] {"text/x-stex", "text/x-latex"}, "stex"),
	TCL("text/x-tcl", "tcl"),
	TIDDLYWIKI("text/x-tiddlywiki", "tiddlywiki"),
	TIKI("text/tiki", "tiki"),
	TOML("text/x-toml", "toml"),
	TURTLE("text/turtle", "turtle"),
	VB("text/x-vb", "vb"),
	VBSCRIPT("text/vbscript", "vbscript"),
	VELOCITY("text/velocity", "velocity"),
	XML(new String[] { "text/xml", "application/xml"}, "xml"),
	XQUERY("application/xquery", "xquery"),
	YAML("text/x-yaml", "yaml"),
	Z80("text/x-z80", "z80"),
	
	HTMLMIXED(new String[] {"text/html"}, "htmlmixed", null, 
			CodeMirrorSyntax.XML,
			CodeMirrorSyntax.CSS,
			CodeMirrorSyntax.JAVASCRIPT),
	HTMLEMBEDDED(new String[] {"application/x-ejs", 
			"application/x-aspx", 
			"application/x-jsp", 
			"application/x-erb"}, 
			"htmlembedded",
			null,
			CodeMirrorSyntax.HTMLMIXED)
	;

	
	
	private final String[] mimes;
	private final String mode;
	private final String group;
	private final CodeMirrorSyntax[] dependencies;
	private final JavaScriptResourceReference resourceReference;
	
	CodeMirrorSyntax(String[] mimes, String mode) {
		this(mimes, mode, null);
	}

	CodeMirrorSyntax(String mime, String mode) {
		this(new String[] {mime}, mode);
	}
	
	CodeMirrorSyntax(String[] mimes, String mode, String group, CodeMirrorSyntax... dependencies) {
		this.mimes = mimes;
		this.mode = mode;
		this.group = group;
		this.dependencies = dependencies;
		
		this.resourceReference = new JavaScriptResourceReference(CodeMirrorSyntax.class, 
				"res/codemirror/mode/" + getJsFilePath());
	}

	public String[] getMimes() {
		return mimes;
	}

	public String getMode() {
		return mode == null ? name().toLowerCase() : mode;
	}

	public String getGroup() {
		return group;
	}
	
	// mapping to CodeMirror mode directory
	public String getJsFilePath() {
		StringBuffer sb = new StringBuffer();
		if (group != null) {
			sb.append(group).append("/");
		}
		sb.append(getMode()).append("/");
		sb.append(getMode()).append(".js");
		return sb.toString();
	}
	
	public CodeMirrorSyntax[] getDependencies() {
		if (dependencies == null || dependencies.length == 0) {
			return new CodeMirrorSyntax[0];
		}
		
		Set<CodeMirrorSyntax> list = Sets.newLinkedHashSet();
		for (CodeMirrorSyntax each : dependencies) {
			list.addAll(Lists.newArrayList(each.getDependencies()));
			list.add(each);
		}
		
		return Iterables.toArray(list, CodeMirrorSyntax.class);
	}

	public JavaScriptResourceReference getResourceReference() {
		return resourceReference;
	}



	static Set<String> modes;
	static Map<String, CodeMirrorSyntax> mimeSyntaxMap;
	
	static {
		Set<String> set = Sets.newHashSet();
		Map<String, CodeMirrorSyntax> map = Maps.newHashMap();
		for (CodeMirrorSyntax each : CodeMirrorSyntax.values()) {
			set.add(each.getMode());
			for (String mime : each.mimes) {
				map.put(mime, each);
			}
		}
		
		modes = ImmutableSet.<String>copyOf(set);
		mimeSyntaxMap = ImmutableMap.<String, CodeMirrorSyntax>copyOf(map);
	}
	
	public static Set<String> getAllModes() {
		return modes;
	}
	
	public static @Nullable CodeMirrorSyntax findByMime(String mime) {
		return mimeSyntaxMap.get(mime);
	}
	
	public static @Nullable CodeMirrorSyntax findByMime(MimeType mime) {
		return findByMime(mime.getType().toString());
	}
}
