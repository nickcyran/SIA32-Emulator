import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainMemory {
	private static final int SIZE = 1024;
	protected static Word[] DRAM = initDRAM(SIZE); 
	
	private MainMemory() {}
	
 	private static Word[] initDRAM(int size) {
		Word[] array = new Word[size];
		
		for(int i = 0; i < size; i++) {
			array[i] = new Word();
		}
		return array;
	}
	
	public static Word read(Word address) {
		Word readWord = new Word();
		readWord.copy(DRAM[(int)address.getUnsigned()]);	// set readWord as a copy of the word at the address
		
		return readWord;
	}
	
	public static void write(Word address, Word value) {
		DRAM[(int)address.getUnsigned()].copy(value);		// set the Word in DRAM @ address to a copy of value
	}
	
	public static void load(String[] data){
		for(Word word : DRAM) {
			word.copy(new Word());
		}
		
		for(int i = 0; i < SIZE; i++) {						// iterate through each Word in DRAM
			Word loadWord = DRAM[i];
			String binaryData;
			
			if(i >= data.length) {
				break;
			}
			else {
				binaryData = data[i];
			}
			
			for (int j = 0; j < binaryData.length(); j++) {	// iterate through each char in data String
				char bit = binaryData.charAt(j);
				
				//NOTE: Words read from right to left but stored right to left. Data in the txt file must be stored where the first char is index 0
				switch (bit) {
					case '1' -> loadWord.setBit(31 - j, new Bit(true));	
					case '0' -> loadWord.setBit(31 - j, new Bit(false));
					default -> throw new IllegalArgumentException("Faulty data loaded");
				}
			}
		}
	}
	
	public static void load(String file) throws IOException {
		String[] data = fileToData(file);
		for(Word word : DRAM) {
			word.copy(new Word());
		}
		
		for(int i = 0; i < SIZE; i++) {						// iterate through each Word in DRAM
			Word loadWord = DRAM[i];
			String binaryData;
			
			if(i >= data.length) {
				break;
			}
			else {
				binaryData = data[i];
			}
			
			for (int j = 0; j < binaryData.length(); j++) {	// iterate through each char in data String
				char bit = binaryData.charAt(j);
				
				//NOTE: Words read from right to left but stored right to left. Data in the txt file must be stored where the first char is index 0
				switch (bit) {
					case '1' -> loadWord.setBit(31 - j, new Bit(true));	
					case '0' -> loadWord.setBit(31 - j, new Bit(false));
					default -> throw new IllegalArgumentException("Faulty data loaded");
				}
			}
		}
	}
	
	private static String[] fileToData(String file) throws IOException {
		var list = Files.readAllLines(Paths.get(file));
		return list.toArray(new String[0]);
	}

	public static void printDRAM() {
		int i = 0;
		for(Word w : DRAM) {
			if(w.getUnsigned() != 0) {
				System.out.println("instruction [" + i + "] - " + w.asBinaryString());
			}
			i++;
		}
	}
}
