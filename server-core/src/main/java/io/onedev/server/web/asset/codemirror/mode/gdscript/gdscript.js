// CodeMirror, copyright (c) by Marijn Haverbeke and others
// Distributed under an MIT license: https://codemirror.net/5/LICENSE

(function(mod) {
  if (typeof exports == "object" && typeof module == "object") // CommonJS
    mod(require("../../lib/codemirror"), require("../../addon/mode/simple"));
  else if (typeof define == "function" && define.amd) // AMD
    define(["../../lib/codemirror", "../../addon/mode/simple"], mod);
  else // Plain browser env
    mod(CodeMirror);
})(function(CodeMirror) {
  "use strict";

  CodeMirror.defineSimpleMode("gdscript", {
    start: [
      {regex: /#.*/, token: "comment"},
      {regex: /r?"""/, token: "string", next: "tripleDoubleString"},
      {regex: /r?'''/, token: "string", next: "tripleSingleString"},
      {regex: /(?:r|&|\^)?"/, token: "string", next: "doubleString"},
      {regex: /(?:r|&|\^)?'/, token: "string", next: "singleString"},
      {regex: /\b(?:0[xX][\da-fA-F](?:_?[\da-fA-F])*|0[bB][01](?:_?[01])*|\d(?:_?\d)*(?:\.\d(?:_?\d)*)?(?:[eE][+-]?\d(?:_?\d)*)?)\b|\.\d(?:_?\d)*(?:[eE][+-]?\d(?:_?\d)*)?\b/,
       token: "number"},
      {regex: /\b(?:true|false|null)\b/, token: "atom"},
      {regex: /\b(?:PI|TAU|INF|NAN)\b/, token: "builtin"},
      {regex: /\b(?:class|class_name|const|enum|extends|func|signal|static|var)\b/,
       token: "keyword"},
      {regex: /\b(?:and|as|assert|await|break|breakpoint|case|continue|elif|else|for|if|in|is|match|not|or|pass|preload|return|self|super|while|yield)\b/,
       token: "keyword"},
      {regex: /@[A-Za-z_]\w*/, token: "meta"},
      {regex: /(?:\*\*|<<|>>|==|!=|<=|>=|&&|\|\||\+=|-=|\*=|\/=|%=|&=|\|=|\^=|<<=|>>=|:=|->|[-+*/%&|^~<>!=?:])/,
       token: "operator"},
      {regex: /[A-Z][A-Za-z0-9_]*/, token: "type"},
      {regex: /[A-Za-z_]\w*/, token: "variable"}
    ],
    doubleString: [
      {regex: /(?:[^\\"]|\\.)+/, token: "string"},
      {regex: /"/, token: "string", next: "start"}
    ],
    singleString: [
      {regex: /(?:[^\\']|\\.)+/, token: "string"},
      {regex: /'/, token: "string", next: "start"}
    ],
    tripleDoubleString: [
      {regex: /(?:[^\\"]|\\.|"(?!\"\"))+/, token: "string"},
      {regex: /"""/, token: "string", next: "start"},
      {regex: /"/, token: "string"}
    ],
    tripleSingleString: [
      {regex: /(?:[^\\']|\\.|'(?!''))+/, token: "string"},
      {regex: /'''/, token: "string", next: "start"},
      {regex: /'/, token: "string"}
    ],
    meta: {
      lineComment: "#"
    }
  });

  CodeMirror.defineMIME("text/x-gdscript", "gdscript");
});
