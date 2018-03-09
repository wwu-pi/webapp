package m.tc.gen.meta;

import org.apache.bcel.generic.MethodGen;

import m.cstr.ConstraintSolution;
import m.var.Variable;

public abstract class TestCaseGenerator {
	
	protected final Variable expectedReturnValue;
	protected final Variable[] methodArguments;
	protected final MethodGen mut; // method under test
	protected final ConstraintSolution solution;

	public TestCaseGenerator(MethodGen mut, Variable[] methodArguments, Variable expectedReturnValue, ConstraintSolution solution) {
		this.mut = mut;
		this.methodArguments = methodArguments;
		this.expectedReturnValue = expectedReturnValue;
		this.solution = solution;
	}
	
	public Variable getExpectedReturnValue() {
		return this.expectedReturnValue;
	}

	public abstract void genearte();
	
}
