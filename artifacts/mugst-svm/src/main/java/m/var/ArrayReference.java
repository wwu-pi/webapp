package m.var;

import m.vm.heap.SymbolicArray;

public class ArrayReference extends Variable {

	private static final long serialVersionUID = 1L;
	
	protected SymbolicArray referencedArray;
	
	public ArrayReference(String name, SymbolicArray referencedArray) {
		super(name);
		this.referencedArray = referencedArray;
	}
	
	
	public void setElement(int i, Variable value) {
		this.referencedArray.setElement(i, value);
	}

	public Variable getElement(int index) {
		return this.referencedArray.getElement(index);
	}


	public IntVariable getLength() {
		return this.referencedArray.getLength();
	}

	public SymbolicArray getReferencedArray() {
		return this.referencedArray;
	}
}
