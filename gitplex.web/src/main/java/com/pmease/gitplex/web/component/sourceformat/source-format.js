gitplex.sourceFormat = {
	init: function(containerId) {
		var $container = $("#" + containerId);

		$container.find(".indent-type").prepend("<optgroup label='Indent type'></optgroup>");
		$container.find(".tab-size").prepend("<optgroup label='Tab size'></optgroup>");
		$container.find(".line-wrap-mode").prepend("<optgroup label='Line wrap mode'></optgroup>");
	}
};