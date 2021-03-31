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
	day: {
		format: function(day) {
			return day.year + "-" + (day.monthOfYear + 1) + "-" + day.dayOfMonth;
		},
		compare: function(day1, day2) {
			if (day1.year < day2.year)
				return -1;
			else if (day1.year > day2.year)
				return 1;
			else if (day1.monthOfYear < day2.monthOfYear)
				return -1;
			else if (day1.monthOfYear > day2.monthOfYear)
				return 1;
			else
				return day1.dayOfMonth - day2.dayOfMonth;
		},
		fromDate: function(date) {
			return {
				year: date.getFullYear(),
				monthOfYear: date.getMonth(),
				dayOfMonth: date.getDate()
			}
		},
		toDate: function(day) {
			return new Date(day.year, day.monthOfYear, day.dayOfMonth, 0, 0, 0, 0);
		},
		fromValue: function(dayValue) {
			return {
				year: dayValue>>>16, 
				monthOfYear: ((dayValue&0x0000ffff)>>>8),
				dayOfMonth: dayValue&0x000000ff
			}
		},
		toValue: function(day) {
			return (day.year<<16) | (day.monthOfYear<<8) | day.dayOfMonth;
		},
		plus(day, numberOfDays) {
			return onedev.server.day.fromDate(onedev.server.day.toDate({
				year: day.year,
				monthOfYear: day.monthOfYear,
				dayOfMonth: day.dayOfMonth+numberOfDays
			}));
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
				if ($form.find(".is-invalid").length != 0) {
					$form.addClass("dirty");
				}
				onedev.server.form.dirtyChanged($form);
			}
			$form.submit(function() {
				$form.removeClass("dirty");				
				setTimeout(function() {
					$form.removeClass("dirty");				
				}, 0);
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
				var attentionSelector = ".is-invalid";
                var $attention = $containers.find(attentionSelector).addBack(attentionSelector).filter(":visible:first");
                if ($attention.length == 0) {
				    attentionSelector = ".feedbackPanelERROR";
                    $attention = $containers.find(attentionSelector).addBack(attentionSelector).filter(":visible:first");
                }
                if ($attention.length == 0) {
				    attentionSelector = ".feedbackPanelWARNING";
                    $attention = $containers.find(attentionSelector).addBack(attentionSelector).filter(":visible:first");
                }

				if ($attention.length != 0) {
					var $focusable = $attention.find(focusibleSelector).addBack(focusibleSelector).filter(":visible");
					if ($focusable.hasClass("CodeMirror") && $focusable[0].CodeMirror.options.readOnly == false) {
						$focusable[0].CodeMirror.focus();					
                    } else if ($focusable.length != 0 && !$focusable.hasClass("select2-input") 
							&& $focusable.closest(".no-autofocus").length == 0) {						
						$focusable.focus();
					} else {
						$attention[0].scrollIntoView({behavior: "smooth", block: "center"});
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
			}, 100);
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
				var $websocketError = $(".websocket-error");
		        $websocketError.css("left", ($(window).width()-$websocketError.outerWidth()) / 2);
				$websocketError.slideDown("slow");
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
		getFromView: function() {
			var $autofit = $(".autofit:visible:not(:has('.autofit:visible'))");
			if ($autofit.length != 0) {
				var events = jQuery._data($autofit[0], 'events');
				if (events && events["getViewState"]) 
					return $autofit.triggerHandler("getViewState");
				else 
					return {scroll:{left: $autofit.scrollLeft(), top: $autofit.scrollTop()}};			
			} else {
				return undefined;
			}
		},
		setToView: function(viewState) {
			var $autofit = $(".autofit:visible:not(:has('.autofit:visible'))");
			if ($autofit) {
				var events = jQuery._data($autofit[0], 'events');
				if (events && events["setViewState"]) { 
					$autofit.triggerHandler("setViewState", viewState);
				} else if (viewState.scroll) {
					$autofit.scrollLeft(viewState.scroll.left);
					$autofit.scrollTop(viewState.scroll.top);
				}
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
		init: function(popStateCallback) {
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
								popStateCallback(event.state.data);
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
		pushState: function(url, data, title) {
			var state = {data: data};
			onedev.server.history.current = {state: state, url: url};
			history.pushState(state, '', url);
			document.title = title;
			
			// Let others have a chance to do something before marking the page as visited
			setTimeout(function() {
				onedev.server.history.setVisited();
			}, 100);
		},
		replaceState: function(url, data, title) {
			var state = {data: data, title: title};
			onedev.server.history.current = {state: state, url: url};
			history.replaceState(state, '', url);
			document.title = title;
			
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
		isObjEmpty: function(obj) {
    		for(var key in obj) {
        		if(obj.hasOwnProperty(key))
            		return false;
    		}
    		return true;
		},
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
	
	setupDropdownToggle: function() {
		function doSetup($container) {
			$container.find(".dropdown-toggle:not('.no-dropdown-caret')").addBack(".dropdown-toggle:not('.no-dropdown-caret')").each(function() {
				if ($(this).find(".dropdown-caret").length == 0) {
					$(this).append("<svg class='dropdown-caret icon rotate-90'><use xlink:href='" + onedev.server.icons + "#arrow'/></svg>");
				}
			});
		}	
		$(document).on("afterElementReplace", function(event, componentId) {
			doSetup($("#" + componentId));
		});
		doSetup($(document));
	},
	
	setupCheckbox: function() {
		function doSetup($container) {
			$container.find(".checkbox").addBack(".checkbox").each(function() {
				if ($(this).find(">input+span:empty").length == 0) {
					$(this).find(">input").after("<span></span>");
				}
			});
		}	
		$(document).on("afterElementReplace", function(event, componentId) {
			doSetup($("#" + componentId));
		});
		doSetup($(document));
	},
	
	setupRadio: function() {
		function doSetup($container) {
			$container.find(".radio").addBack(".radio").each(function() {
				if ($(this).find(">input+span:empty").length == 0) {
					$(this).find(">input").after("<span></span>");
				}
			});
		}	
		$(document).on("afterElementReplace", function(event, componentId) {
			doSetup($("#" + componentId));
		});
		doSetup($(document));
	},
	
	setupSwitch: function() {
		function doSetup($container) {
			$container.find(".switch").addBack(".switch").each(function() {
				if ($(this).find(">label>input+span:empty").length == 0) {
					$(this).find(">label>input").after("<span></span>");
				}
				if ($(this).find(">input+span:empty").length == 0) {
					$(this).find(">input").after("<span></span>");
				}
			});
		}	
		$(document).on("afterElementReplace", function(event, componentId) {
			doSetup($("#" + componentId));
		});
		doSetup($(document));
	},
	perfectScrollbar: {
		setup: function() {
			function doSetup($container, afterElementReplace) {
				$container.find(".ps.ps-scroll").addBack(".ps.ps-scroll").each(function() {
					if (onedev.server.util.isDevice()) {
						$(this).addClass("overflow-auto");
					} else {
				        var ps = new PerfectScrollbar(this);
						$(this).addClass("resize-aware").on("resized", function() {
							ps.update();
							return false;
						});
						setTimeout(function() {
							ps.update();
						}, 0);
						$(this).data("ps", ps);
					}
				});
				if (afterElementReplace) {
					setTimeout(function() {
						$container.parent().closest(".ps-scroll").trigger("resized");
					}, 0);
				}
			}	
			$(document).on("afterElementReplace", function(event, componentId) {
				doSetup($("#" + componentId), true);
			});
			doSetup($(document));
		},
		empty: function(element) {
			$(element).contents().each(function() {
				if (!$(this).hasClass("ps__rail-x") && !$(this).hasClass("ps__rail-y"))
					$(this).remove();
			});
		}
	},
	setupInputClear: function() {
		function installClearer($container) {
			var selector = ".clearable-wrapper";
			var $clearableWrappers = $container.closest(selector);
			if ($clearableWrappers.length == 0)
				$clearableWrappers = $container.find(selector).addBack(selector);
			$clearableWrappers.each(function() {
				var $wrapper = $(this);
                var $input = $wrapper.find("input[type=text], input:not([type])");
				if (!$input.hasClass("clearable")) {
					$input.addClass("clearable");
					var $clear = $("<a class='input-clear'><svg class='icon align-middle'><use xlink:href='" + onedev.server.icons + "#times'/></svg></a>");
					$wrapper.append($clear);
					if ($input.next().hasClass("input-group-append")) {
						$clear.addClass("input-group-clear input-group-clear-" + $input.next().children("button").length);
					}
					$clear.click(function() {
						$input.val("");
						$input.focus();
						$input.trigger("clear");
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
	
	formatBriefDuration(seconds) {
		var intervals = [];
		var days = Math.floor(seconds/86400);
		if (days != 0)
			intervals.push(days + "d");
		seconds = seconds%86400;
		var hours = Math.floor(seconds/3600);
		if (hours != 0)
			intervals.push(hours + "h");
		seconds = seconds%3600;
		var minutes = Math.floor(seconds/60);
		if (minutes != 0)
			intervals.push(minutes + "m");
		seconds = seconds%60;
		if (seconds != 0)
			intervals.push(seconds + "s");
		return intervals.join(" ");
	},
	
	onDomReady: function(icons, popStateCallback) {
		onedev.server.icons = icons;
		
		$(window).resize(function() {
			var $autofit = $(".autofit:visible:not(:has('.autofit:visible'))");
			/* 
			 * 1. Do not use parents(":not('html'):not('body')") here as it will select $autofit self in 
			 * safari. Very odd
			 * 2. We remove fit-content class from parents of autofit as otherwise dimension calculation 
		     * of elements embedded inside it will be based on content instead of viewport, and this 
             * causes issues when view/edit code in a autofit container. Note that removing fit-content 
             * will not break safari in this case as only the innermost autofit can have scrollbars
			 */ 
			$autofit.css("overflow", "auto").parents().not("html").not("body")
					.css("overflow", "hidden").removeClass("fit-content");
			$(document).find(".resize-aware").trigger("resized");
		});
		
		onedev.server.setupAjaxLoadingIndicator();
		onedev.server.form.setupDirtyCheck();
		onedev.server.setupWebsocketCallback();
		onedev.server.mouseState.track();
		onedev.server.ajaxRequests.track();
		onedev.server.setupInputClear();
		onedev.server.setupAlertClose();
		onedev.server.setupModalOverlays();
		onedev.server.setupDropdownToggle();
		onedev.server.setupCheckbox();
		onedev.server.setupRadio();
		onedev.server.setupSwitch();
		onedev.server.history.init(popStateCallback);
		onedev.server.perfectScrollbar.setup();

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
		onedev.server.focus.setupAutoFocus();

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
		
		$(window).resize();
		
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
