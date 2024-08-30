package assembler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class Assembler {
    
    public static void assemble(String in, String out) {
        Path inputFile = Paths.get(in);
        try {
            String content = new String(Files.readAllBytes(inputFile));
            LinkedList<Token> tokens = new Lexer(content).lex();
            LinkedList<String> binary = new Parser(tokens).parse(); // Generates binary strings

            File outputFile = new File(out);

            try {
                if (outputFile.createNewFile()) { // Create the output file
                    System.out.println("File created: " + outputFile.getName());
                } else {
                    System.out.println("File already exists. Replacing...");
                }
                // Write binary strings to the output file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, false))) {
                    for (String line : binary) {
                        writer.write(line);
                        writer.newLine(); // Add a newline after each binary string
                    }
                } catch (IOException e) {
                    System.err.println("Error writing to the output file: " + e.getMessage());
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Error creating the output file: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading the input file: " + e.getMessage());
        }
    }
}