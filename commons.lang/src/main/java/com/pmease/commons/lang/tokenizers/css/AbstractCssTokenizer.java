package com.pmease.commons.lang.tokenizers.css;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;
import com.pmease.commons.lang.AbstractTokenizer;
import com.pmease.commons.lang.StringStream;
import com.pmease.commons.util.StringUtils;

public abstract class AbstractCssTokenizer extends AbstractTokenizer<AbstractCssTokenizer.State> {

	private static Set<String> mediaTypes = Sets.newHashSet(
			"all", "aural", "braille", "handheld", "print", "projection", "screen",
		    "tty", "tv", "embossed");
	
	private static Set<String> mediaFeatures = Sets.newHashSet(
		    "width", "min-width", "max-width", "height", "min-height", "max-height",
		    "device-width", "min-device-width", "max-device-width", "device-height",
		    "min-device-height", "max-device-height", "aspect-ratio",
		    "min-aspect-ratio", "max-aspect-ratio", "device-aspect-ratio",
		    "min-device-aspect-ratio", "max-device-aspect-ratio", "color", "min-color",
		    "max-color", "color-index", "min-color-index", "max-color-index",
		    "monochrome", "min-monochrome", "max-monochrome", "resolution",
		    "min-resolution", "max-resolution", "scan", "grid"
			);
	
	private static Set<String> propertyKeywords = Sets.newHashSet(
		    "align-content", "align-items", "align-self", "alignment-adjust",
		    "alignment-baseline", "anchor-point", "animation", "animation-delay",
		    "animation-direction", "animation-duration", "animation-fill-mode",
		    "animation-iteration-count", "animation-name", "animation-play-state",
		    "animation-timing-function", "appearance", "azimuth", "backface-visibility",
		    "background", "background-attachment", "background-clip", "background-color",
		    "background-image", "background-origin", "background-position",
		    "background-repeat", "background-size", "baseline-shift", "binding",
		    "bleed", "bookmark-label", "bookmark-level", "bookmark-state",
		    "bookmark-target", "border", "border-bottom", "border-bottom-color",
		    "border-bottom-left-radius", "border-bottom-right-radius",
		    "border-bottom-style", "border-bottom-width", "border-collapse",
		    "border-color", "border-image", "border-image-outset",
		    "border-image-repeat", "border-image-slice", "border-image-source",
		    "border-image-width", "border-left", "border-left-color",
		    "border-left-style", "border-left-width", "border-radius", "border-right",
		    "border-right-color", "border-right-style", "border-right-width",
		    "border-spacing", "border-style", "border-top", "border-top-color",
		    "border-top-left-radius", "border-top-right-radius", "border-top-style",
		    "border-top-width", "border-width", "bottom", "box-decoration-break",
		    "box-shadow", "box-sizing", "break-after", "break-before", "break-inside",
		    "caption-side", "clear", "clip", "color", "color-profile", "column-count",
		    "column-fill", "column-gap", "column-rule", "column-rule-color",
		    "column-rule-style", "column-rule-width", "column-span", "column-width",
		    "columns", "content", "counter-increment", "counter-reset", "crop", "cue",
		    "cue-after", "cue-before", "cursor", "direction", "display",
		    "dominant-baseline", "drop-initial-after-adjust",
		    "drop-initial-after-align", "drop-initial-before-adjust",
		    "drop-initial-before-align", "drop-initial-size", "drop-initial-value",
		    "elevation", "empty-cells", "fit", "fit-position", "flex", "flex-basis",
		    "flex-direction", "flex-flow", "flex-grow", "flex-shrink", "flex-wrap",
		    "float", "float-offset", "flow-from", "flow-into", "font", "font-feature-settings",
		    "font-family", "font-kerning", "font-language-override", "font-size", "font-size-adjust",
		    "font-stretch", "font-style", "font-synthesis", "font-variant",
		    "font-variant-alternates", "font-variant-caps", "font-variant-east-asian",
		    "font-variant-ligatures", "font-variant-numeric", "font-variant-position",
		    "font-weight", "grid", "grid-area", "grid-auto-columns", "grid-auto-flow",
		    "grid-auto-position", "grid-auto-rows", "grid-column", "grid-column-end",
		    "grid-column-start", "grid-row", "grid-row-end", "grid-row-start",
		    "grid-template", "grid-template-areas", "grid-template-columns",
		    "grid-template-rows", "hanging-punctuation", "height", "hyphens",
		    "icon", "image-orientation", "image-rendering", "image-resolution",
		    "inline-box-align", "justify-content", "left", "letter-spacing",
		    "line-break", "line-height", "line-stacking", "line-stacking-ruby",
		    "line-stacking-shift", "line-stacking-strategy", "list-style",
		    "list-style-image", "list-style-position", "list-style-type", "margin",
		    "margin-bottom", "margin-left", "margin-right", "margin-top",
		    "marker-offset", "marks", "marquee-direction", "marquee-loop",
		    "marquee-play-count", "marquee-speed", "marquee-style", "max-height",
		    "max-width", "min-height", "min-width", "move-to", "nav-down", "nav-index",
		    "nav-left", "nav-right", "nav-up", "object-fit", "object-position",
		    "opacity", "order", "orphans", "outline",
		    "outline-color", "outline-offset", "outline-style", "outline-width",
		    "overflow", "overflow-style", "overflow-wrap", "overflow-x", "overflow-y",
		    "padding", "padding-bottom", "padding-left", "padding-right", "padding-top",
		    "page", "page-break-after", "page-break-before", "page-break-inside",
		    "page-policy", "pause", "pause-after", "pause-before", "perspective",
		    "perspective-origin", "pitch", "pitch-range", "play-during", "position",
		    "presentation-level", "punctuation-trim", "quotes", "region-break-after",
		    "region-break-before", "region-break-inside", "region-fragment",
		    "rendering-intent", "resize", "rest", "rest-after", "rest-before", "richness",
		    "right", "rotation", "rotation-point", "ruby-align", "ruby-overhang",
		    "ruby-position", "ruby-span", "shape-image-threshold", "shape-inside", "shape-margin",
		    "shape-outside", "size", "speak", "speak-as", "speak-header",
		    "speak-numeral", "speak-punctuation", "speech-rate", "stress", "string-set",
		    "tab-size", "table-layout", "target", "target-name", "target-new",
		    "target-position", "text-align", "text-align-last", "text-decoration",
		    "text-decoration-color", "text-decoration-line", "text-decoration-skip",
		    "text-decoration-style", "text-emphasis", "text-emphasis-color",
		    "text-emphasis-position", "text-emphasis-style", "text-height",
		    "text-indent", "text-justify", "text-outline", "text-overflow", "text-shadow",
		    "text-size-adjust", "text-space-collapse", "text-transform", "text-underline-position",
		    "text-wrap", "top", "transform", "transform-origin", "transform-style",
		    "transition", "transition-delay", "transition-duration",
		    "transition-property", "transition-timing-function", "unicode-bidi",
		    "vertical-align", "visibility", "voice-balance", "voice-duration",
		    "voice-family", "voice-pitch", "voice-range", "voice-rate", "voice-stress",
		    "voice-volume", "volume", "white-space", "widows", "width", "word-break",
		    "word-spacing", "word-wrap", "z-index",
		    // SVG-specific
		    "clip-path", "clip-rule", "mask", "enable-background", "filter", "flood-color",
		    "flood-opacity", "lighting-color", "stop-color", "stop-opacity", "pointer-events",
		    "color-interpolation", "color-interpolation-filters",
		    "color-rendering", "fill", "fill-opacity", "fill-rule", "image-rendering",
		    "marker", "marker-end", "marker-mid", "marker-start", "shape-rendering", "stroke",
		    "stroke-dasharray", "stroke-dashoffset", "stroke-linecap", "stroke-linejoin",
		    "stroke-miterlimit", "stroke-opacity", "stroke-width", "text-rendering",
		    "baseline-shift", "dominant-baseline", "glyph-orientation-horizontal",
		    "glyph-orientation-vertical", "text-anchor", "writing-mode"
			);
	
	private static Set<String> nonStandardPropertyKeywords = Sets.newHashSet(
		    "scrollbar-arrow-color", "scrollbar-base-color", "scrollbar-dark-shadow-color",
		    "scrollbar-face-color", "scrollbar-highlight-color", "scrollbar-shadow-color",
		    "scrollbar-3d-light-color", "scrollbar-track-color", "shape-inside",
		    "searchfield-cancel-button", "searchfield-decoration", "searchfield-results-button",
		    "searchfield-results-decoration", "zoom"
			);

	private static Set<String> colorKeywords = Sets.newHashSet(
		    "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "beige",
		    "bisque", "black", "blanchedalmond", "blue", "blueviolet", "brown",
		    "burlywood", "cadetblue", "chartreuse", "chocolate", "coral", "cornflowerblue",
		    "cornsilk", "crimson", "cyan", "darkblue", "darkcyan", "darkgoldenrod",
		    "darkgray", "darkgreen", "darkkhaki", "darkmagenta", "darkolivegreen",
		    "darkorange", "darkorchid", "darkred", "darksalmon", "darkseagreen",
		    "darkslateblue", "darkslategray", "darkturquoise", "darkviolet",
		    "deeppink", "deepskyblue", "dimgray", "dodgerblue", "firebrick",
		    "floralwhite", "forestgreen", "fuchsia", "gainsboro", "ghostwhite",
		    "gold", "goldenrod", "gray", "grey", "green", "greenyellow", "honeydew",
		    "hotpink", "indianred", "indigo", "ivory", "khaki", "lavender",
		    "lavenderblush", "lawngreen", "lemonchiffon", "lightblue", "lightcoral",
		    "lightcyan", "lightgoldenrodyellow", "lightgray", "lightgreen", "lightpink",
		    "lightsalmon", "lightseagreen", "lightskyblue", "lightslategray",
		    "lightsteelblue", "lightyellow", "lime", "limegreen", "linen", "magenta",
		    "maroon", "mediumaquamarine", "mediumblue", "mediumorchid", "mediumpurple",
		    "mediumseagreen", "mediumslateblue", "mediumspringgreen", "mediumturquoise",
		    "mediumvioletred", "midnightblue", "mintcream", "mistyrose", "moccasin",
		    "navajowhite", "navy", "oldlace", "olive", "olivedrab", "orange", "orangered",
		    "orchid", "palegoldenrod", "palegreen", "paleturquoise", "palevioletred",
		    "papayawhip", "peachpuff", "peru", "pink", "plum", "powderblue",
		    "purple", "rebeccapurple", "red", "rosybrown", "royalblue", "saddlebrown",
		    "salmon", "sandybrown", "seagreen", "seashell", "sienna", "silver", "skyblue",
		    "slateblue", "slategray", "snow", "springgreen", "steelblue", "tan",
		    "teal", "thistle", "tomato", "turquoise", "violet", "wheat", "white",
		    "whitesmoke", "yellow", "yellowgreen"
			);
	
	private static Set<String> valueKeywords = Sets.newHashSet(
		    "above", "absolute", "activeborder", "activecaption", "afar",
		    "after-white-space", "ahead", "alias", "all", "all-scroll", "alternate",
		    "always", "amharic", "amharic-abegede", "antialiased", "appworkspace",
		    "arabic-indic", "armenian", "asterisks", "auto", "avoid", "avoid-column", "avoid-page",
		    "avoid-region", "background", "backwards", "baseline", "below", "bidi-override", "binary",
		    "bengali", "blink", "block", "block-axis", "bold", "bolder", "border", "border-box",
		    "both", "bottom", "break", "break-all", "break-word", "button", "button-bevel",
		    "buttonface", "buttonhighlight", "buttonshadow", "buttontext", "cambodian",
		    "capitalize", "caps-lock-indicator", "caption", "captiontext", "caret",
		    "cell", "center", "checkbox", "circle", "cjk-earthly-branch",
		    "cjk-heavenly-stem", "cjk-ideographic", "clear", "clip", "close-quote",
		    "col-resize", "collapse", "column", "compact", "condensed", "contain", "content",
		    "content-box", "context-menu", "continuous", "copy", "cover", "crop",
		    "cross", "crosshair", "currentcolor", "cursive", "dashed", "decimal",
		    "decimal-leading-zero", "default", "default-button", "destination-atop",
		    "destination-in", "destination-out", "destination-over", "devanagari",
		    "disc", "discard", "document", "dot-dash", "dot-dot-dash", "dotted",
		    "double", "down", "e-resize", "ease", "ease-in", "ease-in-out", "ease-out",
		    "element", "ellipse", "ellipsis", "embed", "end", "ethiopic", "ethiopic-abegede",
		    "ethiopic-abegede-am-et", "ethiopic-abegede-gez", "ethiopic-abegede-ti-er",
		    "ethiopic-abegede-ti-et", "ethiopic-halehame-aa-er",
		    "ethiopic-halehame-aa-et", "ethiopic-halehame-am-et",
		    "ethiopic-halehame-gez", "ethiopic-halehame-om-et",
		    "ethiopic-halehame-sid-et", "ethiopic-halehame-so-et",
		    "ethiopic-halehame-ti-er", "ethiopic-halehame-ti-et",
		    "ethiopic-halehame-tig", "ew-resize", "expanded", "extra-condensed",
		    "extra-expanded", "fantasy", "fast", "fill", "fixed", "flat", "flex", "footnotes",
		    "forwards", "from", "geometricPrecision", "georgian", "graytext", "groove",
		    "gujarati", "gurmukhi", "hand", "hangul", "hangul-consonant", "hebrew",
		    "help", "hidden", "hide", "higher", "highlight", "highlighttext",
		    "hiragana", "hiragana-iroha", "horizontal", "hsl", "hsla", "icon", "ignore",
		    "inactiveborder", "inactivecaption", "inactivecaptiontext", "infinite",
		    "infobackground", "infotext", "inherit", "initial", "inline", "inline-axis",
		    "inline-block", "inline-flex", "inline-table", "inset", "inside", "intrinsic", "invert",
		    "italic", "justify", "kannada", "katakana", "katakana-iroha", "keep-all", "khmer",
		    "landscape", "lao", "large", "larger", "left", "level", "lighter",
		    "line-through", "linear", "lines", "list-item", "listbox", "listitem",
		    "local", "logical", "loud", "lower", "lower-alpha", "lower-armenian",
		    "lower-greek", "lower-hexadecimal", "lower-latin", "lower-norwegian",
		    "lower-roman", "lowercase", "ltr", "malayalam", "match",
		    "media-controls-background", "media-current-time-display",
		    "media-fullscreen-button", "media-mute-button", "media-play-button",
		    "media-return-to-realtime-button", "media-rewind-button",
		    "media-seek-back-button", "media-seek-forward-button", "media-slider",
		    "media-sliderthumb", "media-time-remaining-display", "media-volume-slider",
		    "media-volume-slider-container", "media-volume-sliderthumb", "medium",
		    "menu", "menulist", "menulist-button", "menulist-text",
		    "menulist-textfield", "menutext", "message-box", "middle", "min-intrinsic",
		    "mix", "mongolian", "monospace", "move", "multiple", "myanmar", "n-resize",
		    "narrower", "ne-resize", "nesw-resize", "no-close-quote", "no-drop",
		    "no-open-quote", "no-repeat", "none", "normal", "not-allowed", "nowrap",
		    "ns-resize", "nw-resize", "nwse-resize", "oblique", "octal", "open-quote",
		    "optimizeLegibility", "optimizeSpeed", "oriya", "oromo", "outset",
		    "outside", "outside-shape", "overlay", "overline", "padding", "padding-box",
		    "painted", "page", "paused", "persian", "plus-darker", "plus-lighter", "pointer",
		    "polygon", "portrait", "pre", "pre-line", "pre-wrap", "preserve-3d", "progress", "push-button",
		    "radio", "read-only", "read-write", "read-write-plaintext-only", "rectangle", "region",
		    "relative", "repeat", "repeat-x", "repeat-y", "reset", "reverse", "rgb", "rgba",
		    "ridge", "right", "round", "row-resize", "rtl", "run-in", "running",
		    "s-resize", "sans-serif", "scroll", "scrollbar", "se-resize", "searchfield",
		    "searchfield-cancel-button", "searchfield-decoration",
		    "searchfield-results-button", "searchfield-results-decoration",
		    "semi-condensed", "semi-expanded", "separate", "serif", "show", "sidama",
		    "single", "skip-white-space", "slide", "slider-horizontal",
		    "slider-vertical", "sliderthumb-horizontal", "sliderthumb-vertical", "slow",
		    "small", "small-caps", "small-caption", "smaller", "solid", "somali",
		    "source-atop", "source-in", "source-out", "source-over", "space", "square",
		    "square-button", "start", "static", "status-bar", "stretch", "stroke",
		    "sub", "subpixel-antialiased", "super", "sw-resize", "table",
		    "table-caption", "table-cell", "table-column", "table-column-group",
		    "table-footer-group", "table-header-group", "table-row", "table-row-group",
		    "telugu", "text", "text-bottom", "text-top", "textarea", "textfield", "thai",
		    "thick", "thin", "threeddarkshadow", "threedface", "threedhighlight",
		    "threedlightshadow", "threedshadow", "tibetan", "tigre", "tigrinya-er",
		    "tigrinya-er-abegede", "tigrinya-et", "tigrinya-et-abegede", "to", "top",
		    "transparent", "ultra-condensed", "ultra-expanded", "underline", "up",
		    "upper-alpha", "upper-armenian", "upper-greek", "upper-hexadecimal",
		    "upper-latin", "upper-norwegian", "upper-roman", "uppercase", "urdu", "url",
		    "vertical", "vertical-text", "visible", "visibleFill", "visiblePainted",
		    "visibleStroke", "visual", "w-resize", "wait", "wave", "wider",
		    "window", "windowframe", "windowtext", "x-large", "x-small", "xor",
		    "xx-large", "xx-small"
		    );	
	
	private static Set<String> fontProperties = Sets.newHashSet(
		    "font-family", "src", "unicode-range", "font-variant", "font-feature-settings",
		    "font-stretch", "font-weight", "font-style"
			);
	
	static class State {
		Processor tokenize;
		StateTransition state;
		Context context;
		String override;
		String type;

		State(Processor tokenize, StateTransition state, Context context) {
			this.tokenize = tokenize;
			this.state = state;
			this.context = context;
		}
	}

	static class Context {
		int indent;
		StateTransition type;
		Context prev;

		Context(StateTransition type, int indent, Context prev) {
			this.type = type;
			this.indent = indent;
			this.prev = prev;
		}

	}

	private StateTransition pushContext(State state, StringStream stream, StateTransition type) {
		state.context = new Context(type, stream.indentation() + indentUnit(), state.context);
		return type;
	}

	private StateTransition popContext(State state) {
		state.context = state.context.prev;
	    return state.context.type;
	}
	
	@Override
	protected State startState() {
		return new State(null, new StatesTop(), new Context(new StatesTop(), 0, null));
	}
	
	protected boolean allowNested() {
		return false;
	}

	static interface StateTransition {
		StateTransition transition(String type, StringStream stream, State state);
	}
	
	private static final Pattern STATES_TOP_PATTERN = Pattern.compile("^@(-(moz|ms|o|webkit)-)?keyframes$");
	
	class StatesTop implements StateTransition {

		@Override
		public StateTransition transition(String type, StringStream stream, State state) {
			if (type.equals("{")) {
				return pushContext(state, stream, new StatesBlock());
			} else if (type.equals("}") && state.context.prev != null) {
				return popContext(state);
			} else if (type.equals("@media")) {
				return pushContext(state, stream, new StatesMedia());
			} else if (type.equals("@font-face")) {
				return new StatesFontFaceBefore();
			} else if (STATES_TOP_PATTERN.matcher(type).find()) {
				return new StatesKeyFrames();
			} else if (type.length() != 0 && type.charAt(0) == '@') {
				return pushContext(state, stream, new StatesAt());
			} else if (type.equals("hash")) {
		        state.override = "builtin";
			} else if (type.equals("word")) {
				state.override = "tag";
			} else if (type.equals("variable-definition")) {
				return new StatesMaybeProp();
			} else if (type.equals("interpolation")) {
				return pushContext(state, stream, new StatesInterpolation());
			} else if (type.equals(":")) {
				return new StatesPseudo();
			} else if (allowNested() && type.equals("(")) {
				return pushContext(state, stream, new StatesParens());
			}
			return state.context.type;	
		}
		
	}
	
	private static Pattern STATES_BLOCK_PATTERN = Pattern.compile("^\\s*:(?:\\s|$)"); 
	
	class StatesBlock implements StateTransition {

		@Override
		public StateTransition transition(String type, StringStream stream, State state) {
			if (type.equals("word")) {
				String word = stream.current().toLowerCase();
				if (propertyKeywords.contains(word)) {
					state.override = "property";
					return new StatesMaybeProp();
		        } else if (nonStandardPropertyKeywords.contains(word)) {
		        	state.override = "string-2";
		        	return new StatesMaybeProp();
		        } else if (allowNested()) {
		        	state.override = !stream.match(STATES_BLOCK_PATTERN, false).isEmpty()? "property" : "tag";
		        	return new StatesBlock();
		        } else {
		        	state.override += " error";
		        	return new StatesMaybeProp();
		        }
			} else if (type.equals("meta")) {
				return new StatesBlock();
			} else if (!allowNested() && (type.equals("hash") || type.equals("qualifier"))) {
		        state.override = "error";
		        return new StatesBlock();
			} else {
		        return new StatesTop().transition(type, stream, state);
			}
		}
		
	}
	
	class StatesMaybeProp implements StateTransition {

		@Override
		public StateTransition transition(String type, StringStream stream, State state) {
		    if (type.equals(":")) 
		    	return pushContext(state, stream, new StatesProp());
		    return pass(type, stream, state);
		}
		
	}
	
	private static Pattern STATES_PROP_PATTERN = Pattern.compile("^#([0-9a-fA-f]{3}|[0-9a-fA-f]{6})$");
	
	class StatesProp implements StateTransition {

		@Override
		public StateTransition transition(String type, StringStream stream, State state) {
		    if (type.equals(";")) 
		    	return popContext(state);
		    if (type.equals("{") && allowNested()) 
		    	return pushContext(state, stream, new StatesPropBlock());
		    if (type.equals("}") || type.equals("{")) 
		    	return popAndPass(type, stream, state);
		    if (type.equals("(")) 
		    	return pushContext(state, stream, new StatesParens());

		    if (type.equals("hash") && !STATES_PROP_PATTERN.matcher(stream.current()).find()) {
		    	state.override += " error";
		    } else if (type.equals("word")) {
		    	wordAsValue(stream, state);
		    } else if (type.equals("interpolation")) {
		    	return pushContext(state, stream, new StatesInterpolation());
		    }
		    return new StatesProp();
		}
		
	}
	
	class StatesPropBlock implements StateTransition {

		@Override
		public StateTransition transition(String type, StringStream stream, State state) {
		    if (type.equals("}")) 
		    	return popContext(state);
		    if (type.equals("word")) { 
		    	state.override = "property"; 
		    	return new StatesMaybeProp(); 
		    }
		    return state.context.type;
		}
		
	}
	
	class StatesParens implements StateTransition {

		@Override
		public StateTransition transition(String type, StringStream stream, State state) {
		    if (type.equals("{") || type.equals("}")) 
		    	return popAndPass(type, stream, state);
		    if (type.equals(")")) 
		    	return popContext(state);
		    if (type.equals("(")) 
		    	return pushContext(state, stream, new StatesParens());
		    if (type.equals("word")) 
		    	wordAsValue(stream, state);
		    return new StatesParens();
		}
		
	}
	
	class StatesPseudo implements StateTransition {

		@Override
		public StateTransition transition(String type, StringStream stream, State state) {
		    if (type.equals("word")) {
		        state.override = "variable-3";
		        return state.context.type;
		    }
		    return pass(type, stream, state);
		}
		
	}
	
	class StatesMedia implements StateTransition {

		@Override
		public StateTransition transition(String type, StringStream stream, State state) {
		    if (type.equals("(")) 
		    	return pushContext(state, stream, new StatesMediaParens());
		    if (type.equals("}")) 
		    	return popAndPass(type, stream, state);
		    if (type.equals("{")) {
		    	StateTransition transition = popContext(state);
		    	if (transition != null) 
		    		return pushContext(state, stream, allowNested()? new StatesBlock() : new StatesTop());
		    	else
		    		return null;
		    }

		    if (type.equals("word")) {
		    	String word = stream.current().toLowerCase();
		    	if (word.equals("only") || word.equals("not") || word.equals("and"))
		    		state.override = "keyword";
		      else if (mediaTypes.contains(word))
		        state.override = "attribute";
		      else if (mediaFeatures.contains(word))
		        state.override = "property";
		      else
		        state.override = "error";
		    }
		    return state.context.type;
		}
		
	}
	
	class StatesMediaParens implements StateTransition {

		@Override
		public StateTransition transition(String type, StringStream stream, State state) {
		    if (type.equals(")")) 
		    	return popContext(state);
		    if (type.equals("{") || type.equals("}")) 
		    	return popAndPass(type, stream, state, 2);
		    return new StatesMedia().transition(type, stream, state);
		}
		
	}
	
	class StatesFontFaceBefore implements StateTransition {

		@Override
		public StateTransition transition(String type, StringStream stream, State state) {
		    if (type.equals("{"))
		    	return pushContext(state, stream, new StatesFontFace());
		    return pass(type, stream, state);
		}
		
	}
	
	class StatesFontFace implements StateTransition {

		@Override
		public StateTransition transition(String type, StringStream stream, State state) {
		    if (type.equals("}")) 
		    	return popContext(state);
		    if (type.equals("word")) {
		    	if (!fontProperties.contains(stream.current().toLowerCase()))
		    		state.override = "error";
		    	else
		    		state.override = "property";
		    	return new StatesMaybeProp();
		    }
		    return new StatesFontFace();
		}
		
	}
	
	class StatesKeyFrames implements StateTransition {

		@Override
		public StateTransition transition(String type, StringStream stream, State state) {
		    if (type.equals("word")) { 
		    	state.override = "variable"; 
		    	return new StatesKeyFrames();
		    }
		    if (type.equals("{")) 
		    	return pushContext(state, stream, new StatesTop());
		    return pass(type, stream, state);
		}
		
	}
	
	class StatesAt implements StateTransition {

		@Override
		public StateTransition transition(String type, StringStream stream, State state) {
		    if (type.equals(";")) 
		    	return popContext(state);
		    if (type.equals("{") || type.equals("}")) 
		    	return popAndPass(type, stream, state);
		    if (type.equals("word")) 
		    	state.override = "tag";
		    else if (type.equals("hash")) 
		    	state.override = "builtin";
		    return new StatesAt();
		}
		
	}
	
	class StatesInterpolation implements StateTransition {

		@Override
		public StateTransition transition(String type, StringStream stream, State state) {
		    if (type.equals("}")) 
		    	return popContext(state);
		    if (type.equals("{") || type.equals(";")) 
		    	return popAndPass(type, stream, state);
		    if (!type.equals("variable")) 
		    	state.override = "error";
		    return new StatesInterpolation();
		}
		
	}
	
	StateTransition pass(String type, StringStream stream, State state) {
		return state.context.type.transition(type, stream, state);
	}

	StateTransition popAndPass(String type, StringStream stream, State state) {
		return popAndPass(type, stream, state, 1);
	}
	
	StateTransition popAndPass(String type, StringStream stream, State state, int n) {
	    for (int i = n; i > 0; i--)
	    	state.context = state.context.prev;
	    return pass(type, stream, state);
	}

	void wordAsValue(StringStream stream, State state) {
		String word = stream.current().toLowerCase();
	    if (valueKeywords.contains(word))
	    	state.override = "atom";
	    else if (colorKeywords.contains(word))
	    	state.override = "keyword";
	    else
	    	state.override = "variable";
	}
	
	static interface Processor {
		String process(StringStream stream, State state);
	}

	@Override
	protected String token(StringStream stream, State state) {
		if (state.tokenize == null && stream.eatSpace()) 
			return "";
		String style;
		if (state.tokenize != null)
			style = state.tokenize.process(stream, state);
		else
			style = tokenBase(stream, state);

		if (style.contains(" ")) {
			state.type = StringUtils.substringAfter(style, " ");
			style = StringUtils.substringBefore(style, " ");
		}
		state.override = style;
		state.state = state.state.transition(state.type, stream, state);
		return state.override;
	}
	
	String ret(String style, String tp, State state) { 
		state.type = tp; 
		return style; 
	}
	
	private static Pattern TOKEN_BASE_PATTERN1 = Pattern.compile("[\\w\\\\\\-]");
	
	private static Pattern TOKEN_BASE_PATTERN2 = Pattern.compile("^\\s*\\w*");
	
	private static Pattern TOKEN_BASE_PATTERN3 = Pattern.compile("\\d");
	
	private static Pattern TOKEN_BASE_PATTERN4 = Pattern.compile("[\\w.%]");
	
	private static Pattern TOKEN_BASE_PATTERN5 = Pattern.compile("[\\d.]");
	
	private static Pattern TOKEN_BASE_PATTERN6 = Pattern.compile("^\\w+-");
	
	private static Pattern TOKEN_BASE_PATTERN7 = Pattern.compile("[,+>*\\/]");
	
	private static Pattern TOKEN_BASE_PATTERN8 = Pattern.compile("^-?[_a-z][_a-z0-9-]*", Pattern.CASE_INSENSITIVE);
	
	private static Pattern TOKEN_BASE_PATTERN9 = Pattern.compile("[:;{}\\[\\]\\(\\)]");
	
	private static Pattern TOKEN_BASE_PATTERN10 = Pattern.compile("[\\w\\\\\\-]");
	
	String tokenBase(StringStream stream, State state) {
		String ch = stream.next();
	    if (tokenHooks().containsKey(ch)) {
	    	String result = tokenHooks().get(ch).process(stream, state);
	    	if (result.length() != 0) 
	    		return result;
	    }
	    if (ch.equals("@")) {
	    	stream.eatWhile(TOKEN_BASE_PATTERN1);
	    	return ret("def", stream.current(), state);
	    } else if (ch.equals("=") || (ch.equals("~") || ch.equals("|")) && stream.eat("=").length()!=0) {
	    	return ret("", "compare", state);
	    } else if (ch.equals("\"") || ch.equals("'")) {
	    	state.tokenize = new TokenString(ch);
	    	return state.tokenize.process(stream, state);
	    } else if (ch.equals("#")) {
	    	stream.eatWhile(TOKEN_BASE_PATTERN1);
	    	return ret("atom", "hash", state);
	    } else if (ch.equals("!")) {
	    	stream.match(TOKEN_BASE_PATTERN2);
	    	return ret("keyword", "important", state);
	    } else if (TOKEN_BASE_PATTERN3.matcher(ch).find() || ch.equals(".") && stream.eat(TOKEN_BASE_PATTERN3).length()!=0) {
	    	stream.eatWhile(TOKEN_BASE_PATTERN4);
	    	return ret("number", "unit", state);
	    } else if (ch.equals("-")) {
	    	if (TOKEN_BASE_PATTERN5.matcher(stream.peek()).find()) {
	    		stream.eatWhile(TOKEN_BASE_PATTERN4);
		    	return ret("number", "unit", state);
	    	} else if (!stream.match(TOKEN_BASE_PATTERN6).isEmpty()) {
	    		return ret("meta", "meta", state);
	    	}
	    } else if (TOKEN_BASE_PATTERN7.matcher(ch).find()) {
	    	return ret("", "select-op", state);
	    } else if (ch.equals(".") && !stream.match(TOKEN_BASE_PATTERN8).isEmpty()) {
	    	return ret("qualifier", "qualifier", state);
	    } else if (TOKEN_BASE_PATTERN9.matcher(ch).find()) {
	    	return ret("", ch, state);
	    } else if (ch.equals("u") && stream.match("rl(")) {
	    	stream.backUp(1);
	    	state.tokenize = new TokenParenthesized();
	    	return ret("property", "word", state);
	    } else if (TOKEN_BASE_PATTERN10.matcher(ch).find()) {
	    	stream.eatWhile(TOKEN_BASE_PATTERN10);
	    	return ret("property", "word", state);
	    } else {
	    	return ret("", "", state);
	    }
	    return "";
	}
	
	class TokenString implements Processor {

		private final String quote;
		
		public TokenString(String quote) {
			this.quote = quote;
		}
		
		@Override
		public String process(StringStream stream, State state) {
			boolean escaped = false;
			String ch;
			while ((ch = stream.next()).length() != 0) {
				if (ch.equals(quote) && !escaped) {
					if (quote.equals(")")) 
						stream.backUp(1);
					break;
		        }
				escaped = !escaped && ch.equals("\\");
			}
		    if (ch.equals(quote) || !escaped && !quote.equals(")")) 
		    	state.tokenize = null;
		    return ret("string", "string", state);
		}
		
	}
	
	private static Pattern TOKEN_PARENTHESIZED_PATTERN = Pattern.compile("\\s*[\\\"\\')]");
			
	class TokenParenthesized implements Processor {

		@Override
		public String process(StringStream stream, State state) {
		    stream.next(); // Must be '('
		    if (stream.match(TOKEN_PARENTHESIZED_PATTERN, false).isEmpty())
		    	state.tokenize = new TokenString(")");
		    else
		    	state.tokenize = null;
		    return ret("", "(", state);
		}
		
	}

	class TokenCComment implements Processor {

		@Override
		public String process(StringStream stream, State state) {
		    boolean maybeEnd = false;
		    String ch;
		    while ((ch = stream.next()).length() != 0) {
		    	if (maybeEnd && ch.equals("/")) {
		    		state.tokenize = null;
		    		break;
		    	}
		    	maybeEnd = (ch.equals("*"));
		    }
		    return "comment comment";
		}
		
	}
	
	class TokenSGMLComment implements Processor {

		@Override
		public String process(StringStream stream, State state) {
		    if (stream.skipTo("-->")) {
		    	stream.match("-->");
		    	state.tokenize = null;
		    } else {
		        stream.skipToEnd();
		    }
		    return "comment comment";
		}
		
	}
	
	protected abstract Map<String, Processor> tokenHooks();
}
