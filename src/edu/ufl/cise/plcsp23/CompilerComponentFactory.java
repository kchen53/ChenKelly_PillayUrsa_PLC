/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the spring semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */

package edu.ufl.cise.plcsp23;

import javax.swing.text.html.HTMLEditorKit.Parser;

public class CompilerComponentFactory {
	public static IScanner makeScanner(String input) {
		return new Scanner(input);
	}

	public static IParser makeAssignment2Parser(String input)
     throws LexicalException {
		Scanner scanner = new Scanner(input);
		Parser parser = new Parser(input);
		return parser;
       //add code to create a scanner and parser and return the parser. 
	 }

}
