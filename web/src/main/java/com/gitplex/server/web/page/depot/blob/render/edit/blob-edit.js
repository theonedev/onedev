gitplex.server.blobEdit = {
	init: function(containerId) {
		var $container = $("#" + containerId);
		var $blobEdit = $container.children(".blob-edit");
		var $head = $blobEdit.children(".head");
		var $body = $blobEdit.children(".body");
		var $content = $body.children(".content");
		
	    $head.find(".edit>a").click(function() {
	    	gitplex.server.blobEdit.selectTab($(this).parent());
	    });
	    $head.find(".save>a").click(function() {
	    	$body.children(".content").children(".submit").click();
	    });
	    
	    $blobEdit.on("getViewState", function(e) {
	    	if ($content.is(":visible"))
	    		return {scroll:{left: $body.scrollLeft(), top: $body.scrollTop()}};			
	    	else
	    		return undefined;
		});
		
	    $blobEdit.on("setViewState", function(e, viewState) {
			if ($content.is(":visible") && viewState.scroll) {
				$body.scrollLeft(viewState.scroll.left);
				$body.scrollTop(viewState.scroll.top);
			}
		});
		
	    $blobEdit.on("autofit", function(e, width, height) {
			$blobEdit.outerWidth(width);
			$blobEdit.outerHeight(height);
			height = $blobEdit.height()-$head.outerHeight();
			$body.outerWidth(width).outerHeight(height);
			$body.find(".autofit:visible").first().triggerHandler("autofit", [$body.width(), $body.height()]);
		});
	    
	},
	selectTab: function($tab) {
		gitplex.server.viewState.getFromViewAndSetToHistory();
		
    	var $active = $tab.parent().find(".tab.active");
    	$active.removeClass("active");
    	$tab.addClass("active");
    	var $blobEdit = $tab.closest(".blob-edit");
    	var $body = $blobEdit.children(".body");
    	
		$body.children().hide();
		if ($tab.hasClass("edit")) {
			$body.children(".content").show().find(".autofit:visible").first().triggerHandler("show");
		} else {
			/*
			 * Show scroll bar in case it is hidden in edit tab by sub class
			 */
			$body.css("overflow", "auto");
			$body.children(".commit-options").show();
		}
	},
	checkClean: function(containerId, recreateCallback) {
		if ($("#" + containerId + ">.blob-edit>.body form.dirty").length == 0)
			recreateCallback();
	}
};
