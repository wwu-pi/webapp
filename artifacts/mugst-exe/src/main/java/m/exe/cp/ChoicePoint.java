package m.exe.cp;

import java.util.Set;

import m.cstr.Constraint;
import m.state.CopiedSymbolicState;
import m.vm.Method;
import m.vm.PC;

public class ChoicePoint {

	protected final CopiedSymbolicState state;
	protected final Method method;
	protected final PC continueWithPC;
	protected final Set<Constraint> additionalConstraints;
	
	/**
	 * Generate a new choice point.
	 * @param state the copied state
	 * @param continueWithPC the program counter on which the symbolic execution is to be continued
	 * @param additionalConstraints a set of additional constraints for continuing symbolic execution
	 */
	public ChoicePoint(CopiedSymbolicState state, Method method, PC continueWithPC, Set<Constraint> additionalConstraints) {
		this.state = state;
		this.method = method;
		this.continueWithPC = continueWithPC;
		this.additionalConstraints = additionalConstraints;
	}

	public CopiedSymbolicState getState() {
		return state;
	}
	
	public Method getMethod() {
		return this.method;
	}

	public PC getContinueWithPC() {
		return continueWithPC;
	}

	public Set<Constraint> getAdditionalConstraints() {
		return additionalConstraints;
	}	
	
	@Override
	public String toString() {
		return "(method=["+method+"] @PC="+continueWithPC + ") with additional constraints: " + this.additionalConstraints;
	}
}
