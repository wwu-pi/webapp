package m.vm.heap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import m.state.StateElement;
import m.var.IntVariable;
import m.var.Variable;

public class SymbolicArray extends StateElement implements Serializable {

	private static final long serialVersionUID = 1L;

	protected final String name;
	protected final String type;
	protected final int dimensions;
	
	protected IntVariable length;
	private Map<Integer, Variable> valueMap;
	
	SymbolicArray(String type, int dimensions) {
		this.type = type;
		this.dimensions = dimensions;
		this.name = "symArray"+this.hashCode();
		this.length = new IntVariable(this.name + ".length");
		this.valueMap = new HashMap<>();
	}
	
	public void setElement(int i, Variable value) {
		this.valueMap.put(i, value);
	}

	public Variable getElement(int index) {
		return this.valueMap.get(index);
	}

	public IntVariable getLength() {
		return this.length;
	}
	
	@Override
	public String toString() {
		return this.name + ", values=" + this.valueMap;
	}
}
