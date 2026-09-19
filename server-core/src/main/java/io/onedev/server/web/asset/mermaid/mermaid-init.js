var mermaidTheme = onedev.server.isDarkMode()? "dark": "default";
mermaid.mermaidAPI.initialize({theme: mermaidTheme, themeVariables: {fontSize: "12px"}, startOnLoad:false});
