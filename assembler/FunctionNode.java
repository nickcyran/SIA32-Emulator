package assembler;

import java.util.Optional;

public class FunctionNode extends Node{
	private String functionString;

	
	FunctionNode(String fn, Optional<Node> next){

		functionString = fn;
		nextNode = next;
	}
	
	public String getFunctionString(){
		return functionString;
	}

	@Override
	public String toString() {
		return "[{fn} " + functionString + "],";
	}
}
