onedev.server.select2DragSort = {
	onWindowLoad: function(containerId) {
		var $container = $("#" + containerId);
		$container.select2("container").find("ul.select2-choices").sortable({
		    containment: 'parent',
		    start: function() { 
		    	$container.select2("onSortStart");
		    },
		    update: function() {
		    	$container.select2("onSortEnd");
		    }
		});		
	}
}