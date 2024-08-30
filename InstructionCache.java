public class InstructionCache {
	private Word startingAddress;
	private Word[] cache;
	private Clock clock;
	private L2Cache l2Cache;

	InstructionCache(L2Cache L2, Clock clock) {
		l2Cache = L2;
		startingAddress = new Word();
		cache = new Word[8];
		
		for(int i = 0; i < 8; i++) 
			cache[i] = new Word();	
		
		this.clock = clock;
	}
	
	public Word read(Word address) {
		//index in range of cache
		int index = address.getSigned() - startingAddress.getSigned();
		
		if(address.getSigned() == 0) {	
			clock.currentCycle += 50;
			l2Cache.fillBlock(startingAddress);
			l2Cache.fetchBlock(this, startingAddress);
		}
		
		if(index < 8 && index >= 0) {
			clock.currentCycle += 10;
			return cache[index];
		}
		else {
			clock.currentCycle += 50;
			return l2Cache.fetchBlock(this, address);
		}
	}
	
	public void setStartingAddress(Word address) {
		startingAddress.copy(address);
	}
	
	public void setCache(Word[] c) {
		for(int i=0; i <cache.length; i++) {
			cache[i].copy(c[i]);
		}
	}
	
//	public void fillCache(Word address) {
//		Word cacheAddress = new Word();
//		cacheAddress.copy(address);							//Address of sequential data in cache
//		startingAddress.copy(address);						//Starting Address
//		
//		for(int i = 0; i < 8; i++) {
//			cache[i].copy(MainMemory.read(cacheAddress));	//Store next 8 data points in cache
//			cacheAddress.incriment();
//		}
//		clock.currentCycle += 350;	
//	}
}
