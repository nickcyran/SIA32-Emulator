package assembler;

enum TokenType {
	NUMBER, REGISTER, NEWLINE, 
	MATH, ADD, SUB, MULT, AND, OR, NOT, XOR, 
	SHIFT, LEFT, RIGHT,
	COPY, HALT, BRANCH, JUMPTO, JUMPBY, CALL, PUSH, LOAD, 
	RETURN, STORE, PEEK, POP, INTERRUPT, EQ, NEQ, GT, LT, GE, LE
}

public class Token{
	private TokenType process;
	private String value;
	
	Token(TokenType type){
		process = type;
	}
	
	Token(TokenType type, String val){
		process = type;
		value = val;
	}
	
	public TokenType getProcess() {
		return process;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return process.toString() + (value != null ? ("["+value+"]") : "");
	}
}