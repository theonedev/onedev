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
    		onedev.server.viewState.getFromViewAndSetToHistory();
	    	$body.find(">.content>.save.submit").click();
	    });
	    
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
		
        $container.mouseover(function() {
            /* 
             * While inputting file names of a new file, the blob content will be re-created to 
             * use appropriate blob editor for current file name. In such case we add "no-autofocus"
             * class to the blob content container in order not to jump to blob content area while 
             * inputting file name. However we want to remove this class to get better user 
             * experience (for instance when we are editing the build specs) when we are ready to edit 
             * the blob content by moving mouse into blob edit area
             */
            $container.closest(".no-autofocus").removeClass("no-autofocus");
        });

	},
	selectTab: function($tab) {
		var $active = $tab.parent().find(".tab.active");
    	$active.removeClass("active");
    	$tab.addClass("active");
    	var $blobEdit = $tab.closest(".blob-edit");
    	var $body = $blobEdit.children(".body");
    	
		$body.children().removeClass("d-flex");

        var $content = $body.children(".content");
		if ($tab.hasClass("edit") || $tab.hasClass("edit-plain")) {
			$content.addClass("d-flex");
		} else {
			$body.children(".commit-options").addClass("d-flex");
        }
		
		$(window).resize();
        
        if (!$tab.hasClass("save"))
        	onedev.server.viewState.getFromHistoryAndSetToView();        	
	},
	recordFormFlags: function(formId) {
		var $form = $("#" + formId);
		if ($form.hasClass("dirty"))
			$form.parent().addClass("dirty");
		else
			$form.parent().removeClass("dirty");
		$form.parent().data("autosaveKey", $form.data("autosaveKey"));
	},
	restoreFormFlags: function(formId) {
		var $form = $("#" + formId);
		if ($form.parent().hasClass("dirty"))
			$form.addClass("dirty");
		else
			$form.removeClass("dirty");
		$form.data("autosaveKey", $form.parent().data("autosaveKey"));
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
