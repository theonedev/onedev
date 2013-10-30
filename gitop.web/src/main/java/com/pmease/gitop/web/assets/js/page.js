$(document).ready(function() {
	$('#globalheader a').tooltip({placement: 'bottom'});
	$('#main [data-toggle="tooltip"]').tooltip();
	$('.focusable:first').focus();
	$('.has-error:first .focusable').focus();
	$('body').on('click', function (e) {
	    $('.popover-link').each(function () {
	        //the 'is' for buttons that trigger popups
	        //the 'has' for icons within a button that triggers a popup
	        if (!$(this).is(e.target) && $(this).has(e.target).length === 0 && $('.popover').has(e.target).length === 0) {
	            $(this).popover('hide');
	        }
	    });
	});
	
	$(document).on('click', function (e) {
        $('.popup-marker').each(function () {
            //the 'is' for buttons that trigger popups
            //the 'has' for icons within a button that triggers a popup
            if (!$(this).is(e.target) && $(this).has(e.target).length === 0 && $('.popover').has(e.target).length === 0) {
                $(this).popover('toggle');
            }
        });
    });
	
	$(document).foundation();
});
