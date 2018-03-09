package m.cstr;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import m.state.StateElement;

public class ConstraintStack extends StateElement implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Set<Constraint> constraints;
	
	public ConstraintStack() {
		this.constraints = new HashSet<>();
	}
	
	public int size() {
		return this.constraints.size();
	}
	
	public void addConstraint(Constraint constraint) {
		this.constraints.add(constraint);
	}

	public void addConstraint(Set<Constraint> constraintSet) {
		this.constraints.addAll(constraintSet);
	}
	
	public Set<Constraint> getConstraints() {
		return this.constraints;
	}
	
	@Override
	public String toString() {
		return this.constraints.toString();
	}
}
