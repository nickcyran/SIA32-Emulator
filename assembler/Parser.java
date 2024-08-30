package assembler;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

public class Parser {
	private static final EnumMap<TokenType, String> mopMap = initMopMap();	//MOP function to String Map
	private static final EnumMap<TokenType, String> bopMap = initBopMap();	//BOP function to String Map
	
	private int linePtr;
	private LinkedList<LinkedList<Token>> statements;						//prgm broken by each line of statements
	
	private EnumMap<TokenType, OperationType> instructionMap;
	
	public interface OperationType{
		public String run();
	}
	
	private static EnumMap<TokenType, String> initMopMap(){
		EnumMap<TokenType, String> map = new EnumMap<>(TokenType.class);
		
		// {function} - MOP  [excluding shift]
		map.put(TokenType.ADD, "1110");
		map.put(TokenType.SUB, "1111");
		map.put(TokenType.MULT, "0111");
		map.put(TokenType.AND, "1000");
		map.put(TokenType.OR, "1001");
		map.put(TokenType.XOR, "1010");
		map.put(TokenType.NOT, "1011");
		
		return map;
	}
	
	private static EnumMap<TokenType, String> initBopMap(){
		EnumMap<TokenType, String> map = new EnumMap<>(TokenType.class);
		
		// {funtion} - BOP
		map.put(TokenType.EQ, "0000");
		map.put(TokenType.NEQ, "0001");
		map.put(TokenType.GT, "0100");
		map.put(TokenType.LT, "0010");
		map.put(TokenType.GE, "0011");
		map.put(TokenType.LE, "0101");
		
		return map;
	}

	
	Parser(LinkedList<Token> tokens) {
		statements = new LinkedList<>();
		LinkedList<Token> innerStatement = new LinkedList<>();

		while (!tokens.isEmpty()) {
			Token current = tokens.poll();

			if (current.getProcess() == TokenType.NEWLINE) {
				if (!innerStatement.isEmpty()) {
					statements.add(innerStatement);
				}
				innerStatement = new LinkedList<>();
			} else {
				innerStatement.add(current);

				if (tokens.isEmpty()) {
					statements.add(innerStatement);
				}
			}
		}
		initInstructionMap();
	}
	
	private void initInstructionMap() {
		instructionMap = new EnumMap<>(TokenType.class);

		//---[MATH]----------------------------
		instructionMap.put(TokenType.MATH, () -> {
			InstructionNode iNode = new InstructionNode("000", parseMOP());
			var next = iNode.getNextNode();
																							//MOP operation must exist after a MATH call
			if(next.isEmpty() || !(next.get() instanceof FunctionNode)) {	
				throwException("MOP function must come after MATH");
			}
			
			if(registerCount(iNode) < 2) {													//MATH operations contain 2-3 registers
				throwException("MATH operations must contain 2 or more registers");		
			}
			
			if(iNode.getElement(ImmediateNode.class).isPresent()) {							//MATH operations contain no immediate value
				throwException("MATH operations must not contain an immediate value");
			}
			
			return assembleBinaryString(iNode);
		});
		
		//---[COPY]----------------------------
		instructionMap.put(TokenType.COPY, () -> {
			InstructionNode iNode = new InstructionNode("000", parseMOP());
			var next = iNode.getNextNode();
			
			if(next.isPresent() && (next.get() instanceof RegisterNode regNode)) {			//COPY contains only 1 register and always an immediate value				
				if(regNode.registerCount() != 1 || iNode.getElement(ImmediateNode.class).isEmpty()) {		
					throwException("COPY must only include one Register {Rd} and an immediate value");
				}
			}
			else {
				throwException("COPY must be followed by Rd and immediate value");
			}
			
			return assembleBinaryString(iNode);
		});
		
		//---[HALT]----------------------------
		instructionMap.put(TokenType.HALT, () -> {
			InstructionNode iNode = new InstructionNode("000", parseMOP());
			var next = iNode.getNextNode();
			
			if(next.isPresent()) {
				throwException("HALT must be the only thing on the line");
			}
			return assembleBinaryString(iNode);
		});
		
		//---[JUMPTO]----------------------------
		instructionMap.put(TokenType.JUMPTO, () -> {
			InstructionNode iNode = new InstructionNode("001", parseMOP());
			
			var next = iNode.getNextNode();
			
			if(next.isPresent() && !(next.get() instanceof ImmediateNode)) {
				throwException("JUMPTO must be followed by only an immediate value");
			}
			
			return assembleBinaryString(iNode);
		});
		
		//---[JUMPBY]----------------------------
		instructionMap.put(TokenType.JUMPBY, () -> {
			InstructionNode iNode = new InstructionNode("001", parseMOP());
			
			var next = iNode.getNextNode();
			
			if(next.isPresent() && !(next.get() instanceof ImmediateNode)) {
				throwException("JUMPBY must be followed by only an immediate value");
			}
			
			return changeRegisterType(assembleBinaryString(iNode), "01");
		});
		
		//---[BRANCH]----------------------------
		instructionMap.put(TokenType.BRANCH, () -> {									//In assembly branch 3R same as 2R per documentation
			InstructionNode iNode = new InstructionNode("001", parseBOP());				//=> parsed the exact same => only 1 implementation
			var next = iNode.getNextNode();
			
			if(next.isPresent() && !(next.get() instanceof FunctionNode)) {
				throwException("BRANCH must be followed by a BOP");
			}
			
			var regNode = iNode.getElement(RegisterNode.class);
			if(regNode.isEmpty() || ((RegisterNode)regNode.get()).registerCount() != 2) {
				throwException("BRANCH must contain 2 registers");
			}
			
			if(iNode.getElement(ImmediateNode.class).isEmpty()) {
				throwException("BRANCH must contain an immediate value");
			}
			
			return assembleBinaryString(iNode);
		});
		

		//---[CALL]----------------------------
		instructionMap.put(TokenType.CALL, () -> {
			InstructionNode iNode = new InstructionNode("010", parseBOP());
			var regNode = iNode.getElement(RegisterNode.class);
			var next = iNode.getNextNode();
			
			// Excludes NO_R which just accepts an immediate anyway
			if(regNode.isEmpty()) {
				if(!(next.get() instanceof ImmediateNode)) {
					throwException("CALL [NO_R] must be followed by an Immediate Value");
				}
			}
			else {
				int regCount = registerCount(iNode);
				
				if(regCount > 1) {
					if(!(next.get() instanceof FunctionNode)){			//2R & 3R BRANCH must have BOP
						throwException("CALL [" + regCount + "R] must be followed by a BOP");
					}
				}
				else {													//DEST_ONLY
					if(!(next.get() instanceof RegisterNode)) {
						throwException("CALL [DEST_ONLY] must be followed by a Register");
					}
				}
			}

			if(iNode.getElement(ImmediateNode.class).isEmpty()) {
				throwException("CALL must contain an immediate value");
			}
			
			return assembleBinaryString(iNode);
		});
		
		//---[PUSH]----------------------------
		instructionMap.put(TokenType.PUSH, () -> {
			InstructionNode iNode = new InstructionNode("011", parseMOP());
			var regNode = iNode.getElement(RegisterNode.class);
			var next = iNode.getNextNode();
			
			if(regNode.isPresent()) {
				if(!(next.get() instanceof FunctionNode)) {
					throwException("PUSH must be followed by a MOP");
				}
				
				int regCount = registerCount(iNode);
				if(regCount == 1) {
					if(iNode.getElement(ImmediateNode.class).isEmpty()) {
						throwException("PUSH [1R] must contain an immediate value");
					}
				}
				else if(regCount == 2){											//2R & 3R same implementation
					if(iNode.getElement(ImmediateNode.class).isPresent()) {
						throwException("PUSH [2R] must not contain an immediate value");
					}
				}
				else {
					throwException("PUSH must take at most 2 Registers");
				}
			}	
			else {
				throwException("PUSH must contain atleast one Register");
			}
			
			return assembleBinaryString(iNode);
		});
		
		//---[RETURN]----------------------------
		instructionMap.put(TokenType.RETURN, () -> {
			InstructionNode iNode = new InstructionNode("100", parseMOP());
			
			if(iNode.getNextNode().isPresent()) {
				throwException("RETURN must not contain any additional information"); 
			}
			
			return assembleBinaryString(iNode);
		});
		
		//---[LOAD]----------------------------
		instructionMap.put(TokenType.LOAD, () -> {
			InstructionNode iNode = new InstructionNode("100", parseMOP());
			var regNode = iNode.getElement(RegisterNode.class);
			var next = iNode.getNextNode();
			
			if(regNode.isPresent() && next.get() instanceof RegisterNode) {
				int regCount = registerCount(iNode);
				
				if((regCount > 2) && iNode.getElement(ImmediateNode.class).isPresent()) {
					throwException("LOAD [3R] must not contain an immediate value"); 
				}
				else if((regCount < 3)&& iNode.getElement(ImmediateNode.class).isEmpty()) {
					throwException("LOAD [2R/NO_R] must contain an immediate value"); 
				}
			}
			else {
				throwException("LOAD must be followed by at least one Register"); 
			}
			
			return assembleBinaryString(iNode);
		});
		
		//---[STORE]----------------------------
		instructionMap.put(TokenType.STORE, () -> {
			InstructionNode iNode = new InstructionNode("101", parseMOP());
			var regNode = iNode.getElement(RegisterNode.class);
			var next = iNode.getNextNode();
			
			if(regNode.isPresent() && next.get() instanceof RegisterNode) {
				int regCount = registerCount(iNode);
				var immNode = iNode.getElement(ImmediateNode.class);
				
				if((regCount < 3) && immNode.isEmpty()) {
					throwException("STORE [2R/NO_R] must contain an immediate value"); 
				}
				else if((regCount == 3) && immNode.isPresent()) {
					throwException("STORE [3R] must not contain an immediate value"); 
				}
			}
			else {
				throwException("STORE must be followed by at least one Register"); 
			}
			return assembleBinaryString(iNode);
		});
		
		//---[POP]----------------------------
		instructionMap.put(TokenType.POP, () -> {
			InstructionNode iNode = new InstructionNode("110", parseMOP());
			var next = iNode.getNextNode();
			
			if(next.isEmpty() || !(next.get() instanceof RegisterNode) || registerCount(iNode) != 1) {
				throwException("POP must be followed by a single Register");
			}
			
			if(iNode.getElement(ImmediateNode.class).isPresent()) {
				throwException("POP must not contain an immediate value");
			}
			
			return assembleBinaryString(iNode);
		});
		
		//---[PEEK]----------------------------
		instructionMap.put(TokenType.PEEK, () -> {
			InstructionNode iNode = new InstructionNode("110", parseMOP());
			var next = iNode.getNextNode();
			var regCount = registerCount(iNode);
			
			if(regCount <= 1 || !(next.get() instanceof RegisterNode)) {
				throwException("PEEK must be directly followed by a 2-3 Registers");
			}
			
			var immNode = iNode.getElement(ImmediateNode.class);
			
			if(regCount == 2 && immNode.isEmpty()) {
				throwException("PEEK [2R] must contain an immediate value");
			}
			else if(regCount == 3 && immNode.isPresent()){
				throwException("PEEK [3R] must not contain an immediate value");
			}
			
			return assembleBinaryString(iNode);
		});
		
		//---[INTERRUPT]----------------------------
		instructionMap.put(TokenType.INTERRUPT, () -> {
			throw new IllegalArgumentException("INTERRUPT processes have not been implemented as per piazza post");
		});
	}

	// EVERY ASSEMBLY INSTRUCTION IS FORMATTED AS SUCH:
	// opType {function} [rs1] [rs2] [rd] {imm}
	public LinkedList<String> parse() { 
		LinkedList<String> binary = new LinkedList<>();
		for(int i = 0; i < statements.size(); i++) {
			binary.add(parseStatement());
			linePtr++;
		}
		return binary;
	}
	
	private String parseStatement() {						
		if(tokensLeft()) {
			var currentType = currentTokenType();
			
			if(mapContainsKey(instructionMap, currentType)) {
				return instructionMap.get(currentType).run();
			}
		}
		throw new IllegalArgumentException("Instructions must start with operation type");
	}

	private Optional<Node> parseMOP() {
		if (tokensLeft()) {
			TokenType currentType = currentTokenType();

			if (mapContainsKey(mopMap, currentType)) { 					//function is a MathType
				return Optional.of(
						new FunctionNode(mopMap.get(currentType), parseRegisters()));
			} 
			else if (matchAndRemove(TokenType.SHIFT)) { 				//check that shift has correct syntax
				String fnCode;
				if (matchAndRemove(TokenType.RIGHT)) {
					fnCode = "1101";
				} else if (matchAndRemove(TokenType.LEFT)) {
					fnCode = "1100";
				} else {
					throw new IllegalArgumentException("SHIFT must be followed by direction");
				}

				return Optional.of(new FunctionNode(fnCode, parseRegisters()));
			}
		}
		return parseRegisters();
	}
	
	private Optional<Node> parseBOP() {
		if (tokensLeft()) {
			TokenType currentType = currentTokenType();

			if (mapContainsKey(bopMap, currentType)) { 					//function is a MathType
				return Optional.of(
						new FunctionNode(bopMap.get(currentType), parseRegisters()));
			} 
		}
		return parseRegisters();
	}
	
	private Optional<Node> parseRegisters() {
		if (tokensLeft()) {
			var current = currentToken();
			
			if(currentToken().getProcess() == TokenType.REGISTER) {
				LinkedList<String> ll = new LinkedList<>();
				
				while (matchAndRemove(TokenType.REGISTER)) {
					ll.add(registerToBinary(current.getValue()));
					
					if (!statements.get(linePtr).isEmpty()) {
						current = currentToken();
					}
				}
				
				if(ll.size() > 3) {
					throwException("More than 3 registers");
				}
				
				return Optional.of(new RegisterNode(ll, parseImmediate()));
			}
		}
		return parseImmediate();
	}
	
	private Optional<Node> parseImmediate() {						//returns the {imm} value
		if(tokensLeft() && currentTokenType() == TokenType.NUMBER) {
			var current = currentToken();
			removeCurrent();
			
			if(tokensLeft()) {
				throw new IllegalArgumentException("The immediate value must go at the end");
			}
			
			return Optional.of(new  ImmediateNode(current.getValue()));
		}
		return Optional.empty();
	}
	
	private String calculateImmediate(int length, int immediate) {
		int spaceLeft = 32 - length;
		String string = Integer.toBinaryString(immediate);

		if (string.length() > spaceLeft) {
			throw new NumberFormatException(("[LINE: " + (linePtr + 1) + "] ") + "Integer is too large for word");
		} else {
			StringBuilder sb = new StringBuilder(string);
			while (sb.length() < spaceLeft) {
				sb.insert(0, '0');
			}
			return sb.toString();
		}
	}
	
	private String assembleBinaryString(InstructionNode iNode) {
		StringBuilder sb = new StringBuilder(iNode.getOpType());					//Insert operation Type
		
		var regNode = iNode.getElement(RegisterNode.class);
		
		if(regNode.isEmpty()) {
			sb.append("00");
		}
		else {
			LinkedList<String> registers = ((RegisterNode) regNode.get()).getRegisters();
			
			int size = registers.size();
			sb.append(size == 1 ? "01" : Integer.toBinaryString(size));				//Append register usagse to end		
	
			sb.insert(0, registers.getLast());										//Last in list will always be Rd
			
			var fnNode = iNode.getElement(FunctionNode.class);						//::Only exists if there is atleast Rd::
			if(fnNode.isEmpty()) {													//if instruction doesnt contain a fn -> insert
				sb.insert(0, "0000");												//else insert functionString
			}
			else {
				sb.insert(0, ((FunctionNode)fnNode.get()).getFunctionString());	
			}	
			
			for(int i = registers.size() - 2; i >= 0 ; i--) {						//disregard Rd
				sb.insert(0, registers.get(i));										//insert rs1 and rs2 if applicable
			}
		}
		
		Optional<Node> immNode = iNode.getElement(ImmediateNode.class);				//If instruction contains an immediate value
																					//parse it to a number; else it will be 0
		int imm = immNode.isPresent() ? 
					toNumber(((ImmediateNode)immNode.get()).getImmediate()) : 0;
		
		sb.insert(0, calculateImmediate(sb.length(), imm));
		
		return sb.toString();
	}

	//---[HELPER FUNCTIONS]-----------------------------------------------------
	private void throwException(String msg) {
		throw new IllegalArgumentException(("[LINE: " + (linePtr + 1) + "] ") + msg);
	}
	
	private boolean tokensLeft() {
		return !statements.get(linePtr).isEmpty();
	}
	
	@SuppressWarnings("rawtypes")
	private boolean mapContainsKey(Map map, TokenType key) {
		if(map.containsKey(key)) {
			removeCurrent();
			return true;
		}
		return false;
	}
	
	private TokenType currentTokenType() {
		return currentToken().getProcess();
	}
	
	private Token currentToken() {
		return statements.get(linePtr).get(0);
	}
	
	private void removeCurrent() {
		statements.get(linePtr).remove();
	}
	
	private boolean matchAndRemove(TokenType type) {
		var currentLine = statements.get(linePtr);

		if(!currentLine.isEmpty() && currentLine.get(0).getProcess() == type) {
			currentLine.remove();
			return true;
		}
		return false;
	}
	
	private String registerToBinary(String asInt) {
		int registerIndex = toNumber(asInt);
		
		if(registerIndex < 0) {
			throw new IndexOutOfBoundsException(registerIndex + " is Out of Bounds");
		}
		return intToBinaryString(registerIndex, 5);
	}
	
	private String intToBinaryString(int i, int size) {
	    String string = Integer.toBinaryString(i);

	    if (string.length() > size) {
	        throw new NumberFormatException("Integer is too large");
	    } else if (string.length() < size) {

	        StringBuilder sb = new StringBuilder(string);
	        while (sb.length() < size) {
	            sb.insert(0, '0');
	        }
	        string = sb.toString();
	    }
	    return string;
	}
	
	private int toNumber(String num) {
		try {
			return Integer.parseInt(num);
		}
		catch(Exception e) {
			throw new NumberFormatException(("[LINE: " + (linePtr + 1) + "] ") + "Not a valid Integer");
		}
	}
	
	private String changeRegisterType(String toChange, String regType) {
		return toChange.substring(0, 30) + regType;	
	}
	
	private int registerCount(InstructionNode iNode) {
		var rNode = iNode.getElement(RegisterNode.class);				
		return rNode.isPresent() ? ((RegisterNode) rNode.get()).registerCount() : 0;	//if registers are present -> ret amount; else it is 0
	}

}
