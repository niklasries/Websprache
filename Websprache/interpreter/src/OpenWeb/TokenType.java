package OpenWeb;

enum TokenType {
	/*
	 * any tokens added here must be implemented in the token-checker to be used
	 */

	// Single-character tokens.
	// (){},.-+;/*
	LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

	// One or two character tokens.
	// ! != = == > >= < <=
	BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

	// Literals.
	IDENTIFIER, STRING, NUMBER,

	// Keywords.
	// Nil used instead of null to make it simpler to code using c or java
	AND, CLASS, ELSE, FALSE, FUNKTION, FOR, IF, NIL, OR, PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

	EOF
}
