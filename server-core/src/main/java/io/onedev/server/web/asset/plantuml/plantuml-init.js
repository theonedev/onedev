onedev.server.plantuml = {
	_module: null,
	_queue: Promise.resolve(),
	_load: function() {
		if (!this._module) {
			this._module = import(onedev.server.plantumlModuleUrl);
		}
		return this._module;
	},
	_sourceLines: function(source) {
		source = source.replace(/^\n+|\n+$/g, "");
		if (!/^\s*@start/i.test(source))
			source = "@startuml\n" + source + "\n@enduml";
		return source.split(/\r\n|\r|\n/);
	},
	render: function($elements) {
		var self = this;
		var dark = onedev.server.isDarkMode();
		$elements.each(function() {
			var el = this;
			var lines = self._sourceLines($(el).text());
			self._queue = self._queue.then(function() {
				return self._load().then(function(mod) {
					return new Promise(function(resolve) {
						mod.renderToString(lines, function(svg) {
							$(el).html(svg);
							resolve();
						}, function(message) {
							$(el).empty().append($("<div class='plantuml-error'></div>").text(message || "PlantUML rendering failed"));
							resolve();
						}, {dark: dark});
					});
				}).catch(function(err) {
					$(el).empty().append($("<div class='plantuml-error'></div>").text(err && err.message ? err.message : String(err)));
				});
			});
		});
	}
};
