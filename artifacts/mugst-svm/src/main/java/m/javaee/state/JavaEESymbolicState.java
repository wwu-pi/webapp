package m.javaee.state;

import java.io.IOException;
import java.util.Stack;

import m.cstr.ConstraintStack;
import m.javaee.JavaEEHeap;
import m.javaee.db.SymbolicObjectDatabase;
import m.state.CopiedSymbolicState;
import m.state.SymbolicState;
import m.vm.Frame;
import m.vm.heap.Heap;

public class JavaEESymbolicState extends SymbolicState {

	private static final long serialVersionUID = 1L;

	private final SymbolicObjectDatabase db;
	
	public JavaEESymbolicState(SymbolicObjectDatabase db, JavaEEHeap heap, Frame currentFrame, Stack<Frame> frameStack, ConstraintStack constraints) {
		super(heap, currentFrame, frameStack, constraints);
		this.db = db;
	}

	public SymbolicObjectDatabase getSymbolicObjectDatabase() {
		return this.db;
	}
	
	@Override
	public JavaEEHeap getHeap() {
		return (JavaEEHeap)super.getHeap();
	}
	
	public static JavaEESymbolicState load(CopiedSymbolicState copiedState) throws IOException, ClassNotFoundException {
		return (JavaEESymbolicState)SymbolicState.load(copiedState);
	}
}