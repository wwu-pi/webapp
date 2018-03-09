package m.tc;

import org.apache.bcel.generic.MethodGen;

import m.cstr.ConstraintSolution;
import m.state.CopiedSymbolicState;
import m.var.Variable;

public class TestCase {

	protected final Variable expectedReturnValue;
	protected final Variable[] methodArguments;
	protected final MethodGen mut; // method under test
	protected final CopiedSymbolicState copiedState;
	protected final ConstraintSolution solution;
	
	public TestCase(MethodGen mut, Variable[] methodArguments, Variable expectedReturnValue, CopiedSymbolicState copiedState, ConstraintSolution solution) {
		this.mut = mut;
		this.methodArguments = methodArguments;
		this.expectedReturnValue = expectedReturnValue;
		this.copiedState = copiedState;
		this.solution = solution;
	}
	
	public Variable getExpectedReturnValue() {
		return this.expectedReturnValue;
	}
	
	public CopiedSymbolicState getCopiedState() {
		return this.copiedState;
	}
	
	public ConstraintSolution getSolution() {
		return this.solution;
	}
}
