package OpenWeb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static OpenWeb.TokenType.*;

/* Additions 
 * + Coalescing a run of invalid characters into a single error would give a nicer user experience.
 * +Error detection in separated file
 * +error user report into separated file
 * +add escape sequences like \n for string formatting
 * 
 */

class Scanner {

	private static final Map<String, TokenType> keywords;

	static {
		keywords = new HashMap<>();
		keywords.put("and", AND);
		keywords.put("class", CLASS);
		keywords.put("else", ELSE);
		keywords.put("false", FALSE);
		keywords.put("for", FOR);
		keywords.put("fun", FUNKTION);
		keywords.put("if", IF);
		keywords.put("nil", NIL);
		keywords.put("or", OR);
		keywords.put("print", PRINT);
		keywords.put("return", RETURN);
		keywords.put("super", SUPER);
		keywords.put("this", THIS);
		keywords.put("true", TRUE);
		keywords.put("var", VAR);
		keywords.put("while", WHILE);
	}

	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	/*
	 * The start and current fields are offsets in the string — the first
	 * character in the current lexeme being scanned, and the character we’re
	 * currently considering. The other field tracks what source line current is
	 * on so we can produce tokens that know their location.
	 */
	private int start = 0;
	private int current = 0;
	private int line = 1;

	Scanner(String source) {
		this.source = source;
	}

	List<Token> scanTokens() {
		while (!isAtEnd()) {
			// We are at the beginning of the next lexeme.
			start = current;
			scanToken();
		}

		/*
		 * The scanner works its way through the source code, adding tokens,
		 * until it runs out of characters. When it’s done, it appends one final
		 * “end of file” token.
		 */
		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}

	// Each turn of the loop, we scan a single token.
	private void scanToken() {
		char c = advance();
		switch (c) {
		case '(':
			addToken(LEFT_PAREN);
			break;
		case ')':
			addToken(RIGHT_PAREN);
			break;
		case '{':
			addToken(LEFT_BRACE);
			break;
		case '}':
			addToken(RIGHT_BRACE);
			break;
		case ',':
			addToken(COMMA);
			break;
		case '.':
			addToken(DOT);
			break;
		case '-':
			addToken(MINUS);
			break;
		case '+':
			addToken(PLUS);
			break;
		case ';':
			addToken(SEMICOLON);
			break;
		case '*':
			addToken(STAR);
			break;
		// checks for double character tokens
		case '!':
			addToken(match('=') ? BANG_EQUAL : BANG);
			break;
		case '=':
			addToken(match('=') ? EQUAL_EQUAL : EQUAL);
			break;
		case '<':
			addToken(match('=') ? LESS_EQUAL : LESS);
			break;
		case '>':
			addToken(match('=') ? GREATER_EQUAL : GREATER);
			break;
		// checking for comments or '/' operator
		case '/':
			if (match('/')) {
				// A comment goes until the end of the line.
				while (peek() != '\n' && !isAtEnd())
					advance();
			} else {
				addToken(SLASH);
			}
			break;
		// skip over those other newlines and whitespace
		case ' ':
		case '\r':
		case '\t':
			// Ignore whitespace.
			break;

		case '\n':
			line++;
			break;
		// handles strings that always start with* "
		case '"':
			string();
			break;
		// checks for prohibited characters in the source file
		default:
			if (isDigit(c)) {
				number();
			} else if (isAlpha(c)) {
				identifier();
			} else {
				gen.error(line, "Unexpected character.");
			}
			break;
		}
		// faulty code is no problem, because hadError gets set, resulting in no
		// code execution
	}

	private void identifier() {
		// See if the identifier is a reserved word.
		String text = source.substring(start, current);

		TokenType type = keywords.get(text);
		if (type == null)
			type = IDENTIFIER;
		addToken(type);

		addToken(IDENTIFIER);
	}

	/*
	 * It consumes as many digits as it finds for the integer part of the
	 * literal. Then it looks for a fractional part, which is a decimal point
	 * (.) followed by at least one digit.
	 */
	private void number() {
		while (isDigit(peek()))
			advance();

		// Look for a fractional part.
		if (peek() == '.' && isDigit(peekNext())) {
			// Consume the "."
			advance();

			while (isDigit(peek()))
				advance();
		}

		addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
	}

	/*
	 * Like with comments, it consumes characters until it hits the " that ends
	 * the string. It also handles running out of input before the string is
	 * closed and reports an error for that. multi-line strings are allowed
	 */
	private void string() {
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n')
				line++;
			advance();
		}

		// Unterminated string.
		if (isAtEnd()) {
			gen.error(line, "Unterminated string.");
			return;
		}

		// The closing ".
		advance();

		// Trim the surrounding quotes.
		String value = source.substring(start + 1, current - 1);
		addToken(STRING, value);
	}

	// conditionally advance() because it only consumes the current character if
	// it’s what we’re looking for.
	private boolean match(char expected) {
		if (isAtEnd())
			return false;
		if (source.charAt(current) != expected)
			return false;

		current++;
		return true;
	}

	// lookahead
	private char peek() {
		if (isAtEnd())
			return '\0';
		return source.charAt(current);
	}

	// This requires another character of lookahead since we don’t want to
	// consume the .
	// until we’re sure there is a digit after it.
	private char peekNext() {
		if (current + 1 >= source.length())
			return '\0';
		return source.charAt(current + 1);
	}

	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
	}

	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private boolean isAtEnd() {
		return current >= source.length();
	}
	/*
	 * The advance() method consumes the next character in the source file and
	 * returns it. Where advance() is for input, addToken() is for output. It
	 * grabs the text of the current lexeme and creates a new token for it.
	 */

	private char advance() {
		current++;
		return source.charAt(current - 1);
	}

	private void addToken(TokenType type) {
		addToken(type, null);
	}

	private void addToken(TokenType type, Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}
}