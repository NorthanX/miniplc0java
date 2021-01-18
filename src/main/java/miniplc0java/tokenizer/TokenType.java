package miniplc0java.tokenizer;

public enum TokenType {
    //关键字
    //    FN_KW     -> 'fn'
    FN_KW,
    //    LET_KW    -> 'let'
    LET_KW,
    //    CONST_KW  -> 'const'
    CONST_KW,
    //    AS_KW     -> 'as'
    AS_KW,
    //    WHILE_KW  -> 'while'
    WHILE_KW,
    //    IF_KW     -> 'if'
    IF_KW,
    //    ELSE_KW   -> 'else'
    ELSE_KW,
    //    RETURN_KW -> 'return'
    RETURN_KW,

    // 这两个是扩展 c0 的
    //    BREAK_KW  -> 'break'
    BREAK_KW,
    //    CONTINUE_KW -> 'continue'
    CONTINUE_KW,

    //字面量
    //    digit -> [0-9]
    digit,
    //    UINT_LITERAL -> digit+
    UINT_LITERAL,
    //    escape_sequence -> '\' [\\"'nrt]
    escape_sequence,
    //    string_regular_char -> [^"\\]
    string_regular_char,
    //    STRING_LITERAL -> '"' (string_regular_char | escape_sequence)* '"'
    STRING_LITERAL,

    // 扩展 c0
    //    DOUBLE_LITERAL -> digit+ '.' digit+ ([eE] [+-]? digit+)?
    DOUBLE_LITERAL,
    //    char_regular_char -> [^'\\]
    char_regular_char,
    //    CHAR_LITERAL -> '\'' (char_regular_char | escape_sequence) '\''
    CHAR_LITERAL,

    //标识符
    //IDENT -> [_a-zA-Z] [_a-zA-Z0-9]*
    IDENT,

    //运算符
    //PLUS      -> '+'
    PLUS,
    //MINUS     -> '-'
    MINUS,
    //MUL       -> '*'
    MUL,
    //DIV       -> '/'
    DIV,
    //ASSIGN    -> '='
    ASSIGN,
    //EQ        -> '=='
    EQ,
    //NEQ       -> '!='
    NEQ,
    //LT        -> '<'
    LT,
    //GT        -> '>'
    GT,
    //LE        -> '<='
    LE,
    //GE        -> '>='
    GE,
    //L_PAREN   -> '('
    L_PAREN,
    //R_PAREN   -> ')'
    R_PAREN,
    //L_BRACE   -> '{'
    L_BRACE,
    //R_BRACE   -> '}'
    R_BRACE,
    //ARROW     -> '->'
    ARROW,
    //COMMA     -> ','
    COMMA,
    //COLON     -> ':'
    COLON,
    //SEMICOLON -> ';'
    SEMICOLON,

    //注释
    //COMMENT -> '//' regex(.*) '\n'
    COMMENT,

    //空
    None,
    //EOF
    EOF;
}
