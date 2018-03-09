package m.vm.heap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.classfile.JavaClass;

import m.state.StateElement;
import m.var.Variable;

public class SymbolicObject extends StateElement implements Serializable {

	private static final long serialVersionUID = 1L;
	protected final String name;
	
	protected transient JavaClass javaClass;
	protected String javaClassName;
	protected Map<String, Variable> values;


	SymbolicObject(JavaClass javaClass) {
		this.javaClass = javaClass;
		this.javaClassName = javaClass.getClassName();
		this.values = new HashMap<>();
		this.name = "symObject"+this.hashCode();
	}
	
	public void setValue(String fieldName, Variable value) {
		this.values.put(fieldName, value);
	}
	
	public Variable getValue(String fieldName) {
		return this.values.get(fieldName);
	}
	
	public Map<String, Variable> getValues() {
		return this.values;
	}
	
	public boolean hasValues() {
		return this.values.isEmpty();
	}
	
	public String getJavaClassName() {
		return this.javaClassName;
	}
	
	@Override
	public String toString() {
		return this.name + ", javaName="+this.javaClassName + ", values="+this.values;
	}
	
	public CopiedSymbolicObject copy() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bos);
		out.writeObject(this);
		return new CopiedSymbolicObject(bos);
	}
}
