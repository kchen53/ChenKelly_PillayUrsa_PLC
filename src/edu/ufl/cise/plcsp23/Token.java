package edu.ufl.cise.plcsp23;

public class Token implements IToken {

	final Kind kind;
	final int pos;
	final int length;
	final int line;
	final int column;
	final char[] source;

	//token constructor
	public Token(Kind kind, int pos, int length, int line, int column, char[] source){
		super();
		this.kind = kind;
		this.pos = pos;
		this.length = length;
		this.source = source;
		this.line = line;
		this.column = column - length;
	}

	/**
	 * Returns a SourceLocation record containing the line and column number of this token.
	 * Both counts start numbering at 1.
	 * 
	 * @return Line number and column of this token.  
	 */
	public SourceLocation getSourceLocation() {
		return new SourceLocation(line, column);
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
		return new String(source, pos, length);
	}

    @Override 
    public String toString() {
        return kind + "=" + getTokenString() + " located at" + pos + "," + length;
    }
}
