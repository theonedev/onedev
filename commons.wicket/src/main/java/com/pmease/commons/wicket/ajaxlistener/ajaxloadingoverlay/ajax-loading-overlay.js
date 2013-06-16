$(function() {
	/*
	 * Two divs are used here in order to make overlay works in IE.
	 */
	$("body").append("<div id=\"ajax-loading-overlay\" onclick='onAjaxLoadingOverlayClick(event)'><div></div></div>");
});

function onAjaxLoadingOverlayClick(event) {
    if (!event)
    	event = window.event;

	//IE9 & Other Browsers
	if (event.stopPropagation) {
		event.stopPropagation();
	}
	//IE8 and Lower
	else {
		event.cancelBubble = true;
	}	
}