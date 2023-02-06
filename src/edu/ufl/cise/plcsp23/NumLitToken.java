package edu.ufl.cise.plcsp23;

public class NumLitToken extends Token implements INumLitToken {

    public NumLitToken(Kind kind, int pos, int length, int line, int column, char[] source) {
        super(kind, pos, length, line, column, source);
    }

    //returns integer from num literal
    @Override
    public int getValue() {
        String number = new String(source, pos, length);
        return Integer.parseInt(number);
    }
    
}
