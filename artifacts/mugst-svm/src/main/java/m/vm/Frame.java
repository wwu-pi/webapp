package m.vm;

import java.io.Serializable;

import m.state.StateElement;

public class Frame extends StateElement implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected Method method;
	protected LocalVariables localVars;
	protected OperandStack stack;
	protected PC pc;
	
//	protected Frame calledBy;
	
	/**
	 * Create a new frame. Initialized with an empty operand-stack and an empty local-variable table.
	 * @param method the method of this frame
	 * @param maxLocals the maximum number of local variables
	 */
	public Frame(Method method, int maxLocals) {
		this.method = method;
		this.pc = new PC(0); // start with first instruction
		this.localVars = new LocalVariables(maxLocals);
		this.stack = new OperandStack();
	}

//	/**
//	 * Create a new frame. Initialized with an empty operand-stack and an empty local-variable table.
//	 * Stores called frame.
//	 * @param calledBy
//	 * @param method
//	 * @param maxLocals
//	 */
//	public Frame(Frame calledBy, Method method, int maxLocals) {
//		this.calledBy = calledBy;
//		this.method = method;
//		this.pc = new PC(0); // start with first instruction
//		this.localVars = new LocalVariables(maxLocals);
//		this.stack = new OperandStack();
//	}

	public Method getMethod() {
		return method;
	}

	public LocalVariables getLocalVars() {
		return localVars;
	}

	public OperandStack getStack() {
		return stack;
	}

	public PC getPC() {
		return pc;
	}

//	public Frame getCalledBy() {
//		return calledBy;
//	}
	
	@Override
	public String toString() {
		return "method="+this.method+", pc="+this.pc+", localVars="+localVars+", stack="+stack;
	}
}
