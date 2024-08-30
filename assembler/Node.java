package assembler;

import java.util.Optional;

public abstract class Node {
	protected Optional<Node> nextNode;
	
	Node(){
		nextNode = Optional.empty();
	}
	
	public Optional<Node> getNextNode(){
		return nextNode;
	}
	
	public abstract String toString();
}
