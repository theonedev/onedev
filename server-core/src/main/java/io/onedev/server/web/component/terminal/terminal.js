onedev.server.terminal = {
	onDomReady: function(containerId) {
		var hasLiveOutput = false;
		var replaying = false;
		var suppressInputUntil = 0;
		var pendingOutputs = [];
		var sizeReady = false;
		var fitScheduled = false;
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

		var flushPendingOutputs = function() {
			if (!sizeReady)
				return;
			while (pendingOutputs.length != 0)
				xterm.write(pendingOutputs.shift());
		};

		var ensureSizeReady = function() {
			if (fitScheduled)
				return;
			fitScheduled = true;
			window.requestAnimationFrame(function() {
				fitScheduled = false;
				fitAddon.fit();
				if (xterm.rows > 0 && xterm.cols > 0) {
					sizeReady = true;
					flushPendingOutputs();
					sendResize();
				} else {
					ensureSizeReady();
				}
			});
		};

		var isLikelyAutoResponse = function(data) {
			return data.indexOf("\u001b[") != -1 || data.indexOf("\u009b") != -1 || data.indexOf("rgb:") != -1;
		};
		xterm.onData(function(data) {
			if (replaying)
				return;
			if (new Date().getTime() < suppressInputUntil && isLikelyAutoResponse(data))
				return;
			Wicket.WebSocket.send("SHELL_INPUT:" + data);
		});		
		
		var wsReady = false;
		var sendResize = function() {
			if (wsReady && xterm.rows > 0 && xterm.cols > 0)
				Wicket.WebSocket.send("TERMINAL_RESIZE:" + xterm.rows + "," + xterm.cols);
		};

		xterm.onResize(function(size) {
			sizeReady = size.rows > 0 && size.cols > 0;
			flushPendingOutputs();
			sendResize();
		});		
		$terminal.on("resized", function() {
			sizeReady = false;
			ensureSizeReady();
		});		
		ensureSizeReady();

		Wicket.Event.subscribe("/websocket/open", function() {
			wsReady = true;
			ensureSizeReady();
		});
		Wicket.Event.subscribe("/websocket/message", function(jqEvent, message) {
			if (message == "TERMINAL_REPLAY_START") {
				replaying = true;
			} else if (message == "TERMINAL_REPLAY_END") {
				replaying = false;
				suppressInputUntil = new Date().getTime() + 300;
			} else if (message.startsWith("SHELL_OUTPUT:")) {
				if (!hasLiveOutput && !replaying) {
					hasLiveOutput = true;
					ensureSizeReady();
				}
				var base64 = message.substring("SHELL_OUTPUT:".length);
				var bytes = Uint8Array.from(atob(base64), function(c) { return c.charCodeAt(0); });
				if (sizeReady) {
					xterm.write(bytes);
				} else {
					pendingOutputs.push(bytes);
					ensureSizeReady();
				}
			}
		});
	}
}
