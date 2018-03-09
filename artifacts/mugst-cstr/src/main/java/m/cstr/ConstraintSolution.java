package m.cstr;

import java.util.HashMap;
import java.util.Map;

import m.var.IntVariable;
import m.var.Variable;

public class ConstraintSolution {

	protected Map<IntVariable, Integer> intVars;
	
	public ConstraintSolution() {
		this.intVars = new HashMap<>();
	}
	
	public void setSolution(IntVariable variable, Integer value) {
		this.intVars.put(variable, value);
	}
	
	public Map<IntVariable, Integer> getIntVars() {
		return this.intVars;
	}

	public Integer getIntValue(IntVariable var) {
		for(IntVariable key : intVars.keySet()) {
			if(key.equals(var)) {
				return intVars.get(key);
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return intVars.toString();
	}
}
