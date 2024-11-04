onedev.server.secretEditor = {
	onDomReady: function(containerId) {
		function mask(text) {
			return text.replace(/[^\r\n]/g, '*');
		}
		var $container = $("#" + containerId);
		var $input1 = $container.children("textarea:first-child");
		var $input2 = $container.children("textarea:last-child");
		var $toggle = $container.children("a");
		if ($input1.hasClass("masked"))
			$input1.val(mask($input2.val()));
		else
			$input1.val($input2.val());
			
		if ($input1.val().length != 0)
			$toggle.show();
		var lastRange;
		$input1.on("beforeinput", function() {
			lastRange = $input1.range();
		});
		$input1.on('input', function() {
			if ($input1.hasClass("masked")) {
				var range = $input1.range();
				if (range.start >= lastRange.start) {
					$input2.range(lastRange.start, lastRange.end);
					$input2.range($input1.val().substring(lastRange.start, range.start));
				} else {
					$input2.range(range.start, lastRange.start);
					$input2.range("");
				}
				$input1.val(mask($input2.val()));
				$input1.range(range.start, range.end);
			} else {
				$input2.val($input1.val());
			}
			if ($input1.val().length != 0)
				$toggle.show();
			else
				$toggle.hide();
			$input2.trigger("input");
		});
	}
}