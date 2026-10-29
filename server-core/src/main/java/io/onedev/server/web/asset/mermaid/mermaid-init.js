var mermaidTheme = onedev.server.isDarkMode()? "dark": "default";
mermaid.mermaidAPI.initialize({theme: mermaidTheme, themeVariables: {fontSize: "12px"}, startOnLoad:false});

onedev.server.mermaid = {
	_queue: Promise.resolve(),
	render: function($elements) {
		var self = this;
		$elements.each(function() {
			var element = this;
			// Run one diagram at a time so errors can be displayed beside their source.
			self._queue = self._queue.then(function() {
				if (!element.isConnected)
					return;
				return mermaid.run({
					nodes: [element],
					postRenderCallback: function(id) {
						var svg = document.getElementById(id);
						if (svg && svg.viewBox.baseVal.width > 0) {
							// Keep labels readable instead of shrinking wide diagrams to fit the page.
							$(svg).css({width: svg.viewBox.baseVal.width, maxWidth: "none", height: "auto"});
						}
					}
				}).catch(function(error) {
					var $summary = $(element).find("svg").has(".error-icon").addClass("mermaid-error-summary");
					// Mermaid's error icon occupies the first 512 units of the SVG.
					$summary.find(".error-text").attr("x", 600).css("text-anchor", "start");
					$(element).append($("<pre class='mermaid-error'></pre>").text(error.message || String(error)));
				});
			});
		});
	}
};
