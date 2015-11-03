pmease.commons.dropdownhover = {
	init: function(triggerId, hoverDelay, openCallback) {
		var $trigger = $("#" + triggerId);
		$trigger.addClass("dropdown-hover");
		
		function hide() {
			pmease.commons.floating.close($("body>.floating"), true);
			$trigger.data("hideTimer", null);
		}
		function prepareToHide() {
			if ($trigger.data("hideTimer")) 
				clearTimeout($trigger.data("hideTimer"));
			$trigger.data("hideTimer", setTimeout(function(){
				if ($trigger.hasClass("dropdown-on"))
					hide();
			}, hoverDelay));
		}
		function cancelHide() {
			if ($trigger.data("hideTimer")) {
				clearTimeout($trigger.data("hideTimer"));
				$trigger.data("hideTimer", null);				
			} 
		}
		function cancelShow() {
			if ($trigger.data("showTimer")) {
				clearTimeout($trigger.data("showTimer"));
				$trigger.data("showTimer", null);
			}
		}
		$trigger.mouseover(function(mouse) {
			if (!$trigger.data("showTimer")) {
				$trigger.data("showTimer", setTimeout(function() {
					if (!$trigger.hasClass("dropdown-on")) {
						openCallback();
						cancelHide();
					}
					$trigger.data("showTimer", null);
				}, hoverDelay));
			}
		});
		dropdown.mouseover(function() {
			cancelHide();
		});
		$trigger.mouseout(function() {
			prepareToHide();
			cancelShow();
		});
		$trigger.mousemove(function() {
			cancelHide();
		});
		dropdown.mouseout(function(event) {
			if (event.pageX<dropdown.offset().left+5 || event.pageX>dropdown.offset().left+dropdown.width()-5 
					|| event.pageY<dropdown.offset().top+5 || event.pageY>dropdown.offset().top+dropdown.height()-5) {
				prepareToHide();
			}
		});		
	}, 
	opened: function(triggerId, dropdownId) {
		
	}
}