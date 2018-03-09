package m.cstr.p;

import m.cstr.Constraint;
import m.cstr.meta.BoolOp;
import m.var.IntVariable;

public class IntXopY extends Constraint {

	private static final long serialVersionUID = 1L;
	
	protected final IntVariable var1;
	protected final IntVariable var2;
	protected final BoolOp op;
	
	public IntXopY(IntVariable var1, IntVariable var2, BoolOp op) {
		this.var1 = var1;
		this.var2 = var2;
		this.op = op;
	}

	public IntVariable getVar1() {
		return var1;
	}

	public IntVariable getVar2() {
		return var2;
	}

	public BoolOp getOp() {
		return op;
	}

	@Override
	public IntXopY negate() {
		return new IntXopY(this.var1, this.var2, BoolOp.negate(this.op));
	}
	
	@Override
	public String toString() {
		return "IntXopY: x="+var1+", y="+var2+", op="+op;
	}
}
