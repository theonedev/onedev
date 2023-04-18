// CodeMirror Mode Elixir, copyright (c) by Marijn Haverbeke, Ian Walter, and
// others. Distributed under an MIT license: http://codemirror.net/LICENSE.

(function(mod) {
  if (typeof exports == "object" && typeof module == "object") // CommonJS
    mod(require("../../lib/codemirror"));
  else if (typeof define == "function" && define.amd) // AMD
    define(["../../lib/codemirror"], mod);
  else // Plain browser env
    mod(CodeMirror);
})(function(CodeMirror) {

CodeMirror.defineMode('elixir', config => {
  const wordObj = words => {
    let o = {}
    for (var i = 0, e = words.length; i < e; ++i) o[words[i]] = true
    return o
  }

  const keywords = wordObj([
    'alias', 'case', 'cond', 'def', 'defmodule', 'defp', 'defstruct',
    'defprotocol', 'defimpl', 'defmacro', 'quote', 'unquote', 'receive', 'fn',
    'do', 'else', 'else if', 'end', 'false', 'if', 'in', 'next', 'rescue',
    'for', 'true', 'unless', 'when', 'nil', 'raise', 'throw', 'try', 'catch',
    'after', 'with', 'require', 'use', '__MODULE__', '__FILE__', '__DIR__',
    '__ENV__', '__CALLER__'
  ])
  const indentWords = wordObj([
    'def', 'defmodule', 'defp', 'case', 'cond', 'rescue', 'try', 'catch', '->'
  ])
  const dedentWords = wordObj(['end'])
  const matching = {'[': ']', '{': '}', '(': ')'}

  let curPunc

  const chain = (newtok, stream, state) => {
    state.tokenize.push(newtok)
    return newtok(stream, state)
  }

  const tokenBase = (stream, state) => {
    if (stream.sol() && stream.match('"""') && stream.eol()) {
      state.tokenize.push(readBlockComment)
      return 'comment'
    }

    if (stream.eatSpace()) {
      return null
    }

    let ch = stream.next()
    let m

    if (ch === '\'' || ch === '"') {
      return chain(readQuoted(ch, 'string', ch === '"'), stream, state)
    } else if (ch === '/') {
      let currentIndex = stream.current().length
      if (stream.skipTo('/')) {
        let searchTill = stream.current().length
        let balance = 0  // balance brackets

        stream.backUp(stream.current().length - currentIndex)

        while (stream.current().length < searchTill) {
          const chchr = stream.next()
          if (chchr === '(') {
            balance += 1
          } else if (chchr === ')') {
            balance -= 1
          }
          if (balance < 0) {
            break
          }
        }

        stream.backUp(stream.current().length - currentIndex)

        if (balance === 0) {
          return chain(readQuoted(ch, 'string-2', true), stream, state)
        }
      }

      return 'operator'
    } else if (ch === '%') {
      let style = 'string'
      let embed = true

      if (stream.eat('s')) {
        style = 'atom'
      } else if (stream.eat(/[WQ]/)) {
        style = 'string'
      } else if (stream.eat(/[r]/)) {
        style = 'string-2'
      } else if (stream.eat(/[wxq]/)) {
        style = 'string'
        embed = false
      }

      let delim = stream.eat(/[^\w\s=]/)

      if (!delim) {
        return 'operator'
      }

      if (matching.propertyIsEnumerable(delim)) {
        delim = matching[delim]
      }

      return chain(readQuoted(delim, style, embed, true), stream, state)
    } else if (ch === '#') {
      stream.skipToEnd()
      return 'comment'
    } else if (
      ch === '<' &&
      (m = stream.match(/^<-?[\`\"\']?([a-zA-Z_?]\w*)[\`\"\']?(?:;|$)/))
    ) {
      return chain(readHereDoc(m[1]), stream, state)
    } else if (ch === '0') {
      if (stream.eat('x')) {
        stream.eatWhile(/[\da-fA-F]/)
      } else if (stream.eat('b')) {
        stream.eatWhile(/[01]/)
      } else {
        stream.eatWhile(/[0-7]/)
      }
      return 'number'
    } else if (/\d/.test(ch)) {
      stream.match(/^[\d_]*(?:\.[\d_]+)?(?:[eE][+\-]?[\d_]+)?/)
      return 'number'
    } else if (ch === '?') {
      while (stream.match(/^\\[CM]-/)) {}

      if (stream.eat('\\')) {
        stream.eatWhile(/\w/)
      } else {
        stream.next()
      }
      return 'string'
    } else if (ch === ':') {
      if (stream.eat('\'')) {
        return chain(readQuoted('\'', 'atom', false), stream, state)
      }
      if (stream.eat('"')) {
        return chain(readQuoted('"', 'atom', true), stream, state)
      }

      // :> :>> :< :<< are valid symbols
      if (stream.eat(/[\<\>]/)) {
        stream.eat(/[\<\>]/)
        return 'atom'
      }

      // :+ :- :/ :* :| :& :! are valid symbols
      if (stream.eat(/[\+\-\*\/\&\|\:\!]/)) {
        return 'atom'
      }

      // Symbols can't start by a digit
      if (stream.eat(/[a-zA-Z$@_\xa1-\uffff]/)) {
        stream.eatWhile(/[\w$\xa1-\uffff]/)
        // Only one ? ! = is allowed and only as the last character
        stream.eat(/[\?\!\=]/)
        return 'atom'
      }

      return 'operator'
    } else if (ch === '@' && stream.match(/^@?[a-zA-Z_\xa1-\uffff]/)) {
      stream.eat('@')
      stream.eatWhile(/[\w\xa1-\uffff]/)
      return 'variable-2'
    } else if (ch === '$') {
      if (stream.eat(/[a-zA-Z_]/)) {
        stream.eatWhile(/[\w]/)
      } else if (stream.eat(/\d/)) {
        stream.eat(/\d/)
      } else {
        stream.next() // Must be a special global like $: or $!
      }
      return 'variable-3'
    } else if (/[a-zA-Z_\xa1-\uffff]/.test(ch)) {
      stream.eatWhile(/[\w\xa1-\uffff]/)
      stream.eat(/[\?\!]/)
      if (stream.eat(':')) {
        return 'atom'
      }
      return 'ident'
    } else if (
      ch === '|' &&
      (state.varList || state.lastTok === '{' || state.lastTok === 'do')
    ) {
      curPunc = '|'
      return null
    } else if (/[\(\)\[\]{}\\;]/.test(ch)) {
      curPunc = ch
      return null
    } else if (ch === '-' && stream.eat('>')) {
      return 'arrow'
    } else if (ch === '|' && stream.eat('>')) {
      return 'pipe'
    } else if (/[=+\-\/*:\.^%<>~|]/.test(ch)) {
      if (ch === '.' && !stream.eatWhile(/[=+\-\/*:\.^%<>~|]/)) {
        curPunc = '.'
      }
      return 'operator'
    } else {
      return null
    }
  }

  const tokenBaseUntilBrace = depth => {
    if (!depth) {
      depth = 1
    }

    return (stream, state) => {
      if (stream.peek() === '}') {
        if (depth === 1) {
          state.tokenize.pop()
          return state.tokenize[state.tokenize.length - 1](stream, state)
        } else {
          state.tokenize[state.tokenize.length - 1] = tokenBaseUntilBrace(depth - 1)
        }
      } else if (stream.peek() === '{') {
        state.tokenize[state.tokenize.length - 1] = tokenBaseUntilBrace(depth + 1)
      }
      return tokenBase(stream, state)
    }
  }

  const tokenBaseOnce = () => {
    let alreadyCalled = false
    return (stream, state) => {
      if (alreadyCalled) {
        state.tokenize.pop()
        return state.tokenize[state.tokenize.length - 1](stream, state)
      }
      alreadyCalled = true
      return tokenBase(stream, state)
    }
  }

  const readQuoted = (quote, style, embed, unescaped) => {
    return (stream, state) => {
      let escaped = false
      let ch

      if (state.context.type === 'read-quoted-paused') {
        state.context = state.context.prev
        stream.eat('}')
      }

      while ((ch = stream.next()) != null) { // eslint-disable-line
        if (ch === quote && (unescaped || !escaped)) {
          state.tokenize.pop()
          break
        }

        if (embed && ch === '#' && !escaped) {
          if (stream.eat('{')) {
            if (quote === '}') {
              state.context = {prev: state.context, type: 'read-quoted-paused'}
            }
            state.tokenize.push(tokenBaseUntilBrace())
            break
          } else if (/[@\$]/.test(stream.peek())) {
            state.tokenize.push(tokenBaseOnce())
            break
          }
        }

        escaped = !escaped && ch === '\\'
      }

      return style
    }
  }

  const readHereDoc = phrase => {
    return (stream, state) => {
      if (stream.match(phrase)) {
        state.tokenize.pop()
      } else {
        stream.skipToEnd()
      }
      return 'string'
    }
  }

  const readBlockComment = (stream, state) => {
    if (stream.sol() && stream.match('"""') && stream.eol()) {
      state.tokenize.pop()
    }
    stream.skipToEnd()
    return 'comment'
  }

  return {
    startState: () => {
      return {
        tokenize: [tokenBase],
        indented: 0,
        context: {type: 'top', indented: -config.indentUnit},
        continuedLine: false,
        lastTok: null,
        varList: false
      }
    },
    token: (stream, state) => {
      curPunc = null

      // if (stream.sol()) {
      //   state.indented = stream.indentation()
      // }

      let style = state.tokenize[state.tokenize.length - 1](stream, state)
      let kwtype
      let thisTok = curPunc

      if (style === 'ident') {
        let word = stream.current()

        style = state.lastTok === '.' ? 'property'
          : keywords.propertyIsEnumerable(stream.current()) ? 'keyword'
          : /^[A-Z]/.test(word) ? 'tag'
          : (state.lastTok === 'def' || state.lastTok === 'class' || state.varList) ? 'def'
          : 'variable'

        const isColumnIndent = stream.column() === stream.indentation()
        if (style === 'keyword') {
          thisTok = word
          if (indentWords.propertyIsEnumerable(word)) {
            kwtype = 'indent'
          } else if (dedentWords.propertyIsEnumerable(word)) {
            kwtype = 'dedent'
          } else if ((word === 'if' || word === 'unless') && isColumnIndent) {
            kwtype = 'indent'
          } else if (word === 'do' && state.context.indented < state.indented) {
            kwtype = 'indent'
          }
        }
      }

      if (curPunc || (style && style !== 'comment')) {
        state.lastTok = thisTok
      }

      if (curPunc === '|') {
        state.varList = !state.varList
      }

      if (kwtype === 'indent' || /[\(\[\{]/.test(curPunc)) {
        state.context = {
          prev: state.context,
          type: curPunc || style,
          indented: state.indented
        }
      } else if (
        (kwtype === 'dedent' || /[\)\]\}]/.test(curPunc)) &&
        state.context.prev
      ) {
        state.context = state.context.prev
      }

      if (stream.eol()) {
        state.continuedLine = (curPunc === '\\' || style === 'operator')
      }

      return style
    },
    // indent: (state, textAfter) => {
    //   if (state.tokenize[state.tokenize.length - 1] !== tokenBase) {
    //     return 0
    //   }
    //   let firstChar = textAfter && textAfter.charAt(0)
    //   let ct = state.context
    //   let closing = ct.type === matching[firstChar] ||
    //     ct.type === 'keyword' && /^(?:end|until|else|else if|when|rescue)\b/.test(textAfter)
    //   return ct.indented + (closing ? 0 : config.indentUnit) +
    //     (state.continuedLine ? config.indentUnit : 0)
    // },
    electricInput: /^\s*(?:end|rescue|else if|else|catch\})$/,
    lineComment: '#'
  }
})

CodeMirror.defineMIME('text/x-elixir', 'elixir')

})
