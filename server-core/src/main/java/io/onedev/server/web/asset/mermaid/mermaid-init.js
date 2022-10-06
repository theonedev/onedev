var mermaidTheme = onedev.server.isDarkMode()? "dark": "default";
mermaid.mermaidAPI.initialize({theme: mermaidTheme, startOnLoad:false});