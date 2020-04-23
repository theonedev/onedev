var onedev = {};
String.prototype.escape = function() {
	return (this + '').replace(/[\\"']/g, '\\$&').replace(/\u0000/g, '\\0');
};
String.prototype.escapeHtml = function() {
    return this
         .replace(/&/g, "&amp;")
         .replace(/</g, "&lt;")
         .replace(/>/g, "&gt;")
         .replace(/"/g, "&quot;")
         .replace(/'/g, "&#039;");
 };
 
onedev.server = {
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
			var $floating = target.closest(".floating");
			if ($floating.length != 0) {
				var borderTop = $(window).scrollTop();
				var borderBottom = borderTop + $(window).height();
				var borderLeft = $(window).scrollLeft();
				var borderRight = borderLeft + $(window).width();

				var left = $floating.position().left;
				var top = $floating.position().top;
				var width = $floating.outerWidth();
				var height = $floating.outerHeight();
				
				if (left < borderLeft || left + width > borderRight 
						|| top < borderTop || top + height > borderBottom) {
					if ($floating.data("alignment"))
						$floating.align($floating.data("alignment"));
				}
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
	
	form: {
		/*
		 * This function can be called to mark enclosing form of specified element dirty. It should be
		 * called if underlying data has been changed but no form fields are updated, for instance 
		 * when sorting the elements inside a form. 
		 */
		markDirty: function($forms) {
			$forms.addClass("dirty").each(function() {
				onedev.server.form.dirtyChanged($(this));
			});
		},
		markClean: function($forms) {
			$forms.removeClass("dirty").each(function() {
				onedev.server.form.dirtyChanged($(this));
			});
		},
		trackDirty: function(container) {
			var $form = $(container);
			if (!$form.is("form")) {
				$form = $form.closest("form");
			}
			if ($form.length != 0 && ($form.find(".dirty-aware").length != 0 || $form.hasClass("leave-confirm"))) {
				var fieldSelector = ":input:not(.no-dirtytrack):not(input[type=submit]):not(input[type=button]):not(button)"; 
				var events = "change keyup propertychange input";
				$(container).find(fieldSelector).on(events, function() {
					onedev.server.form.markDirty($form);
				});
				if ($form.find(".has-error").length != 0) {
					$form.addClass("dirty");
				}
				onedev.server.form.dirtyChanged($form);
			}
			$form.submit(function() {
				$form.removeClass("dirty");				
			});
		},
		dirtyChanged: function($form) {
			var $dirtyAware = $form.find(".dirty-aware");
			if ($dirtyAware.length != 0) {
				if ($form.hasClass("dirty")) {
					$dirtyAware.removeAttr("disabled");
				} else {
					$dirtyAware.attr("disabled", "disabled");
				}
			}
		},
		setupDirtyCheck: function() {
			$("form").each(function() {
				onedev.server.form.trackDirty(this);
			});
			
			$(document).on("afterElementReplace", function(event, componentId) {
				var $component = $("#" + componentId);
				$component.find("form").each(function() {
					onedev.server.form.trackDirty(this);
				});
				onedev.server.form.trackDirty($component[0]);
			});
			
			if (Wicket && Wicket.Ajax) {
				var processAjaxResponse = Wicket.Ajax.Call.prototype.processAjaxResponse;
				Wicket.Ajax.Call.prototype.processAjaxResponse = function (data, textStatus, jqXHR, context) {
					if (jqXHR.readyState === 4) {
						var redirectUrl;
						try {
							redirectUrl = jqXHR.getResponseHeader('Ajax-Location');
						} catch (ignore) { // might happen in older mozilla
						}

						if (typeof(redirectUrl) !== "undefined" && redirectUrl !== null && redirectUrl !== "") {
							$("form.leave-confirm").removeClass("dirty");
						}
					}
					processAjaxResponse.call(this, data, textStatus, jqXHR, context);					
				}
			}

			$(window).on("beforeunload", function() {
				$dirtyForms = $("form").filter('.leave-confirm.dirty');
				if ($dirtyForms.length != 0) {
					return "There are unsaved changes";
				}
			});

		},
		confirmLeave: function(containerId) {
			var $container;
			if (containerId)
				$container = $("#" + containerId);
			else
				$container = $(document);
			var selector = "form.leave-confirm.dirty";
			var $dirtyForms = $container.find(selector).addBack(selector);
			if ($dirtyForms.length != 0) {
				if (confirm("There are unsaved changes, do you want to discard and continue?")) {
					onedev.server.form.clearAutosavings($dirtyForms);
					return true;
				} else {
					return false;
				}
			} else {
				return true;
			}
		},
		clearAutosavings: function($dirtyForms) {
			$dirtyForms.each(function() {
				var autosaveKey = $(this).data("autosaveKey");
				if (autosaveKey)
					localStorage.removeItem(autosaveKey);
			});
		},
		registerAutosaveKey: function($form, autosaveKey) {
			$form.data("autosaveKey", autosaveKey);
		}
	},
	setupAutoSize: function() {
		function doAutosize($textarea) {
			$textarea.each(function() {
				if ($(this).closest(".no-autosize").length == 0) {
					autosize($(this));
				}
			});
		}
		
		doAutosize($("textarea"));
		
		$(document).on("beforeElementReplace", function(event, componentId) {
			var $textarea = $("#" + componentId).find("textarea").addBack("textarea");
			$textarea.each(function() {
				if ($(this).closest(".no-autosize").length == 0) {
					autosize.destroy($(this));					
				}
			});
		});
		
		$(document).on("afterElementReplace", function(event, componentId) {
			doAutosize($("#" + componentId).find("textarea").addBack("textarea"));
		});
	},	

	ajaxRequests: {
		count: 0,
		
		track: function() {
			Wicket.Event.subscribe('/ajax/call/beforeSend', function() {
				onedev.server.ajaxRequests.count++;
			});
			Wicket.Event.subscribe('/ajax/call/done', function() {
				onedev.server.ajaxRequests.count--;
			});
		}
	},
	
	setupAjaxLoadingIndicator: function() {
		$("#ajax-loading-overlay").click(function(e) {
			e.stopPropagation();
		});

		var ongoingAjaxRequests = 0;
		Wicket.Event.subscribe('/ajax/call/beforeSend', function(e, attributes) {
			if (ongoingAjaxRequests == 0) {
				var $ajaxLoadingIndicator = $("#ajax-loading-indicator");
				if ($ajaxLoadingIndicator[0].timer)
					clearTimeout($ajaxLoadingIndicator[0].timer);
				$ajaxLoadingIndicator[0].timer = setTimeout(function() {
					if (!$ajaxLoadingIndicator.is(":visible") && $(".ajax-indicator").filter(":visible").length == 0)
						$ajaxLoadingIndicator.show();
				}, 2000);		
			}
			ongoingAjaxRequests++;
		});
		
		Wicket.Event.subscribe('/ajax/call/done', function() {
			ongoingAjaxRequests--;
			if (ongoingAjaxRequests == 0) {
				var $ajaxLoadingIndicator = $("#ajax-loading-indicator");
				if ($ajaxLoadingIndicator[0].timer) {
					clearTimeout($ajaxLoadingIndicator[0].timer);
					$ajaxLoadingIndicator[0].timer = null;
				}
				$ajaxLoadingIndicator.hide();
			}
		});
	}, 		
	
	focus: {
		$components: null,
		
		focusOn: function(componentId) {
			if (componentId)
				onedev.server.focus.doFocus($("#" + componentId));
			else if (document.activeElement != document.body) 
				document.activeElement.blur();
				
			onedev.server.focus.$components = null;
		},
		
		doFocus: function($containers) {
			/*
			 * Do focus with a timeout as otherwise it will not work in a panel replaced
			 * via Wicket
			 */
			setTimeout(function() {
				// do not use :visible selector directly for performance reason 
				var focusibleSelector = "input[type=text], input[type=password], input:not([type]), textarea, .CodeMirror";
				var inErrorSelector = ".has-error";
                var $inError = $containers.find(inErrorSelector).addBack(inErrorSelector).filter(":visible:first");
                if ($inError.length == 0) {
				    inErrorSelector = ".feedbackPanelERROR";
                    $inError = $containers.find(inErrorSelector).addBack(inErrorSelector).filter(":visible:first");
                }
				if ($inError.length != 0) {
					var $focusable = $inError.find(focusibleSelector).addBack(focusibleSelector).filter(":visible");
					if ($focusable.hasClass("CodeMirror") && $focusable[0].CodeMirror.options.readOnly == false) {
						$focusable[0].CodeMirror.focus();					
                    } else if ($focusable.length != 0 
                            && $focusable.closest(".select2-container").length == 0 
                            && $focusable.closest(".no-autofocus").length == 0) {
						$focusable.focus();
					} else {
						$inError[0].scrollIntoView({behavior: "smooth", block: "center"});
					}
				} else {
					$containers.find(focusibleSelector).addBack(focusibleSelector).filter(":visible").each(function() {
						var $this = $(this);
						if ($this.closest(".no-autofocus").length == 0) {
							if ($this.hasClass("CodeMirror") && $this[0].CodeMirror.options.readOnly == false) {
								$this[0].CodeMirror.focus();					
							} else if ($this.closest(".select2-container").length == 0 
									&& $this.attr("readonly") != "readonly"
                                    && $this.attr("disabled") != "disabled") {
								$this.focus();
							}
							return false;
                        }
					});
				}
			}, 0);
		},
		
		setupAutoFocus: function() {
			if (typeof(Wicket) != "undefined" && typeof(Wicket.Focus) != "undefined") {
				var wicketSetFocusOnId = Wicket.Focus.setFocusOnId;
				Wicket.Focus.setFocusOnId = function(componentId) {
					onedev.server.focus.focusOn(componentId);
					wicketSetFocusOnId(componentId);
				}
			}
			
			Wicket.Event.subscribe('/ajax/call/beforeSend', function() {
				onedev.server.focus.$components = $();
			});
			Wicket.Event.subscribe('/ajax/call/complete', function() {
				if (onedev.server.focus.$components != null) {
					onedev.server.focus.doFocus(onedev.server.focus.$components);
				}
			});

			onedev.server.focus.doFocus($(document));

			$(document).on("afterElementReplace", function(event, componentId) {
				if (onedev.server.focus.$components != null)
					onedev.server.focus.$components = onedev.server.focus.$components.add("#" + componentId);
			});			
		},
		
	},

	showSessionFeedback: function() {
		if ($("#session-feedback li").length != 0) {
			var feedback = $("#session-feedback");
	        feedback.css("left", ($(window).width()-feedback.outerWidth()) / 2);
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
	
	choiceFormatter: {
		formatSelection: function(choice) {
            if (choice.id && choice.id.indexOf("<$OneDevSpecialChoice$>") == 0)
                return "<i>" + choice.name.escapeHtml() + "</i>";
            else
                return choice.name.escapeHtml();
		},
		
		formatResult: function(choice) {
            if (choice.id && choice.id.indexOf("<$OneDevSpecialChoice$>") == 0)
                return "<i>" + choice.name.escapeHtml() + "</i>";
            else
                return choice.name.escapeHtml();
		},
		
		escapeMarkup: function(m) {
			return m;
		}
	},	

	setupWebsocketCallback: function() {
		var messagesToSent = [];
		function sendMessages() {
			if (onedev.server.ajaxRequests.count == 0) {
				for (var i in messagesToSent)
					Wicket.WebSocket.send(messagesToSent[i]);
				messagesToSent = [];
			}
			setTimeout(sendMessages, 0);
		}
		sendMessages();
		
		Wicket.Event.subscribe("/websocket/message", function(jqEvent, message) {
			if (message.indexOf("ObservableChanged:") != -1) { 
				if (messagesToSent.indexOf(message) == -1)
					messagesToSent.push(message);				
			} else if (message == "ErrorMessage") {
				$(".websocket-server-error").show();
			}
		});
	},

	/*
	 * View state represents scroll position and edit cursor. We restore view state
	 * when page is backed, or in some cases carry over the view state from one page 
	 * to another (such as scrolling the page to same position being viewed when edit 
	 * a source code)
	 */
	viewState: {
		getInnerMostAutoFit: function() {
			var $innerMost;
			var $autofits = $(".autofit").filter(":visible");
			while ($autofits.length != 0) {
				$innerMost = $autofits.first();
				$autofits = $innerMost.find(".autofit").filter(":visible");
			}
			return $innerMost;
		},
		getFromView: function() {
			var $innerMost = onedev.server.viewState.getInnerMostAutoFit();
			if ($innerMost) {
				return $innerMost.triggerHandler("getViewState");
			} else {
				return undefined;
			}
		},
		setToView: function(viewState) {
			var $innerMost = onedev.server.viewState.getInnerMostAutoFit();
			if ($innerMost) {
				$innerMost.triggerHandler("setViewState", viewState);
			}
		},
		getFromHistory: function() {
			if (history.state && history.state.viewState)
				return history.state.viewState;
			else
				return undefined;
		},
		setToHistory: function(viewState) {
			var state = history.state;
			if (!state)
				state = {};
			var newState = {viewState: viewState, data: state.data, visited: state.visited};
			history.replaceState(newState, '', window.location.href );
			onedev.server.history.current = {
				state: newState,
				url: window.location.href
			};
		},
		getFromViewAndSetToHistory: function() {
			var viewState = onedev.server.viewState.getFromView();
			if (viewState)
				onedev.server.viewState.setToHistory(viewState);
		},
		getFromHistoryAndSetToView: function() {
			var viewState = onedev.server.viewState.getFromHistory();
			if (viewState)
				onedev.server.viewState.setToView(viewState);
		}
	},
	
	history: {
		getHashlessUrl: function(url) {
			var index = url.indexOf('#');
			if (index != -1)
				return url.substring(0, index);
			else
				return url;
		},
		init: function(callback) {
			// Use a timeout here solve the problem that Safari (and previous versions of Chrome) 
			// fires event "onpopstate" on initial page load and this causes the page to reload 
			// infinitely  
			setTimeout(function() {
				window.onpopstate = function(event) {
					if (onedev.server.history.getHashlessUrl(window.location.href) 
							!= onedev.server.history.getHashlessUrl(onedev.server.history.current.url)) {
						if (onedev.server.form.confirmLeave()) {
							if (!event.state || !event.state.data) {
								location.reload();
							} else {
								callback(event.state.data);
								onedev.server.history.current = {
									state: event.state,
									url: window.location.href
								};
							}
						} else {
							/*
							 * In case we want to stay in the page, we should also re-push previous url 
							 * as url has already been changed when user hits back/forward button
							 */
							history.pushState(onedev.server.history.current.state, '' , 
									onedev.server.history.current.url);
						}
					} else {
						onedev.server.viewState.getFromHistoryAndSetToView();
					}
				};
			}, 100);
			
			onedev.server.history.current = {
				url: window.location.href 
			};
		},
		pushState: function(url, data) {
			var state = {data: data};
			onedev.server.history.current = {state: state, url: url};
			history.pushState(state, '', url);
			
			// Let others have a chance to do something before marking the page as visited
			setTimeout(function() {
				onedev.server.history.setVisited();
			}, 100);
		},
		replaceState: function(url, data) {
			var state = {data: data};
			onedev.server.history.current = {state: state, url: url};
			history.replaceState(state, '', url);
			
			// Let others have a chance to do something before marking the page as visited
			setTimeout(function() {
				onedev.server.history.setVisited();
			}, 100);
		},
		
		/*
		 * visited flag is used to determine whether or not a page is newly visited 
		 * or loaded via back/forward button. If loaded via back/forward button we
		 * will not respect scroll flag (such as mark param in url) and let browser
		 * scroll to original position for better usage experience 
		 */
		setVisited: function() {
			var state = history.state;
			if (!state)
				state = {};
			if (!state.visited) {
				var newState = {viewState: state.viewState, data: state.data, visited: true};
				history.replaceState(newState, '', window.location.href);
				onedev.server.history.current = {
					state: newState,
					url: window.location.href
				};
			}
		},
		isVisited: function() {
			return history.state != undefined && history.state.visited === true;
		},
	},
	util: {
		canInput: function(element) {
			var $element = $(element);
			return ($element.is("input") || $element.is("textarea") || $element.is("select")) && !$element.hasClass("readonly");			
		},
		isDevice: function() {
			var ua = navigator.userAgent.toLowerCase();
			return ua.indexOf("android") != -1 
					|| ua.indexOf("iphone") != -1 
					|| ua.indexOf("ipad") != -1 
					|| ua.indexOf("windows phone") != -1; 
		},
		isMac: function() {
			return navigator.userAgent.indexOf('Mac') != -1;		
		},
		describeUrl: function(url) {
			if (url.indexOf("http://") == 0)
				return url.substring("http://".length);
			if (url.indexOf("https://") == 0)
				return url.substring("https://".length);

			var index = url.lastIndexOf("/");
			if (index != -1)
				url = url.substring(index+1, url.length);
			index = url.lastIndexOf(".");
			if (index != -1)
				url = url.substring(0, index);
			index = url.lastIndexOf(".");
			if (index != -1)
				url = url.substring(index+1);

			url = url.replace(/[-_]/g, " ");
			var camelized = "";
			var splitted = url.split(" ");
			for (var i in splitted) 
			  camelized += splitted[i].charAt(0).toUpperCase() + splitted[i].slice(1) + " ";
			return camelized.trim();
		}
	},
	
	mouseState: {
		pressed: false, 
		moved: false,
		track: function() {
			$(document).mousedown(function() { 
				onedev.server.mouseState.pressed = true;
				onedev.server.mouseState.moved = false;
			});
			$(document).mouseup(function() {
				onedev.server.mouseState.pressed = false;
				onedev.server.mouseState.moved = false;
			});	
			$(document).mousemove(function(e) {
				// IE fires mouse move event after mouse click sometimes, so we check 
				// if mouse is really moved here
				if (e.clientX != self.clientX || e.clientY != self.clientY) {
					onedev.server.mouseState.moved = true;
					self.clientX = e.clientX;
					self.clientY = e.clientY;
				}
			});
			$(document).scroll(function() {
				onedev.server.mouseState.moved = false;
			});
		}
	},
	
	setupAlertClose: function() {
		function installListener($container) {
			$container.find(".alert").on("close.bs.alert", function() {
				setTimeout(function() {
					$(window).resize();
				}, 0);
			});
		}
		$(document).on("afterElementReplace", function(event, componentId) {
			installListener($("#" + componentId));
		});
		installListener($(document));
	},
	
	setupInputClear: function() {
		function installClearer($container) {
			$container.find(".clearable-wrapper").each(function() {
				var $wrapper = $(this);
                var $input = $wrapper.find("input[type=text], input:not([type])");
				if (!$input.hasClass("clearable")) {
					$input.addClass("clearable");
					var $clear = $("<a class='input-clear'>x</a>");
					$wrapper.append($clear);
					if ($input.next().hasClass("input-group-btn"))
						$clear.addClass("input-group-clear");
					$clear.click(function() {
						$input.val("");
						$input.focus();
						$input.trigger("input");
					});
					function setVisibility() {
						if ($input.val() != "")
							$clear.show();
						else
							$clear.hide();
					}
					$input.on("input change", setVisibility);
					setVisibility();
				}
			});
		}
		$(document).on("afterElementReplace", function(event, componentId) {
			installClearer($("#" + componentId));
		});
		installClearer($(document));
	},

	setupModalOverlays: function() {
		$(document).on('hidden.bs.modal', '.modal', function () {
			$('.modal').filter(":visible").length && $(document.body).addClass('modal-open');
		});		
	},
	
	onDomReady: function() {
		$(window).resize(function() {
			$(document).find(".resize-aware").trigger("resized");
		});
		$(window).scroll(function() {
			$(document).find(".scroll-aware").trigger("scrolled");
		});
		
		onedev.server.setupAjaxLoadingIndicator();
		onedev.server.form.setupDirtyCheck();
		onedev.server.focus.setupAutoFocus();
		onedev.server.setupWebsocketCallback();
		onedev.server.mouseState.track();
		onedev.server.ajaxRequests.track();
		onedev.server.setupInputClear();
		onedev.server.setupAlertClose();
		onedev.server.setupModalOverlays();

		$(document).keydown(function(e) {
			if (e.keyCode == 27)
				e.preventDefault();
		});

		window.onunload = function() {
			onedev.server.form.clearAutosavings($("form.leave-confirm.dirty"));
		};
	},
	
	onWindowLoad: function() {
		onedev.server.setupAutoSize();
		
		/*
		 * Browser will also issue resize event after window is loaded, but that is too late, 
		 * as getFromHistoryAndSetToView() must be happened after view size has been adjusted
		 */
		$(window).resize(); 
		onedev.server.viewState.getFromHistoryAndSetToView();

		// Let others have a chance to do something before marking the page as visited
		setTimeout(function() {
			onedev.server.history.setVisited();
		}, 100);

		/*
		 * Disable this as calling replaceState in beforeunload also affects 
		 * state of the page to be loaded
		 */
		/*
		$(window).on("beforeunload", function() {
			onedev.server.viewState.getFromViewAndSetToHistory();	
		});
		*/
		
		if (location.hash && !onedev.server.viewState.getFromHistory()) {
			// Scroll anchors into view (for instance the markdown headline)
			var nameOrId = decodeURIComponent(location.hash.slice(1));
			var element = document.getElementById(nameOrId);
			if (!element)
				element = document.getElementsByName(nameOrId)[0];
			if (element) 
				element.scrollIntoView();
		}
	}
	
};
