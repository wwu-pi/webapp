package m.cstr;

import java.util.Map.Entry;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

import m.var.IntVariable;

public class ConstraintManager {

	protected final Model model;
	protected final ChocoVariableManager varManager;
	protected final ChocoConstraintTransformer transformer;
	protected final ConstraintStack constraintStack;
	
	public ConstraintManager(ConstraintStack constraints) {
		this.model = new Model();
		this.varManager = new ChocoVariableManager(this.model);
		this.transformer = new ChocoConstraintTransformer(this.varManager);
		
		this.constraintStack = constraints;
		for(Constraint c : this.constraintStack.constraints) {
			addConstraint(c);
		}
	}

	/**
	 * Get the current constraint store with all constraints.
	 * @return
	 */
	public ConstraintStack getConstraintStack() {
		return constraintStack;
	}

	/**
	 * Add a constraint to the constraint store.
	 * @param constraint
	 */
	public void addConstraint(Constraint constraint) {
		this.constraintStack.addConstraint(constraint);
		this.model.post(this.transformer.getChocoConstraint(constraint));
	}

	/**
	 * Check if the current constraint stack is satisfiable when adding the given additional constraint.
	 * <b>This method does not _add_ the constraint to this constraint stack, just check its satisfiability!</b>
	 * @param constraint the additional constraint to check if constraint stack is still satisfiable
	 * @return true, if constraint stack is still satisfiable with given constraint
	 */
	public boolean isSatisfiable(Constraint constraint) {
		org.chocosolver.solver.constraints.Constraint chocoConstraint = this.transformer.getChocoConstraint(constraint);
		this.model.post(chocoConstraint);
		
		boolean solutionFound = false;
		if(this.model.getSolver().findSolution() != null) {
			solutionFound = true;
		}
		
		this.model.getSolver().reset();
		this.model.unpost(chocoConstraint);
		
		return solutionFound;
	}
	
	public ConstraintSolution solve() {
		ConstraintSolution sol = new ConstraintSolution();
		Solution chocoSolution = this.model.getSolver().findSolution();
		for(Entry<IntVariable, IntVar> entry : varManager.getM2ChocoMap().entrySet()) {
			int val = chocoSolution.getIntVal(entry.getValue());
			sol.setSolution(entry.getKey(), val);
		}
		return sol;
	}
	
}