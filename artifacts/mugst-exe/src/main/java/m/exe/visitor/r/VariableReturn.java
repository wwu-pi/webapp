package m.exe.visitor.r;

import m.var.Variable;

public class VariableReturn extends ReturnValue {

	protected final Variable var;
	
	public VariableReturn(Variable var) {
		this.var = var;
	}

	@Override
	public Variable getReturnValue() {
		return this.var;
	}
}
