package edu.ufl.cise.plcsp23;
import java.util.Arrays;

import edu.ufl.cise.plcsp23.IToken.Kind;

public class Scanner implements IScanner {
    final String input; //array containing input chars, terminated with extra char 0

    final char[] inputChars;

    int pos; //current position
    int line; //current line
    char ch; //current char

    //constructor
    public Scanner(String input) {
        this.input = input;
        inputChars = Arrays.copyOf(input.toCharArray(), input.length() + 1);
        pos = 0;
        line = 1;
        ch = inputChars[pos];
    }


    @Override
    public Token next() throws LexicalException {
        return scanToken();
    }

    private enum State {
        START,
        IN_IDENT,
        IN_NUM_LIT,
        IN_STRING_LIT,
        IN_COMMENT,
        HAVE_EQ,
        HAVE_LT,
        HAVE_GT,
        HAVE_AND,
        HAVE_OR,
        HAVE_TIMES
    }

    private static HashMap<String, Kind> reservedWords;
    static {
        reservedWords = new HashMap<String, Kind>();
        reservedWords.put("image", RES_image);
        reservedWords.put("pixel", RES_pixel);
        reservedWords.put("int", RES_int);
        reservedWords.put("string", RES_string);
        reservedWords.put("void", RES_void);
        reservedWords.put("nil", RES_nil);
        reservedWords.put("load", RES_load);
        reservedWords.put("display", RES_display);
        reservedWords.put("write", RES_write);
        reservedWords.put("x", RES_x);
        reservedWords.put("y", RES_y);
        reservedWords.put("a", RES_a);
        reservedWords.put("r", RES_r);
        reservedWords.put("X", RES_X);
        reservedWords.put("Y", RES_Y);
        reservedWords.put("Z", RES_Z);
        reservedWords.put("x_cart", RES_x_cart);
        reservedWords.put("y_cart", RES_y_cart);
        reservedWords.put("a_polar", RES_a_polar);
        reservedWords.put("r_polar", RES_r_polar);
        reservedWords.put("rand", RES_rand);
        reservedWords.put("sin", RES_sin);
        reservedWords.put("cos", RES_cos);
        reservedWords.put("atan", RES_atan);
        reservedWords.put("if", RES_if);
        reservedWords.put("while", RES_while);
    }

    //move to next char in input
    private void nextChar(){
        pos += 1;
        ch = inputChars[pos];
    }

    //helper functions to check char type
    private boolean isDigit(int ch) {
        return '0' <= ch && ch <= '9';
    }

    private boolean isLetter(int ch) {
        return ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z');
    }

    private boolean isIdentStart(int ch) {
        return isLetter(ch) || (ch == '_');
    }

    //function to throw exception
    private void error(String message) throws LexicalException {
        throw new LexicalException("Error at pos " + pos + ": " + message); 
    }

    private Token scanToken() throws LexicalException {
        State state = State.START;
        int tokenStart = -1;
        while(true) {
            switch(state) {
                case START -> {
                    tokenStart = pos;
                    switch(ch) {
                        //end of input
                        case 0 -> {
                            return new Token(KIND.EOF, tokenStart, 0, inputChars);
                        }

                        //whitespace
                        case ' ','\r','\t','\f' -> nextChar();

                        //newline
                        case '\n' -> {
                            line++;
                            nextChar();
                        }

                        //operators and separators
                        case '.' -> {
                            nextChar(); 
                            return new Token(Kind.DOT, tokenStart, 1, inputChars);
                        }
                        case ',' -> {
                            nextChar();
                            return new Token(Kind.COMMA, tokenStart, 1, inputChars);
                        }
                        case '?' -> {
                            nextChar(); 
                            return new Token(Kind.QUESTION, tokenStart, 1, inputChars);
                        }
                        case ':' -> {
                            nextChar();
                            return new Token(Kind.COLON, tokenStart, 1, inputChars);
                        }
                        case '(' -> {
                            nextChar(); 
                            return new Token(Kind.LPAREN, tokenStart, 1, inputChars);
                        }
                        case ')' -> {
                            nextChar();
                            return new Token(Kind.RPAREN, tokenStart, 1, inputChars);
                        }
                        case '<' -> {
                            state = State.HAVE_LT;
                        }
                        case '>' -> {
                            state = State.HAVE_GT;
                        }
                        case '[' -> {
                            nextChar();
                            return new Token(Kind.LSQUARE, tokenStart, 1, inputChars);
                        }
                        case ']' -> {
                            nextChar();
                            return new Token(Kind.RSQUARE, tokenStart, 1, inputChars);
                        }
                        case '{' -> {
                            nextChar();
                            return new Token(Kind.LCURLY, tokenStart, 1, inputChars);
                        }
                        case '}' -> {
                            nextChar();
                            return new Token(Kind.RCURLY, tokenStart, 1, inputChars);
                        }
                        case '=' -> {
                            state = State.HAVE_EQ;
                        }
                        case '!' -> {
                            nextChar();
                            return new Token(Kind.BANG, tokenStart, 1, inputChars);
                        }
                        case '&' -> {
                            state = State.HAVE_AND;
                        }
                        case '|' -> {
                            state = State.HAVE_OR;
                        }
                        case '+' -> {
                            nextChar();
                            return new Token(Kind.PLUS, tokenStart, 1, inputChars);
                        }
                        case '-' -> {
                            nextChar();
                            return new Token(Kind.MINUS, tokenStart, 1, inputChars);
                        }
                        case '*' -> {
                            state = State.HAVE_TIMES;
                        }
                        case '/' -> {
                            nextChar();
                            return new Token(Kind.DIV, tokenStart, 1, inputChars);
                        }
                        case '%' -> {
                            nextChar();
                            return new Token(Kind.MOD, tokenStart, 1, inputChars);
                        }

                        //comments
                        case '~' -> {
                            state = State.IN_COMMENT;
                            nextChar();
                        }

                        //string literals
                        case '"' -> {
                            state = State.IN_STRING_LIT;
                        }

                        //zero num literal
                        case '0' -> {
                            nextChar();
                            return new Token(Kind.NUM_LIT, tokenStart, 1, inputChars);
                        }

                        //num literals
                        case '1','2','3','4','5','6','7','8','9' -> {   //nonzero digit
                            state = State.IN_NUM_LIT;
                            nextChar();
                        }

                        //idents or reserved words
                        default -> {
                            if (isIdentStart(ch)) {
                                state = State.IN_IDENT;
                                nextChar();
                            }
                            else error("illegal char with ascii value: " + (int)ch);
                        }
                    }
                }

                //less than, exchange, or less than or equal to
                case HAVE_LT -> {
                    nextChar();
                    if(ch == '=') {
                        nextChar();
                        return new Token(Kind.LE, tokenStart, 2, inputChars); 
                    } else if (ch == '-') {
                        nextChar();
                        if (ch == '>') {
                            nextChar();
                            return new Token(Kind.EXCHANGE, tokenStart, 3, inputChars); 
                        }
                    } else {
                        return new Token(Kind.LT, tokenStart, 1, inputChars);
                    }
                }

                //greater than or greater than or equal to
                case HAVE_GT -> {
                    nextChar();
                    if(ch == '=') {
                        nextChar();
                        return new Token(Kind.GE, tokenStart, 2, inputChars); 
                    } else {
                        return new Token(Kind.GT, tokenStart, 1, inputChars);
                    }
                }

                //assign or equal to
                case HAVE_EQ -> {
                    nextChar();
                    if(ch == '=') {
                        nextChar();
                        return new Token(Kind.EQ, tokenStart, 2, inputChars); 
                    } else {
                        return new Token(Kind.ASSIGN, tokenStart, 1, inputChars);
                    }
                }

                //bitand or and
                case HAVE_AND -> {
                    nextChar();
                    if(ch == '&') {
                        nextChar();
                        return new Token(Kind.AND, tokenStart, 2, inputChars); 
                    } else {
                        return new Token(Kind.BITAND, tokenStart, 1, inputChars);
                    }
                }

                //bitor or or
                case HAVE_OR -> {
                    nextChar();
                    if(ch == '|') {
                        nextChar();
                        return new Token(Kind.OR, tokenStart, 2, inputChars); 
                    } else {
                        return new Token(Kind.BITOR, tokenStart, 1, inputChars);
                    }
                }

                //times or exponent
                case HAVE_TIMES -> {
                    nextChar();
                    if(ch == '*') {
                        nextChar();
                        return new Token(Kind.EXP, tokenStart, 2, inputChars); 
                    } else {
                        return new Token(Kind.TIMES, tokenStart, 1, inputChars);
                    }
                }

                //inside comment until newline
                case IN_COMMENT -> {
                    if (ch != '\n') {
                        nextChar();
                    } else {
                        line++;
                        nextChar();
                        state = State.START; 
                    }
                }

                //inside num literal until nondigit
                case IN_NUM_LIT -> {
                    if (isDigit(ch)) {
                        nextChar();
                    } else {
                        int length = pos-tokenStart;
                        return new Token(Kind.NUM_LIT, tokenStart, length, inputChars); 
                    }
                }

                //inside string literal until end quote
                case IN_STRING_LIT -> {
                    if (ch != '\"') {
                        nextChar();
                    } else {
                        int length = pos-tokenStart + 1;
                        nextChar();
                        return new Token(Kind.STRING_LIT, tokenStart, length, inputChars);
                    }
                }

                //ident or reserved word
                case IN_IDENT -> {
                    if (isIdentStart(ch) || isDigit(ch)) {
                        nextChar();
                    } else {
                        int length = pos-tokenStart; 
                        String text = input.substring(tokenStart, tokenStart + length);
                        Kind kind = reservedWords.get(text);
                        if (kind == null) { 
                            kind = IDENT; 
                        }
                        return new Token(kind, tokenStart, length, inputChars); 
                    }
                }
                default -> {
                    throw new UnsupportedOperationException("Bug in Scanner");
                }
            }
        }
    }  
}
