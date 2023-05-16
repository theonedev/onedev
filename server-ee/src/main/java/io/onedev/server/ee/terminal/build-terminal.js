onedev.server.buildTerminal = {
	onDomReady: function() {
		var hasOutput = false;
		var $terminal = $(".terminal");
        var xterm = new Terminal();
		var fitAddon = new window.FitAddon.FitAddon();
		xterm.loadAddon(fitAddon);
		
		Wicket.Event.subscribe("/websocket/message", function(jqEvent, message) {
			if (message == "TERMINAL_OPEN") { 
		        xterm.open($(".terminal")[0]);		
				xterm.onData(function(data) {
					Wicket.WebSocket.send("TERMINAL_INPUT:" + data);
				});
				xterm.onResize(function(size) {
					Wicket.WebSocket.send("TERMINAL_RESIZE:" + size.rows + ":" + size.cols);
				});
				Wicket.WebSocket.send("TERMINAL_READY");
				
				$terminal.on("resized", function() {
					if (hasOutput)
						fitAddon.fit();
				});
			} else if (message == "TERMINAL_CLOSE") {
				window.close();	
			} else if (message.startsWith("TERMINAL_OUTPUT:")) {
				if (!hasOutput) {
					// fit on first prompt to make sure underlying shell 
					// receives the resize command 
					hasOutput = true;
					fitAddon.fit();
				}
				xterm.write(message.substring("TERMINAL_OUTPUT:".length));
			}
		});
	}
}