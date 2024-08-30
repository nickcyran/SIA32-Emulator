package assembler;

import java.util.LinkedList;
import java.util.Optional;

public class RegisterNode extends Node{
	private LinkedList<String> registers;
	
	RegisterNode(LinkedList<String> ll, Optional<Node> next){
		registers = ll;
		nextNode = next;
	}
	
	public int registerCount() {
		return registers.size();
	}
	
	public LinkedList<String> getRegisters() {
		return registers;
	}
	
	public void addRegister(String s) {
		registers.add(s);
	}

	@Override
	public String toString() {
		return "[{REGs}"+registers.toString() + "], ";
	}
}
