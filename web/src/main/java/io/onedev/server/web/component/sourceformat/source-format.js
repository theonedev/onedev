onedev.server.sourceFormat = {
	init: function(containerId) {
		var $container = $("#" + containerId);

		$container.find(".indent-type>select").prepend("<optgroup label='Indent type'></optgroup>");
		$container.find(".tab-size>select").prepend("<optgroup label='Tab size'></optgroup>");
		$container.find(".line-wrap-mode>select").prepend("<optgroup label='Line wrap mode'></optgroup>");
	}
};