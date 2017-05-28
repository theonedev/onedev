gitplex.server.blobEdit = {
	onDomReady: function(containerId) {
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
	    
	    if ($body.find(".autofit").length != 0)
	    	$body.css("overflow", "visible");
	    
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
			$body.children(".content").show();
		} else {
			$body.children(".commit-options").show();
		}
		if ($body.find(".autofit:visible").length != 0)
			$body.css("overflow", "visible");
		else
			$body.css("overflow", "auto");
		
		if ($tab.hasClass("edit")) {
			$body.children(".content").show();
		} else {
			$body.children(".commit-options").show();
		}
	},
	checkClean: function(containerId, recreateCallback) {
		if ($("#" + containerId + ">.blob-edit>.body form.dirty").length == 0)
			recreateCallback();
	}
};
