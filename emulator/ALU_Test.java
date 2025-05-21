import static org.junit.Assert.assertEquals;

import org.junit.Test;

enum BitOps {
	SUBTRACT(new int[] { 1, 1, 1, 1 }),
	MULTIPLY(new int[] { 0, 1, 1, 1 }),
	ADD(new int[] { 1, 1, 1, 0 }),	
	NOT(new int[] { 1, 0, 1, 1 }),		
	R_SHIFT(new int[] { 1, 1, 0, 1 }),
	L_SHIFT(new int[] { 1, 1, 0, 0 }),
	OR(new int[] { 1, 0, 0, 1 }),	
	XOR(new int[] { 1, 0, 1, 0 }),	
	AND(new int[] { 1, 0, 0, 0 }); 

	private int[] operation;

	BitOps(int[] x) {
		operation = x;
	}

	public Bit[] getOp() {
		Bit[] op = new Bit[4];
		
		for(int i = 0; i < 4; i++) {
			op[i] = (operation[i] == 1) ? new Bit(true) : new Bit(false);
		}
		return op;
	}
}

public class ALU_Test {
	interface Operation {
		void execute(int x, int y);
	}
	
	@Test
	public void testAdd2() {
		ALU alu = new ALU();
		
        Operation add = (x, y) -> {
            alu.op1.set(x);
            alu.op2.set(y);
            alu.doOperation(BitOps.ADD.getOp());
            assertEquals(alu.result.getSigned(), (x + y));
        };
		
        // 24 + 13 = 37
        add.execute(24,13);
        
		// -30132031 + 908 = -30131123
		add.execute(-30132031, 908);

		// 0 + 10 = 10
		add.execute(0, 10);
		
		// MIN + 10902981 = -2136580667
		add.execute(Integer.MIN_VALUE, 10902981);	
		
		// MAX + 0 = MAX
		add.execute(Integer.MAX_VALUE, 0);	
	}
	
	@Test
	public void testAdd4() {
		@SuppressWarnings("hiding")
		interface Operation {
			void execute(int w, int x, int y, int z);
		}
		
		ALU alu = new ALU();
		
		Word a = new Word();
		Word b = new Word();
		Word c = new Word();
		Word d = new Word();
		
		Operation add4 = (w,x,y,z) ->{
			a.set(w);
			b.set(x);
			c.set(y);
			d.set(z);
			
			Word result = alu.add4(a, b, c, d);
			assertEquals(result.getSigned(), (w + x + y + z));
		};
		
		// 4 + 8 + 9 + 10 = 31
		add4.execute(4, 8, 9, 10);
		
		// -109122 + 80139 + -1 + 0 = -28984
		add4.execute(-109122, 80139, -1, 0);
		
		// MAX + 1 + -2 + 1 = MAX
		add4.execute(Integer.MAX_VALUE, 1, -2, 1);
		
		// 1303 + 808018294 + 2209 + 12 = 808021818
		add4.execute(1303, 808018294, 2209, 12);
	}
	
	@Test
	public void testMultiply() {
		ALU alu = new ALU();

		Operation multiply = (x, y) -> {
			alu.op1.set(x);
			alu.op2.set(y);
			alu.doOperation(BitOps.MULTIPLY.getOp());
			assertEquals(alu.result.getSigned(), (x * y));
		};
		
		// 1002325 * 0 = 0
		multiply.execute(1002325, 0);
		
		// 14 * 23 = 322
		multiply.execute(14, 23);
		
		// 1924 * -1 = -1924
		multiply.execute(1924, -1);
		
		// -4 * -13 = 52
		multiply.execute(-4, -13);
		
		// MAX * 2
		multiply.execute(2, Integer.MAX_VALUE);
		
		// MIN * 5
		multiply.execute(5, Integer.MIN_VALUE);
	}
	
	@Test
	public void testSubtract() {
		ALU alu = new ALU();

		Operation subtract = (x, y) -> {
			alu.op1.set(x);
			alu.op2.set(y);
			alu.doOperation(BitOps.SUBTRACT.getOp());
			assertEquals(alu.result.getSigned(), (x - y));
		};
		
		// 2416 - 1345 = 1071
		subtract.execute(2416, 1345);
        
		// -30132031 - 908 = -30132939
		subtract.execute(-30132031, 908);

		// 0 - 10 = -10
		subtract.execute(0, 10);
		
		// MIN - 10892 = 2147472756
		subtract.execute(Integer.MIN_VALUE, 10892);	
		
		// MAX - 0 = MAX
		subtract.execute(Integer.MAX_VALUE, 0);	
		
		// 10 - -5 = 15
		subtract.execute(10, -5);	

	}

	@Test
	public void testDispatch() {
		ALU alu = new ALU();
		
		//Test that these are valid operations that do not throw an error
		//Subtract
		alu.doOperation(BitOps.SUBTRACT.getOp()); 
		
		//Multiply
		alu.doOperation(BitOps.MULTIPLY.getOp()); 
		
		//Not
		alu.doOperation(BitOps.NOT.getOp()); 
		
		//R_Shift
		alu.doOperation(BitOps.R_SHIFT.getOp()); 
		
		//Or
		alu.doOperation(BitOps.OR.getOp());
		
		//Add
		alu.doOperation(BitOps.ADD.getOp());
		
		//Xor
		alu.doOperation(BitOps.XOR.getOp());
		
		//L_Shift
		alu.doOperation(BitOps.L_SHIFT.getOp()); 
		
		//And
		alu.doOperation(BitOps.AND.getOp()); 
	}
}
