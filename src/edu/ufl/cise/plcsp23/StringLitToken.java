package edu.ufl.cise.plcsp23;

public class StringLitToken extends Token implements IStringLitToken {

    //constructor
    public StringLitToken(Kind kind, int pos, int length, int line, int column, char[] source) {
        super(kind, pos, length, line, column, source);
    }

    //returns value of string with escape sequences converted to ascii chars
    @Override
    public String getValue() {
        String myString = new String(source, pos + 1, length - 2);
        String value = "";
        for (int i = 0; i < myString.length(); i++) {
            if (myString.charAt(i) == '\\') {
                i++;
                if (myString.charAt(i) == 'n') {
                    value += '\n';
                } else if (myString.charAt(i) == 't') {
                    value += '\t';
                } else if (myString.charAt(i) == 'b') {
                    value += '\b';
                } else if (myString.charAt(i) == 'r') {
                    value += '\r';
                } else if (myString.charAt(i) == 'f') {
                    value += '\f';
                } else if (myString.charAt(i) == '"') {
                    value += '\"';
                } else if (myString.charAt(i) == '\\') {
                    value += '\\';
                }
            } else {
                value += myString.charAt(i);
            }
        }
        return value;
    }
    
}
