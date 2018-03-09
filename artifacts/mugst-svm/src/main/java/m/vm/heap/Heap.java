package m.vm.heap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;

import m.state.StateElement;
import m.var.ArrayReference;
import m.var.CharConstant;
import m.var.ObjectReference;

public class Heap extends StateElement implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * All symbolic objects in this heap.
	 */
	protected Set<SymbolicObject> heapObjects;
	
	/**
	 * All symbolic arrays in this heap.
	 */
	protected Set<SymbolicArray> heapArrays;
	
	protected Map<String, SymbolicObject> stringConstantObjects;
	
	public Heap() {
		this.heapObjects = new HashSet<>();
		this.heapArrays  = new HashSet<>();
		this.stringConstantObjects = new HashMap<>();
	}
	
	public SymbolicObject newObject(JavaClass javaClass) {
		SymbolicObject obj = new SymbolicObject(javaClass);
		this.heapObjects.add(obj);
		return obj;
	}
	
	public SymbolicArray newArray(String type, int dimensions) {
		SymbolicArray arr = new SymbolicArray(type, dimensions);
		this.heapArrays.add(arr);
		return arr;
	}
	
	public Set<SymbolicObject> getHeapObjects() {
		return this.heapObjects;
	}

	public SymbolicObject getStringConstantObject(String stringValue) {
		SymbolicObject stringConstantObj = stringConstantObjects.get(stringValue);
		if(stringConstantObj == null) {
			stringConstantObj = generateNewStringConstant(stringValue);
			stringConstantObjects.put(stringValue, stringConstantObj);
		}
		return stringConstantObj;
	}

	private SymbolicObject generateNewStringConstant(String stringValue) {
		JavaClass stringClass;
		try {
			stringClass = Repository.lookupClass(String.class.getName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Should not happen, we should find the String.class in our class path...");
		}
		SymbolicObject stringObj = new SymbolicObject(stringClass);
		
		// in the string class, the field value contains a char array with dimensions 1 of the 'string representation'
		SymbolicArray valueArray = newArray("char", 1);
		for(int i=0; i<stringValue.length(); i++) {
			CharConstant charConst = new CharConstant(stringValue.charAt(i));
			valueArray.setElement(i, charConst);
		}
		stringObj.setValue("value", new ArrayReference("STRING_CONST_("+stringValue+").value", valueArray));
		
		return stringObj;
	}

	public SymbolicObject getClassConstantObject(String className) {
		JavaClass clazz;
		try {
			clazz = Repository.lookupClass(Class.class.getName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Should not happen, we should find the Class.class in our class path...");
		}
		SymbolicObject clazzObj = new SymbolicObject(clazz);
		
		SymbolicObject clazzName = generateNewStringConstant(className);
		
		clazzObj.setValue("name", new ObjectReference("CLAZZ_CONST_NAME", clazzName));
		
		return clazzObj;
	}
	
	@Override
	public String toString() {
		return "objects="+heapObjects+", arrays="+heapArrays;
	}
}
