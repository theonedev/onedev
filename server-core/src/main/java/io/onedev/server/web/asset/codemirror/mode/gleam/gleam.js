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

  CodeMirror.defineSimpleMode("gleam", {
    start: [
      {regex: /\/\/.*/, token: "comment"},
      {regex: /"/, token: "string", next: "string"},
      {regex: /-?0[xX][0-9a-fA-F_]+/, token: "number"},
      {regex: /-?0[oO][0-7_]+/, token: "number"},
      {regex: /-?0[bB][01_]+/, token: "number"},
      {regex: /-?[0-9][0-9_]*\.[0-9_]*(?:[eE][+-]?[0-9_]+)?/, token: "number"},
      {regex: /-?[0-9][0-9_]*/, token: "number"},
      {regex: /\b(?:as|assert|auto|case|const|delegate|derive|echo|else|fn|if|implement|import|let|macro|opaque|panic|pub|test|todo|type|use)\b/,
       token: "keyword"},
      {regex: /\b[A-Z][0-9A-Za-z]*\b/, token: "type"},
      {regex: /(?:&&|\|\||==|!=|<=?\.?|>=?\.?|<>|<<|>>|<-|->|\|>|[-+*/%.=!|])/, token: "operator"},
      {regex: /[\{\[\(]/, indent: true},
      {regex: /[\}\]\)]/, dedent: true}
    ],
    string: [
      {regex: /"/, token: "string", next: "start"},
      {regex: /(?:[^\\"]|\\.)+/, token: "string"}
    ],
    meta: {
      electricInput: /^\s*\}$/,
      lineComment: "//",
      fold: "brace"
    }
  });

  CodeMirror.defineMIME("text/x-gleam", "gleam");
});
