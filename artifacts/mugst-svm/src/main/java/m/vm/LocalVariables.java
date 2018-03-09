package m.vm;

import java.io.Serializable;
import java.util.Arrays;

import m.state.StateElement;
import m.var.Variable;

public class LocalVariables extends StateElement implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Variable[] variables;
	
	public LocalVariables(int maxSize) {
		this.variables = new Variable[maxSize];
	}
	
	public Variable get(int index) {
		return this.variables[index];
	}
	
	public void set(int index, Variable value) {
		this.variables[index] = value;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(variables);
	}
}
