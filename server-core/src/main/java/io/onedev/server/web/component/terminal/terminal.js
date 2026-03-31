onedev.server.terminal = {
	onDomReady: function(containerId, command) {
		var $terminal = $("#" + containerId).children(".terminal");
		var xterm = new Terminal({
			fontSize: 14,
			fontFamily: "Consolas, 'Liberation Mono', 'Menlo, Courier', monospace"
		});
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

		var isLikelyAutoResponse = function(data) {
			return data.indexOf("\u001b[") != -1 || data.indexOf("\u009b") != -1 || data.indexOf("rgb:") != -1;
		}

		var replaying = false;
		var suppressInputUntil = 0;
		xterm.onData(function(data) {
			if (replaying)
				return;
			if (new Date().getTime() < suppressInputUntil && isLikelyAutoResponse(data))
				return;
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
		$terminal.on("resized", function() {
			fitAddon.fit();
		});		

		Wicket.Event.subscribe("/websocket/open", function() {
			wsReady = true;
			sendResize(xterm.rows-1, xterm.cols-1);
			sendResize(xterm.rows, xterm.cols);
		});

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
					if (command) {
						Wicket.WebSocket.send("SHELL_INPUT:" + command + "\r");
						command = null;
					}
				}
				var base64 = message.substring("SHELL_OUTPUT:".length);
				var bytes = Uint8Array.from(atob(base64), function(c) { return c.charCodeAt(0); });
				xterm.write(bytes);
			}
		});
	}
}
