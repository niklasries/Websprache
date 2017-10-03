package OpenWeb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class gen {
	// dont redo already false flagged code
	static boolean hadError = false;

	public static void main(String[] args) throws IOException {

		if (args.length > 1) {
			System.out.println("Usage: jGen [script]");
		} else if (args.length == 1) {
			runFile(args[0]);
		} else {
			runPrompt();
		}
	}

	private static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()));
	}

	private static void runPrompt() throws IOException {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);

		for (;;) {
			System.out.print("> ");
			run(reader.readLine());
			// resets the flag so not the entire session is killed if the user
			// makes a mistake
			hadError = false;
		}
	}

	// Right now, it prints out the tokens our forthcoming scanner will emit so
	// that we can see if we’re making progress.
	private static void run(String source) {

		// Indicate an error in the exit code.
		if (hadError)
			System.exit(65);

		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();

		for (Token token : tokens) {
			System.out.println(token);
		}
	}

	// This tells users some syntax error occurred on a given line.
	// error reporting can be seperated into two different classes error
	// detection and reporting system and the results collected into an
	// interface for the user with
	// the linenumber, the actual snippet thats faulty and the type of error
	static void error(int line, String message) {
		report(line, "", message);
	}

	static private void report(int line, String where, String message) {
		System.err.println("[line " + line + "] Error" + where + ": " + message);
		hadError = true;
	}

}
