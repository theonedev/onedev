/*
 * Copyright (c) 2014 by Bart Kiers
 *
 * The MIT license.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Project      : PCRE Parser, an ANTLR 4 grammar for PCRE
 * Developed by : Bart Kiers, bart@big-o.nl
 */
grammar PCRE;

// Most single line comments above the lexer- and  parser rules 
// are copied from the official PCRE man pages (last updated: 
// 10 January 2012): http://www.pcre.org/pcre.txt
parse
 : alternation EOF
 ;

// ALTERNATION
//
//         expr|expr|expr...
alternation
 : expr ('|' expr)*
 ;

expr
 : element*
 ;

element
 : atom quantifier?
 ;

// QUANTIFIERS
//
//         ?           0 or 1, greedy
//         ?+          0 or 1, possessive
//         ??          0 or 1, lazy
//         *           0 or more, greedy
//         *+          0 or more, possessive
//         *?          0 or more, lazy
//         +           1 or more, greedy
//         ++          1 or more, possessive
//         +?          1 or more, lazy
//         {n}         exactly n
//         {n,m}       at least n, no more than m, greedy
//         {n,m}+      at least n, no more than m, possessive
//         {n,m}?      at least n, no more than m, lazy
//         {n,}        n or more, greedy
//         {n,}+       n or more, possessive
//         {n,}?       n or more, lazy
quantifier
 : QuestionMark quantifier_type								  						
 | Plus quantifier_type								
 | Star quantifier_type  							
 | '{' exact=number '}' quantifier_type  					
 | '{' min=number ',' '}' quantifier_type  				
 | '{' min=number ',' max=number '}' quantifier_type 		
 ;

quantifier_type
 : '+'
 | '?'
 | /* nothing */
 ;

// CHARACTER CLASSES
//
//         [...]       positive character class
//         [^...]      negative character class
//         [x-y]       range (can be used for hex characters)
//         [[:xxx:]]   positive POSIX named set
//         [[:^xxx:]]  negative POSIX named set
//
//         alnum       alphanumeric
//         alpha       alphabetic
//         ascii       0-127
//         blank       space or tab
//         cntrl       control character
//         digit       decimal digit
//         graph       printing, excluding space
//         lower       lower case letter
//         print       printing, including space
//         punct       printing, excluding alphanumeric
//         space       white space
//         upper       upper case letter
//         word        same as \w
//         xdigit      hexadecimal digit
//
//       In PCRE, POSIX character set names recognize only ASCII  characters  by
//       default,  but  some  of them use Unicode properties if PCRE_UCP is set.
//       You can use \Q...\E inside a character class.
character_class
 : '[' '^' CharacterClassEnd Hyphen cc_atom+ ']'
 | '[' '^' CharacterClassEnd cc_atom* ']'
 | '[' '^' cc_atom+ ']'
 | '[' CharacterClassEnd Hyphen cc_atom+ ']'
 | '[' CharacterClassEnd cc_atom* ']'
 | simple_character_class
 ;

simple_character_class
 : '[' cc_atom+ ']'
 ;
 
// BACKREFERENCES
//
//         \n              reference by number (can be ambiguous)
//         \gn             reference by number
//         \g{n}           reference by number
//         \g{-n}          relative reference by number
//         \k<name>        reference by name (Perl)
//         \k'name'        reference by name (Perl)
//         \g{name}        reference by name (Perl)
//         \k{name}        reference by name (.NET)
//         (?P=name)       reference by name (Python)
backreference
 : backreference_or_octal
 | '\\g' number
 | '\\g' '{' number '}'
 | '\\g' '{' '-' number '}'
 | '\\k' '<' name '>'
 | '\\k' '\'' name '\''
 | '\\g' '{' name '}'
 | '\\k' '{' name '}'
 | '(' '?' 'P' '=' name ')'
 ;

backreference_or_octal
 : octal_char
 | Backslash digit
 ;

// CAPTURING
//
//         (...)           capturing group
//         (?<name>...)    named capturing group (Perl)
//         (?'name'...)    named capturing group (Perl)
//         (?P<name>...)   named capturing group (Python)
//         (?:...)         non-capturing group
//         (?|...)         non-capturing group; reset group numbers for
//                          capturing groups in each alternative
//
// ATOMIC GROUPS
//
//         (?>...)         atomic, non-capturing group
capture
 : '(' '?' '<' name '>' alternation ')'
 | '(' '?''\'' name '\'' alternation ')'
 | '(' '?' 'P' '<' name '>' alternation ')'
 | '(' alternation ')'
 ;

non_capture
 : '(' '?' ':' alternation ')'
 | '(' '?' '|' alternation ')'
 | '(' '?' '>' alternation ')'
 ;

// COMMENT
//
//         (?#....)        comment (not nestable)
comment
 : '(' '?' '#' non_close_parens ')'
 ;

// OPTION SETTING
//
//         (?i)            caseless
//         (?J)            allow duplicate names
//         (?m)            multiline
//         (?s)            single line (dotall)
//         (?U)            default ungreedy (lazy)
//         (?x)            extended (ignore white space)
//         (?-...)         unset option(s)
//
//       The following are recognized only at the start of a  pattern  or  after
//       one of the newline-setting options with similar syntax:
//
//         (*NO_START_OPT) no start-match optimization (PCRE_NO_START_OPTIMIZE)
//         (*UTF8)         set UTF-8 mode: 8-bit library (PCRE_UTF8)
//         (*UTF16)        set UTF-16 mode: 16-bit library (PCRE_UTF16)
//         (*UCP)          set PCRE_UCP (use Unicode properties for \d etc)
option
 : '(' '?' option_flags '-' option_flags ')'
 | '(' '?' option_flags ')'
 | '(' '?' '-' option_flags ')'
 | '(' '*' 'N' 'O' '_' 'S' 'T' 'A' 'R' 'T' '_' 'O' 'P' 'T' ')'
 | '(' '*' 'U' 'T' 'F' '8' ')'
 | '(' '*' 'U' 'T' 'F' '1' '6' ')'
 | '(' '*' 'U' 'C' 'P' ')'
 ;

option_flags
 : option_flag+
 ;

option_flag
 : 'i'
 | 'J'
 | 'm'
 | 's'
 | 'U'
 | 'x'
 ;

// LOOKAHEAD AND LOOKBEHIND ASSERTIONS
//
//         (?=...)         positive look ahead
//         (?!...)         negative look ahead
//         (?<=...)        positive look behind
//         (?<!...)        negative look behind
//
//       Each top-level branch of a look behind must be of a fixed length.
look_around
 : '(' '?' '=' alternation ')'
 | '(' '?' '!' alternation ')'
 | '(' '?' '<' '=' alternation ')'
 | '(' '?' '<' '!' alternation ')'
 ;

// SUBROUTINE REFERENCES (POSSIBLY RECURSIVE)
//
//         (?R)            recurse whole pattern
//         (?n)            call subpattern by absolute number
//         (?+n)           call subpattern by relative number
//         (?-n)           call subpattern by relative number
//         (?&name)        call subpattern by name (Perl)
//         (?P>name)       call subpattern by name (Python)
//         \g<name>        call subpattern by name (Oniguruma)
//         \g'name'        call subpattern by name (Oniguruma)
//         \g<n>           call subpattern by absolute number (Oniguruma)
//         \g'n'           call subpattern by absolute number (Oniguruma)
//         \g<+n>          call subpattern by relative number (PCRE extension)
//         \g'+n'          call subpattern by relative number (PCRE extension)
//         \g<-n>          call subpattern by relative number (PCRE extension)
//         \g'-n'          call subpattern by relative number (PCRE extension)
subroutine_reference
 : '(' '?' 'R' ')'
 | '(' '?' number ')'
 | '(' '?' '+' number ')'
 | '(' '?' '-' number ')'
 | '(' '?' '&' name ')'
 | '(' '?' 'P' '>' name ')'
 | '\\g' '<' name '>'
 | '\\g' '\'' name '\''
 | '\\g' '<' number '>'
 | '\\g' '\'' number '\''
 | '\\g' '<' '+' number '>'
 | '\\g' '\'' '+' number '\''
 | '\\g' '<' '-' number '>'
 | '\\g' '\'' '-' number '\''
 ;

// CONDITIONAL PATTERNS
//
//         (?(condition)yes-pattern)
//         (?(condition)yes-pattern|no-pattern)
//
//         (?(n)...        absolute reference condition
//         (?(+n)...       relative reference condition
//         (?(-n)...       relative reference condition
//         (?(<name>)...   named reference condition (Perl)
//         (?('name')...   named reference condition (Perl)
//         (?(name)...     named reference condition (PCRE)
//         (?(R)...        overall recursion condition
//         (?(Rn)...       specific group recursion condition
//         (?(R&name)...   specific recursion condition
//         (?(DEFINE)...   define subpattern for reference
//         (?(assert)...   assertion condition
conditional
 : '(' '?' '(' number ')' alternation ('|' alternation)? ')'
 | '(' '?' '(' '+' number ')' alternation ('|' alternation)? ')'
 | '(' '?' '(' '-' number ')' alternation ('|' alternation)? ')'
 | '(' '?' '(' '<' name '>' ')' alternation ('|' alternation)? ')'
 | '(' '?' '(' '\'' name '\'' ')' alternation ('|' alternation)? ')'
 | '(' '?' '(' 'R' number ')' alternation ('|' alternation)? ')'
 | '(' '?' '(' 'R' ')' alternation ('|' alternation)? ')'
 | '(' '?' '(' 'R' '&' name ')' alternation ('|' alternation)? ')'
 | '(' '?' '(' 'D' 'E' 'F' 'I' 'N' 'E' ')' alternation ('|' alternation)? ')'
 | '(' '?' '(' 'a' 's' 's' 'e' 'r' 't' ')' alternation ('|' alternation)? ')'
 | '(' '?' '(' name ')' alternation ('|' alternation)? ')'
 ;

// BACKTRACKING CONTROL
//
//       The following act immediately they are reached:
//
//         (*ACCEPT)       force successful match
//         (*FAIL)         force backtrack; synonym (*F)
//         (*MARK:NAME)    set name to be passed back; synonym (*:NAME)
//
//       The  following  act only when a subsequent match failure causes a back-
//       track to reach them. They all force a match failure, but they differ in
//       what happens afterwards. Those that advance the start-of-match point do
//       so only if the pattern is not anchored.
//
//         (*COMMIT)       overall failure, no advance of starting point
//         (*PRUNE)        advance to next starting character
//         (*PRUNE:NAME)   equivalent to (*MARK:NAME)(*PRUNE)
//         (*SKIP)         advance to current matching position
//         (*SKIP:NAME)    advance to position corresponding to an earlier
//                         (*MARK:NAME); if not found, the (*SKIP) is ignored
//         (*THEN)         local failure, backtrack to next alternation
//         (*THEN:NAME)    equivalent to (*MARK:NAME)(*THEN)
backtrack_control
 : '(' '*' 'A' 'C' 'C' 'E' 'P' 'T' ')'
 | '(' '*' 'F' ('A' 'I' 'L')? ')'
 | '(' '*' ('M' 'A' 'R' 'K')? ':' 'N' 'A' 'M' 'E' ')'
 | '(' '*' 'C' 'O' 'M' 'M' 'I' 'T' ')'
 | '(' '*' 'P' 'R' 'U' 'N' 'E' ')'
 | '(' '*' 'P' 'R' 'U' 'N' 'E' ':' 'N' 'A' 'M' 'E' ')'
 | '(' '*' 'S' 'K' 'I' 'P' ')'
 | '(' '*' 'S' 'K' 'I' 'P' ':' 'N' 'A' 'M' 'E' ')'
 | '(' '*' 'T' 'H' 'E' 'N' ')'
 | '(' '*' 'T' 'H' 'E' 'N' ':' 'N' 'A' 'M' 'E' ')'
 ;

// NEWLINE CONVENTIONS
//capture
//       These are recognized only at the very start of the pattern or  after  a
//       (*BSR_...), (*UTF8), (*UTF16) or (*UCP) option.
//
//         (*CR)           carriage return only
//         (*LF)           linefeed only
//         (*CRLF)         carriage return followed by linefeed
//         (*ANYCRLF)      all three of the above
//         (*ANY)          any Unicode newline sequence
//
// WHAT \R MATCHES
//
//       These  are  recognized only at the very start of the pattern or after a
//       (*...) option that sets the newline convention or a UTF or UCP mode.
//
//         (*BSR_ANYCRLF)  CR, LF, or CRLF
//         (*BSR_UNICODE)  any Unicode newline sequence
newline_convention
 : '(' '*' 'C' 'R' ')'
 | '(' '*' 'L' 'F' ')'
 | '(' '*' 'C' 'R' 'L' 'F' ')'
 | '(' '*' 'A' 'N' 'Y' 'C' 'R' 'L' 'F' ')'
 | '(' '*' 'A' 'N' 'Y' ')'
 | '(' '*' 'B' 'S' 'R' '_' 'A' 'N' 'Y' 'C' 'R' 'L' 'F' ')'
 | '(' '*' 'B' 'S' 'R' '_' 'U' 'N' 'I' 'C' 'O' 'D' 'E' ')'
 ;

// CALLOUTS
//
//         (?C)      callout
//         (?Cn)     callout with data n
callout
 : '(' '?' 'C' ')'
 | '(' '?' 'C' number ')'
 ;

atom
 : subroutine_reference
 | shared_atom
 | literal
 | character_class
 | capture
 | non_capture
 | comment
 | option
 | look_around
 | backreference
 | conditional
 | backtrack_control
 | newline_convention
 | callout
 | Dot
 | Caret
 | StartOfSubject
 | WordBoundary
 | NonWordBoundary
 | EndOfSubjectOrLine
 | EndOfSubjectOrLineEndOfSubject
 | EndOfSubject
 | PreviousMatchInSubject
 | ResetStartMatch
 | OneDataUnit
 | ExtendedUnicodeChar
 ;

cc_atom
 : cc_literal Hyphen cc_literal
 | shared_atom
 | cc_atom_literal=cc_literal
 | backreference_or_octal // only octal is valid in a cc
 ;

shared_atom
 : POSIXNamedSet
 | POSIXNegatedNamedSet
 | ControlChar
 | DecimalDigit
 | NotDecimalDigit
 | HorizontalWhiteSpace
 | NotHorizontalWhiteSpace
 | NotNewLine
 | CharWithProperty
 | CharWithoutProperty
 | NewLineSequence
 | WhiteSpace
 | NotWhiteSpace
 | VerticalWhiteSpace
 | NotVerticalWhiteSpace
 | WordChar
 | NotWordChar 
 ;

literal
 : shared_literal
 | CharacterClassEnd
 ;

cc_literal
 : shared_literal
 | Dot
 | CharacterClassStart
 | Caret
 | QuestionMark
 | Plus
 | Star
 | WordBoundary
 | EndOfSubjectOrLine
 | Pipe
 | OpenParen
 | CloseParen
 ;

shared_literal
 : octal_char
 | letter
 | digit
 | BellChar
 | EscapeChar
 | FormFeed
 | NewLine
 | CarriageReturn
 | Tab
 | HexChar
 | Quoted
 | BlockQuoted
 | OpenBrace
 | CloseBrace
 | Comma
 | Hyphen
 | LessThan
 | GreaterThan
 | SingleQuote
 | Underscore
 | Colon
 | Hash
 | Equals
 | Exclamation
 | Ampersand
 | OtherChar
 ;

number
 : digits
 ;

octal_char
 : ( Backslash D0 (D0 | D1 | D2 | D3) octal_digit octal_digit
   | Backslash D0 octal_digit octal_digit                     
   | Backslash D0 octal_digit
   )

 ;

octal_digit
 : D0 | D1 | D2 | D3 | D4 | D5 | D6 | D7
 ;
 
digits
 : digit+
 ;

digit
 : D0 | D1 | D2 | D3 | D4 | D5 | D6 | D7 | D8 | D9
 ;

name
 : alpha_nums
 ;

alpha_nums
 : (letter | Underscore) (letter | Underscore | digit)*
 ;

non_close_parens
 : non_close_paren+
 ;

non_close_paren
 : ~CloseParen
 ;

letter
 : ALC | BLC | CLC | DLC | ELC | FLC | GLC | HLC | ILC | JLC | KLC | LLC | MLC | NLC | OLC | PLC | QLC | RLC | SLC | TLC | ULC | VLC | WLC | XLC | YLC | ZLC |
   AUC | BUC | CUC | DUC | EUC | FUC | GUC | HUC | IUC | JUC | KUC | LUC | MUC | NUC | OUC | PUC | QUC | RUC | SUC | TUC | UUC | VUC | WUC | XUC | YUC | ZUC
 ;

// QUOTING
//
//         \x         where x is non-alphanumeric is a literal x
//         \Q...\E    treat enclosed characters as literal
Quoted      : '\\' NonAlphaNumeric;
BlockQuoted : '\\Q' .*? '\\E';

// CHARACTERS
//
//         \a         alarm, that is, the BEL character (hex 07)
//         \cx        "control-x", where x is any ASCII character
//         \e         escape (hex 1B)
//         \f         form feed (hex 0C)
//         \n         newline (hex 0A)
//         \r         carriage return (hex 0D)
//         \t         tab (hex 09)
//         \ddd       character with octal code ddd, or backreference
//         \xhh       character with hex code hh
//         \x{hhh..}  character with hex code hhh..
BellChar       : '\\a';
ControlChar    : '\\c';
EscapeChar     : '\\e';
FormFeed       : '\\f';
NewLine        : '\\n';
CarriageReturn : '\\r';
Tab            : '\\t';
Backslash      : '\\';
HexChar        : '\\x' ( HexDigit HexDigit
                       | '{' HexDigit HexDigit HexDigit+ '}'
                       )
               ;

// CHARACTER TYPES
//
//         .          any character except newline;
//                      in dotall mode, any character whatsoever
//         \C         one data unit, even in UTF mode (best avoided)
//         \d         a decimal digit
//         \D         a character that is not a decimal digit
//         \h         a horizontal white space character
//         \H         a character that is not a horizontal white space character
//         \N         a character that is not a newline
//         \p{xx}     a character with the xx property
//         \P{xx}     a character without the xx property
//         \R         a newline sequence
//         \s         a white space character
//         \S         a character that is not a white space character
//         \v         a vertical white space character
//         \V         a character that is not a vertical white space character
//         \w         a "word" character
//         \W         a "non-word" character
//         \X         an extended Unicode sequence
//
//       In  PCRE,  by  default, \d, \D, \s, \S, \w, and \W recognize only ASCII
//       characters, even in a UTF mode. However, this can be changed by setting
//       the PCRE_UCP option.
Dot                     : '.';
OneDataUnit             : '\\C';
DecimalDigit            : '\\d';
NotDecimalDigit         : '\\D';
HorizontalWhiteSpace    : '\\h';
NotHorizontalWhiteSpace : '\\H';
NotNewLine              : '\\N';
CharWithProperty        : '\\p{' UnderscoreAlphaNumerics '}';
CharWithoutProperty     : '\\P{' UnderscoreAlphaNumerics '}';
NewLineSequence         : '\\R';
WhiteSpace              : '\\s';
NotWhiteSpace           : '\\S';
VerticalWhiteSpace      : '\\v';
NotVerticalWhiteSpace   : '\\V';
WordChar                : '\\w';
NotWordChar             : '\\W';
ExtendedUnicodeChar     : '\\X';

// CHARACTER CLASSES
//
//         [...]       positive character class
//         [^...]      negative character class
//         [x-y]       range (can be used for hex characters)
//         [[:xxx:]]   positive POSIX named set
//         [[:^xxx:]]  negative POSIX named set
//
//         alnum       alphanumeric
//         alpha       alphabetic
//         ascii       0-127
//         blank       space or tab
//         cntrl       control character
//         digit       decimal digit
//         graph       printing, excluding space
//         lower       lower case letter
//         print       printing, including space
//         punct       printing, excluding alphanumeric
//         space       white space
//         upper       upper case letter
//         word        same as \w
//         xdigit      hexadecimal digit
//
//       In PCRE, POSIX character set names recognize only ASCII  characters  by
//       default,  but  some  of them use Unicode properties if PCRE_UCP is set.
//       You can use \Q...\E inside a character class.
CharacterClassStart  : '[';
CharacterClassEnd    : ']';
Caret                : '^';
Hyphen               : '-';
POSIXNamedSet        : '[[:' AlphaNumerics ':]]';
POSIXNegatedNamedSet : '[[:^' AlphaNumerics ':]]';

QuestionMark : '?';
Plus         : '+';
Star         : '*';
OpenBrace    : '{';
CloseBrace   : '}';
Comma        : ',';

// ANCHORS AND SIMPLE ASSERTIONS
//
//         \b          word boundary
//         \B          not a word boundary
//         ^           start of subject
//                      also after internal newline in multiline mode
//         \A          start of subject
//         $           end of subject
//                      also before newline at end of subject
//                      also before internal newline in multiline mode
//         \Z          end of subject
//                      also before newline at end of subject
//         \z          end of subject
//         \G          first matching position in subject
WordBoundary                   : '\\b';
NonWordBoundary                : '\\B';
StartOfSubject                 : '\\A'; 
EndOfSubjectOrLine             : '$';
EndOfSubjectOrLineEndOfSubject : '\\Z'; 
EndOfSubject                   : '\\z'; 
PreviousMatchInSubject         : '\\G';

// MATCH POINT RESET
//
//         \K          reset start of match
ResetStartMatch : '\\K';

SubroutineOrNamedReferenceStartG : '\\g';
NamedReferenceStartK             : '\\k';

Pipe        : '|';
OpenParen   : '(';
CloseParen  : ')';
LessThan    : '<';
GreaterThan : '>';
SingleQuote : '\'';
Underscore  : '_';
Colon       : ':';
Hash        : '#';
Equals      : '=';
Exclamation : '!';
Ampersand   : '&';

ALC : 'a';
BLC : 'b';
CLC : 'c';
DLC : 'd';
ELC : 'e';
FLC : 'f';
GLC : 'g';
HLC : 'h';
ILC : 'i';
JLC : 'j';
KLC : 'k';
LLC : 'l';
MLC : 'm';
NLC : 'n';
OLC : 'o';
PLC : 'p';
QLC : 'q';
RLC : 'r';
SLC : 's';
TLC : 't';
ULC : 'u';
VLC : 'v';
WLC : 'w';
XLC : 'x';
YLC : 'y';
ZLC : 'z';

AUC : 'A';
BUC : 'B';
CUC : 'C';
DUC : 'D';
EUC : 'E';
FUC : 'F';
GUC : 'G';
HUC : 'H';
IUC : 'I';
JUC : 'J';
KUC : 'K';
LUC : 'L';
MUC : 'M';
NUC : 'N';
OUC : 'O';
PUC : 'P';
QUC : 'Q';
RUC : 'R';
SUC : 'S';
TUC : 'T';
UUC : 'U';
VUC : 'V';
WUC : 'W';
XUC : 'X';
YUC : 'Y';
ZUC : 'Z';

D1 : '1';
D2 : '2';
D3 : '3';
D4 : '4';
D5 : '5';
D6 : '6';
D7 : '7';
D8 : '8';
D9 : '9';
D0 : '0';

OtherChar : . ;

// fragments
fragment UnderscoreAlphaNumerics : ('_' | AlphaNumeric)+;
fragment AlphaNumerics           : AlphaNumeric+;
fragment AlphaNumeric            : [a-zA-Z0-9];
fragment NonAlphaNumeric         : ~[a-zA-Z0-9];
fragment HexDigit                : [0-9a-fA-F];
fragment ASCII                   : [\u0000-\u007F];
