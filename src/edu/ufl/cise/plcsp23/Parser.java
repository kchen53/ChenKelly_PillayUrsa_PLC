package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.ast.BinaryExpr;
import edu.ufl.cise.plcsp23.ast.ConditionalExpr;
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

    public Parser(Scanner scanner) throws PLCException {
        scan = scanner;
        t = scan.next();
    }

    public AST parse() throws PLCException {
        return expr();
    }

    protected boolean isKind(Kind kind) {   //check kind of current token
        return t.getKind() == kind;
    }

    void consume() throws LexicalException {    //go to next token
        t = scan.next();
    }

    Expr expr() throws SyntaxException, LexicalException {
        Expr e = null;
        
        //if expression starts with if, it is conditional expression, else it is or expression
        if (isKind(Kind.RES_if)) {
            e = conditional_expr();
        } else {
            e = or_expr();
        }

        return e;   //return expression
    }

    Expr conditional_expr() throws SyntaxException, LexicalException{
        IToken firstToken = t;  //current token
        Expr e1 = null;       //three expressions in conditional expression
        Expr e2 = null;
        Expr e3 = null;

        //check if expression contains if, ?, and ?, expression between each
        if (isKind(Kind.RES_if)) {
            consume();
            e1 = expr();
            if (isKind(Kind.QUESTION)) {
                consume();
                e2 = expr();
                if (isKind(Kind.QUESTION)) {
                    consume();
                    e3 = expr();
                } else {
                    throw new SyntaxException("syntax error");
                }
            } else {
                throw new SyntaxException("syntax error");
            }
        } else {
            throw new SyntaxException("syntax error");
        }

        //return conditional expression object for AST
        return new ConditionalExpr(firstToken, e1, e2, e3);
    }

    Expr or_expr() throws SyntaxException, LexicalException {
        IToken firstToken = t;  //current token
        Expr left = null;       //left side of binary expression
        Kind op = null;         //operator
        Expr right = null;      //right side of binary expression

        //left expression is an and expression
        left = and_expr();

        //if token is not | or || operator, return single and expression
        if (!isKind(Kind.BITOR) && !isKind(Kind.OR)) {
            return left;
        }

        //while next token is | or || operator, set kind to operator type and go to next token
        while (isKind(Kind.BITOR) || isKind(Kind.OR)) {
            if (isKind(Kind.BITOR)) {
                op = Kind.BITOR;
                consume();
            } else if (isKind(Kind.OR)) {
                op = Kind.OR;
                consume();
            }

            //right expression is an and expression
            right = and_expr();

            if (isKind(Kind.BITOR) || isKind(Kind.OR)) {
                left = new BinaryExpr(firstToken, left, op, right);
            }
        }

        //return binary expression object for AST
        return new BinaryExpr(firstToken, left, op, right);
    }

    Expr and_expr() throws SyntaxException, LexicalException {
        IToken firstToken = t;  //current token
        Expr left = null;       //left side of binary expression
        Kind op = null;         //operator
        Expr right = null;      //right side of binary expression

        //left expression is a comparison expression
        left = compare_expr();

        //if token is not & or && operator, return single comparision expression
        if (!isKind(Kind.BITAND) && !isKind(Kind.AND)) {
            return left;
        }

        //while next token is & or && operator, set kind to operator type and go to next token
        while (isKind(Kind.BITAND) || isKind(Kind.AND)) {
            if (isKind(Kind.BITAND)) {
                op = Kind.BITAND;
                consume();
            } else if (isKind(Kind.AND)) {
                op = Kind.AND;
                consume();
            }

            //right expression is a comparison expression
            right = compare_expr();

            if (isKind(Kind.BITAND) || isKind(Kind.AND)) {
                left = new BinaryExpr(firstToken, left, op, right);
            }
        }

        //return binary expression object for AST
        return new BinaryExpr(firstToken, left, op, right);
    }

    Expr compare_expr() throws SyntaxException, LexicalException {

        IToken firstToken = t;  //current token
        Expr left = null;       //left side of binary expression
        Kind op = null;         //operator
        Expr right = null;      //right side of binary expression

        //left expression is a power expression
        left = power_expr();

        //if token is not <, >, ==, <=, >= operator, return single power expression
        if (!isKind(Kind.LT) && !isKind(Kind.GT) && !isKind(Kind.EQ) && !isKind(Kind.LE) && !isKind(Kind.GE)) {
            return left;
        }

        //while next token is <, >, ==, <=, >= operator, set kind to operator type and go to next token
        while (isKind(Kind.LT) || isKind(Kind.GT) || isKind(Kind.EQ) || isKind(Kind.LE) || isKind(Kind.GE)) {
            if (isKind(Kind.LT)) {
                op = Kind.LT;
                consume();
            } else if (isKind(Kind.GT)) {
                op = Kind.GT;
                consume();
            }
            else if (isKind(Kind.EQ)) {
                op = Kind.EQ;
                consume();
            }
            else if (isKind(Kind.LE)) {
                op = Kind.LE;
                consume();
            }
            else if (isKind(Kind.GE)) {
                op = Kind.GE;
                consume();
            }

            //right expression is power expression
            right = power_expr();

            if (isKind(Kind.LT) || isKind(Kind.GT) || isKind(Kind.EQ) || isKind(Kind.LE) || isKind(Kind.GE)) {
                left = new BinaryExpr(firstToken, left, op, right);
            }
        }

        //return binary expression object for AST
        return new BinaryExpr(firstToken, left, op, right);
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

            //if next token is + or - operator, new left side is whole expression
            if (isKind(Kind.PLUS) || isKind(Kind.MINUS)) {
                left = new BinaryExpr(firstToken, left, op, right);
            }
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

            //if next token is *, /, or % operator, new left side is whole expression
            if (isKind(Kind.TIMES) || isKind(Kind.DIV) || isKind(Kind.MOD)) {
                left = new BinaryExpr(firstToken, left, op, right);
            }
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
