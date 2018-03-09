package m.cstr.meta;

import java.io.Serializable;

public enum BoolOp implements Serializable {

	LT,
	GT,
	LE,
	GE,
	EQ,
	NEQ;
	
	public static BoolOp negate(BoolOp op) {
		switch(op) {
			case LT : return GE;
			case GT : return LE;
			case LE : return GT;
			case GE : return LT;
			case EQ : return NEQ;
			case NEQ : return EQ;
		}
		throw new RuntimeException("Can not happen...");
	}
}