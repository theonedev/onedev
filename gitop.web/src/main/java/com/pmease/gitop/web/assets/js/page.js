$(document).ready(function() {
	$('#globalheader a').tooltip({placement: 'bottom'});
	$('.focusable:first').focus();
	$('.has-error:first .focusable').focus();
});