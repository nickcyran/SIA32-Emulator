
public class Word {
	private static final int WORD_SIZE = 32;

	private Bit[] bitArray;

	Word() {
		bitArray = new Bit[WORD_SIZE];

		// (NOTE: direction doesn't matter as it all gets initialized to f)
		for (int i = 0; i < WORD_SIZE; i++) {
			bitArray[i] = new Bit(false);
		}
	}
	
	public static Word createMask(int start, int end) {
		Word mask = new Word();
		
		for(int i = start; i <= end; i++) {
			mask.setBit(i, new Bit(true));
		}
		return mask;
	}

	/*
	 * the data is read right to left and will be stored in such a manner that Last
	 * in the array is first read.
	 */
	public Bit getBit(int i) {
		return new Bit(bitArray[(WORD_SIZE - 1) - i].getValue());
	}
	
	public void setBit(int i, Bit value) {
		bitArray[(WORD_SIZE - 1) - i] = value;
	}

	// And each bit in this word with the other word
	public Word and(Word other) {
		Word returnWord = new Word();

		for (int i = 0; i < WORD_SIZE; i++) {
			returnWord.setBit(i, getBit(i).and(other.getBit(i)));
		}
		return returnWord;
	}

	// Or each bit in this word with the other word
	public Word or(Word other) {
		Word returnWord = new Word();

		for (int i = 0; i < WORD_SIZE; i++) {
			returnWord.setBit(i, getBit(i).or(other.getBit(i)));
		}
		return returnWord;
	}

	// Xor each bit in this word with the other word
	public Word xor(Word other) {
		Word returnWord = new Word();

		for (int i = 0; i < WORD_SIZE; i++) {
			returnWord.setBit(i, getBit(i).xor(other.getBit(i)));
		}
		return returnWord;
	}

	// Go through each bit setting as the inverse; return the compliment
	public Word not() {
		Word returnWord = new Word();

		for (int i = 0; i < WORD_SIZE; i++) {
			returnWord.setBit(i,getBit(i).not());
		}
		return returnWord;
	}
	
	public Word rightShift(int amount) {
		amount = (amount + WORD_SIZE) % WORD_SIZE;				// carryover i.e) bit >> 40 = bit >> 8
		
		if(amount == 0) {										// bit shifting 0 places results in the same word
			var word = new Word();
			word.copy(this);
			return word;
		}
		
		Word returnWord = new Word();
		
		if(getBit(WORD_SIZE - 1).getValue()) {					// if negative number, pre-flip all bits
			returnWord = returnWord.not();
		}
		
		for(int i = 0; i < WORD_SIZE; i++ ) {
			int shiftIndex = (i - amount);	
																				
			if(shiftIndex >= 0 && shiftIndex < WORD_SIZE) {		// if the amount we want to shift is in bounds then shift
				returnWord.setBit(shiftIndex, getBit(i));		
			}													
		}
		
		return returnWord;
	}
	
	public void incriment() {
		Bit carry = new Bit(true);
		
		for(int i = 0; i < WORD_SIZE; i++) {
			Bit currentBit = getBit(i);						//A: currentBit; B: carry;
			setBit(i, currentBit.xor(carry));				//A xor B = sum
			carry = currentBit.and(carry);					//A and B = carry
		}
	}
	
	public void decrement() {
	    Bit borrow = new Bit(true); 			

	    for (int i = 0; i < WORD_SIZE; i++) {
	        Bit currentBit = getBit(i);
	        setBit(i, currentBit.xor(borrow)); 
	        borrow = borrow.and(currentBit.not()); 	// If current bit is 0, set borrow to 1, else 0;
	    }
	}
	
	public Word leftShift(int amount) {
		amount = ((amount % WORD_SIZE) + WORD_SIZE) % WORD_SIZE;	// WHY IS THIS WORKING?????
		
		if(amount == 0) {								  			// bit shifting 0 places results in the same word
			var word = new Word();
			word.copy(this);
			return word;
		}
		
		Word returnWord = new Word();
		
		for(int i = 0; i < (WORD_SIZE); i++ ) {
			int shiftIndex = (i + amount);	
																				
			if(shiftIndex < WORD_SIZE && shiftIndex >= 0) {			// if the amount we want to shift is in bounds then shift
				returnWord.setBit(shiftIndex, getBit(i));		
			}		
		}
		
		return returnWord;
	}
	
	public boolean equalTo(Word other) {
		for(int i = 0; i < (WORD_SIZE); i++) {
			if(getBit(i).getValue() != other.getBit(i).getValue()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Bit bit : bitArray) {
			sb.append(bit.toString() + ",");
		}

		// Cut off ending ','
		return sb.substring(0, sb.length() - 1);
	}
	
	public String asBinaryString() {
		StringBuilder sb = new StringBuilder();
		
		int i = 0;
		for (Bit bit : bitArray) {
			sb.append((bit.getValue() ? 1 : 0));
			
			i++;
			if(i==4) {
				sb.append(" ");
				i = 0;
			}
		}
		return sb.toString();
	}

	public void copy(Word other) {
		for(int i = 0; i < WORD_SIZE; i++) {		
			setBit(i, new Bit(other.getBit(i).getValue()));
		}
	}

	public void addCarryOver(int i) {						// ex) 101001 + 1 -> 101010
		if(getBit(i).getValue() && i < (WORD_SIZE - 1)) {	// bit 1 -> add one to the next bit
			setBit(i, new Bit(false));
			addCarryOver(i + 1);
		}
		else {
			setBit(i, new Bit(true));
		}
	}
	
	// !!!USE FOR UNIT TESTING!!!
	public int getSigned() {
		int total = 0;
		boolean negative = getBit(WORD_SIZE - 1).getValue();

		if (negative) {
			total -= 1; 								// signed negatives start at -1

			for (int i = 0; i < WORD_SIZE; i++) {
				if (!getBit(i).getValue()) {			// subtract present powers of 2 from total
					total -= Math.pow(2, i);			
				}
			}
		} else {
			for (int i = 0; i < WORD_SIZE; i++) {
				if (getBit(i).getValue()) {
					total += Math.pow(2, i);
				}
			}
		}

		return total;
	}
	
	public long getUnsigned() {
		long total = 0;
		
		for (int i = 0; i < WORD_SIZE; i++) {	// go through all 32. no sign means all bits add to total
			if(getBit(i).getValue()) {
				total += Math.pow(2, i);		// binary base 2. add up all bits that are true
			}
		}
		return total;
	}

	public void set(int value) {
		int number = value;
		
		// If the number is negative do with twos compliment
		if(number < 0) {
			number = Math.abs(number);

			setBit((WORD_SIZE - 1), new Bit(true));			// set the bit to indicate negative
			
			for (int i = 0; i < (WORD_SIZE - 1); i++) {		
				Boolean asBool = (number % 2 != 1);			// negatives work opposite
				
				setBit(i, new Bit(asBool));
				number >>= 1;								// set number to next spot
			}
			addCarryOver(0);								// add a single bit at the end for twos compliment
		}
		else {
			for (int i = 0; i < (WORD_SIZE - 1); i++) {
				Boolean asBool = (number % 2 == 1);
				
				setBit(i, new Bit(asBool));
				number >>= 1;								// set number to next spot
			}
			this.setBit((WORD_SIZE - 1), new Bit(false));	// ensure first is false
		}
	}
	
}
