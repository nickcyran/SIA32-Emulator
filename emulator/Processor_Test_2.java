import org.junit.Assert;
import org.junit.Test;

public class Processor_Test_2 {
	private Processor cpu;
	private String halt = "0".repeat(32);
	
	private void setup(String...s) {
		for(String word : s) {
			if(word.length() != 32) {
				throw new IllegalArgumentException(word);
			}
		}
		
		MainMemory.load(s);
//		MainMemory.printDRAM();
		cpu = new Processor();
		cpu.run();
	}
	
	private void setupPlusPlus(int address, int word, String...s) {
		for(String w : s) {
			if(w.length() != 32) {
				throw new IllegalArgumentException(w);
			}
		}
		MainMemory.load(s);
		
		Word add = new Word();
		add.set(address);
		
		Word setWord = new Word();
		setWord.set(word);
		
		MainMemory.write(add, setWord);
//		MainMemory.printDRAM();
		cpu = new Processor();
		cpu.run();
	}
	
	private void printRegisters() {
		int i = 0;
		for(Word word : cpu.getRegisters()) {
			System.out.println("[R" + i + "] - " + word.asBinaryString() + " (" + word.getSigned() + ")");
			i++;
		}
	}
	
	@Test 
	public void testLoad() {
		String pc0;
		String pc1;
		String pc2;
		//NO R
		//Add data to the stack
		pc0 = "000000000000000000000000001" + "01000";	//CALL 1
		pc1 = "000000000000000000000000010" + "01000";	//CALL 2
		pc2 = "000000000000000000000000011" + "01000";	//CALL 3
		String pc4 = "000000000000000000000000000" + "10000";	//RETURN
		
		setup(pc0,pc1,pc2,halt,pc4);
		Assert.assertEquals(cpu.getPC().getSigned() - 1, 3);
		
		//DEST_ONLY
		pc0 = "000000000000000010" + "0000" + "00001" + "00001";		//MATH DestOnly 2, R1: R1 = 2
		pc1 = "000000000000000110" + "0000" + "00001" + "10001";		//LOAD R1 6
		
		setupPlusPlus(8, 1101, pc0, pc1);
		Assert.assertEquals(cpu.getRegisters()[1].getSigned(), 1101);
		
		//R2
		pc0 = "000000000000001100" + "0000" + "00001" + "00001";		//MATH DestOnly 12, R1: R1 = 12
		pc1 = "0000000001111" + "00001" + "0000" + "00010" + "10010";	//LOAD R1 15 R2
		
		setupPlusPlus(27, 21134, pc0, pc1);
		Assert.assertEquals(cpu.getRegisters()[2].getSigned(), 21134);
		
		//R3
		pc0 = "000000000000001111" + "0000" + "00001" + "00001";			//MATH DestOnly 13, R1: R1 = 13
		pc1 = "000000000000000011" + "0000" + "00010" + "00001";			//MATH DestOnly 3, R2: R2 = 3
		pc2 = "00000000" + "00001" + "00010" + "0000" + "00011" + "10011";	//LOAD R1 R2 R3
		
		setupPlusPlus(18, 1004, pc0, pc1,pc2);
		Assert.assertEquals(cpu.getRegisters()[3].getSigned(), 1004);
	}
	
	@Test 
	public void testStore() {
		//DEST_ONLY
		String pc0 = "000000000000001110" + "0000" + "00001" + "00001";		//MATH DestOnly 14, R1: R1 = 14
		String pc1 = "000000000000001010" + "0000" + "00001" + "10101";		//STORE R1 10
		
		setup(pc0, pc1);
		Assert.assertEquals(MainMemory.DRAM[14].getSigned(), 10);
		
		//R2
		pc0 = "000000000000001010" + "0000" + "00001" + "00001";			//MATH DestOnly 10, R1: R1 = 10
		pc1 = "000000000000000010" + "0000" + "00010" + "00001";			//MATH DestOnly 2, R2: R2 = 2
		String pc2 = "0000000000001" + "00010" + "0000" + "00001" + "10110";//STORE R2 R1 1
	
		setup(pc0, pc1, pc2);
		Assert.assertEquals(MainMemory.DRAM[11].getSigned(), 2);
		
		//R3
		pc0 = "000000000000001010" + "0000" + "00001" + "00001";			//MATH DestOnly 10, R1: R1 = 10
		pc1 = "000000000000000010" + "0000" + "00010" + "00001";			//MATH DestOnly 2, R2: R2 = 2
		pc2 = "000000000000000011" + "0000" + "00011" + "00001";			//MATH DestOnly 3, R3: R3 = 3
		
		String pc3 = "00000000" + "00001" + "00011" + "0000" + "00010" + "10111";	//STORE R3 R2 R1
		
		setup(pc0, pc1, pc2, pc3);
		Assert.assertEquals(MainMemory.DRAM[12].getSigned(), 3);
	}
	
	@Test 
	public void testPop() {
		//DEST_ONLY
		//Add data to the stack
		String pc0 = "000000000000000000000000001" + "01000";	//CALL 1
		String pc1 = "000000000000000000000000010" + "01000";	//CALL 2
		String pc2 = "000000000000000000000000011" + "01000";	//CALL 3
		String pc3 = "000000000000000000" + "0000" + "00001" + "11001"; //POP R1
				
		setup(pc0, pc1, pc2, pc3);
		Assert.assertEquals(cpu.getRegisters()[1].getSigned(), 2);
		Assert.assertEquals(cpu.getStackPointer().getSigned(), 1022);		//confirm it increments
		
		//R2
		pc0 = "000000000000000001" + "0000" + "00001" + "00001";		//MATH DestOnly 1, R1: R1 = 1
		pc1 = "0000000000100" + "00001" + "0000" + "00010" + "11010";	//PEEK R1 4 R2
		
		setupPlusPlus(1019, 11, pc0, pc1);
		
		Assert.assertEquals(cpu.getRegisters()[2].getSigned(), 11);
		
		//R3
		pc0 = "000000000000000001" + "0000" + "00001" + "00001";			//MATH DestOnly 1, R1: R1 = 1
		pc1 = "000000000000000100" + "0000" + "00010" + "00001";			//MATH DestOnly 4, R2: R2 = 4
		pc2 = "00000000" + "00001" + "00010" + "0000" + "00011" + "11011";	//PEEK R1 R2 R3
		
		setupPlusPlus(1019, 1034, pc0, pc1, pc2);
		Assert.assertEquals(cpu.getRegisters()[3].getSigned(), 1034);
	}

	@Test 
	public void testCall() {
		// NO R
		String instruction = "000000000000000000000000111" + "01000";	//CALL 7
		
		setup(instruction);
		Assert.assertEquals(cpu.getPC().getSigned() - 1, 7);			//accounts for PC after halt
		Assert.assertEquals(MainMemory.DRAM[1023].getSigned(), 0);		//pushed properly
		
		
		//DEST_ONLY
		String setR1 = "000000000000000010" + "0000" + "00001" + "00001";	//MATH DestOnly 2, R1: R1 = 2
		instruction = "000000000000000001" + "0000" + "00001" + "01001";	//CALL R1 1
		String post = "000000000000000000000000010" + "01000";				//CALL 2
		
		setup(setR1, instruction, halt, post);
		
		Assert.assertEquals(cpu.getPC().getSigned() - 1, 2);
		Assert.assertEquals(MainMemory.DRAM[1023].getSigned(), 1);
		Assert.assertEquals(MainMemory.DRAM[1022].getSigned(), 3);
		
		//R2
		setR1 = "000000000000000010" + "0000" + "00001" + "00001";				//MATH DestOnly 2, R1: R1 = 2
		String setR2 = "000000000000000011" + "0000" + "00010" + "00001";		//MATH DestOnly 3, R2: R2 = 3
		instruction = "0000000000010" + "00010" + "0100" + "00001" + "01010";	//CALL R2 GT R1 2
		post = "000000000000000111" + "0000" + "00101" + "00001";				//MATH DestOnly 7, R5: R5 = 7
		
		setup(setR1, setR2, instruction, halt, post);
		
		Assert.assertEquals(cpu.getPC().getSigned() - 1, 5);
		Assert.assertEquals(MainMemory.DRAM[1023].getSigned(), 2);
		Assert.assertEquals(cpu.getRegisters()[5].getSigned(), 7);				//confirm it jumped over halt
		
		//R3
		setR1 = "000000000000000010" + "0000" + "00001" + "00001";					//MATH DestOnly 2, R1: R1 = 2
		setR2 = "000000000000000011" + "0000" + "00010" + "00001";					//MATH DestOnly 3, R2: R2 = 3
		String setRd = "000000000000000010" + "0000" + "00011" + "00001";			//MATH DestOnly 2, R3: R3 = 2
		instruction = "00000011" + "00010" + "00001" + "0011" + "00011" + "01011";	//CALL R2 GE R1 R3 5
		
		setup(setR1, setR2, setRd, instruction, halt, post);
		Assert.assertEquals(cpu.getPC().getSigned() - 1, 6);
		Assert.assertEquals(MainMemory.DRAM[1023].getSigned(),3);
		Assert.assertEquals(cpu.getRegisters()[5].getSigned(), 7);				//confirm it jumped over halt
	}
	
	@Test 
	public void testPush() {
		//DEST_ONLY
		String setR1 = "000000000000000111" + "0000" + "00001" + "00001"; 			//MATH DestOnly 7, R1: R1 = 7
		String instruction = "000000000000000010" + "1110" + "00001" + "01101"; 	//PUSH R1 ADD 2: Push 7 + 9
		
		setup(setR1, instruction);	
		Assert.assertEquals(MainMemory.DRAM[1023].getSigned(), 9);
		
		//R2
		setR1 = "000000000000000011" + "0000" + "00001" + "00001"; 					//MATH DestOnly 3, R1: R1 = 3
		String setR2 = "000000000000000011" + "0000" + "00010" + "00001"; 			//MATH DestOnly 3, R2: R2 = 3
		instruction = "0000000000000" + "00001" + "1110" + "00010" + "01110"; 		//PUSH R1 ADD Rd: Push 3 + 3
		
		setup(setR1, setR2, instruction);	
		Assert.assertEquals(MainMemory.DRAM[1023].getSigned(), 6);
		
		//R3
		setR1 = "000000000000000011" + "0000" + "00001" + "00001"; 					//MATH DestOnly 3, R1: R1 = 3
		setR2 = "000000000000001001" + "0000" + "00010" + "00001"; 					//MATH DestOnly 9, R2: R2 = 9
		instruction = "00000000" + "00001" + "00010" + "1110" + "00000" + "01111";	//PUSH R1 ADD R2: Push 3 + 9
		
		setup(setR1, setR2, instruction);
		Assert.assertEquals(MainMemory.DRAM[1023].getSigned(), 12);
		
		//check to see SP decrements properly
		setR1 = "000000000000000111" + "0000" + "00001" + "00001"; 					//MATH DestOnly 7, R1: R1 = 7
		instruction = "000000000000000010" + "1110" + "00001" + "01101"; 			//PUSH R1 ADD 2: Push 7 + 2
		String instruction2 = "000000000000000010" + "0111" + "00001" + "01101"; 	//PUSH R1 MULT 2: Push 7 * 2
				
		setup(setR1, instruction, instruction2);	
		Assert.assertEquals(MainMemory.DRAM[1023].getSigned(), 9);
		Assert.assertEquals(MainMemory.DRAM[1022].getSigned(), 14);
	}
	
	@Test 
	public void testBoolean() {
				// x = 8; (x >= 5) {x +=5}; -> R1(x = 13)
				String setR1 = "000000000000001000" + "0000" + "00001" + "00001"; 				//MATH DestOnly 8, R1: R1 = 8
				String setR2 = "000000000000000101" + "0000" + "00010" + "00001"; 				//MATH DestOnly 5, R2: R2 = 5
				String branch = "0000000000001" + "00001" + "0010" + "00010" + "00110";			//BRANCH 2R R1 LT R2 1
				String inner = "00000000" + "00010" + "00001" + "1110" + "00001" + "00011"; 	//MATH ADD R2 R1 R1: R1 += 5
				
				setup(setR1, setR2, branch, inner, halt);
				Assert.assertEquals(cpu.getRegisters()[1].getSigned(), 13);		
				
				// x = 8; (x < 5) {x +=5}; -> R1(x = 8)
				setR1 = "000000000000001000" + "0000" + "00001" + "00001"; 				//MATH DestOnly 8, R1: R1 = 8
				setR2 = "000000000000000101" + "0000" + "00010" + "00001"; 				//MATH DestOnly 5, R2: R2 = 5
				branch = "0000000000001" + "00001" + "0011" + "00010" + "00110";		//BRANCH 2R R1 GE R2 1
				inner = "00000000" + "00010" + "00001" + "1110" + "00001" + "00011"; 	//MATH ADD R2 R1 R1: R1 += 5
				
				setup(setR1, setR2, branch, inner, halt);
				Assert.assertEquals(cpu.getRegisters()[1].getSigned(), 8);		
				
				// x = 5; (x == 5) {x += 5}; -> R1(x = 10)
				setR1 = "000000000000000101" + "0000" + "00001" + "00001"; 				//MATH DestOnly 5, R1: R1 = 5
				setR2 = "000000000000000101" + "0000" + "00010" + "00001"; 				//MATH DestOnly 5, R2: R2 = 5
				branch = "0000000000001" + "00001" + "0001" + "00010" + "00110";		//BRANCH 2R R1 NEQ R2 1
				inner = "00000000" + "00010" + "00001" + "1110" + "00001" + "00011"; 	//MATH ADD R2 R1 R1: R1 += 5
				
				setup(setR1, setR2, branch, inner, halt);
				Assert.assertEquals(cpu.getRegisters()[1].getSigned(), 10);	
				
				// x = 5; (x <= 5) {x += 5}; -> R1(x = 10)
				setR1 = "000000000000000101" + "0000" + "00001" + "00001"; 				//MATH DestOnly 5, R1: R1 = 5
				setR2 = "000000000000000101" + "0000" + "00010" + "00001"; 				//MATH DestOnly 5, R2: R2 = 5
				branch = "0000000000001" + "00001" + "0100" + "00010" + "00110";		//BRANCH 2R R1 GT R2 1
				inner = "00000000" + "00010" + "00001" + "1110" + "00001" + "00011"; 	//MATH ADD R2 R1 R1: R1 += 5
				
				setup(setR1, setR2, branch, inner, halt);
				Assert.assertEquals(cpu.getRegisters()[1].getSigned(), 10);	
	}
	
	@Test
	public void testBranch() {
		//No R
		String branch = "000000000000000000000000110" + "00100";					//BRANCH JUMP TO 6
		String instruction = "000000000000000010" + "0000" + "00001" + "00001"; 	//MATH DestOnly 2, R1: R1 = 2
		
		setup(branch, instruction);	
		Assert.assertEquals(cpu.getRegisters()[1].getSigned(), 0);					//jumped over insertion
		
		//Dest Only
		branch = "000000000000000010" + "0000" + "00000" + "00101";					//BRANCH JUMP BY 6
		instruction = "000000000000000010" + "0000" + "00001" + "00001"; 			//MATH DestOnly 2, R1: R1 = 2
		
		setup(branch, instruction);	
		Assert.assertEquals(cpu.getRegisters()[1].getSigned(), 0);					//jumped over insertion
		
		//2R
		String setUpRs = "000000000000000011" + "0000" + "00001" + "00001"; 		//MATH DestOnly 3, R1: R1 = 3
		branch = "0000000000001" + "00001" + "0001" + "00010" + "00110";			//BRANCH R1 NEQ Rd 1: (3 != 0)
		instruction = "000000000000000101" + "0000" + "00011" + "00001"; 			//MATH DestOnly 5, R3: R3 = 5;
		String instruction2 = "000000000000000111" + "0000" + "00100" + "00001"; 	//MATH DestOnly 7, R4: R4 = 7;
		
		setup(setUpRs, branch, instruction, instruction2);
		Assert.assertEquals(cpu.getRegisters()[3].getSigned(), 0);
		Assert.assertEquals(cpu.getRegisters()[4].getSigned(), 7);					//after jump so should be changed
		
		//3R
		branch = "00000001" + "00001" + "00010" + "0000" + "00000" + "00111";		//BRANCH Rs1 EQ Rs2 1: (0 == 0)
		instruction = "000000000000000101" + "0000" + "00011" + "00001"; 			//MATH DestOnly 5, R3: R3 = 5;
		
		setup(branch, instruction);
		Assert.assertEquals(cpu.getRegisters()[3].getSigned(), 0);
		
	}
	
	@Test 
	public void testDecrement() {
		// 12-- = 11
		Word word = new Word();
		word.set(12);
		word.decrement();
		
		Assert.assertEquals(word.getSigned(),11);
		
		//121541-- = 121540
		word.set(121541);
		word.decrement();
		
		Assert.assertEquals(word.getSigned(),121540);
		
		//0-- = -1
		word.set(0);
		word.decrement();
				
		Assert.assertEquals(word.getSigned(), -1);
		
		//-36-- = -37
		word.set(-36);
		word.decrement();
						
		Assert.assertEquals(word.getSigned(), -37);
	}
	
}
