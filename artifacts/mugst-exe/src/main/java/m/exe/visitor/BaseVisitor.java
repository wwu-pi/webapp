package m.exe.visitor;

import java.io.IOException;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import m.cstr.ConstraintManager;
import m.cstr.ConstraintStack;
import m.exe.SymbolicExecutor;
import m.exe.bcel.PC;
import m.exe.ex.SymbolicExecutionException;
import m.exe.visitor.r.ReturnValue;
import m.state.CopiedSymbolicState;
import m.state.SymbolicState;
import m.var.Variable;
import m.vm.Frame;
import m.vm.LocalVariables;
import m.vm.OperandStack;
import m.vm.heap.Heap;

public abstract class BaseVisitor extends WorkingOnVisitor {

	protected final SymbolicExecutor symbolicExecutor;
	protected final MethodGen methodGen;
	protected final ConstantPoolGen cpg;
	protected final OperandStack stack;
	protected final PC pc;
	protected final LocalVariables localVariables;
	protected final Heap heap;
	protected final ConstraintManager constraintManager;
	protected final Frame frame;
	
	protected Variable returnValue;
	
	protected boolean isInterrupted; // interrupted by a new method invocation -> new frame is constructed and executed!
		
	public boolean isInterrupted() {
		return this.isInterrupted;
	}
	
	public void printCurrentStack() {
		System.out.println("        stack: " + this.stack);
	}
	
	public void printCurrentConstraintStack() {
		System.out.println("\tconstraints: " + this.constraintManager.getConstraintStack());
	}

	public BaseVisitor(SymbolicExecutor symbolicExecutor, Heap heap, Frame frame, ConstraintManager constraintManager) {
		this.symbolicExecutor = symbolicExecutor;
		this.heap = heap;
		this.frame = frame;
		this.constraintManager = constraintManager;
		this.isInterrupted = false;
		
		try {
			this.methodGen = getMethodGen(frame.getMethod());
		} catch (ClassNotFoundException e) {
			throw new SymbolicExecutionException("Could not find method: " + frame.getMethod());
		}
		this.cpg = methodGen.getConstantPool();
		this.stack = frame.getStack();
		this.pc = getPC(frame.getPC());
		this.localVariables = frame.getLocalVars();
	}
	
	
//	public BaseVisitor(SymbolicState symbolicState) throws ClassNotFoundException {
//		this(symbolicState, null);
//	}
//	
//	public BaseVisitor(SymbolicState symbolicState, BaseVisitor calledBy) throws ClassNotFoundException {
//		if(calledBy != null) {
//			if(symbolicState.getConstraintStack() != null) {
//				throw new SymbolicExecutionException("Don't knwo which constraint manager to use? The given one in the symbolic-state, or the one shared from the called-by-visitor? Most likely the latter one!");
//			}
//			// if it is called by another visitor, we share the constraint manager!
//			this.constraintManager = calledBy.constraintManager;
//		} else {
//			this.constraintManager = new ConstraintManager(symbolicState.getConstraintStack());
//		}
//		
//		this.methodGen = getMethodGen(symbolicState.getMethod());
//		this.cpg = methodGen.getConstantPool();
//		this.method = symbolicState.getMethod();
//		this.stack = symbolicState.getOperandStack();
//		this.pc = getPC(symbolicState.getPC());
//		this.localVariables = symbolicState.getLocalVariables();
//		this.heap = symbolicState.getHeap();
//	}

	private PC getPC(m.vm.PC pc) {
		for(InstructionHandle ih : this.methodGen.getInstructionList()) {
			if(ih.getPosition() == pc.getPC()) {
				return new PC(pc, ih);
			}
		}
		throw new SymbolicExecutionException("Could not program counter address: " + pc);
	}

	protected MethodGen getMethodGen(m.vm.Method method) throws ClassNotFoundException {
		JavaClass jc = Repository.lookupClass(method.getClassName());
		for(Method m : jc.getMethods()) {
			if(m.getName().equals(method.getMethodName())
			&& m.getSignature().equals(method.getMethodSignature())) {
				return new MethodGen(m, jc.getClassName(), new ConstantPoolGen(jc.getConstantPool()));
			}
		}
		throw new SymbolicExecutionException("Could not find method in class path: " + method);
	}
	
	public PC getPC() {
		return this.pc;
	}
	
//	/**
//	 * Copy the current state.
//	 */
//	public CopiedSymbolicState copyState() {
//		SymbolicState state = new SymbolicState(heap, frame, constraintManager.getConstraintStack());
//		try {
//			return state.copy();
//		} catch (IOException e) {
//			throw new SymbolicExecutionException("Error while copy symoblic state!", e);
//		}
//	}
	
	@Override
	public String toString() {
		return "Visitor["+methodGen.getClassName()+" # "+methodGen+"]";
	}

	public Variable getReturnValue() {
		return this.returnValue;
	}

	public MethodGen getVisitedMethod() {
		return this.methodGen;
	}

	public Frame getFrame() {
		return this.frame;
	}
}
