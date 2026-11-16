onedev.server.terminal = {
	onDomReady: function(containerId, disableScrollback, tmuxTerminal) {
		var $container = $("#" + containerId);
		var $terminal = $container.children(".terminal");
		var $mobileKeys = $container.children(".terminal-mobile-keys");
		if (tmuxTerminal)
			$container.addClass("terminal-container");
		var options = {
			fontSize: 14,
			fontFamily: "LocalNerdFont, Consolas, 'Liberation Mono', Menlo, Courier, monospace"
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

		var decodeBase64 = function(base64) {
			return Uint8Array.from(atob(base64), function(c) { return c.charCodeAt(0); });
		};
		var parsingReplay = false;
		if (tmuxTerminal) {
			var mac = onedev.server.util.isMac();
			xterm.attachCustomKeyEventHandler(function(e) {
				var paste = e.key.toLowerCase() == "v" && !e.altKey && !e.shiftKey
						&& (mac ? e.metaKey && !e.ctrlKey : e.ctrlKey && !e.metaKey);
				// Let the browser deliver the clipboard text in a native paste event.
				return !paste;
			});

			var $clipboard = $container.children(".terminal-clipboard");
			var $copyButton = $clipboard.children("button");
			var $clipboardError = $clipboard.children(".terminal-clipboard-error");
			var clipboardRequest;
			var showClipboard = function(visible) {
				$clipboard.toggleClass("d-none", !visible);
				$terminal.trigger("resized");
			};
			var writeClipboard = function(request, fromButton) {
				var finish = function(copied) {
					if (request != clipboardRequest)
						return;
					if (copied) {
						clipboardRequest = null;
						if (document.activeElement == $copyButton[0])
							xterm.focus();
					}
					$clipboardError.toggleClass("d-none", copied || !fromButton);
					showClipboard(!copied);
				};
				if (navigator.clipboard) {
					navigator.clipboard.writeText(request.text).then(function() {
						finish(true);
					}, function() {
						finish(false);
					});
				} else {
					// The Copy button can also work on HTTP, where the Clipboard API is unavailable.
					var copy = function(e) {
						e.clipboardData.setData("text/plain", request.text);
						e.preventDefault();
					};
					document.addEventListener("copy", copy);
					try {
						finish(document.execCommand("copy"));
					} finally {
						document.removeEventListener("copy", copy);
					}
				}
			};
			$copyButton.on("click", function() {
				writeClipboard(clipboardRequest, true);
			});

			var utf8 = new TextDecoder("utf-8", {fatal: true});
			xterm.parser.registerOscHandler(52, function(data) {
				if (parsingReplay)
					return true;
				// Accept writes to the default selection or clipboard. Read requests use '?'.
				var match = /^([cpqs0-7]*);([A-Za-z0-9+/=]*)$/.exec(data);
				if (!match || (match[1] && !match[1].includes("c")))
					return true;
				var text;
				try {
					text = utf8.decode(decodeBase64(match[2]));
				} catch (e) {
					return true;
				}

				var request = {text: text};
				clipboardRequest = request;
				$clipboardError.addClass("d-none");
				if (navigator.clipboard && document.hasFocus() && document.activeElement == xterm.textarea)
					writeClipboard(request, false);
				else
					showClipboard(true);
				return true;
			});
		}

		var tmuxCopyMode = false;
		var updateTmuxCopyMode = function() {
			var buffer = xterm.buffer.active;
			var firstLine = buffer.getLine(buffer.viewportY);
			var firstLineText = firstLine ? firstLine.translateToString(true) : "";
			tmuxCopyMode = /\[\d+\/\d+\]\s*$/.test(firstLineText);
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
					"backspace": "\x7f",
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

		var viewportWidth = window.innerWidth;
		var expandedViewportHeight = window.innerHeight;
		var isKeyboardOpen = function() {
			if (!window.visualViewport)
				return document.activeElement == xterm.textarea;
			var viewport = window.visualViewport;
			// Normalize zoom, including Safari's automatic zoom when focusing an input.
			var viewportHeight = viewport.height * viewport.scale;
			if (window.innerWidth != viewportWidth) {
				viewportWidth = window.innerWidth;
				expandedViewportHeight = window.innerHeight;
			}
			// Remember the expanded height as some browsers also shrink innerHeight
			// when opening the keyboard. Focus alone may mean a hardware keyboard.
			expandedViewportHeight = Math.max(expandedViewportHeight,
					window.innerHeight, viewportHeight);
			return document.activeElement == xterm.textarea
					&& expandedViewportHeight - viewportHeight > 150;
		};

		var activateMobileKey = function($key) {
			if ($key.data("terminal-action") == "keyboard") {
				if (isKeyboardOpen()) {
					xterm.blur();
				} else {
					// iOS can keep the textarea focused with the keyboard dismissed.
					// Refocus synchronously within this tap to reopen it immediately.
					xterm.blur();
					xterm.focus();
				}
				return;
			}
			var modifier = $key.data("terminal-modifier");
			if (modifier) {
				modifiers[modifier] = !modifiers[modifier];
				updateModifierKeys();
			} else {
				sendMobileKey($key.data("terminal-key"));
			}
		};

		var repeatableKeySelector = "[data-terminal-key]:not([data-terminal-key^='tmux-'])";
		var keyHold;
		var suppressedClickButton;
		var stopKeyRepeat = function() {
			if (keyHold) {
				clearTimeout(keyHold.timer);
				keyHold = null;
			}
		};
		var cancelKeyHold = function() {
			if (keyHold) {
				suppressedClickButton = keyHold.button;
				stopKeyRepeat();
			}
		};

		$mobileKeys.on("pointerdown", repeatableKeySelector, function(e) {
			var pointer = e.originalEvent;
			if (!pointer.isPrimary || pointer.button != 0)
				return;
			stopKeyRepeat();
			suppressedClickButton = null;
			var hold = {button: this, pointerId: pointer.pointerId, startX: pointer.clientX};
			keyHold = hold;
			var key = $(this).data("terminal-key");
			var repeat = function() {
				if (!hold.button.isConnected || document.hidden) {
					stopKeyRepeat();
					return;
				}
				suppressedClickButton = hold.button;
				hold.timer = setTimeout(repeat, 80);
				sendMobileKey(key);
			};
			hold.timer = setTimeout(repeat, 600);
		}).on("pointermove pointerup pointercancel pointerleave lostpointercapture", repeatableKeySelector, function(e) {
			var pointer = e.originalEvent;
			if (!keyHold || pointer.pointerId != keyHold.pointerId)
				return;
			if (e.type == "pointerup")
				stopKeyRepeat();
			// A horizontal drag can reach this threshold even when the bar cannot scroll further.
			else if (e.type != "pointermove" || Math.abs(pointer.clientX - keyHold.startX) >= 8)
				cancelKeyHold();
		}).on("contextmenu", repeatableKeySelector, function(e) {
			if (keyHold || e.originalEvent.pointerType == "touch")
				e.preventDefault();
		});
		$mobileKeys.children(".terminal-mobile-key-scroll").on("scroll", cancelKeyHold);

		$mobileKeys.on("mousedown", ".terminal-mobile-key", function(e) {
			// Preserve focus until click so the keyboard button can toggle the current state.
			e.preventDefault();
		});

		$mobileKeys.on("click", ".terminal-mobile-key", function(e) {
			e.preventDefault();
			// The release can generate a click after a drag or a hold that has sent the key.
			if (e.detail && suppressedClickButton == this) {
				suppressedClickButton = null;
				return;
			}
			activateMobileKey($(this));
		});

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
				if (window.visualViewport)
					$(window.visualViewport).off(eventNamespace);
			};
			var fitTerminal = function() {
				if (fitRequest)
					cancelAnimationFrame(fitRequest);
				fitRequest = requestAnimationFrame(function() {
					fitRequest = null;
					if (!document.body.contains($container[0])) {
						removeViewportListeners();
						return;
					}
					if ($mobileKeys.css("display") != "none" && window.visualViewport) {
						var viewport = window.visualViewport;
						$container.toggleClass("terminal-keyboard-open", isKeyboardOpen());
						var containerTop = $container[0].getBoundingClientRect().top;
						var viewportBottom = viewport.offsetTop + viewport.height;
						var availableHeight = viewportBottom - Math.max(containerTop, viewport.offsetTop);
						if (availableHeight > 120) {
							// A flex-growing panel can exceed its height when the layout viewport
							// stays tall behind the keyboard. Cap it before fitting terminal rows.
							$container.css({height: availableHeight + "px", maxHeight: availableHeight + "px"});
						} else {
							$container.css({height: "", maxHeight: ""});
						}
					} else {
						$container.removeClass("terminal-keyboard-open");
						$container.css({height: "", maxHeight: ""});
					}
					fitAddon.fit();
				});
			};
			$terminal.on("resized", fitTerminal);
			$(xterm.textarea).on("focus blur", fitTerminal);

			removeViewportListeners();
			$(window).on("resize" + eventNamespace + " orientationchange" + eventNamespace, fitTerminal);
			$(window).on("blur" + eventNamespace + " pagehide" + eventNamespace, stopKeyRepeat);
			if (window.visualViewport) {
				$(window.visualViewport).on(
						"resize" + eventNamespace + " scroll" + eventNamespace, fitTerminal);
			}
			Wicket.Event.subscribe("/websocket/closed", stopKeyRepeat);
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
		var firstLiveOutput = true;
		Wicket.Event.subscribe("/websocket/message", function(jqEvent, message) {
			if (message == "TERMINAL_REPLAY_START") {
				replaying = true;
				// Track replay in parser order since xterm processes writes asynchronously.
				xterm.write("", function() { parsingReplay = true; });
			} else if (message == "TERMINAL_REPLAY_END") {
				replaying = false;
				suppressInputUntil = new Date().getTime() + 300;
				xterm.write("", function() { parsingReplay = false; });
			} else if (message.startsWith("SHELL_OUTPUT:")) {
				if (!replaying && firstLiveOutput) {
					sendResize(xterm.rows-1, xterm.cols-1);
					sendResize(xterm.rows, xterm.cols);
					firstLiveOutput = false;
				}
				var base64 = message.substring("SHELL_OUTPUT:".length);
				xterm.write(decodeBase64(base64));
			}
		});
	}
}
