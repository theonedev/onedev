onedev.server.onQuickSearchDomReady = function(containerId, callback) {
	var $body = $("#" + containerId + ">.quick-search>.modal-body");
	
	var $input = $body.children("input");
	
	$input.doneEvents("inputchange", function() {
		callback("input", $(this).val());
	}, 100);

	function onReturn() {
		if (onedev.server.form.confirmLeave()) {
			var $result = $body.children(".result");
			var $active = $result.find("li.hit.active");
			if ($active.length != 0) {
				callback("return", $active.index());
			}
		}
	}
	
	function onKeyup(e) {
		e.preventDefault();
		var $result = $body.children(".result");
		var $active = $result.find("li.hit.active");
		var $prev = $active.prev("li.hit");
		if ($prev.length != 0) {
			$active.removeClass("active");
			$prev.addClass("active");
		} 
		$result.find("li.hit.active").scrollIntoView();
	};
	
	function onKeydown(e) {
		e.preventDefault();
		var $result = $body.children(".result");
		var $active = $result.find("li.hit.active");
		var $next = $active.next("li.hit");
		if ($next.length != 0) {
			$active.removeClass("active");
			$next.addClass("active");
		} 
		$result.find("li.hit.active").scrollIntoView();
	};
	
	$body.children().bind("keydown", "return", onReturn);
	$body.children().bind("keydown", "up", onKeyup);
	$body.children().bind("keydown", "down", onKeydown);
	
};