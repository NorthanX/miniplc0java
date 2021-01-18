package miniplc0java.tokenizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

    private StringIterator iterator;

    public Tokenizer(StringIterator iterator) {
        this.iterator = iterator;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws Exception 如果解析有异常则抛出
     */
    public Token nextToken() throws Exception {
        iterator.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (iterator.isEOF()) {
            return new Token(TokenType.EOF, "", iterator.currentPos(), iterator.currentPos());
        }

        char peek = iterator.peekChar();
        if (Character.isDigit(peek)) {
            return lexUInt();
        }
        else if (Character.isAlphabetic(peek) || peek == '_') {
            return lexIdentOrKeyword();
        }
        else if (peek == '"'){
            return lexString();
        }
        else {
            return lexOperatorOrUnknown();
        }
    }

    private void skipSpaceCharacters() {
        while (!iterator.isEOF() && Character.isWhitespace(iterator.peekChar())) {
            iterator.nextChar();
        }
    }

    public Token lexUInt() throws Exception {

        StringBuilder token = new StringBuilder();

        while (Character.isDigit(iterator.peekChar())){
            token.append(iterator.nextChar());
        }

        String uint_Literal = "[0-9]+";
        Pattern pattern = Pattern.compile(uint_Literal);
        Matcher matcher = pattern.matcher(token);
        if (matcher.matches()){
            return new Token(TokenType.UINT_LITERAL, Integer.parseInt(token.toString()), iterator.previousPos(), iterator.currentPos());
        }
        else {
            throw new Exception();//可进行升级
        }
    }

    public Token lexIdentOrKeyword() throws Exception {

        StringBuilder token = new StringBuilder();

        while (Character.isLetterOrDigit(iterator.peekChar()) || iterator.peekChar() == '_' ){
            token.append(iterator.nextChar());
        }

        //首先判断标识符
        if (token.toString().equals("fn")){
            return new Token(TokenType.FN_KW, token.toString(), iterator.previousPos(), iterator.currentPos());
        }
        else if (token.toString().equals("let")){
            return new Token(TokenType.LET_KW, token.toString(), iterator.previousPos(), iterator.currentPos());
        }
        else if (token.toString().equals("const")){
            return new Token(TokenType.CONST_KW, token.toString(), iterator.previousPos(), iterator.currentPos());
        }
        else if (token.toString().equals("as")){
            return new Token(TokenType.AS_KW, token.toString(), iterator.previousPos(), iterator.currentPos());
        }
        else if (token.toString().equals("while")){
            return new Token(TokenType.WHILE_KW, token.toString(), iterator.previousPos(), iterator.currentPos());
        }
        else if (token.toString().equals("if")){
            return new Token(TokenType.IF_KW, token.toString(), iterator.previousPos(), iterator.currentPos());
        }
        else if (token.toString().equals("else")){
            return new Token(TokenType.ELSE_KW, token.toString(), iterator.previousPos(), iterator.currentPos());
        }
        else if (token.toString().equals("return")){
            return new Token(TokenType.RETURN_KW, token.toString(), iterator.previousPos(), iterator.currentPos());
        }
        else if (token.toString().equals("break")){
            return new Token(TokenType.BREAK_KW, token.toString(), iterator.previousPos(), iterator.currentPos());
        }
        else if (token.toString().equals("continue")){
            return new Token(TokenType.CONTINUE_KW, token.toString(), iterator.previousPos(), iterator.currentPos());
        }

        //开始找标识符
        String uint_Literal = "[_a-zA-Z][_a-zA-Z0-9]*";
        Pattern pattern = Pattern.compile(uint_Literal);
        Matcher matcher = pattern.matcher(token);
        if (matcher.matches()){
            return new Token(TokenType.IDENT, token.toString(), iterator.previousPos(), iterator.currentPos());
        }
        else {
            throw new Exception();//可进行升级
        }
    }

    private Token lexString() throws Exception{
        String str = "";
        int limit=20000;
        char a;
        while(limit>=0){
            limit--;
            a=iterator.nextChar();
            if(a=='\\'){
                a=iterator.nextChar();
                if(a=='\\'){
                    str+='\\';
                }
                else if(a=='n'){
                    str+='\n';
                }
                else if(a=='"'){
                    str+='"';
                }
                else if(a=='\''){
                    str+='\'';
                }
                else if(a=='r'){
                    str+='\r';
                }
                else if(a=='t'){
                    str+='\t';
                }
                else{
                    throw new Exception();
                }
            }
            else{
                if(a=='"'){
                    return new Token(TokenType.STRING_LITERAL, str, iterator.previousPos(), iterator.currentPos());
                }
                else{
                    str+=a;
                }
            }
        }
        throw new Exception();
    }

    public Token lexOperatorOrUnknown() throws Exception {
        char now = iterator.nextChar();

        if (now == '+'){
            return new Token(TokenType.PLUS, '+', iterator.previousPos(), iterator.currentPos());
        }
        else if (now == '-'){
            if (iterator.peekChar() == '>'){
                iterator.nextChar();
                return new Token(TokenType.ARROW, "->", iterator.previousPos(), iterator.currentPos());
            }
            return new Token(TokenType.MINUS, '-', iterator.previousPos(), iterator.currentPos());
        }
        else if (now == '*'){
            return new Token(TokenType.MUL, '*', iterator.previousPos(), iterator.currentPos());
        }
        else if (now == '/'){
            return new Token(TokenType.DIV, '/', iterator.previousPos(), iterator.currentPos());
        }
        else if (now == '='){
            if (iterator.peekChar() == '='){
                iterator.nextChar();
                return new Token(TokenType.EQ, "==", iterator.previousPos(), iterator.currentPos());
            }
            return new Token(TokenType.ASSIGN, '=', iterator.previousPos(), iterator.currentPos());
        }
        else if (now == '!'){
            if (iterator.peekChar() == '='){
                iterator.nextChar();
                return new Token(TokenType.NEQ, "!=", iterator.previousPos(), iterator.currentPos());
            }
            throw new Exception();//可进行升级
        }
        else if (now == '<'){
            if (iterator.peekChar() == '='){
                iterator.nextChar();
                return new Token(TokenType.LE, "<=", iterator.previousPos(), iterator.currentPos());
            }
            return new Token(TokenType.LT, '<', iterator.previousPos(), iterator.currentPos());
        }
        else if (now == '>'){
            if (iterator.peekChar() == '='){
                iterator.nextChar();
                return new Token(TokenType.GE, ">=", iterator.previousPos(), iterator.currentPos());
            }
            return new Token(TokenType.GT, '>', iterator.previousPos(), iterator.currentPos());
        }
        else if (now == '('){
            return new Token(TokenType.L_PAREN, '(', iterator.previousPos(), iterator.currentPos());
        }
        else if (now == ')'){
            return new Token(TokenType.R_PAREN, ')', iterator.previousPos(), iterator.currentPos());
        }
        else if (now == '{'){
            return new Token(TokenType.L_BRACE, '{', iterator.previousPos(), iterator.currentPos());
        }
        else if (now == '}'){
            return new Token(TokenType.R_BRACE, '}', iterator.previousPos(), iterator.currentPos());
        }
        else if (now == ','){
            return new Token(TokenType.COMMA, ',', iterator.previousPos(), iterator.currentPos());
        }
        else if (now == ':'){
            return new Token(TokenType.COLON, ':', iterator.previousPos(), iterator.currentPos());
        }
        else if (now == ';'){
            return new Token(TokenType.SEMICOLON, ';', iterator.previousPos(), iterator.currentPos());
        }

        throw new Exception();//可进行升级
    }

}
