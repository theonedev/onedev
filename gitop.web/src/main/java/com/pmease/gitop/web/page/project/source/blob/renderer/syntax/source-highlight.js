var SourceHighLight = SourceHighLight || {};

SourceHighLight.highlight = function($el) {
	if ($el.text() == '' || $el.text() === undefined) {
		$el.html('<b>This file is empty</b>')
	} else if ($el.data('lang') !== undefined) {
		hljs.highlightBlock($el[0], '    ', false)
	}
}