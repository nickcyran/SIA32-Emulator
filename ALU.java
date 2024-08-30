	
public class ALU {
	public Word op1;
	public Word op2;
	public Word result;
	
	ALU (){
		this(new Word(), new Word());
	}
	
	ALU (Word op1, Word op2){
		this.op1 = op1;
		this.op2 = op2;
		result = new Word();
	}
	
	private Word add2(Word A, Word B, Bit Cin) {
		// A + B
		Word retWord = new Word();
		Bit carry = Cin;
		
		for(int i = 0; i < 32; i++) { 	// Word length = 32
			Bit a = A.getBit(i);
			Bit b = B.getBit(i);
			
			Bit out = a.xor(b);
			Bit S = out.xor(carry);
			retWord.setBit(i, S);
			
			carry = a.and(b).or(out.and(carry));
		}
		return retWord;
	}
	
	private Word add2(Word A, Word B) {
		// A + B
		Word retWord = new Word();
		Bit carry = new Bit(false);
		
		for(int i = 0; i < 32; i++) { 	// Word length = 32
			Bit a = A.getBit(i);
			Bit b = B.getBit(i);
			
			Bit out = a.xor(b);
			Bit S = out.xor(carry);
			retWord.setBit(i, S);
			
			carry = a.and(b).or(out.and(carry));
		}
		return retWord;
	}
	
	//TODO: public for unit testing
	public Word add4(Word A, Word B, Word C, Word D) {
		// A + B + C + D
	    Word retWord = new Word();

	    Bit c0 = new Bit(false);
	    Bit c1 = new Bit(false); 
	    Bit c2 = new Bit(false); 
	    
	    for (int i = 0; i < 32; i++) { // Word length = 32
	        Bit a = A.getBit(i);
	        Bit b = B.getBit(i);
	        Bit c = C.getBit(i);
	        Bit d = D.getBit(i);
	        
	        Bit sumFA0 = (a.xor(b)).xor(c0);						// sum:  FA(a, b, c0)
	        Bit sumFA1 = (c.xor(d)).xor(c1);						// sum:  FA(c, d, c1)
	        
	        Bit coutFA0 = ((a.xor(b)).and(c0)).or(a.and(b));		// Cout: FA(a, b, c0)
	        Bit coutFA1 = ((c.xor(d)).and(c1)).or(c.and(d));  		// Cout: FA(c, d, c1)
	        
	        Bit totalSum = sumFA0.xor(sumFA1);						// sum:  HA(x, y)
	        Bit cout0 = sumFA0.and(sumFA1);							// Cout: HA(x, y)
	        
	        Bit cout1 = (coutFA0.xor(coutFA1)).xor(c2);								// sum:  FA(j, f, c2)
	        Bit cout2 = ((coutFA0.xor(coutFA1)).and(c2)).or(coutFA0.and(coutFA1));	// Cout: FA(j, f, c2)
	        
	        retWord.setBit(i, totalSum);
	        c0 = cout0;
	        c1 = cout1;
	        c2 = cout2;
	    }

	    return retWord;
	}
	
	private Word multiply(Word A, Word B) {
		Word[] ands = new Word[32];
		
		// Round set up
		for(int i = 0; i < 32; i++) {
			Word andedWord = new Word();
			for(int k = 0; k < 32; k++) {
				andedWord.setBit(k, B.getBit(i).and(A.getBit(k)));
			}
			ands[i] = andedWord.leftShift(i);
		}
		
		//-----ROUND 1-----
		var res0 = add4(ands[0], ands[1],ands[2], ands[3]);
		var res1 = add4(ands[4], ands[5],ands[6], ands[7]);
		var res2 = add4(ands[8], ands[9],ands[10], ands[11]);
		var res3 = add4(ands[12], ands[13],ands[14], ands[15]);
		
		var res4 = add4(ands[16], ands[17],ands[18], ands[19]);
		var res5 = add4(ands[20], ands[21],ands[22], ands[23]);
		var res6 = add4(ands[24], ands[25],ands[26], ands[27]);
		var res7 = add4(ands[28], ands[29],ands[30], ands[31]);
		
		//-----ROUND 2-----
		var x = add4(res0, res1, res2, res3);
		var y = add4(res4, res5, res6, res7);
		
		//-----ROUND 3-----
		return add2(x, y);
	}
	
	private Word subtract(Word word1, Word word2) {
		// word1 + -word2
		// negative in twos compliment: !word + 1
		return add2(word1, word2.not(), new Bit(true));
	}
	
	public void doOperation(Bit[] operation) {		// 4 bits in array
		if(operation[3].getValue()) {				// --xxx1--
			if(operation[2].getValue()) {			// --xx11--
				if(operation[1].getValue()) {		// --x111--	
					if(operation[0].getValue()) {	// --1111-- (subtract)
						result.copy(subtract(op1, op2));
					}
					else {							// --0111-- (multiply)
						result.copy(multiply(op1, op2));
						
					}
				}
				else {								// --x011--
					if(operation[0].getValue()) {	// --1011-- (not)
						result.copy(op1.not());
					}
					else {							// --0011--
						throw new IllegalArgumentException("bit operation 0011");
					}
				}
			}
			else {									// --xx01--
				if(operation[1].getValue()) {		// --x101--
					if(operation[0].getValue()) {	// --1101-- (right shift)
						int shiftAmount = 0;
						
						for(int i = 0; i < 5; i++) {
							if(op2.getBit(i).getValue()) {
								shiftAmount += Math.pow(2, i);
							}
						}
						
						result.copy(op1.rightShift(shiftAmount));
					}
					else {							// --0101--
						throw new IllegalArgumentException("bit operation 0101");
					}
				}
				else {								// --x001--
					if(operation[0].getValue()) {	// --1001-- (or)
						result.copy(op1.or(op2));
					}
					else {							// --0001--
						throw new IllegalArgumentException("bit operation 0001");
					}
				}
			}
		}
		else {										// --xxx0--
			if(operation[2].getValue()) {			// --xx10--
				if(operation[1].getValue()) {		// --x110--
					if(operation[0].getValue()) {	// --1110-- (add)
						result.copy(add2(op1, op2));
					}
					else {							// --0110--
						throw new IllegalArgumentException("bit operation 0110");
					}
				}
				else {								// --x010--
					if(operation[0].getValue()) {	// --1010-- (xor)
						result.copy(op1.xor(op2));
					}
					else {							// --0010--
						throw new IllegalArgumentException("bit operation 0010");
					}
				}
			}
			else {									// --xx00--
				if(operation[1].getValue()) {		// --x100--
					if(operation[0].getValue()) {	// --1100-- (left shift)
						int shiftAmount = 0;
						
						for(int i = 0; i < 5; i++) {
							if(op2.getBit(i).getValue()) {
								shiftAmount += Math.pow(2, i);
							}
						}
						
						result.copy(op1.leftShift(shiftAmount));
					}
					else {							// --0100-- 
						throw new IllegalArgumentException("bit operation 0100");
					}
				}
				else {								// --x000--
					if(operation[0].getValue()) {	// --1000-- (and)
						result.copy(op1.and(op2));
					}
					else {							// --0000-- 
						throw new IllegalArgumentException("bit operation 0000");
					}
				}
			}
		}
	}
}
