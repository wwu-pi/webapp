package m.javaee.exe;

import java.io.IOException;
import java.util.Stack;

import org.apache.bcel.generic.InstructionHandle;

import m.cstr.ConstraintManager;
import m.cstr.ConstraintSolution;
import m.cstr.ConstraintStack;
import m.exe.SymbolicExecutor;
import m.exe.cp.ChoicePoint;
import m.exe.ex.SymbolicExecutionException;
import m.exe.visitor.BaseVisitor;
import m.javaee.JavaEEHeap;
import m.javaee.db.SymbolicObjectDatabase;
import m.javaee.state.JavaEESymbolicState;
import m.javaee.visitor.JavaEEVisitor;
import m.state.CopiedSymbolicState;
import m.tc.TestCase;
import m.vm.Frame;

public class JavaEESymbolicExecutor extends SymbolicExecutor {
	
	protected SymbolicObjectDatabase db;
	
	protected JavaEEHeap heap;

	public JavaEESymbolicExecutor(SymbolicObjectDatabase db, JavaEEHeap heap, Stack<Frame> frameStack, ConstraintManager constraintManager) {
		super(heap, frameStack, constraintManager);
		this.db = db;
		this.heap = heap;
	}
	
	
	protected void run() {
		while(!frameStack.isEmpty()) {
			Frame frame = frameStack.pop();
			BaseVisitor executedVisitor = executeFrame(frame);
			if(frameStack.isEmpty()) {
				// we executed the last frame on the stack -> finished symbolic execution
				String returnValueString = executedVisitor.getReturnValue() != null ? executedVisitor.getReturnValue().toString() : "<no return value>";
				System.out.println("*** Finished symbolic execution with return value: "+returnValueString);
				
				JavaEESymbolicState state = new JavaEESymbolicState(db, (JavaEEHeap)heap, frame, frameStack, this.constraintManager.getConstraintStack());
				CopiedSymbolicState copiedState = null;
				try {
					copiedState = state.copy();
				} catch (IOException e) {
					throw new SymbolicExecutionException("Could not copy state", e);
				}
				
				ConstraintSolution solution = this.constraintManager.solve();
				testCases.add(new TestCase(
						executedVisitor.getVisitedMethod(),
						getMethodArgumentsFromFrame(executedVisitor.getVisitedMethod(), executedVisitor.getFrame()), 
						executedVisitor.getReturnValue(),
						copiedState,
						solution));
//				testCases.add(buildTestCase(executedVisitor.getVisitedMethod(), getMethodArgumentsFromFrame(executedVisitor.getVisitedMethod(), executedVisitor.getFrame()), executedVisitor.getReturnValue(), solution));
				checkChoicePoints();
			} else {
				// there is another frame on the stack
				// if the last executed frame had a return value
				// we push that value onto the stack of the next frame to be executed
				if(executedVisitor.getReturnValue() != null) {
					frameStack.peek().getStack().push(executedVisitor.getReturnValue());
				}
			}
		}
	}
	
	protected BaseVisitor executeFrame(Frame frame) {
		BaseVisitor visitor = new JavaEEVisitor(this, db, heap, frame, constraintManager);
		while(visitor.getPC().valid() && !visitor.isInterrupted()) {
			InstructionHandle ih = visitor.getPC().getCurrentInstruction();
			System.out.println(visitor + " executes: " + ih);
			ih.accept(visitor);
			visitor.printCurrentStack();
		}
		return visitor;
	}
	
	
	@Override
	protected void checkChoicePoints() {
		if(this.choicePoints.size() > 0) {
			ChoicePoint next = this.choicePoints.remove(0);
			System.out.println("*** There are choice points, next one is: " + next);
			
			CopiedSymbolicState copiedState = next.getState();
			
			JavaEESymbolicState cpState = null;
			try {
				cpState = JavaEESymbolicState.load(copiedState);
			} catch (ClassNotFoundException | IOException e) {
				throw new SymbolicExecutionException("Could not load symbolic state.", e);
			}
			
			SymbolicObjectDatabase db = cpState.getSymbolicObjectDatabase();
			JavaEEHeap cpHeap = cpState.getHeap();
			Stack<Frame> cpFrameStack = cpState.getFrameStack();
			Frame cpStartFrame = cpState.getCurrentFrame();
			cpStartFrame.getPC().updatePC(next.getContinueWithPC().getPC());
			
			ConstraintStack cpConstraints = cpState.getConstraintStack();
			cpConstraints.addConstraint(next.getAdditionalConstraints());
			ConstraintManager cpConstraintManager = new ConstraintManager(cpConstraints);			
			
			JavaEESymbolicExecutor cpSymbolicExecutor = new JavaEESymbolicExecutor(db, cpHeap, cpFrameStack, cpConstraintManager);
			cpSymbolicExecutor.start(cpStartFrame);
		} else {
			System.out.println("*** No choice points left!");
		}
	}
	
	
	
	public CopiedSymbolicState copyState(Frame currentFrame) {
		JavaEESymbolicState state = new JavaEESymbolicState(db, heap, currentFrame, frameStack, constraintManager.getConstraintStack());
		try {
			return state.copy();
		} catch (IOException e) {
			throw new SymbolicExecutionException("Could not copy the current state.", e);
		}
	}

}
