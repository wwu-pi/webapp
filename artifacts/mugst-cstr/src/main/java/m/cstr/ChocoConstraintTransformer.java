package m.cstr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

import m.cstr.a.AllDifferent;
import m.cstr.ex.ConstraintException;
import m.cstr.p.IntXopY;
import m.var.IntConstant;
import m.var.IntVariable;
import m.var.SumVariable;

public class ChocoConstraintTransformer {

	protected final ChocoVariableManager varManager;
	
	public ChocoConstraintTransformer(ChocoVariableManager varManager) {
		this.varManager = varManager;
	}
	
	public Constraint getChocoConstraint(m.cstr.Constraint mConstraint) {
		if(mConstraint instanceof IntXopY) {
			IntXopY cstr = (IntXopY)mConstraint;
			
			if(cstr.getVar1() instanceof IntConstant && cstr.getVar1() instanceof IntConstant) {
				throw new ConstraintException("Cannot compare two constants here, do it in symoblic execution before!");
			}
			
			if(cstr.getVar1() instanceof SumVariable && cstr.getVar2() instanceof SumVariable) {
				return handleSum(
						(SumVariable)cstr.getVar1(), 
						(SumVariable)cstr.getVar2(), 
						this.varManager.getOperand(cstr.getOp()));
			}
			
			if(cstr.getVar1() instanceof SumVariable) {
				return handleSum(
						(SumVariable)cstr.getVar1(), 
						cstr.getVar2(), 
						this.varManager.getOperand(cstr.getOp()));
			}
			
			if(cstr.getVar2() instanceof SumVariable) {
				return handleSum(
						(SumVariable)cstr.getVar2(), 
						cstr.getVar1(), 
						this.varManager.getOperand(cstr.getOp()));
			}
				
			
			if(cstr.getVar1() instanceof IntConstant) {
				IntConstant intConst = (IntConstant)cstr.getVar1();
				return varManager.model.arithm(
						this.varManager.getVariable(cstr.getVar2()),
						this.varManager.getMirrorOperand(cstr.getOp()),
						intConst.getIntValue());
			} else if(cstr.getVar2() instanceof IntConstant) {
				IntConstant intConst = (IntConstant)cstr.getVar2();
				return varManager.model.arithm(
						this.varManager.getVariable(cstr.getVar1()),
						this.varManager.getOperand(cstr.getOp()),
						intConst.getIntValue());
			} else {
				return varManager.model.arithm(
						this.varManager.getVariable(cstr.getVar1()),
						this.varManager.getOperand(cstr.getOp()),
						this.varManager.getVariable(cstr.getVar2()));
			}
		} else if(mConstraint instanceof AllDifferent) {
			AllDifferent allDiff = (AllDifferent)mConstraint;
			
			IntVariable[] mVars = allDiff.getValues().toArray(new IntVariable[allDiff.getValues().size()]);
			IntVar[] chocoVars = new IntVar[mVars.length];
			for(int i=0; i<mVars.length; i++) {
				chocoVars[i] = this.varManager.getVariable(mVars[i]);
			}
			
			return varManager.model.allDifferent(chocoVars);
		}
		
		throw new ConstraintException("Constraint " + mConstraint + " not supported yet in Choco!");
	}
	
	private Constraint handleSum(SumVariable mugstSum1, SumVariable mugstSum2, String operator) {
		IntVar sumEqualsVar = varManager.createIntVariable(mugstSum1+"="+mugstSum2);
		Constraint c1 = varManager.model.sum(getSumVariables(mugstSum1), operator, sumEqualsVar);
		Constraint c2 = varManager.model.sum(getSumVariables(mugstSum2), operator, sumEqualsVar);
		return varManager.model.and(c1, c2);
	}
	
	private Constraint handleSum(SumVariable mugstSum, IntVariable mugstSumValue, String operator) {
		IntVar[] sumVariables = getSumVariables(mugstSum);
		if(mugstSumValue instanceof IntConstant) {
			int sum = ((IntConstant)mugstSumValue).getIntValue();
			return varManager.model.sum(sumVariables, operator, sum);
		} else {
			IntVar sumVar = this.varManager.getVariable(mugstSumValue);
			return varManager.model.sum(sumVariables, operator, sumVar);
		}
	}

	private IntVar[] getSumVariables(IntVariable sumVar) {
		List<IntVar> intVars = new ArrayList<>();
		fillIntVarSet(sumVar, intVars);
		return intVars.toArray(new IntVar[intVars.size()]); 
	}
	
	private void fillIntVarSet(IntVariable var, List<IntVar> intVars) {
		if(var instanceof SumVariable) {
			fillIntVarSet(((SumVariable)var).getVal1(), intVars);
			fillIntVarSet(((SumVariable)var).getVal2(), intVars);
		} else if(var instanceof IntConstant) {
			intVars.add(varManager.model.intVar(((IntConstant)var).getIntValue()));
		} else if(var instanceof IntVariable) {
			intVars.add(this.varManager.getVariable(var));
		} else {
			throw new ConstraintException("Not supported yet in sums: " + var);
		}
	}
}
