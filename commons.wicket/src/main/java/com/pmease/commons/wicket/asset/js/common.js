/**************************************************************
 * Define common javascript functions and setups here.
 * 
 **************************************************************/

// define the pmease namespace
var pmease={};

pmease.onAjaxSend = function(jqEvent, attributes, jqXHR, errorThrown, textStatus) {
	// Freeze the whole page when ajax request starts. Refer to CommonPage.html for details.
	$("#ajax-loading-overlay").show();
	
	$("#ajax-loading-indicator").show();
}
pmease.onAjaxComplete = function() {
	// Un-freeze the whole page when ajax request starts.
	$("#ajax-loading-overlay").hide();
	
	$("#ajax-loading-indicator").hide();
}

// Register function to be called when DOM is ready for page.
$(function() {
	//Wicket.Event.subscribe('/ajax/call/beforeSend', pmease.onAjaxSend);  
	//Wicket.Event.subscribe('/ajax/call/complete', pmease.onAjaxComplete);
});