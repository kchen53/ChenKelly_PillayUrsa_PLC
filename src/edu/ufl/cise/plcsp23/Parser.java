package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.AST;

public class Parser implements IParser {
    public Parser(Scanner scanner) {
        
    }

    public AST parse() throws PLCException {
        return new AST();
    }

    public void expr() { 
        term();
        while (isKind(PLUS, MINUS)) { 
            consume(); 
            term(); 
        }
        return;
    }

    void term() { 
        factor();
        while (isKind(TIMES,DIV)) {
            consume();
            factor();
        }
        return;
    }

    void primary_expr() {
        if (isKind(STRING_LIT)) {
            consume();
        } else if (isKind(NUM_LIT)) {
            consume();
        } else if (isKind(IDENT)) {
            consume();
        } else if (isKind(Z)) {
            consume();
        } else if (isKind(rand)) {
            consume();
        } else if (isKind(LPAREN)) {
            consume();
            expr();
            match(RPAREN);
        } else error();
        return;
    }
}
