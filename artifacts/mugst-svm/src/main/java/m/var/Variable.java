package m.var;

import java.io.Serializable;

import m.state.StateElement;

public abstract class Variable extends StateElement implements Serializable {

	private static final long serialVersionUID = 1L;

	protected final String name;
	
	public Variable(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
