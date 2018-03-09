package m.state;

import java.io.Serializable;

public abstract class StateElement implements Serializable {

	private static final long serialVersionUID = 1L;

	protected final String UUID;
	protected final int HASH_CODE;
	
	public StateElement() {
		this.UUID = java.util.UUID.randomUUID().toString();
		this.HASH_CODE = this.UUID.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof StateElement) {
			StateElement s = (StateElement)obj;
			return(s.UUID.equals(this.UUID));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.HASH_CODE;
	}
}
