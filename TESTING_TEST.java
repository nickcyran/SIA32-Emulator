import static org.junit.Assert.assertEquals;

import org.junit.Test;
import java.io.IOException;
import org.junit.Test;
import assembler.Assembler;

public class TESTING_TEST {
	
	public void printData(Processor cpu) {
		System.out.println("-----------------------------------------------------------");
		MainMemory.printDRAM();
		System.out.println("-----------------------------------------------------------");
		int i = 0;
		for(var r: cpu.getRegisters()) {
			if(r.getUnsigned() != 0) {
				System.out.println("[R" + i + "]: " + r.getUnsigned());
			}
			i++;
		}
	}

	@Test
	public void test_forward_sum_array() throws IOException {
		
//		Assembler.assemble("sum_array.asm", "sum_array.txt");
		MainMemory.load("sum_array.txt");
		
		Processor cpu = new Processor();
		cpu.run();
//		printData(cpu);
		assertEquals(cpu.getRegisters()[6].getSigned(), 31);
		System.out.println("----------------------------------------------------[forward]");
	}
	
	@Test
	public void test_sum_linked_list() throws IOException {
//		Assembler.assemble("sum_linked_list.asm", "sum_linked_list.txt");
		MainMemory.load("sum_linked_list.txt");
		
		Processor cpu = new Processor();
		cpu.run();
		
		
		
		assertEquals(cpu.getRegisters()[2].getSigned(), 20);
		System.out.println("-------------------------------------------------[linkedList]");
	}
	
	@Test
	public void test_reverse_sum_array() throws IOException {
//		Assembler.assemble("rev_sum_array.asm", "rev_sum_array.txt");
		MainMemory.load("rev_sum_array.txt");
		
		Processor cpu = new Processor();
		cpu.run();
		
		assertEquals(cpu.getRegisters()[6].getSigned(), 31);
		System.out.println("----------------------------------------------------[reverse]");
	}
}
