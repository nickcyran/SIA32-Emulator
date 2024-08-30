public class L2Cache {
	private int nextCache;

	private Clock clock;

	private Word[] cacheStart;
	private Word[][] caches;

	L2Cache(Clock clock) {
		this.clock = clock;

		caches = new Word[4][8];
		cacheStart = new Word[4];

		for (int i = 0; i < 4; i++) {
			cacheStart[i] = new Word();

			for (int j = 0; j < 8; j++) {
				caches[i][j] = new Word();
			}
		}
	}

	public Word read(Word address) {
		clock.currentCycle += 50;

		int findAddress = address.getSigned();
		for (int i = 0; i < cacheStart.length; i++) {
			int index = findAddress - cacheStart[i].getSigned();

			if (index < 8 && index >= 0) {
				return caches[i][index];
			}
		}
		return fillBlock(address)[0];
	}
	
	public void write(Word address, Word value) {
		clock.currentCycle += 50;
		
		int findAddress = address.getSigned();					
		
		for (int i = 0; i < cacheStart.length; i++) {				//iterate through caches
			int index = findAddress - cacheStart[i].getSigned();	//check if address exists in cache
			
			if (index < 8 && index >= 0) {							//if it exists ->
				caches[i][index].copy(value);						//set value into cache @ address
				MainMemory.write(address, caches[i][index]);		//put the cache data into Memory
				return;
			}
		}
		fillBlock(address)[0].copy(value);							//NOT FOUND! fill cache
		MainMemory.write(address, caches[(nextCache - 1) % 4][0]);	//set memory
	}

	public Word fetchBlock(InstructionCache iCache, Word address) {	//used to set InstructionCache
		clock.currentCycle += 20;
		int findAddress = address.getSigned();
		
		for (int i = 0; i < cacheStart.length; i++) {				//check all caches to see if address exists
			int index = findAddress - cacheStart[i].getSigned();

			if (index < 8 && index >= 0) {
				iCache.setCache(caches[i]);							//FOUND! set InstructionCache to reflect
				iCache.setStartingAddress(cacheStart[i]);
				return caches[i][index];
			}
		}
		iCache.setCache(fillBlock(address));						//NOT FOUND. fill cache then set InstructionCache
		iCache.setStartingAddress(address);
		
		return caches[(nextCache - 1) % 4][0];						//address will always be the first in newly filled
	}

	
	public Word[] fillBlock(Word address) {
		var cache = caches[nextCache % 4];				//Get index of next cache in cycle

		Word cacheAddress = new Word();					//Increment address to fill in cache data
		cacheAddress.copy(address);
		cacheStart[nextCache % 4].copy(address);		//Set new start address

		for (int i = 0; i < 8; i++) {
			if (cacheAddress.getSigned() > 1023) {		
				return cache;
			}
														//Fill cache with data at address
			cache[i].copy(MainMemory.read(cacheAddress));
			cacheAddress.incriment();
		}

		clock.currentCycle += 350;
		nextCache++;
		return cache;
	}
}
