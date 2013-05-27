/**************************************************************
 * Define common javascript functions and setups here.
 * 
 **************************************************************/

// define the pmease namespace
var pmease={};

pmease.onAjaxSend = function() {
	// Freeze the whole page when ajax request starts. Refer to CommonPage.html for details.
	$(".frozen-overlay").show();
}
pmease.onAjaxComplete = function() {
	// Un-freeze the whole page when ajax request starts.
	$(".frozen-overlay").hide();
}

$(function() {
	Wicket.Event.subscribe('/ajax/call/beforeSend', pmease.onAjaxSend);  
	Wicket.Event.subscribe('/ajax/call/complete', pmease.onAjaxComplete);
});