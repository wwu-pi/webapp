package m.var;

import m.vm.heap.SymbolicObject;

public class ObjectReference extends Variable {

	private static final long serialVersionUID = 1L;

	protected SymbolicObject referencedObject;
	
	public ObjectReference(String name, SymbolicObject referencedObject) {
		super(name);
		this.referencedObject = referencedObject;
	}
	
	/**
	 * Get the value of the field with the given field-name.
	 * @param fieldName the name of the field to get the value from
	 * @return the value of the field, or <code>null</code> if field value has not been initialized / set yet...
	 */
	public Variable getValue(String fieldName) {
		return this.referencedObject.getValue(fieldName);
	}
	
	/**
	 * Set the given value to the field with the given name.
	 * @param fieldName
	 * @param value
	 */
	public void setValue(String fieldName, Variable value) {
		this.referencedObject.setValue(fieldName, value);
	}
	
	public boolean hasValues() {
		return this.referencedObject.hasValues();
	}

	public String getClassName() {
		return this.referencedObject.getJavaClassName();
	}

	public SymbolicObject getReferencedObject() {
		return this.referencedObject;
	}
}
