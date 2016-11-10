package com.gitplex.commons.antlr;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;

import com.gitplex.commons.antlr.ANTLRv4Lexer;

public abstract class LexerAdaptor extends Lexer {

	public LexerAdaptor(CharStream input) {
		super(input);
	}

	/**
	 * Track whether we are inside of a rule and whether it is lexical parser. _currentRuleType==Token.INVALID_TYPE
	 * means that we are outside of a rule. At the first sign of a rule name reference and _currentRuleType==invalid, we
	 * can assume that we are starting a parser rule. Similarly, seeing a token reference when not already in rule means
	 * starting a token rule. The terminating ';' of a rule, flips this back to invalid type.
	 *
	 * This is not perfect logic but works. For example, "grammar T;" means that we start and stop a lexical rule for
	 * the "T;". Dangerous but works.
	 *
	 * The whole point of this state information is to distinguish between [..arg actions..] and [charsets]. Char sets
	 * can only occur in lexical rules and arg actions cannot occur.
	 */
	private int _currentRuleType = Token.INVALID_TYPE;

	public int getCurrentRuleType() {
		return _currentRuleType;
	}

	public void setCurrentRuleType(int ruleType) {
		this._currentRuleType = ruleType;
	}

	protected void handleBeginArgument() {
		if (inLexerRule()) {
			pushMode(ANTLRv4Lexer.LexerCharSet);
			more();
		} else {
			pushMode(ANTLRv4Lexer.Argument);
		}
	}

	protected void handleEndArgument() {
		popMode();
		if (_modeStack.size() > 0) {
			setType(ANTLRv4Lexer.ARGUMENT_CONTENT);
		}
	}

	protected void handleEndAction() {
		popMode();
		if (_modeStack.size() > 0) {
			setType(ANTLRv4Lexer.ACTION_CONTENT);
		}
	}

	@Override
	public Token emit() {
		if (_type == ANTLRv4Lexer.ID) {
			String firstChar = _input.getText(Interval.of(_tokenStartCharIndex, _tokenStartCharIndex));
			if (Character.isUpperCase(firstChar.charAt(0))) {
				_type = ANTLRv4Lexer.TOKEN_REF;
			} else {
				_type = ANTLRv4Lexer.RULE_REF;
			}

			if (_currentRuleType == Token.INVALID_TYPE) { // if outside of rule def
				_currentRuleType = _type; // set to inside lexer or parser rule
			}
		} else if (_type == ANTLRv4Lexer.SEMI) { // exit rule def
			_currentRuleType = Token.INVALID_TYPE;
		}

		return super.emit();
	}

	private boolean inLexerRule() {
		return _currentRuleType == ANTLRv4Lexer.TOKEN_REF;
	}

	@SuppressWarnings("unused")
	private boolean inParserRule() { // not used, but added for clarity
		return _currentRuleType == ANTLRv4Lexer.RULE_REF;
	}
}