package assembler;

public class ImmediateNode extends Node{
	private String immediate;
	
	ImmediateNode(String imm){
		immediate  = imm;
	}
	
	public String getImmediate() {
		return immediate;
	}

	@Override
	public String toString() {
		return "[{IMM} " + immediate + "]";
	}
	
}
