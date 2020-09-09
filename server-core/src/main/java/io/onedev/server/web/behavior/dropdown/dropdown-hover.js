onedev.server.dropdownHover = {
	onDomReady: function(triggerId, hoverDelay, openCallback) {
		var $trigger = $("#" + triggerId);
		$trigger.addClass("dropdown-hover");
		
		$trigger.data("hide", function() {
			if ($trigger.data("dropdown"))
				$trigger.data("dropdown").data("closeCallback")();
			$trigger.data("hideTimer", null);
		});
		$trigger.data("prepareToHide", function() {
			if ($trigger.data("hideTimer")) 
				clearTimeout($trigger.data("hideTimer"));
			$trigger.data("hideTimer", setTimeout(function(){
				if ($trigger.hasClass("dropdown-open"))
					$trigger.data("hide")();
			}, hoverDelay));
		});
		$trigger.data("cancelHide", function() {
			if ($trigger.data("hideTimer")) {
				clearTimeout($trigger.data("hideTimer"));
				$trigger.data("hideTimer", null);				
			} 
		});
		$trigger.data("cancelShow", function() {
			if ($trigger.data("showTimer")) {
				clearTimeout($trigger.data("showTimer"));
				$trigger.data("showTimer", null);
			}
		});
		$trigger.mouseover(function(mouse) {
			if (!$trigger.data("showTimer")) {
				$trigger.data("showTimer", setTimeout(function() {
					if (!$trigger.hasClass("dropdown-open")) {
						openCallback();
						$trigger.data("cancelHide")();
					}
					$trigger.data("showTimer", null);
				}, hoverDelay));
			}
		});
		$trigger.mouseout(function() {
			$trigger.data("prepareToHide")();
			$trigger.data("cancelShow")();
		});
		$trigger.mousemove(function() {
			$trigger.data("cancelHide")();
		});
	}, 
	opened: function(triggerId, dropdownId) {
		var $dropdown = $("#" + dropdownId);
		var $trigger = $("#" + triggerId);
		$trigger.addClass("dropdown-open");
		$trigger.data("dropdown", $dropdown);
		$dropdown.mouseover(function() {
			$trigger.data("cancelHide")();
		});
		$dropdown.mouseout(function(event) {
			if (event.pageX<$dropdown.offset().left+5 || event.pageX>$dropdown.offset().left+$dropdown.width()-5 
					|| event.pageY<$dropdown.offset().top+5 || event.pageY>$dropdown.offset().top+$dropdown.height()-5) {
				$trigger.data("prepareToHide")();
			}
		});		
	},
	closed: function(triggerId, dropdownId) {
		var $trigger = $("#" + triggerId);
		$trigger.removeClass("dropdown-open");
		$trigger.data("dropdown", null);
	}
};