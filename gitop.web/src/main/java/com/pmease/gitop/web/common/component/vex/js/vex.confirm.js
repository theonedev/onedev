!function($, window, document, undefined) {
	'use strict';

	$.fn.confirm = function(options) {
		options = $.extend({}, $.fn.confirm.defaults, options);
		$(this).each(function(index) {
				var $this = $(this);
				var icon = $this.data('confirm-icon')
						|| 'question';
				var messageLabel = $this.data('confirm-message') || 'Confirm';
				var yesLabel = $this.data('confirm-yes-label') || 'OK';
				var noLabel = $this.data('confirm-no-label') || 'Cancel';
				var cssClassName = $this.data('confirm-css-class') || '';

				vex.dialog.confirm({
					message : "<div class='vex-dialog-icon'><i class='vex-icon icon-type-" + icon + "'></i></div> " + messageLabel,
					callback : function(data) {
						if (data)
							$this.trigger('vex.confirm');
						else 
							$this.trigger('vex.cancel');
					},
					overlayClosesOnClick : false,
					buttons : {
						YES : {
							text : yesLabel,
							type : 'submit',
							className : 'vex-dialog-button-primary'
						},
						NO : {
							text : noLabel,
							type : 'button',
							className : 'vex-dialog-button-secondary',
							click : function($vexContent, event) {
								$vexContent.data().vex.value = false;
								return vex.close($vexContent.data().vex.id);
							}
						}
					}
				});
		});
	}
	
}(window.jQuery, window, document);