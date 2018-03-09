package m.cstr;

import java.util.HashMap;
import java.util.Map;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import m.cstr.ex.ConstraintException;
import m.cstr.meta.BoolOp;
import m.var.CharVariable;
import m.var.IntVariable;


public class ChocoVariableManager {

	protected Model model;
	protected Map<IntVariable, IntVar> m2choco;
	
	protected final int INT_MIN_VALUE = -1000;
	protected final int INT_MAX_VALUE =  1000;
	
	protected final int CHAR_MIN_VALUE = 33;
	protected final int CHAR_MAX_VALUE = 127;

	public ChocoVariableManager(Model model) {
		this.model = model;
		this.m2choco = new HashMap<>();
	}

	public IntVar getVariable(IntVariable mugstVar) {
		IntVar var = m2choco.get(mugstVar);
		if(var == null) {
			if(mugstVar instanceof CharVariable) {
				var = createCharVariable(mugstVar.getName());
			} else {
				var = createIntVariable(mugstVar.getName());
			}
			this.m2choco.put(mugstVar, var);
		}
		if(mugstVar.isNegated()) {
			throw new RuntimeException("must negate the int variable");
//			return var.neg();
		}
		return var;
	}
	
	public Map<IntVariable, IntVar> getM2ChocoMap() {
		return this.m2choco;
	}

	IntVar createIntVariable(String name) {
		return this.model.intVar(name, INT_MIN_VALUE, INT_MAX_VALUE);
	}
	
	IntVar createCharVariable(String name) {
		return this.model.intVar(name, CHAR_MIN_VALUE, CHAR_MAX_VALUE);
	}

	public String getOperand(BoolOp op) {
		switch(op) {
			case EQ : return "=";
			case NEQ : return "!=";
			case LT : return "<";
			case GT : return ">";
			case LE : return "<=";
			case GE : return ">=";
		}
		throw new ConstraintException("What is this boolean operand? " + op);
	}

	public String getMirrorOperand(BoolOp op) {
		switch(op) {
			case EQ : return "=";
			case NEQ : return "!=";
			case LT : return ">";
			case GT : return "<";
			case LE : return ">=";
			case GE : return "<=";
		}
		throw new ConstraintException("What is this boolean operand? " + op);
	}
	
}
