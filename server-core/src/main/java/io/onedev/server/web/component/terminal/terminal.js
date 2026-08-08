onedev.server.terminal = {
	onDomReady: function(containerId, disableScrollback, tmuxTerminal) {
		var $container = $("#" + containerId);
		var $terminal = $container.children(".terminal");
		var $mobileKeys = $container.children(".terminal-mobile-keys");
		if (tmuxTerminal)
			$container.addClass("terminal-container");
		var options = {
			fontSize: 14,
			fontFamily: "Consolas, 'Liberation Mono', 'Menlo, Courier', monospace"
		};
		if (disableScrollback)
			options.scrollback = 0;
		var xterm = new Terminal(options);
		var fitAddon = new window.FitAddon.FitAddon();
		xterm.loadAddon(fitAddon);

		try {
			var webglAddon = new window.WebglAddon.WebglAddon();
			webglAddon.onContextLoss(function() {
				webglAddon.dispose();
			});
			xterm.loadAddon(webglAddon);
		} catch (e) {
			console.warn("Error loading webgl addon", e);
		}

		xterm.open($terminal[0]);

		var tmuxCopyMode = false;
		var updateTmuxCopyMode = function() {
			var buffer = xterm.buffer.active;
			var firstLine = buffer.getLine(buffer.viewportY);
			var firstLineText = firstLine ? firstLine.translateToString(true) : "";
			tmuxCopyMode = /\[\d+\/\d+\]\s*$/.test(firstLineText);
			$container.toggleClass("terminal-tmux-copy-mode", tmuxCopyMode);
			$mobileKeys.find("[data-terminal-key='tmux-copy']").attr("aria-label",
					tmuxCopyMode ? "Exit tmux copy mode" : "Enter tmux copy mode");
		};
		if (tmuxTerminal) {
			xterm.onWriteParsed(updateTmuxCopyMode);
			updateTmuxCopyMode();
		}

		var modifiers = {
			ctrl: false,
			alt: false
		};

		var updateModifierKeys = function() {
			$mobileKeys.find("[data-terminal-modifier='ctrl']")
					.toggleClass("active", modifiers.ctrl).attr("aria-pressed", modifiers.ctrl);
			$mobileKeys.find("[data-terminal-modifier='alt']")
					.toggleClass("active", modifiers.alt).attr("aria-pressed", modifiers.alt);
		};

		var clearModifiers = function() {
			modifiers.ctrl = false;
			modifiers.alt = false;
			updateModifierKeys();
		};

		var applyCtrl = function(data) {
			if (data.length != 1)
				return data;
			var code = data.toUpperCase().charCodeAt(0);
			if (code >= 64 && code <= 95)
				return String.fromCharCode(code - 64);
			if (data == "?")
				return "\x7f";
			if (data == " ")
				return "\x00";
			return data;
		};

		var applyModifiers = function(data) {
			if (!modifiers.ctrl && !modifiers.alt)
				return data;
			if (modifiers.ctrl)
				data = applyCtrl(data);
			if (modifiers.alt)
				data = "\x1b" + data;
			clearModifiers();
			return data;
		};

		var getArrowSequence = function(key) {
			var suffixes = {
				up: "A",
				down: "B",
				right: "C",
				left: "D"
			};
			var suffix = suffixes[key];
			if (modifiers.ctrl || modifiers.alt) {
				var modifier = modifiers.ctrl && modifiers.alt ? 7 : modifiers.ctrl ? 5 : 3;
				return "\x1b[1;" + modifier + suffix;
			} else if (xterm.modes.applicationCursorKeysMode) {
				return "\x1bO" + suffix;
			} else {
				return "\x1b[" + suffix;
			}
		};

		var sendMobileKey = function(key) {
			var data;
			if (key == "up" || key == "down" || key == "left" || key == "right") {
				data = getArrowSequence(key);
			} else {
				var sequences = {
					"escape": "\x1b",
					"tab": "\t",
					"shift-tab": "\x1b[Z",
					"ctrl-c": "\x03",
					"enter": "\r",
					"delete": "\x1b[3~",
					"tmux-prefix": "\x02",
					"tmux-copy": tmuxCopyMode ? "q" : "\x02[",
					"page-up": "\x1b[5~",
					"page-down": "\x1b[6~"
				};
				data = sequences[key];
			}
			if (data != null) {
				clearModifiers();
				xterm.input(data, true);
			}
		};

		var lastTouchY;
		var touchRemainder = 0;
		var touchStep = Math.max(18, options.fontSize * 1.4);
		var touchScrollMultiplier = 3;
		var batchingTouchScroll = false;
		var batchedTouchScrollData = "";
		var dispatchWheel = function(deltaY, touch) {
			xterm.element.dispatchEvent(new WheelEvent("wheel", {
				bubbles: true,
				cancelable: true,
				view: window,
				deltaMode: 0,
				deltaY: deltaY,
				clientX: touch.clientX,
				clientY: touch.clientY
			}));
		};
		$terminal.on("touchstart", function(e) {
			if (!tmuxTerminal)
				return;
			updateTmuxCopyMode();
			var touches = e.originalEvent.touches;
			if (!tmuxCopyMode || touches.length != 1)
				return;
			e.preventDefault();
			lastTouchY = touches[0].clientY;
			touchRemainder = 0;
		});
		$terminal.on("touchmove", function(e) {
			if (!tmuxTerminal)
				return;
			var touches = e.originalEvent.touches;
			if (!tmuxCopyMode || touches.length != 1 || lastTouchY == null)
				return;
			e.preventDefault();
			var touchY = touches[0].clientY;
			touchRemainder += touchY - lastTouchY;
			lastTouchY = touchY;
			var lines = touchRemainder > 0
					? Math.floor(touchRemainder / touchStep)
					: Math.ceil(touchRemainder / touchStep);
			if (lines != 0) {
				touchRemainder -= lines * touchStep;
				var wheelEvents = Math.min(Math.abs(lines) * touchScrollMultiplier, 30);
				batchingTouchScroll = true;
				try {
					for (var i = 0; i < wheelEvents; i++)
						dispatchWheel(lines > 0 ? -touchStep : touchStep, touches[0]);
				} finally {
					batchingTouchScroll = false;
					if (batchedTouchScrollData) {
						var data = batchedTouchScrollData;
						batchedTouchScrollData = "";
						Wicket.WebSocket.send("SHELL_INPUT:" + data);
					}
				}
			}
		});
		$terminal.on("touchend touchcancel", function() {
			lastTouchY = null;
			touchRemainder = 0;
		});

		var repeatDelay;
		var repeatInterval;
		var stopKeyRepeat = function() {
			clearTimeout(repeatDelay);
			clearInterval(repeatInterval);
			repeatDelay = null;
			repeatInterval = null;
		};

		$mobileKeys.on("pointerdown", ".terminal-mobile-key", function(e) {
			e.preventDefault();
			stopKeyRepeat();
			if (this.setPointerCapture)
				this.setPointerCapture(e.pointerId);
			if ($(this).data("terminal-action") == "keyboard") {
				xterm.focus();
				return;
			}
			var modifier = $(this).data("terminal-modifier");
			if (modifier) {
				modifiers[modifier] = !modifiers[modifier];
				updateModifierKeys();
			} else {
				var key = $(this).data("terminal-key");
				sendMobileKey(key);
				if ($(this).hasClass("terminal-mobile-key-arrow")) {
					repeatDelay = setTimeout(function() {
						if (!document.body.contains($container[0])) {
							stopKeyRepeat();
							return;
						}
						repeatInterval = setInterval(function() {
							if (document.body.contains($container[0]))
								sendMobileKey(key);
							else
								stopKeyRepeat();
						}, 75);
					}, 400);
				}
			}
		});
		$mobileKeys.on("pointerup pointercancel lostpointercapture", stopKeyRepeat);

		var isLikelyAutoResponse = function(data) {
			return data.indexOf("\u001b[") != -1 || data.indexOf("\u009b") != -1 || data.indexOf("rgb:") != -1;
		};

		var replaying = false;
		var suppressInputUntil = 0;
		xterm.onData(function(data) {
			if (replaying)
				return;
			if (new Date().getTime() < suppressInputUntil && isLikelyAutoResponse(data))
				return;
			if (!isLikelyAutoResponse(data))
				data = applyModifiers(data);
			if (batchingTouchScroll)
				batchedTouchScrollData += data;
			else
				Wicket.WebSocket.send("SHELL_INPUT:" + data);
		});
		
		var sendResize = function(rows, cols) {
			Wicket.WebSocket.send("TERMINAL_RESIZE:" + rows + "," + cols);
		};

		var wsReady = false;

		xterm.onResize(function(size) {
			if (wsReady)
				sendResize(size.rows, size.cols);
		});		
		if (tmuxTerminal) {
			var fitRequest;
			var eventNamespace = ".onedev-terminal-" + containerId;
			var removeViewportListeners = function() {
				$(window).off(eventNamespace);
				$(document).off(eventNamespace);
				if (window.visualViewport)
					$(window.visualViewport).off(eventNamespace);
			};
			var fitTerminal = function() {
				if (fitRequest)
					cancelAnimationFrame(fitRequest);
				fitRequest = requestAnimationFrame(function() {
					fitRequest = null;
					if (!document.body.contains($container[0])) {
						stopKeyRepeat();
						removeViewportListeners();
						return;
					}
					if ($mobileKeys.css("display") != "none" && window.visualViewport) {
						var viewport = window.visualViewport;
						var containerTop = $container[0].getBoundingClientRect().top;
						var viewportBottom = viewport.offsetTop + viewport.height;
						var availableHeight = viewportBottom - Math.max(containerTop, viewport.offsetTop);
						if (availableHeight > 120)
							$container.css("height", availableHeight + "px");
						else
							$container.css("height", "");
					} else {
						$container.css("height", "");
					}
					fitAddon.fit();
				});
			};
			$terminal.on("resized", fitTerminal);

			removeViewportListeners();
			$(window).on("resize" + eventNamespace + " orientationchange" + eventNamespace, fitTerminal);
			if (window.visualViewport) {
				$(window.visualViewport).on(
						"resize" + eventNamespace + " scroll" + eventNamespace, fitTerminal);
			}
			$(window).on("pointerup" + eventNamespace + " pointercancel" + eventNamespace
					+ " blur" + eventNamespace + " pagehide" + eventNamespace, stopKeyRepeat);
			$(document).on("visibilitychange" + eventNamespace, function() {
				if (document.hidden)
					stopKeyRepeat();
			});
			fitTerminal();
		} else {
			$terminal.on("resized", function() {
				fitAddon.fit();
			});
		}

		Wicket.Event.subscribe("/websocket/open", function() {
			wsReady = true;
			sendResize(xterm.rows-1, xterm.cols-1);
			sendResize(xterm.rows, xterm.cols);
		});
		if (tmuxTerminal)
			Wicket.Event.subscribe("/websocket/closed", stopKeyRepeat);

		var firstLiveOutput = true;
		Wicket.Event.subscribe("/websocket/message", function(jqEvent, message) {
			if (message == "TERMINAL_REPLAY_START") {
				replaying = true;
			} else if (message == "TERMINAL_REPLAY_END") {
				replaying = false;
				suppressInputUntil = new Date().getTime() + 300;
			} else if (message.startsWith("SHELL_OUTPUT:")) {
				if (!replaying && firstLiveOutput) {
					sendResize(xterm.rows-1, xterm.cols-1);
					sendResize(xterm.rows, xterm.cols);
					firstLiveOutput = false;
				}
				var base64 = message.substring("SHELL_OUTPUT:".length);
				var bytes = Uint8Array.from(atob(base64), function(c) { return c.charCodeAt(0); });
				xterm.write(bytes);
			}
		});
	}
}
