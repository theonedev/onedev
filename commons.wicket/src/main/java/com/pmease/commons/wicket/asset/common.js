/**************************************************************
 * Define common javascript functions and setups here.
 * 
 **************************************************************/

$(function() {
	Wicket.Event.subscribe("/dom/node/added", function(jqEvent, element) {
		decorateElements(element);
	});	
	decorateElements(document);
});

function decorateElements(scope) {
	if ($(scope).is("table"))
		decorateTable(scope);
	$(scope).find("table").each(function() {
		decorateTable(this);
	});
}

function decorateTable(table) {
	if ($(table).attr("class") == undefined)
		$(table).addClass("table table-striped table-hover table-condensed");
}