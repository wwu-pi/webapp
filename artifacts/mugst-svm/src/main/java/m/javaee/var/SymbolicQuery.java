package m.javaee.var;

import m.var.ObjectReference;
import m.var.Variable;

public class SymbolicQuery extends Variable {

	private static final long serialVersionUID = 1L;
	
	protected ObjectReference qlString;
	protected ObjectReference resultClass;
	
	public SymbolicQuery(String name, ObjectReference qlString, ObjectReference resultClass) {
		super(name);
		this.qlString = qlString;
		this.resultClass = resultClass;
	}
	
	public ObjectReference getResultClass() {
		return this.resultClass;
	}
}