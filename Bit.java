
public class Bit {
	private Boolean value;
	
	Bit(Boolean val) {
		value = val;
	}
	
	public void set(Boolean val) {
		value = val;
	}

	public void set() {
		value = true;
	}

	public void clear() {
		value = false;
	}

	public void toggle() {
		value = !value;
	}

	public Boolean getValue() {
		return value;
	}
	
	public Bit and(Bit other) {
		// one false results in a false case
		return new Bit(value ? (other.getValue() ? true : false) : false);
	}
	
	public Bit or(Bit other) {
		// one true results in a true case
		return new Bit(value ? true : (other.getValue() ? true : false));
	}
	
	public Bit xor(Bit other) {
		if(value) {
			if(other.getValue()) {		// Case: t ^ t = f
				return new Bit(false);
			}
			return new Bit(true);		// Case: t ^ f = t
		}
		
		// Case: first is false -> just preform an or
		return new Bit(this.or(other).getValue());
	}
	
	public Bit not() {
		return new Bit(!value);
	}
	
	@Override
	public String toString() {
		return value ? "t" : "f";
	}
}
