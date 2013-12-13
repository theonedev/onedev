(function(hljs) {
    // This is to fix the following issue, which (still) has an open PR
    // https://github.com/isagalaev/highlight.js/issues/243
    var ANNOTATION = {
        className: 'annotation', begin: '@[A-Za-z]+'
    };
    var STRING = {
        className: 'string',
        begin: 'u?r?"""', end: '"""',
        relevance: 10
    };
    hljs.LANGUAGES.scala.contains =  [
        {
            className: 'javadoc',
            begin: '/\\*\\*', end: '\\*/',
            contains: [{
                className: 'javadoctag',
                begin: '@[A-Za-z]+'
            }],
            relevance: 10
        },
        hljs.C_LINE_COMMENT_MODE, hljs.C_BLOCK_COMMENT_MODE,
        STRING, hljs.APOS_STRING_MODE, hljs.QUOTE_STRING_MODE,
        {
            className: 'class',
            begin: '((case )?class |object |trait )', end: '({|$)', // beginWithKeyword won't work because a single "case" shouldn't start this mode
            illegal: ':',
            keywords: 'case class trait object',
            contains: [
                {
                    beginWithKeyword: true,
                    keywords: 'extends with',
                    relevance: 10
                },
                {
                    className: 'title',
                    begin: hljs.UNDERSCORE_IDENT_RE
                },
                {
                    className: 'params',
                    begin: '\\(', end: '\\)',
                    contains: [
                        hljs.APOS_STRING_MODE, hljs.QUOTE_STRING_MODE, STRING,
                        ANNOTATION
                    ]
                }
            ]
        },
        hljs.C_NUMBER_MODE,
        ANNOTATION
    ];

})(hljs);
