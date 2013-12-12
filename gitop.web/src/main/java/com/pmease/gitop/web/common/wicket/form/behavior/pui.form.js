(function($, window, document, undefined) {
	'use strict';

	var pui = pui || {};
	pui.forms = {
			toggleCheckBox: function($element) {
				var $input = $element.find('input[type="checkbox"]'),
					input = $input[0];
				
				if (false === $input.is(':disabled')) {
					input.checked = ((input.checked) ? false : true);
			        $element.toggleClass('checked');
			        $input.trigger('change');
				}
			},
			
			toggleRadio: function($element) {
				var $input = $element.find('input[type="radio"]'),
	            	$form = $input.closest('form.custom-form'),
	            	input = $input[0];

		        if (false === $input.is(':disabled')) {
		          $form.find('input[type="radio"][name="' + $input.attr('name') + '"]')
		            .parent().not($element).removeClass('checked');
	
		          if (!$element.hasClass('checked')) {
		            $element.toggleClass('checked');
		          }
	
		          input.checked = $element.hasClass('checked');
	
		          $input.trigger('change');
		        }
			}
	};
	
	$(document).ready(function() {
		$('.custom-form .checkbox-flat label').on('click', function(e) {
			e.preventDefault();
			e.stopPropagation();
			pui.forms.toggleCheckBox($(this));
		});
		
		$('.custom-form .checkbox-flat .icon-checkbox').on('click', function(e) {
			var $icon = $(this);
			e.preventDefault();
			e.stopPropagation();
			pui.forms.toggleCheckBox($icon.parent());
		});
		
		$('.custom-form .checkbox-flat label').each(function() {
			var $element = $(this);
			var $input = $element.find('input[type="checkbox"]'),
				input = $input[0];
			
			if (input.checked) {
				$element.addClass('checked');
			}
			if ($input.is(':disabled')) {
				$element.addClass('disabled');
			}
		});
		
		$('.custom-form .radio-flat label').on('click', function(e) {
			e.preventDefault();
			e.stopPropagation();
			pui.forms.toggleRadio($(this));
		});
		
	});
}(window.jQuery, window, document));