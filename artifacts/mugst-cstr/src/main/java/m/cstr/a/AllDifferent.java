package m.cstr.a;

import java.util.Set;

import m.cstr.Constraint;
import m.var.IntVariable;

public class AllDifferent extends Constraint {

	private static final long serialVersionUID = 1L;

	protected final Set<IntVariable> values;
	
	public AllDifferent(Set<IntVariable> values) {
		this.values = values;
	}
		
	public Set<IntVariable> getValues() {
		return values;
	}
	@Override
	public Constraint negate() {
		throw new RuntimeException("not implemented yet");
	}
	

	
}
