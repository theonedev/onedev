onedev.server.editable = {
    onBeanEditorDomReady: function(containerId) {
		var $container = $("#" + containerId);
        var $group = $container.children(".group");
        $group.children("a").click(function() {
            $(this).parent().toggleClass("expanded");
        });
        $container.find(".feedbackPanelERROR").closest(".group.bean-properties").addClass("expanded");
    },
    onBeanViewerDomReady: function(containerId) {
		var $container = $("#" + containerId);
        var $group = $container.children(".group");
        $group.children("a").click(function() {
            $(this).parent().toggleClass("expanded");   
            $(window).resize();  
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
		
		$container.find("td.value>div>.checkbox").each(function() {
			var $checkbox = $(this);
			var $td = $checkbox.parent().parent().prev("td.name");
			$checkbox.children("label").append($td.children("span").text());
			$td.empty();
		});
	}
}