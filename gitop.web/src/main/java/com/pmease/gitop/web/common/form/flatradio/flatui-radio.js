/* =============================================================
 * flatui-radio.js v0.0.3
 * ============================================================ */

!function ($) {

 /* RADIO PUBLIC CLASS DEFINITION
	* ============================== */

	var Radio = function (element, options) {
		this.init(element, options);
	}

	Radio.prototype = {

		constructor: Radio

	, init: function (element, options) {			 
			var $el = this.$element = $(element)

			this.options = $.extend({}, $.fn.radio.defaults, options);			
			$el.before(this.options.template);		
			this.setState();
		}		

	, setState: function () {		 
			var $el = this.$element
				, $parent = $el.closest('.radio');

				$el.prop('disabled') && $parent.addClass('disabled');		
				$el.prop('checked') && $parent.addClass('checked');
		} 

	, toggle: function () {		
			var $input = this.$element
	    		, $form = $input.closest('form').length ? $input.closest('form') : $input.closest('body')
	    		, input = $input[0]
	    		, $element = $input.closest('.radio-flat');

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

	, setCheck: function (option) {		 
			var ch = 'checked'
				, $el = this.$element
				, $parent = $el.closest('.radio')
				, checkAction = option == 'check' ? true : false
				, checked = $el.prop(ch)
				, $parentWrap = $el.closest('form').length ? $el.closest('form') : $el.closest('body')
				, $elemGroup = $parentWrap.find(':radio[name="' + $el['attr']('name') + '"]')
				, e = $.Event(option)

			$elemGroup.not($el).each(function () {
				var $el = $(this)
					, $parent = $(this).closest('.radio');

					$parent.removeClass(ch) && $el.removeAttr(ch);
			});

			$parent[checkAction ? 'addClass' : 'removeClass'](ch) && checkAction ? $el.prop(ch, ch) : $el.removeAttr(ch);
			$el.trigger(e);	 

			if (checked !== $el.prop(ch)) {
				$el.trigger('change'); 
			}
		}	 

	}


 /* RADIO PLUGIN DEFINITION
	* ======================== */

	var old = $.fn.radio

	$.fn.radio = function (option) {
		return this.each(function () {
			var $this = $(this)
				, data = $this.data('radio')
				, options = $.extend({}, $.fn.radio.defaults, $this.data(), typeof option == 'object' && option);
			if (!data) $this.data('radio', (data = new Radio(this, options)));
			if (option == 'toggle') data.toggle()
			if (option == 'check' || option == 'uncheck') data.setCheck(option)
			else if (option) data.setState(); 
		});
	}

	$.fn.radio.defaults = {
/*		template: '<span class="icons"><span class="first-icon fui-radio-unchecked"></span><span class="second-icon fui-radio-checked"></span></span>' */
		template: '<span class="icon-radio"></span>'
	}


 /* RADIO NO CONFLICT
	* ================== */

	$.fn.radio.noConflict = function () {
		$.fn.radio = old;
		return this;
	}


 /* RADIO DATA-API
	* =============== */

	$(document).on('click.radio.data-api', '[data-toggle^=radio], .radio', function (e) {
		var $radio = $(e.target);
		if (e.target.tagName != "A") {		
			e && e.preventDefault() && e.stopPropagation();
			if (!$radio.hasClass('radio')) $radio = $radio.closest('.radio');
			$radio.find(':radio').radio('toggle');
		}
	});

	$(function () {
		$('[data-toggle="radio"]').each(function () {
			var $radio = $(this);
			$radio.radio();
		});
	});

}(window.jQuery);