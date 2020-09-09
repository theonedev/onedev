onedev.server.pagingNavigator = {
	onDomReady: function(containerId) {
		$('#' + containerId).find('a[disabled=disabled]').each(function() {
			$(this).addClass("disabled");
	  		$(this).parent().addClass('disabled');
		}); 
	}
}