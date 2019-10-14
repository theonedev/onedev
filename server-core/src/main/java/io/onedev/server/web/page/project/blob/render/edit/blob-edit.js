onedev.server.blobEdit = {
	onDomReady: function(containerId) {
		var $container = $("#" + containerId);
		var $blobEdit = $container.children(".blob-edit");
		var $head = $blobEdit.children(".head");
		var $body = $blobEdit.children(".body");
		
	    $head.find(".edit>a").click(function() {
            if (!$(this).parent().hasClass("active")) {
        		$body.find(">.content>.edit.submit").click();
            }
	    });
	    $head.find(".edit-plain>a").click(function() {
            if (!$(this).parent().hasClass("active")) {
        		$body.find(">.content>.edit-plain.submit").click();
            }
	    });
	    $head.find(".save>a").click(function() {
	    	var $content = $body.find(">.content");
	    	var $positionAware = $content.find(".position-aware");
	    	if ($positionAware.length != 0) {
		    	var position = $positionAware.data("getPosition")();
		    	$content.children(".position").val(position);
	    	}
        	$content.children(".save.submit").click();
	    });

	    if ($body.find(".autofit:visible").length != 0)
	    	$body.css("overflow", "visible");
	    
	    $blobEdit.on("getViewState", function(e) {
        	var $content = $body.children(".content");
	    	if ($content.is(":visible"))
	    		return {scroll:{left: $body.scrollLeft(), top: $body.scrollTop()}};			
	    	else
	    		return undefined;
		});
		
	    $blobEdit.on("setViewState", function(e, viewState) {
        	var $content = $body.children(".content");
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
        
        $container.mouseover(function() {
            /* 
             * While inputting file names of a new file, the blob content will be re-created to 
             * use appropriate blob editor for current file name. In such case we add "no-autofocus"
             * class to the blob content container in order not to jump to blob content area while 
             * inputting file name. However we want to remove this class to get better user 
             * experience (for instance when we are editing the CI specs) when we are ready to edit 
             * the blob content by moving mouse into blob edit area
             */
            $container.closest(".no-autofocus").removeClass("no-autofocus");
        });

	},
	selectTab: function($tab) {
		onedev.server.viewState.getFromViewAndSetToHistory();
        
    	var $active = $tab.parent().find(".tab.active");
    	$active.removeClass("active");
    	$tab.addClass("active");
    	var $blobEdit = $tab.closest(".blob-edit");
    	var $body = $blobEdit.children(".body");
    	
		$body.children().hide();

		if ($body.find(".autofit:visible").length != 0)
			$body.css("overflow", "visible");
		else
			$body.css("overflow", "auto");
        
        var $content = $body.children(".content");
		if ($tab.hasClass("edit") || $tab.hasClass("edit-plain")) {
			$content.show();
		} else {
			$body.children(".commit-options").show();
        }

        $(window).resize();
	},
	onNameChanging: function(containerId, addingFile, recreateCallback) {
		var $body = $("#" + containerId + ">.blob-edit>.body");
		var contentModified = $body.find("form.dirty").length != 0;
		if (addingFile && !contentModified)
			recreateCallback();
		else
			$body.find(".name-changing-listener").trigger("nameChanging");
	}
};
