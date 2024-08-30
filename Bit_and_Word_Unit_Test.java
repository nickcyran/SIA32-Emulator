import org.junit.Assert;
import org.junit.Test;

public class Bit_and_Word_Unit_Test {

	public void printBinary(Word w) {
		System.out.println("["+w.toString().replace("t", "1").replace("f", "0")+"]");
	}

	/* ----------------------------------------- TEST BIT -----------------------------------------*/
	@Test
	public void testBitAnd() {
		Bit tBit = new Bit(true);
		Bit fBit = new Bit(false);

		// t & t = t
		Assert.assertTrue(tBit.and(tBit).getValue());

		// t & f = f
		Assert.assertFalse(tBit.and(fBit).getValue());

		// f & t = f
		Assert.assertFalse(fBit.and(tBit).getValue());

		// f & f = f
		Assert.assertFalse(fBit.and(fBit).getValue());
	}
	
	@Test
	public void testBitOr() {
		Bit tBit = new Bit(true);
		Bit fBit = new Bit(false);

		// t | t = t
		Assert.assertTrue(tBit.or(tBit).getValue());

		// t | f = t
		Assert.assertTrue(tBit.or(fBit).getValue());

		// f | t = t
		Assert.assertTrue(fBit.or(tBit).getValue());

		// f | f = f
		Assert.assertFalse(fBit.or(fBit).getValue());
	}
	
	@Test
	public void testBitXor() {
		Bit tBit = new Bit(true);
		Bit fBit = new Bit(false);

		// t ^ f = t
		Assert.assertTrue(tBit.xor(fBit).getValue());
		
		// f ^ t = t
		Assert.assertTrue(fBit.xor(tBit).getValue());
		
		// t ^ t = f
		Assert.assertFalse(tBit.xor(tBit).getValue());

		// f ^ f = f
		Assert.assertFalse(fBit.xor(fBit).getValue());
	}
	
	@Test
	public void testBitNot() {
		Bit tBit = new Bit(true);
		Bit fBit = new Bit(false);
		
		// !f = t
		Assert.assertTrue(fBit.not().getValue());

		// !t = f
		Assert.assertFalse(tBit.not().getValue());
	}
	
	/* ----------------------------------------- TEST WORD -----------------------------------------  */
	@Test
	public void testWordSetBitAndGetBit() {
		int index = 16;
		Word word = new Word();	// all f
		
		word.setBit(index, new Bit(true));
		Assert.assertTrue(word.getBit(index).getValue());
	}
	
	@Test
	public void testWordGetUnsigned() {
		// test with -1 so all bits are true
		Word word = new Word();
		word.set(-1);
		long l = word.getUnsigned();
		
		Assert.assertNotEquals(l,-1);
		Assert.assertEquals(l,(long)Math.pow(2, 32) - 1);
	}
	
	@Test
	public void testWordGetSigned() {
		// 10
		int val = 10;
		Word word = new Word();
		word.set(val);
		Assert.assertEquals(word.getSigned(), val);
		
		// MIN
		val = Integer.MIN_VALUE;
		word = new Word();
		word.set(val);
		Assert.assertEquals(word.getSigned(), val);
		
		// MAX
		val = Integer.MAX_VALUE;
		word = new Word();
		word.set(val);
		Assert.assertEquals(word.getSigned(), val);
		
		// 0
		val = 0;
		word = new Word();
		word.set(val);
		Assert.assertEquals(word.getSigned(), val);
		
		// -320145893
		val = -320145893;
		word = new Word();
		word.set(val);
		Assert.assertEquals(word.getSigned(), val);
		
		// 151652623
		val = 151652623;
		word = new Word();
		word.set(val);
		Assert.assertEquals(word.getSigned(), val);
	}
	
	@Test
	public void testWordCopy() {
		// 1302
		int val = 1302;
		
		Word input = new Word();
		input.set(val);
		
		Word apply = new Word();
		apply.copy(input);
		
		Assert.assertEquals(input.getSigned(), apply.getSigned());
		
		// -13124513
		val = -13124513;
		
		input.set(val);
		apply.copy(input);
		Assert.assertEquals(input.getSigned(), apply.getSigned());
	}
	
	@Test
	public void testWordNot() {
		// ~1203553
		int val = 1203553;
		
		Word word = new Word();
		word.set(val);
		
		Assert.assertEquals(word.not().getSigned(), (~val));
		
		// ~-3145151
		val = -3145151;
				
		word = new Word();
		word.set(val);
				
		Assert.assertEquals(word.not().getSigned(), (~val));
		
		// ~MAX
		val = Integer.MAX_VALUE;
				
		word = new Word();
		word.set(val);
				
		Assert.assertEquals(word.not().getSigned(), (~val));
		
		// ~MIN
		val = Integer.MIN_VALUE;
				
		word = new Word();
		word.set(val);
				
		Assert.assertEquals(word.not().getSigned(), (~val));
		
		// ~0
		val = 0;
				
		word = new Word();
		word.set(val);
				
		Assert.assertEquals(word.not().getSigned(), (~val));
	}
	
	@Test
	public void testWordXor() {
		// 1203 ^ 5202
		int intOne = 1203;
		int intTwo = 5202;
						
		Word wordOne = new Word();
		Word wordTwo = new Word();		
						
		wordOne.set(intOne);
		wordTwo.set(intTwo);	
						
		Assert.assertEquals(wordOne.xor(wordTwo).getSigned(), (intOne ^ intTwo));
		
		// -1 | 3194001
		intOne = -1;
		intTwo = 3194001;
				
		wordOne.set(intOne);
		wordTwo.set(intTwo);	
						
		Assert.assertEquals(wordOne.xor(wordTwo).getSigned(), (intOne ^ intTwo));
		
		// MIN | 14023
		intOne = Integer.MIN_VALUE;
		intTwo = 14023;
				
		wordOne.set(intOne);
		wordTwo.set(intTwo);	
						
		Assert.assertEquals(wordOne.xor(wordTwo).getSigned(), (intOne ^ intTwo));
		
		// -45151 | -124441
		intOne = -45151;
		intTwo = -124441;
				
		wordOne.set(intOne);
		wordTwo.set(intTwo);	
						
		Assert.assertEquals(wordOne.xor(wordTwo).getSigned(), (intOne ^ intTwo));
	}
	
	@Test
	public void testWordOr() {
		// 10 | 5202
		int intOne = 10;
		int intTwo = 5202;
				
		Word wordOne = new Word();
		Word wordTwo = new Word();		
				
		wordOne.set(intOne);
		wordTwo.set(intTwo);	
				
		Assert.assertEquals(wordOne.or(wordTwo).getSigned(), (intOne | intTwo));
		
		// -1 | 3194001
		intOne = -1;
		intTwo = 3194001;
		
		wordOne.set(intOne);
		wordTwo.set(intTwo);	
				
		Assert.assertEquals(wordOne.or(wordTwo).getSigned(), (intOne | intTwo));
		
		// MIN | 0
		intOne = Integer.MIN_VALUE;
		intTwo = 0;
		
		wordOne.set(intOne);
		wordTwo.set(intTwo);	
				
		Assert.assertEquals(wordOne.or(wordTwo).getSigned(), (intOne | intTwo));
		
		// -45151 | -124441
		intOne = -45151;
		intTwo = -124441;
				
		wordOne.set(intOne);
		wordTwo.set(intTwo);	
						
		Assert.assertEquals(wordOne.or(wordTwo).getSigned(), (intOne | intTwo));
	}
	
	@Test
	public void testWordAnd() {
		// 10 & 5202
		int intOne = 10;
		int intTwo = 5202;
		
		Word wordOne = new Word();
		Word wordTwo = new Word();		
		
		wordOne.set(intOne);
		wordTwo.set(intTwo);	
		
		Assert.assertEquals(wordOne.and(wordTwo).getSigned(), (intOne & intTwo));
		
		// -30 & MIN
		intOne = Integer.MIN_VALUE;
		intTwo = -30;
		
		wordOne.set(intOne);
		wordTwo.set(intTwo);	
		
		Assert.assertEquals(wordOne.and(wordTwo).getSigned(), (intOne & intTwo));
		
		// 0 & MAX
		intOne = Integer.MAX_VALUE;
		intTwo = 0;
		
		wordOne.set(intOne);
		wordTwo.set(intTwo);	
		
		Assert.assertEquals(wordOne.and(wordTwo).getSigned(), (intOne & intTwo));
		
	}

	@Test
	public void testWordRightShift() {
		Word word = new Word();
		Word shifted;

		// 33 >> 1
		word.set(33);
		shifted = word.rightShift(1);
		Assert.assertEquals(shifted.getSigned(), 33 >> 1);

		// MAX >> 18
		word.set(Integer.MAX_VALUE);
		shifted = word.rightShift(18);
		Assert.assertEquals(shifted.getSigned(), Integer.MAX_VALUE >> 18);

		// MIN >> 3
		word.set(Integer.MIN_VALUE);
		shifted = word.rightShift(3);
		Assert.assertEquals(shifted.getSigned(), Integer.MIN_VALUE >> 3);

		// -1800421 >> 36 -> carryover so -1800421 >> 4
		word.set(-1800421);
		shifted = word.rightShift(36);
		Assert.assertEquals(shifted.getSigned(), -1800421 >> 4);

		// -1 >> 12
		word.set(-1);
		shifted = word.rightShift(12);
		Assert.assertEquals(shifted.getSigned(), -1 >> 12);

		// 0 >> 12
		word.set(0);
		shifted = word.rightShift(12);
		Assert.assertEquals(shifted.getSigned(), 0 >> 12);

		// 42 >> -31
		word.set(42);
		shifted = word.rightShift(-31);
		Assert.assertEquals(shifted.getSigned(), 42 >> -31);

		// -302 >> 0
		word.set(-302);
		shifted = word.rightShift(0);
		Assert.assertEquals(shifted.getSigned(), -302 >> 0);

		// 290125 >> 123
		word.set(290125);
		shifted = word.rightShift(123);
		Assert.assertEquals(shifted.getSigned(), 290125 >> 123);

		// -1 >> -39
		word.set(-1);
		shifted = word.rightShift(-39);
		Assert.assertEquals(shifted.getSigned(), -1 >> -39);
	}

	@Test
	public void testWordLeftShift() {
		Word word = new Word();
		Word shifted;

		int num;
		int shiftBy;

		// 33 << 1
		num = 33;
		shiftBy = 1;

		word.set(num);
		shifted = word.leftShift(shiftBy);
		Assert.assertEquals(shifted.getSigned(), num << shiftBy);

		// MAX << 12
		num = Integer.MAX_VALUE;
		shiftBy = 12;

		word.set(num);
		shifted = word.leftShift(shiftBy);
		Assert.assertEquals(shifted.getSigned(), num << shiftBy);

		// MIN << 3
		num = Integer.MIN_VALUE;
		shiftBy = 3;

		word.set(num);
		shifted = word.leftShift(shiftBy);

		Assert.assertEquals(shifted.getSigned(), num << shiftBy);

		// -10001321 << -6
		num = -10001321;
		shiftBy = -6;

		word.set(num);
		shifted = word.leftShift(shiftBy);

		Assert.assertEquals(shifted.getSigned(), num << shiftBy);

		// -1 << -39
		num = -1;
		shiftBy = -39;

		word.set(num);
		shifted = word.leftShift(shiftBy);
		Assert.assertEquals(shifted.getSigned(), num << shiftBy);

		// 121 << 0
		num = 121;
		shiftBy = 0;

		word.set(num);
		shifted = word.leftShift(shiftBy);
		Assert.assertEquals(shifted.getSigned(), num << shiftBy);

		// 0 << 10
		num = 0;
		shiftBy = 10;

		word.set(num);
		shifted = word.leftShift(shiftBy);
		Assert.assertEquals(shifted.getSigned(), num << shiftBy);

	}
	
	
	@Test
	public void pp() {
		Word word = new Word();
		
		int s = -39;
		word.set(-1);
	
		var k = word.leftShift(s);
		
		printBinary(word);
		printBinary(k);
		
	}
}
