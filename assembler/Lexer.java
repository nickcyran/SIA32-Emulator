package assembler;

import java.util.LinkedList;
import java.util.Queue;

public class Lexer {
	public class StringHandler{
		private int index;
		private String data;

		StringHandler(String data){
			this.data = data;
		}
		
		public boolean handleWhiteSpace() {
			boolean found = false;
			
			while(!isDone()) {
				switch(peek(0)) {
				case ' ', '\t', '\r' -> {
					swallow(1);
					found = true;
				}
				default -> {return found;}
				}
			}
			return found;
		}

		public char peek(int i) {
			return data.charAt(index + i);
		}

		public char getChar() {
			return data.charAt(index++);
		}

		public void swallow(int i) {
			index = index + i;
		}

		public boolean isDone() {
			return index >= data.length();
		}
	}
	
	private StringHandler file;
	
	Lexer(String data){
		file = new StringHandler(data);
	}
	
	public String getCurrentWord() {											//return next full string: stop at whiteSpace
		StringBuilder sb = new StringBuilder();
		
		while(!file.isDone() && !file.handleWhiteSpace()) {			
			sb.append(file.getChar());
		}
		
		return sb.toString();
	}
	
	public LinkedList<Token> lex(){
		LinkedList<Token> list = new LinkedList<>();
		
		while(!file.isDone()) {
			char currentChar = file.peek(0);
	
			if(currentChar == '\n') {
				list.add(new Token(TokenType.NEWLINE));
				file.swallow(1);
			}
			else if(currentChar =='#') {										//Skip comments
			    while (!file.isDone() && file.peek(0) != '\n') {
			        file.swallow(1); 	
			    }
			}
			else if(Character.isDigit(currentChar) || currentChar == '-') {
				list.add(new Token(TokenType.NUMBER, getCurrentWord()));		//Type: number, Value: some number
			}
			else if(Character.isLetter(currentChar)){
				if(currentChar == 'R' && Character.isDigit(file.peek(1))) {
					file.swallow(1);											//R2 <-- on 2 atp
					list.add(new Token(TokenType.REGISTER, getCurrentWord()));	//Type: register, Value: reg_index
				}
				else {
					String s = getCurrentWord().toUpperCase();
					try {
						list.add(new Token(TokenType.valueOf(s)));				//String as the token 
					}
					catch (Exception e) {
						throw new IllegalArgumentException("INVALID LINE DETECTED");
					}
				}
			}
			else {
				throw new IllegalArgumentException("INVALID CHARACTER FOUND");
			}
			
			file.handleWhiteSpace();
		}
		return list;
	}
}
