
enum instruction {
	MATH, BRANCH, CALL, PUSH, LOAD, STORE, POP;
}
enum registerType{
	R3, R2, DEST_ONLY, NO_R
}
enum mathOps{
	ADD(new Bit[] { new Bit(true), new Bit(true), new Bit(true), new Bit(false)}), 
	SUBTRACT(new Bit[] { new Bit(true), new Bit(true), new Bit(true), new Bit(true)});
	
	private Bit[] opCode;
	mathOps(Bit[] bits){
		opCode = bits;
	}
	
	public Bit[] getOpcode(){
		return opCode;
	}
}

public class Processor {
	private Bit haltedBit;	
	private Word PC;		// Program Counter - start at 0
	private Word SP;		// Stack Pointer - start at 1024
	private Word currentInstruction;
	private Word valueToStore;

	private Clock clock;
	private ALU logicUnit;
	private Word[] registers;
	private InstructionCache instructionCache;
	private L2Cache L2Cache;
	
 	public Processor() {
		PC = new Word();	// starts all false which = 0
		SP = new Word();
		SP.set(1024);
		
		haltedBit = new Bit(false);
		registers = new Word[32];
		valueToStore = new Word();
		
		for(int i = 0; i < 32; i++) {
			registers[i] = new Word();
		}
		
		logicUnit = new ALU();
		
		clock = new Clock();
		L2Cache = new L2Cache(clock);
		instructionCache = new InstructionCache(L2Cache, clock);
		
	}
	
	public void run() {
		while(!haltedBit.getValue()) {
			fetch();						// fetch an instruction from memory
			Word[] storage = decode();		// get the data for the instruction (decode)
			execute(storage);				// execute the instruction
			store();						// store the results
		}
		System.out.println("[Exiting ...]");
		System.out.println("End Clock Cycle: " + clock.currentCycle);
	}
	
	public void fetch() {
//		clock.currentCycle += 300;
//		currentInstruction = MainMemory.read(PC);
		currentInstruction = instructionCache.read(PC);
		PC.incriment();
	}
	
	private instruction getInstruction() {
		boolean bit2 = currentInstruction.getBit(4).getValue();
	    boolean bit1 = currentInstruction.getBit(3).getValue();
	    boolean bit0 = currentInstruction.getBit(2).getValue();
	    
	    instruction instructionType = null;
	    
		if(!bit2 && !bit1 && !bit0) {		// Math - [000xx]
			instructionType = instruction.MATH;
		}
		else if(!bit2 && !bit1 && bit0) {	// Branch - [001xx]
			instructionType = instruction.BRANCH;
		}	
		else if(!bit2 && bit1 && !bit0) {	// Call - [010xx]
			instructionType = instruction.CALL;
		}
		else if(!bit2 && bit1 && bit0) {	// Push - [011xx]
			instructionType = instruction.PUSH;
		}
		else if(bit2 && !bit1 && !bit0) {	// Load - [100xx]
			instructionType = instruction.LOAD;
		}
		else if(bit2 && !bit1 && bit0) {	// Store - [101xx]
			instructionType = instruction.STORE;
		}
		else if(bit2 && bit1 && !bit0) {	// Pop - [110xx]
			instructionType = instruction.POP;
		}
		
		return instructionType;
	    
	}
	
	private registerType getRegisterType() {
		 boolean bit0 = currentInstruction.getBit(0).getValue();
		 boolean bit1 = currentInstruction.getBit(1).getValue();
		 
		 if (bit1 && bit0) { 					// [11] - 3 Register (3R)       
		        return registerType.R3;
		        
		    } else if (bit1 && !bit0) { 		// [10] - 2 Register (2R)
		    	return registerType.R2;
		        
		    } else if (!bit1 && bit0) { 		// [01] - Dest Only (1R)
		        return registerType.DEST_ONLY;
		        
		    } else { 							// [00] - No Register (0R)
		        return registerType.NO_R;	
		    }
	}
	
	public Word[] decode() {
		switch(getRegisterType()) {
			case R3 -> {return get3Rdata();}
			case R2 -> {return get2Rdata();}
			case DEST_ONLY -> {return getDestOnlyData();}
			case NO_R -> { return new Word[]{extractValue(5, 31)};}		// returns only the immediate
			default -> {return new Word[]{};}							// Hardware => wrong format then tough luck
		}
	}
	
	private void execute(Word[] decodedValues) {
	    switch(getInstruction()) {
	    	case MATH -> math(decodedValues);
	    	case BRANCH -> branch(decodedValues);
	    	case CALL -> call(decodedValues);
	    	case PUSH -> push(decodedValues);
	    	case LOAD -> load(decodedValues);
	    	case POP -> pop(decodedValues);
	    	case STORE -> storeInstruction(decodedValues);
	    	default -> {/*This is hardware. a wrong bit pattern wont halt everything*/}
	    }
	}
	
	private void storeInRegister() {
		Word Rd = extractValue(5, 9);
		int index = 0;
		
		for (int i = 0; i < 5; i++) {
			if (Rd.getBit(i).getValue()) {
				index += Math.pow(2, i);
			}
		}
		
		if (index > 0) { // if R0 do not override
			registers[index].copy(valueToStore);
		}
	}
	
	private void store() {
		switch (getInstruction()) {
			case MATH, LOAD -> storeInRegister();
			case POP -> {
				if(getRegisterType() != registerType.NO_R) {
					storeInRegister();
				}
			}
			default -> {/* This is hardware. a wrong bit pattern wont halt everything */}
		}
	}
	
	private void storeInstruction(Word[] decodedValues) {
		switch(getRegisterType()) {
		case R3 -> L2Cache.write(															//Mem[Rd + Rs1] <- Rs2
					doMath(decodedValues[1],mathOps.ADD, decodedValues[2]), decodedValues[3]);	
		case R2 -> L2Cache.write(															//Mem[Rd + imm] <- Rs
					doMath(decodedValues[1], mathOps.ADD, decodedValues[0]), decodedValues[2]);	
		case DEST_ONLY -> L2Cache.write(decodedValues[1], decodedValues[0]);				//Mem[Rd] <- imm
		case NO_R -> {/*UNUSED*/}
		}
	}
	
	private void math(Word[] decodedValues) {
		switch(getRegisterType()) {
		case R3 -> {
			logicUnit.op1.copy(decodedValues[2]);			// Rd <- Rs1 MOP Rs2
			logicUnit.op2.copy(decodedValues[3]);
			
			logicUnit.doOperation(extractFunction());
			
			valueToStore.copy(logicUnit.result);
		}
		case R2 -> {
			
			logicUnit.op1.copy(decodedValues[1]);			// Rd <- Rd MOP Rs
			logicUnit.op2.copy(decodedValues[2]);

			logicUnit.doOperation(extractFunction());
			
			valueToStore.copy(logicUnit.result);
			
		}
		case DEST_ONLY-> valueToStore.copy(decodedValues[0]); 	// COPY: Rd <- imm 
		case NO_R -> haltedBit.set();
		}
	}
	
	private void branch(Word[] decodedValues) { //{immediate, Rd, Rs1, Rs2}
		switch(getRegisterType()) {
			case R3 -> {															//pc <- Rs1 BOP Rs2 ? (pc + imm) : pc				
				doMath(decodedValues[2], mathOps.SUBTRACT, decodedValues[3]);
				
				if(evaluateBoolean(logicUnit.result)) {		
					PC.copy(doMath(PC, mathOps.ADD, decodedValues[0]));
				}
			}
			case R2 -> {															//pc <- Rs BOP Rd ? (pc + imm) : pc
				doMath(decodedValues[2], mathOps.SUBTRACT, decodedValues[1]);
	
				if(evaluateBoolean(logicUnit.result)) {	
					PC.copy(doMath(PC, mathOps.ADD, decodedValues[0]));
				}
				
			}
			case DEST_ONLY -> PC.copy(doMath(PC, mathOps.ADD, decodedValues[0])); 	//JUMP: pc <- pc + imm
			case NO_R -> PC.copy(decodedValues[0]);									//JUMP: pc <- imm
		}
	}
	
	private Word doMath(Word op1, mathOps mathOp, Word op2) {
		clock.currentCycle += 2;
		logicUnit.op1.copy(op1);
		logicUnit.op2.copy(op2);
		
		logicUnit.doOperation(mathOp.getOpcode());
		
		return logicUnit.result;
	}
	
	private void call(Word[] decodedValues) {
		switch(getRegisterType()) {
		case R3 -> {					//pc <- Rs1 BOP Rs2 ? (push pc; Rd + imm) : pc
			doMath(decodedValues[2], mathOps.SUBTRACT, decodedValues[3]);
			
			if(evaluateBoolean(logicUnit.result)) {
				PC.decrement();			//push old pc on stack
				pushToStack(PC);
				PC.copy(doMath(decodedValues[1], mathOps.ADD, decodedValues[0]));
			}
		}
		case R2 -> {					//pc <- Rs BOP Rd ? (push pc; pc + imm) : pc
			doMath(decodedValues[2], mathOps.SUBTRACT, decodedValues[1]);
			
			if(evaluateBoolean(logicUnit.result)) {	
				PC.decrement();			//push old pc on stack
				pushToStack(PC);
				PC.copy(doMath(PC, mathOps.ADD, decodedValues[0]));
			}
		}
		case DEST_ONLY -> {				//push pc; pc <- Rd + imm
			PC.decrement();				//push old pc on stack
			pushToStack(PC);
			PC.copy(doMath(decodedValues[1], mathOps.ADD, decodedValues[0]));
		}
		case NO_R -> {					//push pc; pc <- imm
			PC.decrement();				//push old pc on stack
			pushToStack(PC);
			PC.copy(decodedValues[0]);
		}
		}
	}
	
	private void pushToStack(Word word) {
		SP.decrement();
		L2Cache.write(SP, word);
	}
	
	private void push(Word[] decodedValues) { //{imm, Rd, Rs1, Rs2}	
		switch(getRegisterType()) {
		case R3 -> {									//mem[--sp] <- Rs1 MOP Rs2
			logicUnit.op1.copy(decodedValues[2]);
			logicUnit.op2.copy(decodedValues[3]);
			
			logicUnit.doOperation(extractFunction());
			pushToStack(logicUnit.result);
		}
		case R2 -> {									//mem[--sp] <- Rd MOP Rs
			logicUnit.op1.copy(decodedValues[1]);
			logicUnit.op2.copy(decodedValues[2]);
			
			logicUnit.doOperation(extractFunction());
			pushToStack(logicUnit.result);
		}
		case DEST_ONLY -> {								//mem[--sp] <- Rd MOP imm
			logicUnit.op1.copy(decodedValues[1]);
			logicUnit.op2.copy(decodedValues[0]);
			
			logicUnit.doOperation(extractFunction());
			pushToStack(logicUnit.result);
		}
		case NO_R -> {/*UNUSED*/}
		}
	}
	
	private void load(Word[] decodedValues) {
		switch(getRegisterType()) {
		case R3 -> {																//Rd <- mem[Rs1 + Rs2]
			Word index = doMath(decodedValues[2], mathOps.ADD, decodedValues[3]);
			valueToStore.copy(L2Cache.read(index));
		}
		case R2 ->{																	//Rd <- mem[Rs + imm]
			Word index = doMath(decodedValues[2], mathOps.ADD, decodedValues[0]);
			valueToStore.copy(L2Cache.read(index));
		}
		case DEST_ONLY -> {															//Rd <- mem[Rd + imm]
			Word index = doMath(decodedValues[1], mathOps.ADD, decodedValues[0]);
			valueToStore.copy(L2Cache.read(index));
		}
		case NO_R -> {																//[RETURN] pc <- pop 
			PC.copy(L2Cache.read(SP));
			SP.incriment();
		}
		}
	}
	
	private void pop(Word[] decodedValues) {
		switch(getRegisterType()) {
		case R3 -> {							//PEEK: Rd <- mem[SP - (Rs1 + Rs2)]
			Word inner = doMath(decodedValues[2], mathOps.ADD, decodedValues[3]);			//x = Rs1 + Rs2
			valueToStore.copy(L2Cache.read(doMath(SP, mathOps.SUBTRACT, inner)));		//Rd <- mem[SP - x]
		}
		case R2 -> {							//PEEK: Rd <- mem[SP - (Rs + imm)]
			Word inner = doMath(decodedValues[2], mathOps.ADD, decodedValues[0]);			//x = Rs + imm
			valueToStore.copy(L2Cache.read(doMath(SP, mathOps.SUBTRACT, inner)));		//Rd <- mem[SP - x]
		}
		case DEST_ONLY -> {						//POP: Rd <- mem[SP++]
			valueToStore.copy(L2Cache.read(SP));
			SP.incriment();
		}			
		case NO_R -> {/*INTERUPT*/}
		}
	}
	
	private boolean equals(Word word) {
		for(int i = 31; i >= 0; i--) {			// Should be 0 if equal
			if(word.getBit(i).getValue()) {
				return false;
			}
		}
		return true;
	}
	
	private boolean evaluateBoolean(Word word) {
		var bit1 = currentInstruction.getBit(10).getValue(); 	// [xxx1]
		var bit2 = currentInstruction.getBit(11).getValue(); 	// [xx1x]
		var bit3 = currentInstruction.getBit(12).getValue(); 	// [x1xx]
		var bit4 = currentInstruction.getBit(13).getValue(); 	// [1xxx]
		
		if(!bit4 && !bit3 && !bit2 && !bit1) {		// Equals - [0000]
			return equals(word);
		}
		else if(!bit4 && !bit3 && !bit2 && bit1) {	// Not Equal - [0001]
			return !equals(word);
		}
		else if(!bit4 && !bit3 && bit2 && !bit1) {	// Less Than - [0010]
			return word.getBit(31).getValue();		// significant bit set then negative => less than
		}
		else if(!bit4 && !bit3 && bit2 && bit1) {	// Greater Than or Equal - [0011]
			return (!word.getBit(31).getValue() || equals(word));
		}
		else if(!bit4 && bit3 && !bit2 && !bit1) {	// Greater Than - [0100]
			boolean sigBitFalse = !word.getBit(31).getValue();
			
			for(int i = 30; i >= 0; i--) {			// ex. 0 -> sigBit is 0 but is NOT greater
				if(word.getBit(i).getValue()) {		// if a 1 is found then immediately return 
					return (sigBitFalse);		
				}
			}
			return false;
		}
		else if(!bit4 && bit3 && !bit2 && bit1) {	// Less Than or Equal - [0101]
			return (word.getBit(31).getValue() || equals(word));
		}
		else {
			throw new IllegalArgumentException("Invalid Boolean Operation");
		}
	}
	
	private Word[] get3Rdata() {
		Word Rd = extractValue(5, 9);
        Word Rs2 = extractValue(14, 18);
        Word Rs1 = extractValue(19, 23);
        Word immediate = extractValue(24, 31);
        
       return new Word[] { 
        		immediate,  
        		valueAtRegister(Rd), 
        		valueAtRegister(Rs1),
        		valueAtRegister(Rs2)
        	};
	}
	
	private Word[] get2Rdata() {
		Word Rd = extractValue(5, 9);
        Word Rs = extractValue(14, 18);
        Word immediate = extractValue(19, 31);
        
        return new Word[] { 
        		immediate,  
				valueAtRegister(Rd), 
				valueAtRegister(Rs)
			};
	}
	
	private Word[] getDestOnlyData() {
		Word Rd = extractValue(5, 9);
		Word immediate = extractValue(14, 31);
		

		return new Word[] { 
				immediate, 
				valueAtRegister(Rd) 
			};
	}
	
	private Bit[] extractFunction() {
		Word word = currentInstruction.and(Word.createMask(10, 13)).rightShift(10);
		
		//0111
		var fn = new Bit[] {word.getBit(3), word.getBit(2), word.getBit(1), word.getBit(0)};
		
		if(!fn[0].getValue() && fn[1].getValue() && fn[2].getValue() && fn[3].getValue()) {
			clock.currentCycle += 10;
		}
		else {
			clock.currentCycle += 2;
		}
	    return fn;
	}
	
	private Word extractValue(int startBit, int endBit) {
	    return currentInstruction.and(Word.createMask(startBit, endBit)).rightShift(startBit);
	}

	private Word valueAtRegister(Word register) {
		int total = 0;
		
		for(int i = 0; i < 5; i++) {
			if(register.getBit(i).getValue()) {
				total += Math.pow(2, i);		
			}
		}
		return registers[total];
	}
	
	// FOR  TESTING
	public Bit getHalted() {
		return haltedBit;
	}
	
	public Word[] getRegisters() {
		return registers;
	}
	
	public Word getPC() {
		return PC;
	}
	
	public Word getStackPointer() {
		return SP;
	}
}
