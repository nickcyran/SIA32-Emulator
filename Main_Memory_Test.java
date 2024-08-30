import org.junit.Assert;
import org.junit.Test;

public class Main_Memory_Test {
	private String[] strings = {"00000000000000011001001011011100",		//103132
								"11111101100010101011010101110010",		//-41241230
								"00000000001011111101011100000010"		//3135234
								};
	
	@Test
	public void initializedDRAMTest() {
		var x = MainMemory.DRAM[10];		// an uninitialized array will throw an error
	}
	
	@Test
	public void readTest() {
		MainMemory.load(strings);
		
		int index = 2;
		
		Word address = new Word();
		address.set(index);
		
		Assert.assertEquals(MainMemory.read(address).getSigned(), 3135234);	// value at index 2 (from read) expected to be same as data
		Assert.assertEquals(MainMemory.DRAM[index].getSigned(), 3135234);	// value at index 2 (from DRAM) expected to be same as data
	}
	
	@Test
	public void writeTest() {
		int data = 39411;
		int index = 10;
		
		Word address = new Word();
		address.set(index);
		
		Word value = new Word();
		value.set(data);
		
		MainMemory.write(address, value);
		
		Assert.assertEquals(MainMemory.DRAM[index].getSigned(), data);		// Compare the data currently at index 10 to the expected data
	}
	
	@Test
	public void loadTest() {
		MainMemory.load(strings);
		
		Assert.assertEquals(MainMemory.DRAM[0].getSigned(), 103132);		//Confirm memory was loaded
		Assert.assertEquals(MainMemory.DRAM[1].getSigned(), -41241230);
		Assert.assertEquals(MainMemory.DRAM[2].getSigned(), 3135234);
	}
	
	@Test
	public void incrimentTest() {
		Word word = new Word();	// 0
		word.incriment();
		
		Assert.assertEquals(word.getSigned(), 1);
		
		word.set(19312);
		word.incriment();
		Assert.assertEquals(word.getSigned(), 19313);
		
		word.set(-132319312);
		word.incriment();
		Assert.assertEquals(word.getSigned(), -132319311);
	}
}