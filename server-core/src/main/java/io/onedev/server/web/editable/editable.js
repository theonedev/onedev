onedev.server.editable = {
    onBeanEditorDomReady: function(containerId) {
		var $container = $("#" + containerId);
        var $groups = $container.children(".group");
        $groups.children("a").click(function() {
			var $parent = $(this).parent();
            $parent.toggleClass("expanded");
			// Fix the issue that placeholder is not displayed after showing the group
			$parent.find(".select2-container-multi+input").each(function() {
				$(this).data("select2").clearSearch();				
			});
        });
        $groups.each(function() {
        	var $group = $(this);
        	if ($group.children("a").length != 0) 
        		onedev.server.editable.checkGroup($group);
        });
        $container.find(".feedbackPanelERROR").parents(".group.bean-properties").addClass("expanded");
    },
    onBeanViewerDomReady: function(containerId) {
		var $container = $("#" + containerId);
        var $groups = $container.children(".group");
        $groups.children("a").click(function() {
            $(this).parent().toggleClass("expanded"); 
            $(window).resize();  
        });
        $groups.each(function() {
        	var $group = $(this);
        	if ($group.children("a").length != 0) 
        		onedev.server.editable.checkGroup($group);
        });
    },
	checkGroup: function($group) {
		var hasVisibleProperties = false;
		if ($group.children("table").length != 0) {
			$group.children("table").find(">tbody>tr").each(function() {
				if ($(this).css("display") != "none")
					hasVisibleProperties = true;
			});
		} else {
			$group.children("div").children("div").each(function() {
				if ($(this).css("display") != "none")
					hasVisibleProperties = true;
			});
		}
		if (hasVisibleProperties)
			$group.show();
		else
			$group.hide();
	},
};
$(document).on("afterElementReplace", function(event, componentId) {
	var $component = $("#" + componentId);
	var $group = $component.closest(".bean-properties.group");
	if ($group.length != 0 && $group.children("a").length != 0) 
		onedev.server.editable.checkGroup($group);
});