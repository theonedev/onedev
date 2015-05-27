gitplex.copyClipboard = function(bridgeId) {
	var $bridge = $('#' + bridgeId);
	var client = new ZeroClipboard($bridge);
	client.on('ready', function(e) {
		client.on('aftercopy', function(e) {
			$bridge.attr('title', 'Copied to clipboard!');
			$bridge.tooltip('destroy').tooltip('show');
		});
	});
	$bridge.mouseout(function() {
		$bridge.attr('title', 'Click to copy');
		$bridge.tooltip('destroy').tooltip();
	});
	$bridge.tooltip();
}