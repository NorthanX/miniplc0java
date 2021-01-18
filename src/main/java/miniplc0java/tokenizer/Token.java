package miniplc0java.tokenizer;

import util.Position;

public class Token {
    private miniplc0java.tokenizer.TokenType tokenType;
    private Object value;
    private Position startPos;
    private Position endPos;

    public Token(miniplc0java.tokenizer.TokenType tokenType, Object value, Position startPos, Position endPos) {
        this.tokenType = tokenType;
        this.value = value;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public miniplc0java.tokenizer.TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(miniplc0java.tokenizer.TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Position getStartPos() {
        return startPos;
    }

    public void setStartPos(Position startPos) {
        this.startPos = startPos;
    }

    public Position getEndPos() {
        return endPos;
    }

    public void setEndPos(Position endPos) {
        this.endPos = endPos;
    }
}
