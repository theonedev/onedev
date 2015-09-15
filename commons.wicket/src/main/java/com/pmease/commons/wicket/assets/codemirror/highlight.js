pmease.commons.highlight = function($container) {
	$container.find("pre>code").each(function() {
		var $this = $(this);
		$this.addClass("highlight");
		var text = $this.text();
		$this.empty();
		var cm = CodeMirror(this, {
			readOnly: "nocursor",
			value: text, 
			theme: "eclipse",
			lineNumbers: true,
			lineWrapping: true,
			matchBrackets: true
		});
	});
}