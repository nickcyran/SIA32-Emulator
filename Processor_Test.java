import org.junit.Assert;
import org.junit.Test;

public class Processor_Test {
	private Processor cpu;
	private String halt = "0".repeat(32);
	
	private void setup(String...s) {
		MainMemory.load(s);
		cpu = new Processor();
		cpu.run();
	}
	
	@Test
	public void testStoreInR0() {
		setup("000000000000001101" + "0000" + "00000" + "00001"); 				// MATH DestOnly 13, R0
		Assert.assertEquals(cpu.getRegisters()[0].getSigned(), 0);
	}
	
	@Test
	public void testHalt() {
		String one = "000000000000000101" + "0000" + "00001" + "00001"; 			// MATH DestOnly 5, R1: R1 = 5
		String two = "00000000" + "00001" + "00001" + "1110" + "00010" + "00011";   // MATH ADD R1 R1 R2: R2 = 10
		String postHalt = "0000000000000" + "00001" + "1110" + "00010" + "00010";	// MATH ADD R1 R2: HALTED DOES NOT EQUAL 15
		
		setup(one, two, halt, postHalt);
		Assert.assertEquals(cpu.getRegisters()[2].getSigned(), 10);	// Halted before 5 added once more => R2 = 10
	}
	
	@Test
	public void testProcessing() {
		String one = "000000000000000101" + "0000" + "00001" + "00001"; 			// MATH DestOnly 5, R1
		String two = "00000000" + "00001" + "00001" + "1110" + "00010" + "00011";   // MATH ADD R1 R1 R2
		String three = "0000000000000" + "00010" + "1110" + "00010" + "00010";		// MATH ADD R2 R2
		String four = "00000000" + "00010" + "00001" + "1110" + "00011" + "00011";	// MATH ADD R2 R1 R3
		
		setup(one, two, three, four, halt);		
		Assert.assertEquals(cpu.getRegisters()[3].getSigned(), 25);
	}
	
	@Test
	public void testMath() {
		// 5 * 8 = 40
		String x = "000000000000000101" + "0000" + "00001" + "00001"; 			// MATH DestOnly 5, R1 [R1 = 5]
		String y = "000000000000001000" + "0000" + "00010" + "00001"; 			// MATH DestOnly 8, R2 [R2 = 8]
		String z = "00000000" + "00001" + "00010" + "0111" + "00010" + "00011";	// MATH MULT R1 R2 R2  [R2 = 40]
		
		setup(x,y,z, halt);
		Assert.assertEquals(cpu.getRegisters()[2].getSigned(), 40);
		
		// 40 - 5 = 35
		x = "000000000000000101" + "0000" + "00001" + "00001"; 					// MATH DestOnly 5, R1 [R1 = 5]
		y = "000000000000101000" + "0000" + "00010" + "00001"; 					// MATH DestOnly 40, R2 [R2 = 40]
		z = "0000000000000" + "00001" + "1111" + "00010" + "00010";  			// MATH SUB R1 R2 [R2 = 35]
		
		setup(x,y,z, halt);
		Assert.assertEquals(cpu.getRegisters()[2].getSigned(), 35);
		
		// 16 >> 2 = 4
		x = "000000000000000010" + "0000" + "00001" + "00001"; 					// MATH DestOnly 2, R21 [R1 = 2]
		y = "000000000000010000" + "0000" + "00010" + "00001"; 					// MATH DestOnly 16, R2 [R2 = 16]
		z = "0000000000000" + "00001" + "1101" + "00010" + "00010";  			// MATH RSHIFT R1 R2 [R2 = 4]
		
		setup(x,y,z, halt);
		Assert.assertEquals(cpu.getRegisters()[2].getSigned(), 4);
		
		// 5 - 40 = -35
		x = "000000000000101000" + "0000" + "00001" + "00001"; 					// MATH DestOnly 40, R1 [R1 = 40]
		y = "000000000000000101" + "0000" + "00010" + "00001"; 					// MATH DestOnly 5, R2 [R2 = 5]
		z = "0000000000000" + "00001" + "1111" + "00010" + "00010";  			// MATH SUB R1 R2 [R2 = -35]
				
		setup(x,y,z, halt);
		Assert.assertEquals(cpu.getRegisters()[2].getSigned(), -35);
	}
	
	public void printRegs() {
		int i = 0;
		for(var w : cpu.getRegisters()) {
			System.err.println("[R" + i + "]: " + w.getSigned());
			i++;
		}
	}
}
