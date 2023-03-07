package edu.ufl.cise.plcsp23;
import java.util.ArrayList;
import java.util.List;
import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.ast.AssignmentStatement;
import edu.ufl.cise.plcsp23.ast.BinaryExpr;
import edu.ufl.cise.plcsp23.ast.Block;
import edu.ufl.cise.plcsp23.ast.ColorChannel;
import edu.ufl.cise.plcsp23.ast.ConditionalExpr;
import edu.ufl.cise.plcsp23.ast.Declaration;
import edu.ufl.cise.plcsp23.ast.Dimension;
import edu.ufl.cise.plcsp23.ast.ExpandedPixelExpr;
import edu.ufl.cise.plcsp23.ast.Expr;
import edu.ufl.cise.plcsp23.ast.Ident;
import edu.ufl.cise.plcsp23.ast.IdentExpr;
import edu.ufl.cise.plcsp23.ast.LValue;
import edu.ufl.cise.plcsp23.ast.NameDef;
import edu.ufl.cise.plcsp23.ast.NumLitExpr;
import edu.ufl.cise.plcsp23.ast.PixelFuncExpr;
import edu.ufl.cise.plcsp23.ast.PixelSelector;
import edu.ufl.cise.plcsp23.ast.PredeclaredVarExpr;
import edu.ufl.cise.plcsp23.ast.Program;
import edu.ufl.cise.plcsp23.ast.RandomExpr;
import edu.ufl.cise.plcsp23.ast.Statement;
import edu.ufl.cise.plcsp23.ast.StringLitExpr;
import edu.ufl.cise.plcsp23.ast.Type;
import edu.ufl.cise.plcsp23.ast.UnaryExpr;
import edu.ufl.cise.plcsp23.ast.UnaryExprPostfix;
import edu.ufl.cise.plcsp23.ast.WhileStatement;
import edu.ufl.cise.plcsp23.ast.WriteStatement;
import edu.ufl.cise.plcsp23.ast.ZExpr;

public class Parser implements IParser {
    IToken t;
    Scanner scan;

    public Parser(Scanner scanner) throws PLCException {
        scan = scanner;
        t = scan.next();
    }

    public AST parse() throws PLCException {
        return program();
    }

    protected boolean isKind(Kind kind) {   //check kind of current token
        return t.getKind() == kind;
    }

    protected boolean isType(Type type) {   //check type of current token
        return Type.getType(t) == type;
    }

    void consume() throws LexicalException {    //go to next token
        t = scan.next();
    }

    //Program::=  Type IDENT ( ParamList ) Block
    Program program() throws SyntaxException, LexicalException {
        IToken firstToken = t;
        Type type = null;
        Ident i = null;
        List<NameDef> paramList = null;
        Block b = null;

        type = type();

        if(isKind(Kind.IDENT)){
            i = new Ident(t);
            consume();
            if (isKind(Kind.LPAREN)) {
                consume();
                paramList = ParamList();
                if (isKind(Kind.RPAREN)) {
                    consume();
                    b = block();
                    if (!isKind(Kind.EOF)) {
                        throw new SyntaxException("syntax error");
                    }
                } else {
                    throw new SyntaxException("syntax error");
                }
            } else {
                throw new SyntaxException("syntax error");
            }
        } else {
            throw new SyntaxException("syntax error");
        }

        return new Program(firstToken, type, i, paramList, b);
    }

    //Block ::= { DecList  StatementList }
    Block block() throws SyntaxException, LexicalException {
        IToken firstToken = t;
        List<Declaration> decList = null;
        List<Statement> statementList = null;

        if(isKind(Kind.LCURLY)) {
            consume();
            decList = decList();
            statementList = StatementList();
            if(isKind(Kind.RCURLY)){
                consume();
            } else {
                throw new SyntaxException("syntax error");
            }
        } else {
            throw new SyntaxException("syntax error");
        }

        return new Block(firstToken, decList, statementList);
    }

    //DecList ::= ( Declaration . )*
    List<Declaration> decList() throws SyntaxException, LexicalException {
        List<Declaration> declarations = new ArrayList<>();

        if (!isKind(Kind.IDENT) && !isKind(Kind.RES_write) && !isKind(Kind.RES_while) && !isKind(Kind.RCURLY)) {
            try {
                while((isType(Type.IMAGE)) || (isType(Type.INT)) || (isType(Type.PIXEL)) || (isType(Type.STRING)) || (isType(Type.VOID))){
                    declarations.add(declaration());
                    if (isKind(Kind.DOT)) {
                        consume();
                    } else {
                        throw new SyntaxException("syntax error");
                    }
    
                    if(isKind(Kind.IDENT) || isKind(Kind.RES_write) || isKind(Kind.RES_while) || isKind(Kind.RCURLY)) {
                        return declarations;
                    }
                }
            } catch (Exception runException) {
                throw new SyntaxException("syntax error");
            }
        }

        return declarations;

    }

    //StatementList ::= ( Statement . ) *
    List<Statement> StatementList() throws SyntaxException, LexicalException {
        List<Statement> statements = new ArrayList<>();

        if ((!isKind(Kind.RCURLY))) {
            while(isKind(Kind.IDENT) || isKind(Kind.RES_write) || isKind(Kind.RES_while)){
                statements.add(statement());
                if (isKind(Kind.DOT)) {
                    consume();
                } else {
                    throw new SyntaxException("syntax error");
                }

                if ((isKind(Kind.RCURLY))) {
                    return statements;
                }
            }
        }

        return statements;
    }

    //ParamList ::= ε |  NameDef  ( , NameDef ) *
    List<NameDef> ParamList() throws SyntaxException, LexicalException {
        List<NameDef> nameDefs = new ArrayList<>();

        if ((isKind(Kind.RPAREN))) {
            return nameDefs;
        }

        if ((isType(Type.IMAGE)) || (isType(Type.INT)) || (isType(Type.PIXEL)) || (isType(Type.STRING)) || (isType(Type.VOID))) {
            nameDefs.add(nameDef());
            while(isKind(Kind.COMMA)){
                consume();
                nameDefs.add(nameDef());
            }
        }

        return nameDefs;
    }

    //NameDef ::= Type IDENT | Type Dimension IDENT
    NameDef nameDef() throws SyntaxException, LexicalException{
        IToken firstToken = t;
        Type type = null;
        Ident i = null; 
        Dimension d = null;

        type = type();

        if(isKind(Kind.IDENT)){
            i = new Ident(t);
            consume();
            return new NameDef(firstToken, type, d, i);
        } else {
            d = dimension();
            i = new Ident(t);
            consume();
            return new NameDef(firstToken, type, d, i);
        }
    }
    
    //Type ::= image | pixel | int | string | void
    Type type() throws SyntaxException, LexicalException {
        Type type = null;

        try {
            type = Type.getType(t);
        } catch (Exception runException) {
            throw new SyntaxException("syntax error");
        }

        consume();
        return type; //return type
    }

    //Declaration::= NameDef |  NameDef = Expr
    Declaration declaration() throws SyntaxException, LexicalException {
        IToken firstToken = t;
        NameDef n = null;
        Expr e = null;

        n = nameDef();

        //if token has an expression
        if (isKind(Kind.ASSIGN)) {
            consume();
            e = expr();
            return new Declaration(firstToken, n, e);
        }

        return new Declaration(firstToken, n, e);

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
           return unary_expr_post();
        }

        //return unary expression object for AST
        return new UnaryExpr(firstToken, op, e);
    }

    Expr unary_expr_post() throws LexicalException, SyntaxException {
        IToken firstToken = t;
        Expr primary = null;
        PixelSelector pixel = null;
        ColorChannel color = null;

        primary = primary_expr();

        if (isKind(Kind.LSQUARE)) {
            pixel = pixel_selector();
        }

        if (isKind(Kind.COLON)) {
            color = channel_selector();
        }

        if ((pixel == null) && (color == null)) {
            return primary;
        }

        return new UnaryExprPostfix(firstToken, primary, pixel, color);
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
        } else if (isKind(Kind.RES_x)) {
            e = new PredeclaredVarExpr(firstToken);
            consume();
        } else if (isKind(Kind.RES_y)) {
            e = new PredeclaredVarExpr(firstToken);
            consume();
        } else if (isKind(Kind.RES_a)) {
            e = new PredeclaredVarExpr(firstToken);
            consume();
        } else if (isKind(Kind.RES_r)) {
            e = new PredeclaredVarExpr(firstToken);
            consume();
        } else if (isKind(Kind.LSQUARE)) {
            e = exp_pixel_selector();
        } else {
            e = pixel_func_expr();
        };

        return e;   //return expression object for AST
    }

    ColorChannel channel_selector() throws LexicalException, SyntaxException {
        //check red, grn, or blu reserved word and return proper color channel
        if (isKind(Kind.COLON)) {
            consume();
            if (isKind(Kind.RES_red)) {
                consume();
                return ColorChannel.red;
            } else if (isKind(Kind.RES_grn)) {
                consume();
                return ColorChannel.grn;
            } else if (isKind(Kind.RES_blu)) {
                consume();
                return ColorChannel.blu;
            } else {
                throw new SyntaxException("syntax error");
            }
        } else {
            throw new SyntaxException("syntax error");
        }
    }

    PixelSelector pixel_selector() throws LexicalException, SyntaxException {
        IToken firstToken = t;  //save current token
        Expr x = null;          //initialize expressions
        Expr y = null; 

        //expanded pixel selector should be [ , ] with expressions in between
        if (isKind(Kind.LSQUARE)) {
            consume();
            x = expr();
            if (isKind(Kind.COMMA)) {
                consume();
                y = expr();
                if (isKind(Kind.RSQUARE)) {
                    consume();
                } else {
                    throw new SyntaxException("syntax error");
                }
            } else {
                throw new SyntaxException("syntax error");
            }
        } else {
            throw new SyntaxException("syntax error");
        }

        //return expanded pixel expression object
        return new PixelSelector(firstToken, x, y);
    }

    Expr exp_pixel_selector() throws LexicalException, SyntaxException {
        IToken firstToken = t;  //save current token
        Expr x = null;          //initialize expressions
        Expr y = null;
        Expr z = null; 

        //expanded pixel selector should be [ , , ] with expressions in between
        if (isKind(Kind.LSQUARE)) {
            consume();
            x = expr();
            if (isKind(Kind.COMMA)) {
                consume();
                y = expr();
                if (isKind(Kind.COMMA)) {
                    consume();
                    z = expr();
                    if (isKind(Kind.RSQUARE)) {
                        consume();
                    } else {
                        throw new SyntaxException("syntax error");
                    }
                } else {
                    throw new SyntaxException("syntax error");
                }
            } else {
                throw new SyntaxException("syntax error");
            }
        } else {
            throw new SyntaxException("syntax error");
        }

        //return expanded pixel expression object
        return new ExpandedPixelExpr(firstToken, x, y, z);
    }

    Expr pixel_func_expr() throws LexicalException, SyntaxException {
        IToken firstToken = t;  //save current token
        Kind function = null;          //initialize expressions
        PixelSelector selector = null; 

        //check which reserved word and save as kind
        if (isKind(Kind.RES_x_cart)) {
            consume();
            function = Kind.RES_x_cart;
        } else if (isKind(Kind.RES_y_cart)) {
            consume();
            function = Kind.RES_y_cart;
        } else if (isKind(Kind.RES_a_polar)) {
            consume();
            function = Kind.RES_a_polar;
        } else if (isKind(Kind.RES_r_polar)) {
            consume();
            function = Kind.RES_r_polar;
        } else {
            throw new SyntaxException("syntax error");
        }

        //next token is pixel selector
        selector = pixel_selector();

        //return pixel function expression object
        return new PixelFuncExpr(firstToken, function, selector);
    }

    Dimension dimension() throws LexicalException, SyntaxException {
        IToken firstToken = t;  //save current token
        Expr width = null;      //initialize expressions
        Expr height = null;

        //dimension should be [ , ] with expressions in between
        if (isKind(Kind.LSQUARE)) {
            consume();
            width = expr();
            if (isKind(Kind.COMMA)) {
                consume();
                height = expr();
                if (isKind(Kind.RSQUARE)) {
                    consume();
                } else {
                    throw new SyntaxException("syntax error");
                }
            } else {
                throw new SyntaxException("syntax error");
            }
        } else {
            throw new SyntaxException("syntax error");
        }

        //return dimension object
        return new Dimension(firstToken, width, height);
    }

    LValue l_value() throws LexicalException, SyntaxException {
        IToken firstToken = t;  //save current token
        Ident ident = null;             //initialize expressions
        PixelSelector pixel = null;
	    ColorChannel color = null;

        //save first token as ident
        if (isKind(Kind.IDENT)) {
            ident = new Ident(t);
            consume();
        } else {
            throw new SyntaxException("syntax error");
        }

        //if token is left bracket, next token is pixel selector
        if (isKind(Kind.LSQUARE)) {
            pixel = pixel_selector();
        }

        //if token is colon, next token is channel selector
        if (isKind(Kind.COLON)) {
            color = channel_selector();
        }

        //return LValue object
        return new LValue(firstToken, ident, pixel, color);
    }

    Statement statement() throws LexicalException, SyntaxException {
        IToken firstToken = t;  //save current token
        LValue l = null;        //initialize expressions
        Expr e = null;
        Block b = null;

        //if token is write, return write expression
        if (isKind(Kind.RES_write)) {
            consume();
            e = expr();
            return new WriteStatement(firstToken, e);
        } else if (isKind(Kind.RES_while)) { //if token is while, return while expression
            consume();
            e = expr();
            b = block();
            return new WhileStatement(firstToken, e, b);
        } else {
            l = l_value(); //if not, must be assignment statement
            if (isKind(Kind.ASSIGN)) {
                consume();
                e = expr();
                return new AssignmentStatement(firstToken, l, e);
            } else {
                throw new SyntaxException("syntax error");
            }
        }

    }

}
