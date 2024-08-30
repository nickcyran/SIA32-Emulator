package assembler;

import java.util.Optional;

public class InstructionNode extends Node{
	private String opType;
	
	InstructionNode(String op, Optional<Node> next){
		opType = op;
		nextNode = next;
	}
	
	public String getOpType(){
		return opType;
	}

	
	public Optional<Node> getElement(Class<? extends Node> classToFind) {
		Optional<Node> currentNode = getNextNode();
		
		while(currentNode.isPresent()) {
			var thisNode = currentNode.get();
			
			if(thisNode.getClass() == classToFind) {
				return currentNode;
			}
			currentNode = thisNode.getNextNode();
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[" + opType + "], ");
		
		Optional<Node> currentNode = getNextNode();

		while(currentNode.isPresent()) {
			sb.append(currentNode.get().toString());
			currentNode = currentNode.get().getNextNode();
		}
		return sb.toString();
	}
}
