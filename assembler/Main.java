package assembler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class Main {
	public static void main(String[] args) throws IOException {
		if (args.length <= 0 || args.length > 2) {
			throw new IllegalArgumentException("The Assembler needs 1) input file, 2) output file");
		}

		Path inputFile = Paths.get(args[0]);
		String content = new String(Files.readAllBytes(inputFile));
		
		LinkedList<Token> tokens = new Lexer(content).lex();
		LinkedList<String> binary = new Parser(tokens).parse();		// generates binary strings

		File outputFile = new File(args[1]);
		
		if (outputFile.createNewFile()) {    // create the output file
		    System.out.println("File created: " + outputFile.getName());
		    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
		        for (String line : binary) {
		            writer.write(line);
		            writer.newLine(); // Add a newline after each binary string
		        }
		    } catch (IOException e) {
		        System.err.println("Error writing to the output file: " + e.getMessage());
		    }
		} else {
		    System.out.println("File already exists.");
		}
	}
}
