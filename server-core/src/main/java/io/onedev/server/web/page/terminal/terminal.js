onedev.server.terminal = {
	onDomReady: function() {
		Wicket.Event.subscribe("/websocket/message", function(jqEvent, message) {
			var $terminal = $(".terminal");
			if (message == "TerminalOpen") { 
		        var xterm = new Terminal();
				var fitAddon = new window.FitAddon.FitAddon();
				xterm.loadAddon(fitAddon);
		        xterm.open($(".terminal")[0]);		
				xterm.onData(function(data) {
					Wicket.WebSocket.send("TerminalInput:" + data);
				});
				xterm.onResize(function(size) {
					Wicket.WebSocket.send("TerminalResize:" + size.rows + ":" + size.cols);
				});
				fitAddon.fit();
				
				$terminal.data("xterm", xterm);
				$terminal.data("fitAddon", fitAddon);
				
				Wicket.WebSocket.send("TerminalReady");
				
				$terminal.on("resized", function() {
					$terminal.data("fitAddon").fit();
				});
			} else if (message == "TerminalClose") {
				window.close();				
			} else if (message.startsWith("TerminalOutput:")) {
				$terminal.data("xterm").write(message.substring("TerminalOutput:".length));
			}
		});
	}
}