package edu.ufl.cise.plcsp23;
import java.util.*;

public class Token {

    public record SourceLocation(int line, int column) {
		Map<Integer, Integer> location = new HashMap<Integer, Integer>();
		location.put(new Integer(line), new Integer(column));

	}
	
	public static enum Kind {
		IDENT,
		NUM_LIT,
		STRING_LIT,
		RES_image,
		RES_pixel,
		RES_int,
		RES_string,
		RES_void,
		RES_nil,
		RES_load,
		RES_display,
		RES_write,
		RES_x,
		RES_y,
		RES_a,
		RES_r,
		RES_X,
		RES_Y,
		RES_Z,
		RES_x_cart,
		RES_y_cart,
		RES_a_polar,
		RES_r_polar,
		RES_rand,
		RES_sin,
		RES_cos,
		RES_atan,
		RES_if,
		RES_while,
		DOT, //  .
		COMMA, // ,
		QUESTION, // ?
		COLON, // :
		LPAREN, // (
		RPAREN, // )
		LT, // <
		GT, // >
		LSQUARE, // [
		RSQUARE, // ]
		LCURLY, // {
		RCURLY, // }
		ASSIGN, // =
		EQ, // ==
		EXCHANGE, // <->
		LE, // <=
		GE, // >=
		BANG, // !
		BITAND, // &
		AND, // &&
		BITOR, // |
		OR, // ||
		PLUS, // +
		MINUS, // -
		TIMES, // *
		EXP, // **
		DIV, // /
		MOD, // %
		EOF,
		ERROR
	}

	final Kind kind;
	final int pos;
	final int length;
	final char[] source;

	public Token(Kind kind, int pos, int length, char[] source){
		super();
		this.kind = kind;
		this.pos = pos;
		this.length = length;
		this.source = source;
	}

	/**
	 * Returns a SourceLocation record containing the line and column number of this token.
	 * Both counts start numbering at 1.
	 * 
	 * @return Line number and column of this token.  
	 */
	public SourceLocation getSourceLocation() {
		return SourceLocation(pos, length);
	}
	
	/** Returns the kind of this Token
	 * 
	 * @return kind
	 */
	public Kind getKind(){
		return kind;
	}
	
	/**
	 * Returns a char array containing the characters of this token.
	 * 
	 * @return
	 */
	public String getTokenString(){
		

	}
    
}
