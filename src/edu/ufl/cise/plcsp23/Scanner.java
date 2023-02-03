package edu.ufl.cise.plcsp23;
import java.util.Arrays;

public class Scanner implements IScanner {
    final String input;
    //array containing input chars, terminated with extra char 0

    final char[] inputChars;
    //invariant ch == inputChars[pos]

    int pos; //position of ch
    char ch; //next char

    //constructor
    public Scanner(String input) {
        this.input = input;
        inputChars = Arrays.copyOf(input.toCharArray(), input.length() + 1);
        pos = 0;
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
        HAVE_EQ,
    }

    private static HashMap<String, Kind> reservedWords;
    static {
        reservedWords = new HashMap<String, Kind>();
        reservedWords.put("if", RES_IF);
        reservedWords.put("else", RES_ELSE);
    }

    private void nextChar(){
        pos += 1;
        ch = inputChars[pos];
    }

    private boolean isDigit(int ch) {
        return '0' <= ch && ch <= '9';
    }

    private boolean isLetter(int ch) {
        return ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z');
    }

    private boolean isIdentStart(int ch) {
        return isLetter(ch) || (ch == '$') || (ch == '_');
    }

    private void error(String message) throws LexicalException {
        throw new LexicalException("Error at pos " + pos + ": " + message); 
    }

    private Token scanToken() throws LexicalException {
        State state = State.START;
        int tokenStart = -1;
        while(true) { //read chars, loop terminates when a Token is returned 
            switch(state) {
                case START -> {
                    tokenStart = pos;
                    switch(ch) {
                        case 0 -> { //end of input
                            return new Token(EOF, tokenStart, 0, inputChars);
                        }
                        case ' ','\n','\r','\t','\f' -> nextChar();
                        case '+' -> {
                            nextChar(); 
                            return new Token(PLUS, tokenStart, 1, inputChars);
                        }
                        case '*' -> {
                            nextChar();
                            return new Token(TIMES, tokenStart, 1, inputChars);
                        }
                        case '0' -> {
                            nextChar();
                            return new Token(NUM_LIT, tokenStart, 1, inputChars);
                        }
                        case '1','2','3','4','5','6','7','8','9' -> {   //nonzero digit
                            state = State.IN_NUM_LIT;
                            nextChar();
                        } 
                        default -> {
                            if (isLetter(ch)) {
                                state = State.IN_IDENT;
                                nextChar();
                              }
                              else error("illegal char with ascii value: " + (int)ch);
                            }
                        }
                    }
                }
                case HAVE_EQ -> {}
                case IN_NUM_LIT -> {
                    if (isDigit(ch)) {//char is digit, continue in IN_NUM_LIT state
                        nextChar();
                    } else {
                    //current char belongs to next token, so don't get next char
                    int length = pos-tokenStart;
                        return new Token(Kind.NUM_LIT, tokenStart, length, inputChars); 
                    }
                }
                case IN_IDENT -> {
                    if (isIdentStart(ch) || isDigit(ch)) {
                        nextChar();
                    } else {
                        //current char belongs to next token, so don't get next char
                        int length = pos-tokenStart; 
                        //determine if this is a reserved word. If not, it is an ident.
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
