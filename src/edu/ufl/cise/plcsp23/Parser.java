package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.ast.BinaryExpr;
import edu.ufl.cise.plcsp23.ast.Expr;
import edu.ufl.cise.plcsp23.ast.IdentExpr;
import edu.ufl.cise.plcsp23.ast.NumLitExpr;
import edu.ufl.cise.plcsp23.ast.RandomExpr;
import edu.ufl.cise.plcsp23.ast.StringLitExpr;
import edu.ufl.cise.plcsp23.ast.UnaryExpr;
import edu.ufl.cise.plcsp23.ast.ZExpr;

public class Parser implements IParser {
    IToken t;
    Scanner scan;
    AST ast;

    public Parser(Scanner scanner) throws PLCException {
        scan = scanner;
    }

    public AST parse() throws PLCException {
        t = scan.next();
        
        return ast;
    }

    protected boolean isKind(Kind kind) {   //check kind of current token
        return t.getKind() == kind;
    }

    void consume() throws LexicalException {    //go to next token
        t = scan.next();
    }


    Expr expr() { 
        
    }

    Expr power_expr() throws LexicalException, SyntaxException {
        IToken firstToken = t;  //current token
        Expr left = null;       //left side of binary expression
        Kind op = null;         //operator
        Expr right = null;      //right side of binary expression

        //left expression is additive expression
        left = add_expr();

        //if token is ** operator, set kind to EXP and go to next token, which is a power expression
        if (isKind(Kind.EXP)) {
            op = Kind.EXP;
            consume();
            right = power_expr();
        } else {
            //if not **, return single additive expression
            return left;
        }

        return new BinaryExpr(firstToken, left, op, right);
    }

    Expr add_expr() throws SyntaxException, LexicalException {
        IToken firstToken = t;  //current token
        Expr left = null;       //left side of binary expression
        Kind op = null;         //operator
        Expr right = null;      //right side of binary expression

        //left expression is a multiplicative expression
        left = mult_expr();

        //if token is not + or - operator, return single multiplicative expression
        if (!isKind(Kind.PLUS) && !isKind(Kind.MINUS)) {
            return left;
        }

        //while next token is + or - operator, set kind to operator type and go to next token
        while (isKind(Kind.PLUS) || isKind(Kind.MINUS)) {
            if (isKind(Kind.PLUS)) {
                op = Kind.PLUS;
                consume();
            } else if (isKind(Kind.MINUS)) {
                op = Kind.MINUS;
                consume();
            }

            //right expression is multiplicative expression
            right = mult_expr();
        }

        //return binary expression object for AST
        return new BinaryExpr(firstToken, left, op, right);
    }

    Expr mult_expr() throws SyntaxException, LexicalException {
        IToken firstToken = t;  //current token
        Expr left = null;       //left side of binary expression
        Kind op = null;         //operator
        Expr right = null;      //right side of binary expression

        //left expression is a unary expression
        left = unary_expr();

        //if token is not *, /, or % operator, return single unary expression
        if (!isKind(Kind.TIMES) && !isKind(Kind.DIV) && !isKind(Kind.MOD)) {
            return left;
        }

        //while next token is *, /, or % operator, set kind to operator type and go to next token
        while (isKind(Kind.TIMES) || isKind(Kind.DIV) || isKind(Kind.MOD)) {
            if (isKind(Kind.TIMES)) {
                op = Kind.TIMES;
                consume();
            } else if (isKind(Kind.DIV)) {
                op = Kind.DIV;
                consume();
            } else if (isKind(Kind.MOD)) {
                op = Kind.MOD;
                consume();
            }

            //right expression is unary expression
            right = unary_expr();
        }

        //return binary expression object for AST
        return new BinaryExpr(firstToken, left, op, right);
    }

    Expr unary_expr() throws LexicalException, SyntaxException {
        IToken firstToken = t;
        Kind op = null;         //initialize operator and expression
        Expr e = null;

        //if token is unary operator, set kind and move to next token, which is a unary expression
        if (isKind(Kind.BANG)) {
            op = Kind.BANG;
            consume();
            e = unary_expr();
        } else if (isKind(Kind.MINUS)) {
            op = Kind.MINUS;
            consume();
            e = unary_expr();
        } else if (isKind(Kind.RES_sin)) {
            op = Kind.RES_sin;
            consume();
            e = unary_expr();
        } else if (isKind(Kind.RES_cos)) {
            op = Kind.RES_cos;
            consume();
            e = unary_expr();
        } else if (isKind(Kind.RES_atan)) {
            op = Kind.RES_atan;
            consume();
            e = unary_expr();
        } else {
            //if token is not operator, return single primary expression
            return primary_expr();
        }

        //return unary expression object for AST
        return new UnaryExpr(firstToken, op, e);
    }

    Expr primary_expr() throws SyntaxException, LexicalException {
        IToken firstToken = t;  //save current token
        Expr e = null;          //initialize expression

        //if token is specified kind, set e to appropriate expression object and go to next token
        if (isKind(Kind.STRING_LIT)) {
            e = new StringLitExpr(firstToken);
            consume();
        } else if (isKind(Kind.NUM_LIT)) {
            e = new NumLitExpr(firstToken);
            consume();
        } else if (isKind(Kind.IDENT)) {
            e = new IdentExpr(firstToken);
            consume();
        } else if (isKind(Kind.RES_Z)) {
            e = new ZExpr(firstToken);
            consume();
        } else if (isKind(Kind.RES_rand)) {
            e = new RandomExpr(firstToken);
            consume();
        } else if (isKind(Kind.LPAREN)) {
            consume();
            e = expr();
            if (isKind(Kind.RPAREN)) {
                consume();
            } else {
                throw new SyntaxException("syntax error");
            }
        } else {
            throw new SyntaxException("syntax error");
        };

        return e;   //return expression object for AST
    }
}
