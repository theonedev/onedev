/*
 * 2022, Robin Shen
 */
(function ( $ ) {
 
    $.fn.selectByTyping = function(container) {
    	var $input = jQuery(this);

		function onReturn() {
			$(container).find(".active.selectable").find("a").addBack("a").click();
		}
		
		function onTab() {
			var $container = $(container);
			function complete() {
				var completion = $container.find(".active.selectable").data("completion");
				if (completion == undefined)
					completion = $container.find(".active.selectable").find("a").addBack("a").data("completion");
				if (completion != undefined) {
					$input.val(completion);
					$input.trigger("input");
				}
			}
			var processedInput = $container.data("input");
			if (processedInput != undefined) { 
				if (processedInput != $input.val()) 
					setTimeout(onTab, 100);				
				else 
					complete();
			} else {
				complete();				
			}
			return false;
		}
		
		function onKeyup(e) {
			var $container = $(container);
			e.preventDefault();
			var $active = $container.find(".active.selectable");
			var $selectables = $container.find(".selectable");
			var index = $selectables.index($active);
			if (index > 0) {
				index--;
				var $prev = $selectables.eq(index);
				$active.removeClass("active");
				$prev.addClass("active");
				$prev[0].scrollIntoViewIfNeeded(false);
			}
		};
		
		function onKeydown(e) {
			var $container = $(container);
			e.preventDefault();
			var $active = $container.find(".active.selectable");
			var $selectables = $container.find(".selectable");
			var index = $selectables.index($active);
			if (index < $selectables.length-1) {
				index++;
				var $next = $selectables.eq(index);
				$active.removeClass("active");
				$next.addClass("active");
				$next[0].scrollIntoViewIfNeeded(false);
			}
		};
		
		$input.bind("keydown", "return", onReturn)
				.bind("keydown", "up", onKeyup)
				.bind("keydown", "down", onKeydown)
				.bind("keydown", "tab", onTab);		    	
		
    	return this;
    };
 
}( jQuery ));
