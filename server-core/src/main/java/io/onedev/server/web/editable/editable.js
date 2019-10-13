onedev.server.editable = {
    onBeanEditorDomReady: function(containerId) {
		var $container = $("#" + containerId);
        var $group = $container.children(".group");
        $group.children("a").click(function() {
            $(this).parent().toggleClass("expanded");
        });
        $container.find(".feedbackPanelERROR").parents(".group.bean-properties").addClass("expanded");
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
	}
}