/**************************************************************
 * Define common javascript functions and setups here.
 * 
 **************************************************************/

var pmease = {};

pmease.commons = {
	modal: {
		setupTrigger: function(triggerId, modalId, contentLoader) {
			var trigger = $("#" + triggerId);
			var modal = $("#" + modalId);

			// This script can still be called if CollapseBehavior is added to a 
			// a component enclosed in an invisible wicket:enclosure. So we 
			// should check if relevant element exists.
			if (!trigger[0] || !modal[0])
				return;
			
			$("#" + triggerId).click(function() {
				pmease.commons.modal.show(modalId, contentLoader);
			});
		},

		setup: function(modalId, modalWidth, showImmediately) {
			var modal = $("#" + modalId);
			
			// This script can still be called if CollapseBehavior is added to a 
			// a component enclosed in an invisible wicket:enclosure. So we 
			// should check if relevant element exists.
			if (!modal[0])
				return;
			
			modal.before("<div id='" + modalId + "-placeholder' class='hide'></div>");
			modal.modal({backdrop: "static", keyboard: false, show: false});
			if (modalWidth)
				modal.find(".modal-dialog").css({"width": modalWidth});
			if (showImmediately)
				pmease.commons.modal.show(modalId, undefined);
		},

		show: function(modalId, contentLoader) {
			var modal = $("#" + modalId);
			$("body").append(modal);
			
			modal.modal("show");

			pmease.commons.modal.afterShow(modal);
			
			if (!modal.find(">.modal-dialog>.content")[0])
				contentLoader();
			else
				modal.find("input[type=text], input[type=textarea]").filter(":visible:first").focus();
		},
		
		afterShow: function(modal) {
			var backdrop = $(".modal-backdrop:last");
			var prevPopup = modal.prevAll(".popup:visible");
			
			if (prevPopup[0]) {
				var prevPopupZIndex = parseInt(prevPopup.css("z-index"));
				var backdropZIndex = prevPopupZIndex + 10;
				backdrop.css("z-index", backdropZIndex);
				modal.css("z-index", backdropZIndex + 10);
			}
		},

		loaded: function(modalId) {
			$("#" + modalId).find("input[type=text], input[type=textarea]").filter(":visible:first").focus();
		},

		hide: function(modalId) {
			var modal = $("#" + modalId);
			modal.modal("hide");
			
			$("#" + modalId + "-placeholder").after(modal);
		}
	},
	
	confirm: {
		show: function(trigger, message, callback) {
			var html = 
				"<div class='modal popup'><div class='modal-dialog'><div class='modal-content'>" + 
				"	<div class='modal-body'>" +
				"		<p>" + message + "</p>" +
				"	</div>" +
				"	<div class='modal-footer'>" +
				"		<button class='btn btn-default' onclick='$(this).closest(\".modal\").modal(\"hide\").remove();'>Cancel</button>" +
				"		<button class='btn btn-primary'>Ok</button>" +
				"	</div>" +
				"</div></div></div>";
			
			var modal = $(html);

			$("body").append(modal);
			modal[0].confirm = {};
			modal[0].confirm.trigger= trigger[0];
			modal[0].confirm.callback = callback;

			modal.find(".btn-primary").on("click", function() {
				modal.modal("hide").remove();
				callback();
			});
			
			trigger.blur();
			
			modal.modal({keyboard: false, backdrop: "static"});
			
			pmease.commons.modal.afterShow(modal);
		},
		
		setup: function(triggerId, message) {
			$(function() {
				var trigger = $("#" + triggerId);
				
				var previousClick;

				var handlers = $._data(trigger[0], 'events').click;

				$.each(handlers, function(i,f) {
					previousClick = f.handler; 
					return false; 
				});
				
				trigger.unbind('click');

				trigger.click(function(event){
					pmease.commons.confirm.show(trigger, message, function() {
						previousClick(event);
					});
				});
			});
		}
		
	},
	
	setupCollapse: function(triggerId, targetId) {
		var trigger = $("#" + triggerId);
		var target = $("#" + targetId);

		// This script can still be called if CollapseBehavior is added to a 
		// a component enclosed in an invisible wicket:enclosure. So we 
		// should check if relevant element exists.
		if (!trigger[0] || !target[0])
			return;
		
		target[0].trigger = trigger[0];

		target.on("shown.bs.collapse hidden.bs.collapse", function() {
			var dropdown = target.closest(".dropdown-panel");
			if (dropdown.length != 0) {
				var borderTop = $(window).scrollTop();
				var borderBottom = borderTop + $(window).height();
				var borderLeft = $(window).scrollLeft();
				var borderRight = borderLeft + $(window).width();

				var left = dropdown.position().left;
				var top = dropdown.position().top;
				var width = dropdown.outerWidth();
				var height = dropdown.outerHeight();
				
				if (left < borderLeft || left + width > borderRight || top < borderTop || top + height > borderBottom)
					dropdown.align();
			}
			
		});
		trigger.click(function() {
			if (target[0].collapsibleIds == undefined) {
				if (!target.hasClass("in")) {
					target.collapse("show");
					$(target[0].trigger).removeClass("collapsed");
				} else {
					target.collapse("hide");
					$(target[0].trigger).addClass("collapsed");
				}
			} else if (!target.hasClass("in")) {
				for (var i in target[0].collapsibleIds) {
					var collapsible = $("#" + target[0].collapsibleIds[i]);
					if (collapsible.hasClass("in")) {
						collapsible.collapse("hide");
						$(collapsible[0].trigger).addClass("collapsed");
					}
				}
				target.collapse("show");
				$(target[0].trigger).removeClass("collapsed");
			}
		});
	},
	
	setupAccordion: function(accordionId) {
		var accordion = $("#" + accordionId);
		var collapsibleIds = new Array();
		accordion.find(".collapse:not(#" + accordionId + " .collapse .collapse, #" + accordionId + " .accordion .collapse)").each(function() {
			collapsibleIds.push(this.id);
		});
		if (collapsibleIds[0]) {
			var collapsible = $("#" + collapsibleIds[0]);
			collapsible.removeClass("collapse");
			collapsible.addClass("in");
		}
		for (var i in collapsibleIds) {
			var collapsible = $("#" + collapsibleIds[i]);
			if (i == 0) {
				$(collapsible[0].trigger).removeClass("collapsed");
				collapsible.removeClass("collapse");
				collapsible.addClass("in");
			} else {
				$(collapsible[0].trigger).addClass("collapsed");
			}
			collapsible[0].collapsibleIds = collapsibleIds;
		}
	},
	
	dropdown: {
		setup: function(triggerId, dropdownId, hoverDelay, alignment, dropdownLoader) {
			if (alignment.target) {
				var target = $("#" + alignment.target.id);
				if (!target[0])
					return;
				alignment.target.element = target[0];
				$(alignment.target.element).addClass("dropdown-alignment");
			}
			
			var trigger = $("#" + triggerId);
			
			// This script can still be called if CollapseBehavior is added to a 
			// a component enclosed in an invisible wicket:enclosure. So we 
			// should check if relevant element exists.
			if (!trigger[0])
				return;
			
			trigger.addClass("dropdown-trigger");
			if (hoverDelay >= 0)
				trigger.addClass("dropdown-hover");
			
			var dropdown = $("#" + dropdownId);

			if (!dropdown[0])
				return;
					
			// Dropdown can associate with multiple triggers, and we should initialize it only once.
			if (!$("#" + dropdownId + "-placeholder")[0]) 
				dropdown.before("<div id='" + dropdownId + "-placeholder' class='hide'></div>");

			if (hoverDelay >= 0) {
				function hide() {
					var topmostDropdown = $("body>.dropdown-panel:visible:last");
					if (topmostDropdown[0] == dropdown[0]) 
						pmease.commons.dropdown.hide(dropdownId);
					trigger.hideTimer = null;
				}
				function prepareToHide() {
					if (trigger.hideTimer != null) 
						clearTimeout(trigger.hideTimer);
					trigger.hideTimer = setTimeout(function(){
						if (trigger.hasClass("open"))
							hide();
					}, hoverDelay);
				}
				function cancelHide() {
					if (trigger.hideTimer != null) {
						clearTimeout(trigger.hideTimer);
						trigger.hideTimer = null;				
					} 
				}
				function cancelShow() {
					if (trigger.showTimer != null) {
						clearTimeout(trigger.showTimer);
						trigger.showTimer = null;
					}
				}
				trigger.mouseover(function(mouse){
					if (!trigger.showTimer) {
						trigger.showTimer = setTimeout(function() {
							if (!trigger.hasClass("open")) {
								if (alignment.target == undefined || alignment.target.id == undefined)
									alignment.target = mouse;
								pmease.commons.dropdown.show(trigger, dropdown, alignment, dropdownLoader);
								cancelHide();
							}
							trigger.showTimer = null;
						}, hoverDelay);
					}
				});
				dropdown.mouseover(function() {
					cancelHide();
				});
				trigger.mouseout(function() {
					prepareToHide();
					cancelShow();
				});
				trigger.mousemove(function() {
					cancelHide();
				});
				dropdown.mouseout(function(event) {
					if (event.pageX<dropdown.offset().left+5 || event.pageX>dropdown.offset().left+dropdown.width()-5 
							|| event.pageY<dropdown.offset().top+5 || event.pageY>dropdown.offset().top+dropdown.height()-5) {
						prepareToHide();
					}
				});
			} else {
				trigger.click(function(mouse) {
					if (!trigger.hasClass("open")) {
						if (alignment.target == undefined || alignment.target.id == undefined)
							alignment.target = mouse;
						pmease.commons.dropdown.show(trigger, dropdown, alignment, dropdownLoader);
					} else {
						pmease.commons.dropdown.hide(dropdownId);
					} 
					return false;
				});
			}
			
			return this;
		},

		// hide all dropdowns except the dropdown attached to specified trigger
		hideExcept: function(trigger) {
			var start;
			var dropdown = trigger.closest(".dropdown-panel");
			if (dropdown[0]) {
				start = dropdown;
			} else {
				var topmostModal = $("body>.modal:visible:last");
				if (topmostModal[0]) 
					start = topmostModal;
				else 
					start = $("body").children(":first");
			}		
			var childDropdownEl = start.nextAll(".dropdown-panel:visible")[0];
			if (childDropdownEl)
				pmease.commons.dropdown.hide(childDropdownEl.id);
		},

		show: function(trigger, dropdown, alignment, dropdownLoader) {
			pmease.commons.dropdown.hideExcept(trigger);
			
			dropdown[0].trigger = trigger[0];
			dropdown[0].alignment = alignment;
			
			trigger.addClass("open");
			trigger.parent().addClass("open");

			if (alignment.target.element)
				$(alignment.target.element).addClass("open");

			if (alignment.showIndicator) {
				dropdown.prepend("<div class='indicator'></div>");
				dropdown.append("<div class='indicator'></div>");
			}
			
			var topmostPopup = $("body>.popup:visible:last");
			if (topmostPopup[0])
				dropdown.css("z-index", parseInt(topmostPopup.css("z-index")) + 10);

			$("body").append(dropdown);
			dropdown.align(alignment).show();

			if (!dropdown.find(">.content")[0]) {
				dropdown.find(">div").addClass("content");
				dropdownLoader();
			}
			dropdown.trigger("show");
		},
		
		loaded: function(dropdownId) {
		},
		
		hide: function(dropdownId) {
			var dropdown = $("#" + dropdownId);
			dropdown.trigger("hide");
			
			var childDropdown = dropdown.nextAll(".dropdown-panel:visible");

			if (childDropdown[0])
				hideDropdown(childDropdown[0].id);
			
			var trigger = $(dropdown[0].trigger);
			trigger.removeClass("open");
			trigger.parent().removeClass("open");
			
			if ($(dropdown[0].alignment.target.element))
				$(dropdown[0].alignment.target.element).removeClass("open");
			dropdown.find(">.indicator").remove();
			
			dropdown.hide();
			
			$("#" + dropdownId + "-placeholder").after(dropdown);
		}
	},
	
	editable: {
		adjustReflectionEditor: function(editorId) {
			var editor = $("#" + editorId);
			var input = editor.find(".value>input[type=checkbox]");
			input.parent().prev("label.name").addClass("pull-left");
			input.after("<div style='clear:both;'/>")
			
			input = editor.find(".value>div>input[type=checkbox]");
			input.parent().parent().prev("label.name").addClass("pull-left");
			input.after("<div style='clear:both;'/>")
		}
	},
	
	form: {
		/*
		 * This function can be called to mark enclosing form of specified element dirty. It should be
		 * called if underlying data has been changed but no form fields are updated, for instance 
		 * when sorting the elements inside a form. 
		 */
		markDirty: function(componentId) {
			var $component = $("#" + componentId);
			$component.closest("form").addClass("dirty").find(".dirty-aware").removeAttr("disabled");
		},
		removeDirty: function(triggerId) {
			$(function() {
				var $trigger = $("#" + triggerId);
				
				var previousClick;

				var handlers = $._data($trigger[0], 'events').click;

				$.each(handlers, function(i,f) {
					previousClick = f.handler; 
					return false; 
				});
				
				$trigger.unbind('click');

				$trigger.click(function(event){
					$trigger.closest("form").removeClass("dirty");
					previousClick(event);
				});
			});
		},
		trackDirty: function(form) {
			var $form = $(form);
			var $dirtyAware = $form.find(".dirty-aware");

			if ($dirtyAware.length != 0) {
				$form.addClass("ays-inited");
				$dirtyAware.attr("disabled", "disabled");

				$form.areYouSure({
					"silent": !$form.hasClass("leave-confirm"),
					"addRemoveFieldsMarksDirty": true,
					change: function() {
						if ($(this).hasClass("dirty")) {
							$(this).find(".dirty-aware").removeAttr("disabled");
						} else {
							$(this).find(".dirty-aware").attr("disabled", "disabled")
						}
					}
				});
			} else if ($form.hasClass("leave-confirm")) {
				$form.areYouSure({"addRemoveFieldsMarksDirty": true});
			}
		},
		setupDirtyCheck: function() {
			$("form").each(function() {
				pmease.commons.form.trackDirty(this);
			});
			
			$(document).on("replace", function(event, componentId) {
				var $component = $("#" + componentId);
				var $forms = $component.find("form");
				if ($component.is("form"))
					$forms = $forms.add($component);
				$forms.each(function() {
					pmease.commons.form.trackDirty(this);
				});
				
				$component.closest("form.ays-inited").not($component).trigger("checkform.areYouSure");
			});
			
		},
	},
	
	setupAjaxLoadingIndicator: function() {
		$("#ajax-loading-overlay").click(function(e) {
			e.stopPropagation();
		});

		Wicket.Event.subscribe('/ajax/call/beforeSend', function() {
			var ajaxLoadingIndicator = $("#ajax-loading-indicator");
			ajaxLoadingIndicator[0].timer = setTimeout(function() {
				if (!$(".ajax-indicator").is(":visible"))
					ajaxLoadingIndicator.show();
			}, 500);		
		});
		
		Wicket.Event.subscribe('/ajax/call/complete', function() {
			var ajaxLoadingIndicator = $("#ajax-loading-indicator");
			clearTimeout(ajaxLoadingIndicator[0].timer);
			ajaxLoadingIndicator.hide();
		});
	}, 
		
	setupDropdownAndModel: function() {
		$(document).mousedown(function(event) {
			var source = $(event.target);
			if (!source.closest(".dropdown-trigger")[0])
				pmease.commons.dropdown.hideExcept(source);
		});
		
		$(document).keypress(function(e) {
			if (e.keyCode == 27) { // esc
				if ($(".select2-drop:visible").length == 0) {
					var topmostPopup = $("body>.popup:visible:last");
					if (topmostPopup.hasClass("modal")) {
						if (!topmostPopup[0].confirm)
							pmease.commons.modal.hide(topmostPopup[0].id);
						else
							topmostPopup.modal("hide").remove();
					}
					if (topmostPopup.hasClass("dropdown-panel"))
						pmease.commons.dropdown.hide(topmostPopup[0].id);
				}
			} else if (e.keyCode == 13) {
				var topmostPopup = $("body>.popup:visible:last");
				if (topmostPopup[0].confirm) {
					topmostPopup[0].confirm.callback();
					topmostPopup.modal("hide").remove();
				}
			}
		});

		Wicket.Event.subscribe('/ajax/call/complete', function() {
			$("body>.dropdown-panel:visible").each(function() {
				if (!$("#" + this.id + "-placeholder")[0])
					$(this).remove();
			});
			
			$("body>.dropdown-panel:visible:last").align();

			$("body>.modal:visible").each(function() {
				if (!this.confirm) {
					if (!$("#" + this.id + "-placeholder")[0]) {
						$(this).modal("hide");
						$(this).remove();
					}
				} else {
					if (!$("#" + this.confirm.trigger.id)[0])
						$(this).modal("hide").remove();
				}
			});
		});
	},
	
	stick: function(sticky, parent) {
		var $sticky = $(sticky);
		var offset = 0;
		var $parent;
		if (parent != undefined)
			$parent = $(parent);
		else
			$parent = $sticky.parent();
		$parent.parents().each(function() {
			var $this = $(this);
			var maxHeight = 0;
			$this.children(".sticky").each(function() {
				var height = $(this).outerHeight();
				if (height > maxHeight)
					maxHeight = height;
			});
			offset += maxHeight;
		});
		if (parent != undefined)
			$sticky.stick_in_parent({"offset_top": offset, "parent": parent, "inner_scrolling": false});
		else
			$sticky.stick_in_parent({"offset_top": offset, "inner_scrolling": false});
	},
	
	scroll: {
		setupScrollStop: function() {
			var $window = $(window);
		    $window.scroll(function() {
		        if ($window.data('scrollTimeout')) {
		          clearTimeout($window.data('scrollTimeout'));
		        }
		        $window.data('scrollTimeout', setTimeout("$(window).trigger('scrollStopped');",250));
		    });
		},
		
		getTopOffset: function(selector) {
			var offset = 0;
			$(selector).first().parents().each(function() {
				var $this = $(this);
				var maxHeight = 0;
				$this.children(".sticky").each(function() {
					var height = $(this).outerHeight();
					if (height > maxHeight)
						maxHeight = height;
				});
				offset += maxHeight;
			});
			return offset;
		},
		
		next: function(selector, margin) {
			var next = pmease.commons.scroll.getNext(selector, margin);
			if (next != null) {
				var topOffset = margin + pmease.commons.scroll.getTopOffset(selector);
				$('html, body').animate({scrollTop: $(next).offset().top - topOffset}, 500);			
			}
		},

		prev: function(selector, margin) {
			var prev = pmease.commons.scroll.getPrev(selector, margin);
			if (prev != null) {
				var topOffset = margin + pmease.commons.scroll.getTopOffset(selector);
				$('html, body').animate({scrollTop: $(prev).offset().top - topOffset}, 500);				
			}
		}, 
		
		getNext: function(selector, margin) {
			var next = null;
			$(selector).each(function() {
		        var bottom = $(window).scrollTop() + $(window).height() - margin;

		        var thisTop = $(this).offset().top;
		        if (bottom < thisTop && $(document).height() - thisTop > margin) {
		        	next = this;
		        	return false;
		        }
			});
			return next;
		},
		
		getPrev: function(selector, margin) {
			var topOffset = margin + pmease.commons.scroll.getTopOffset(selector);
			var prev = null;
			$($(selector).get().reverse()).each(function() {
		        var top = $(window).scrollTop() + topOffset;

		        var thisTop = $(this).offset().top;
		        if (top > thisTop && thisTop > topOffset) {
		        	prev = this;
		        	return false;
		        }
			});
			return prev;
		}
		
	},
	
	focus: {
		$components: null,
		
		focusOn: function(componentId) {
			if (componentId)
				pmease.commons.focus.doFocus($("#" + componentId));
			pmease.commons.focus.$components = null;
		},
		
		doFocus: function($containers) {
			$containers.find(".focusable:visible").each(function() {
				var $this = $(this);
				if ($this.parents(".nofocus").length == 0) {
					$this.focus();
					return false;
				}
			});
			$containers.find(".has-error:first .focusable").focus();
		},
		
		setupAutoFocus: function() {
			if (typeof(Wicket) != "undefined" && typeof(Wicket.Focus) != "undefined") {
				var wicketSetFocusOnId = Wicket.Focus.setFocusOnId;
				Wicket.Focus.setFocusOnId = function(componentId) {
					pmease.commons.focus.focusOn(componentId);
					wicketSetFocusOnId(componentId);
				}
			}
			
			Wicket.Event.subscribe('/ajax/call/beforeSend', function() {
				pmease.commons.focus.$components = $();
			});
			Wicket.Event.subscribe('/ajax/call/complete', function() {
				if (pmease.commons.focus.$components != null)
					pmease.commons.focus.doFocus(pmease.commons.focus.$components);
			});

			pmease.commons.focus.doFocus($(document));

			$(document).on("replace", function(event, componentId) {
				if (pmease.commons.focus.$components != null)
					pmease.commons.focus.$components = pmease.commons.focus.$components.add("#" + componentId);
			});			
		},
		
	},

	// Disable specified button if value of specified input is blank 
	disableIfBlank: function(inputId, buttonId) {
		var $input = $("#" + inputId);
		$input.bind("input propertychange keyup", function() {
			var value = $(this).val();
			var $button = $("#" + buttonId);
			if (value.trim().length != 0)
				$button.removeAttr("disabled");
			else
				$button.attr("disabled", "disabled");
		});
		$input.trigger("input");
	},
	
	autoHeight: function(targetSelector, bottomOffset) {
		var adjustHeight = function() {
			$(targetSelector).css("max-height", $(document).scrollTop() + $(window).height()
					- $(targetSelector).offset().top - bottomOffset + "px");
		};
		adjustHeight();
		$(window).resize(adjustHeight);
		$(window).scroll(adjustHeight);
	},
	
	showSessionFeedback: function() {
		if ($("#session-feedback li").length != 0) {
			var feedback = $("#session-feedback");
	        var x = ($(window).width() - feedback.outerWidth()) / 2;
	        feedback.css("left", x+$(window).scrollLeft());
			feedback.css("top", $(window).scrollTop());
			feedback.slideDown("slow");
			
			var body = $("body");
			if (body[0].hideCatchAllFeedbackTimer) {
				clearTimeout(body[0].hideCatchAllFeedbackTimer);
			}
			body[0].hideCatchAllFeedbackTimer = setTimeout(function() {
				$("#session-feedback").slideUp();
			}, 5000);
		}
	},
	
	backToTop: function(backToTop) {
		var $backToTop = $(backToTop);
        $backToTop.hide();
        $(window).scroll(function(){
        	if ($(window).scrollTop()>500)
        		$backToTop.fadeIn(1000);
        	else
        		$backToTop.fadeOut(1000);
        });
        $backToTop.click(function(){
        	$("body, html").animate({scrollTop:0}, 700);
        	return false;
        });
	},
	
	choiceFormatter: {
		id: {
			formatSelection: function(choice) {
				return choice.id;
			},
			
			formatResult: function(choice) {
				return choice.id;
			},
			
			escapeMarkup: function(m) {
				return m;
			}
		}
	},	
	
	websocket: {
		setupCallback: function() {
			Wicket.Event.subscribe("/websocket/message", function(jqEvent, message) {
				var json = JSON.parse(message);
				if (json.type == "RenderCallback")
					Wicket.WebSocket.send(message);
				else if (json.type == "ErrorMessage")
					$("#websocket-error").show();
			});
			Wicket.Event.subscribe("/websocket/open", function(jqEvent, message) {
				// Request to render via web socket right away after a web socket is opened 
				// in case we missed the message sent by WebSocketRender.requestToRender
				// at Java side if web socket connection is not established at that time
				var messages = [];
				for (var i in pmease.commons.websocket.renderTraits) {
					var message = JSON.stringify(pmease.commons.websocket.renderTraits[i]);
					// filter out duplicate messages
					if (messages.indexOf(message) == -1)
						messages.push(message);
				} 
				for (var i in messages)
					Wicket.WebSocket.send(messages[i]);
			});
		},
		renderTraits:[]
	},

	setupClearableInput: function(inputId) {
		var $input = $("#" + inputId);
		$input.addClass("clearable");
		$input.after("<span class='input-clear'>&nbsp;&nbsp;&nbsp;&nbsp;</span>");
		$input.next().click(function() {
			$input.val("");
			$(this).hide();
			$input.trigger("donetyping");
		});
		$input.keyup(function() {
			var value = $(this).val();
			if (value.trim().length != 0)
				$input.next().show();
			else
				$input.next().hide();
		});
	},

	setupDoneTyping: function(inputId, timeout) {
		var $input = $("#" + inputId);
		$input.on("input", function() {
			if ($input[0].typingTimer) 
				clearTimeout($input[0].typingTimer);
			$input[0].typingTimer = setTimeout(function() {
				$input.trigger("donetyping");
			}, timeout);
		});
	},
};

$(function() {
	pmease.commons.setupDropdownAndModel();
	pmease.commons.setupAjaxLoadingIndicator();
	pmease.commons.form.setupDirtyCheck();
	pmease.commons.focus.setupAutoFocus();
	pmease.commons.scroll.setupScrollStop();
	pmease.commons.websocket.setupCallback();
});
