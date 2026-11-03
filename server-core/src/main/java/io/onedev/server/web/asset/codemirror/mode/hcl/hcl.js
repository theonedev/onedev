(function(mod) {
  if (typeof exports == "object" && typeof module == "object") // CommonJS
    mod(require("../../lib/codemirror"));
  else if (typeof define == "function" && define.amd) // AMD
    define(["../../lib/codemirror"], mod);
  else // Plain browser env
    mod(CodeMirror);
})(function(CodeMirror) {
  "use strict";

  CodeMirror.defineMode("hcl", function() {
    var keywords = /^(?:resource|variable|module|provider|locals|data|output|terraform|backend|provisioner|connection|lifecycle|dynamic|content|for|in|if|else|endif|endfor)$/;
    var identifier = /^[A-Za-z_\u00a1-\uffff][\w\u00a1-\uffff-]*/;

    function tokenComment(stream, state) {
      while (!stream.eol()) {
        if (stream.match("*/")) {
          state.contexts.pop();
          break;
        }
        stream.next();
      }
      return "comment";
    }

    function tokenTemplate(stream, state, context) {
      if (context.type == "heredoc" && stream.sol()) {
        var line = stream.string.replace(/[ \t]+$/, "");
        if (context.indented) line = line.replace(/^[ \t]+/, "");
        if (line == context.delimiter) {
          stream.skipToEnd();
          state.contexts.pop();
          return "string";
        }
      }
      while (!stream.eol()) {
        if (stream.match("$${") || stream.match("%%{")) continue;
        if (stream.match(/^(?:\$|%)\{~?/, false)) {
          if (stream.pos > stream.start) return "string";
          stream.match(/^(?:\$|%)\{~?/);
          state.contexts.push({type: "interpolation", depth: 0});
          return "punctuation";
        }
        var ch = stream.next();
        if (context.type == "string") {
          if (ch == "\\") {
            stream.next();
          } else if (ch == '"') {
            state.contexts.pop();
            break;
          }
        }
      }
      return "string";
    }

    return {
      startState: function() {
        return {contexts: [{type: "expression"}]};
      },
      copyState: function(state) {
        // Each editor snapshot needs its own interpolation brace counters.
        return {contexts: state.contexts.map(function(context) {
          var copy = {};
          for (var key in context) copy[key] = context[key];
          return copy;
        })};
      },
      token: function(stream, state) {
        var context = state.contexts[state.contexts.length - 1];
        if (context.type == "comment") return tokenComment(stream, state);
        if (context.type == "string" || context.type == "heredoc")
          return tokenTemplate(stream, state, context);
        if (stream.eatSpace()) return null;
        if (stream.match("#") || stream.match("//")) {
          stream.skipToEnd();
          return "comment";
        }
        if (stream.match("/*")) {
          state.contexts.push({type: "comment"});
          return tokenComment(stream, state);
        }
        if (stream.eat('"')) {
          state.contexts.push({type: "string"});
          return "string";
        }
        var heredoc = stream.match(/^<<(-?)([A-Za-z_\u00a1-\uffff][\w\u00a1-\uffff-]*)[ \t]*$/);
        if (heredoc) {
          state.contexts.push({type: "heredoc", delimiter: heredoc[2], indented: !!heredoc[1]});
          return "string";
        }
        if (context.type == "interpolation") {
          if (context.depth == 0 && stream.match(/^~?\}/)) {
            state.contexts.pop();
            return "punctuation";
          }
          if (stream.peek() == "{") context.depth++;
          else if (stream.peek() == "}") context.depth--;
        }
        if (stream.match(/^\d+(?:\.\d+)?(?:[eE][+-]?\d+)?/)) return "number";
        var word = stream.match(identifier);
        if (word) {
          if (/^(?:true|false|null)$/.test(word[0])) return "atom";
          if (keywords.test(word[0])) return "keyword";
          if (stream.match(/^[ \t]*=(?!=|>)/, false)) return "property";
          if (stream.match(/^[ \t]*\(/, false)) return "def";
          return "variable";
        }
        if (stream.match(/^(?:[=!<>]=|=>|&&|\|\||[-+*\/%=!?<>:])/)) return "operator";
        if (stream.match(/^[{}\[\]()]/)) return "bracket";
        stream.next();
        return null;
      },
      lineComment: "#",
      blockCommentStart: "/*",
      blockCommentEnd: "*/",
      fold: "brace"
    };
  });

  CodeMirror.defineMIME("text/x-hcl", "hcl");
});
