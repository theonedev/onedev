var SourceHighLight = SourceHighLight || {};

SourceHighLight.highlight = function($el) {
	hljs.highlightBlock($el[0], '    ', false)
}