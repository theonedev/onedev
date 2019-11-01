onedev.server.editable = {
    onBeanEditorDomReady: function(containerId) {
		var $container = $("#" + containerId);
        var $groups = $container.children(".group");
        $groups.children("a").click(function() {
            $(this).parent().toggleClass("expanded");
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
	onBeanEditorPropertyContainerDomReady: function(containerId) {
		var $container = $("#" + containerId);
		$container.find("div.value>.checkbox").each(function() {
			var $checkbox = $(this);
			var $label = $checkbox.parent().prev("label.name");
			$checkbox.children("label").append($label.children("span").text());
			$label.remove();
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
$(document).on("elementReplaced", function(event, componentId) {
	var $component = $("#" + componentId);
	var $group = $component.closest(".bean-properties.group");
	if ($group.length != 0 && $group.children("a").length != 0) 
		onedev.server.editable.checkGroup($group);
});