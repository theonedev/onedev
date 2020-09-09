onedev.server.buildSpec = {
    onDomReady: function(selection, selectCallback, deleteCallback) {
        var $valid = $(".build-spec>.valid");
        
        $valid.data("selectCallback", selectCallback);
        $valid.data("deleteCallback", deleteCallback);

        var $body = $valid.children(".body");
        var $jobs = $body.children(".jobs");
        var $properties = $body.children(".properties");
        if ($jobs.find(".feedbackPanelERROR").length != 0) {
            onedev.server.buildSpec.showJobs();
            $jobs.find(">.contents>.content").each(function() {
                var $this = $(this);
                if ($this.find(".feedbackPanelERROR").length != 0) {
                    onedev.server.buildSpec.showJob($this.index());
                    return false;
                }
            });
        } else if ($properties.find(".feedbackPanelERROR").length != 0) {
            onedev.server.buildSpec.showProperties();
        } else if (selection) {
        	onedev.server.buildSpec.showSelection(selection);
        } else {
            onedev.server.buildSpec.showJobs();
            onedev.server.buildSpec.showJob(0);
        }

        $body.data("getPosition", function() {
        	if ($jobs.is(":visible")) {
                var $navs = $jobs.find(">.side>.navs");
                return "buildspec-jobs/" + $navs.children(".active").data("name");
        	} else if ($properties.is(":visible")) {
            	return "buildspec-properties";
            } else {
            	return "buildspec-";
            }
        });
        
        var $head = $valid.children(".head");
        $head.children(".jobs").click(onedev.server.buildSpec.selectJobs);
        $head.children(".properties").click(onedev.server.buildSpec.selectProperties);
        
        // use mouseup together with ui-sortable-helper (see selectJob method) class check 
        // to avoid the issue that sortable will fire onclick event in firefox (hence cause 
        // the job being selected while sorting
        $jobs.find(">.side>.navs>.nav>.select").mouseup(onedev.server.buildSpec.selectJob);
        $jobs.find(">.side>.navs>.nav>.delete").mouseup(onedev.server.buildSpec.deleteJob);
    },
    showJobs: function() {
    	var $valid = $(".build-spec>.valid");
    	var $head = $valid.children(".head");
    	$head.children().removeClass("active");
    	$head.children(".jobs").addClass("active");
    	var $body = $valid.children(".body");
    	$body.children().removeClass("d-flex");
        var $jobs = $body.children(".jobs");
        $jobs.addClass("d-flex");
        onedev.server.focus.doFocus($jobs);
    },
    showJob: function(index) {
    	var $jobs = $(".build-spec>.valid>.body>.jobs");
        var $navs = $jobs.find(">.side>.navs");
        $navs.children().removeClass("active");
        var $nav = $navs.children().eq(index);
        $nav.addClass("active");
        var $contents = $jobs.children(".contents");
        $contents.children().hide();
        var $content = $contents.children().eq(index);
        $content.show();
        onedev.server.focus.doFocus($content);

		// Fix the issue that add button and icon not aligned sometimes
		$navs.next().hide().show(0);
    },
    showProperties: function() {
    	var $valid = $(".build-spec>.valid");
    	var $head = $valid.children(".head");
    	$head.children().removeClass("active");
    	$head.children(".properties").addClass("active");
    	
    	var $body = $valid.children(".body");
    	$body.children().removeClass("d-flex");
        var $properties = $body.children(".properties");
        $properties.addClass("d-flex");
        onedev.server.focus.doFocus($properties);
    },
    showSelection: function(selection) {
        if (selection == "jobs") {
            onedev.server.buildSpec.showJobs();
            onedev.server.buildSpec.showJob(0);
        } else if (selection.startsWith("jobs/")) {
            onedev.server.buildSpec.showJobs();
            var jobName = selection.substring("jobs/".length);
            var index = $(".build-spec>.valid>.body>.jobs>.side>.navs>.nav[data-name='" + jobName.escape() + "']").index();                
            if (index != -1) 
                onedev.server.buildSpec.showJob(index);
            else
                onedev.server.buildSpec.showJob(0);
        } else if (selection == "properties" || selection.startsWith("properties/")) {
            onedev.server.buildSpec.showProperties();
        } else {
            onedev.server.buildSpec.showJobs();
            onedev.server.buildSpec.showJob(0);
        }
    },
    selectJobs: function() {
        onedev.server.buildSpec.showJobs();
        var $valid = $(".build-spec>.valid");
    	var $nav = $valid.find(">.body>.jobs>.side>.navs>.active");
    	var selection = "jobs";
    	if ($nav.length != 0)
    		selection += "/" + $nav.data("name");
        if ($valid.data("selectCallback")) 
        	$valid.data("selectCallback")(selection);
		$(window).resize();
    },
    selectJob: function() {
        var $nav = $(this).parent();
        if (!$nav.hasClass("ui-sortable-helper")) {
            onedev.server.buildSpec.showJob($nav.index());
            var $valid = $(".build-spec>.valid");
            if ($valid.data("selectCallback"))
                $valid.data("selectCallback")("jobs/" + $nav.data("name"));
        }
		$(window).resize();
    },
    selectProperties: function() {
        onedev.server.buildSpec.showProperties();
        var $valid = $(".build-spec>.valid");
        if ($valid.data("selectCallback"))
            $valid.data("selectCallback")("properties");
		$(window).resize();
    },
    deleteJob: function() {
        var $nav = $(this).parent();
        if (!$nav.hasClass("ui-sortable-helper")) {
        	var $valid = $(".build-spec>.valid");
            var $jobs = $valid.find(">.body>.jobs");
            var $navs = $jobs.find(">.side>.navs");
            var $contents = $jobs.children(".contents");
            var index = $nav.index();
            var $nav = $navs.children().eq(index);
            $nav.remove();
            var $content = $contents.children().eq(index);
            $content.find(".deletion-aware").trigger("beforeDelete");
            $content.remove();

            if ($nav.hasClass("active")) 
                onedev.server.buildSpec.showJob(0);
            
            $valid.data("deleteCallback")(index);
			// Fix the issue that add button and icon not aligned sometimes
			$navs.next().hide().show(0);
			
			$(window).resize();
        }
    }, 
    swapJobs: function(index1, index2) {
        var $contents = $(".build-spec>.valid>.body>.jobs>.contents");

        if (index1 < index2) {
            for (var i = 0; i < index2-index1; i++) 
                $contents.children().eq(index1+i).before($contents.children().eq(index1+i+1));
        } else {
            for (var i = 0; i < index1-index2; i++) 
                $contents.children().eq(index1-i).after($contents.children().eq(index1-i-1));
        }
		$(window).resize();
    },
    trackJobNameChange: function(index) {
        var $jobs = $(".build-spec>.valid>.body>.jobs");
        var $navs = $jobs.find(">.side>.navs");
        var $nav = $navs.children().eq(index);
        var $contents = $jobs.children(".contents");
        var $content = $contents.children().eq(index);
        
        var $input = $content.find(">div>div>.property-name input");

        function syncName() {
            var name = $input.val().trim();
            var $name = $nav.find("a.select>.name");
            if (name.length != 0) 
                $name.text(name);
            else
                $name.html("<i>Name not specified</i>");
            $name.closest(".nav").data("name", name);
        }

        $input.on("input", syncName);
        
        syncName();
    }
}