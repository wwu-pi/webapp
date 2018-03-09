package m.state;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Stack;

import m.cstr.ConstraintStack;
import m.javaee.JavaEEHeap;
import m.vm.Frame;
import m.vm.heap.Heap;

public class SymbolicState implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected final Heap heap;
	protected final Frame currentFrame;
	protected final Stack<Frame> frameStack;
	protected final ConstraintStack constraints;
	
	public SymbolicState(Heap heap, Frame currentFrame, Stack<Frame> frameStack, ConstraintStack constraints) {
		this.heap = heap;
		this.currentFrame = currentFrame;
		this.constraints = constraints;
		this.frameStack = frameStack;
	}
	
	public Heap getHeap() {
		return this.heap;
	}
	
	public Frame getCurrentFrame() {
		return this.currentFrame;
	}
		
	public Stack<Frame> getFrameStack() {
		return this.frameStack;
	}
	
	public ConstraintStack getConstraintStack() {
		return this.constraints;
	}
	
	// -------------------------------
	// -- METHODS TO COPY THE STATE --
	// -------------------------------
	
	public CopiedSymbolicState copy() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bos);
		out.writeObject(this);
		return new CopiedSymbolicState(bos);
	}
	
	public static SymbolicState load(CopiedSymbolicState copiedState) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(copiedState.bytes.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        SymbolicState copied = (SymbolicState) in.readObject();
        return copied;
	}

}
