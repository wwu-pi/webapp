package m.exe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import m.cstr.ConstraintManager;
import m.cstr.ConstraintSolution;
import m.cstr.ConstraintStack;
import m.exe.cp.ChoicePoint;
import m.exe.ex.SymbolicExecutionException;
import m.exe.visitor.BaseVisitor;
import m.exe.visitor.MethodVisitor;
import m.javaee.visitor.JavaEEVisitor;
import m.state.CopiedSymbolicState;
import m.state.SymbolicState;
import m.tc.TestCase;
import m.tc.gen.SimpleJUnitGenerator;
import m.tc.gen.meta.TestCaseGenerator;
import m.var.Variable;
import m.vm.Frame;
import m.vm.heap.Heap;

public class SymbolicExecutor {
	
	protected List<ChoicePoint> choicePoints;
	
	protected final Stack<Frame> frameStack;
	protected final Heap heap;
	protected final ConstraintManager constraintManager;
	
	protected static final Set<TestCase> testCases = new HashSet<>();
	
	public SymbolicExecutor(Heap heap, Stack<Frame> frameStack, ConstraintManager constraintManager) {
		this.choicePoints = new ArrayList<>();	
		this.heap = heap;
		this.constraintManager = constraintManager;
		this.frameStack = frameStack;
	}
	
	public CopiedSymbolicState copyState(Frame currentFrame) {
		SymbolicState state = new SymbolicState(heap, currentFrame, frameStack, constraintManager.getConstraintStack());
		try {
			return state.copy();
		} catch (IOException e) {
			throw new SymbolicExecutionException("Could not copy the current state.", e);
		}
	}

	public void addChoicePoint(ChoicePoint choicePoint) {
		this.choicePoints.add(choicePoint);
	}
	
	public void start(Frame startFrame) {
		this.frameStack.push(startFrame);
		run();
	}
	
	protected void run() {
		while(!frameStack.isEmpty()) {
			Frame frame = frameStack.pop();
			BaseVisitor executedVisitor = executeFrame(frame);
			if(frameStack.isEmpty()) {
				// we executed the last frame on the stack -> finished symbolic execution
				String returnValueString = executedVisitor.getReturnValue() != null ? executedVisitor.getReturnValue().toString() : "<no return value>";
				System.out.println("*** Finished symbolic execution with return value: "+returnValueString);
				
				SymbolicState state = new SymbolicState(heap, frame, frameStack, this.constraintManager.getConstraintStack());
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
		BaseVisitor visitor = new MethodVisitor(this, heap, frame, constraintManager);
//		BaseVisitor visitor = new JavaEEVisitor(this, heap, frame, constraintManager);
		while(visitor.getPC().valid() && !visitor.isInterrupted()) {
			InstructionHandle ih = visitor.getPC().getCurrentInstruction();
			System.out.println(visitor + " executes: " + ih);
			ih.accept(visitor);
			visitor.printCurrentStack();
		}
		return visitor;
	}

	public void pushFrame(Frame frame) {
		this.frameStack.push(frame);
	}
	
	
	protected void checkChoicePoints() {
		if(this.choicePoints.size() > 0) {
			ChoicePoint next = this.choicePoints.remove(0);
			System.out.println("*** There are choice points, next one is: " + next);
			
			CopiedSymbolicState copiedState = next.getState();
			
			SymbolicState cpState = null;
			try {
				cpState = SymbolicState.load(copiedState);
			} catch (ClassNotFoundException | IOException e) {
				throw new SymbolicExecutionException("Could not load symbolic state.", e);
			}
			
			Heap cpHeap = cpState.getHeap();
			Stack<Frame> cpFrameStack = cpState.getFrameStack();
			Frame cpStartFrame = cpState.getCurrentFrame();
			cpStartFrame.getPC().updatePC(next.getContinueWithPC().getPC());
			
			ConstraintStack cpConstraints = cpState.getConstraintStack();
			cpConstraints.addConstraint(next.getAdditionalConstraints());
			ConstraintManager cpConstraintManager = new ConstraintManager(cpConstraints);			
			
			SymbolicExecutor cpSymbolicExecutor = new SymbolicExecutor(cpHeap, cpFrameStack, cpConstraintManager);
			cpSymbolicExecutor.start(cpStartFrame);
		} else {
			System.out.println("*** No choice points left!");
		}
	}
	
	protected Variable[] getMethodArgumentsFromFrame(MethodGen mut, Frame frame) {
		int offset = 0;
		if(!mut.isStatic()) {
			offset = 1;
		}
		int argCtn = mut.getArgumentTypes().length;
		Variable[] argVars = new Variable[argCtn];
		for(int i=0; i<argCtn; i++) {
			argVars[i] = frame.getLocalVars().get(i+offset);
		}
		return argVars;
	}
	
	private TestCaseGenerator buildTestCase(MethodGen mut, Variable[] methodArguments, Variable expectedReturnValue, ConstraintSolution solution) {
		TestCaseGenerator generator = new SimpleJUnitGenerator(mut, methodArguments, expectedReturnValue, solution);
		generator.genearte();
		return generator;
	}
	
	public static Set<TestCase> getTestCases() {
		return testCases;
	}

	public static void resetTestCases() {
		testCases.clear();
	}
	
	
	
	
	
	
	
	
//	
//	
//	public void start(Frame frame) {
//		pushAndExecuteFrame(frame);
//		
////		executeFrame(frame);
////		
////		if(this.choicePoints.size() > 0) {
////			ChoicePoint next = this.choicePoints.remove(0);
////			System.out.println("*** There are choice points, next one is: " + next);
////		}
//	}
//	
//	private void pushAndExecuteFrame(Frame frame) {
//		this.frameStack.push(frame);
//		this.executeNextFrame();
//	}
//	
//	
//
//	private void executeNextFrame() {
//		if(!this.frameStack.isEmpty()) {
//			Frame frame = this.frameStack.pop();
//			System.out.println("**** EXECUTE FRAME FOR METHOD: " + frame.getMethod() + " AND STACK="+frame.getStack() + " AND LOCALVARS="+frame.getLocalVars() );
//			BaseVisitor visitor = new MethodVisitor(this, heap, frame, constraintManager);
//			while(visitor.getPC().valid()) {
//				InstructionHandle ih = visitor.getPC().getCurrentInstruction();
//				System.out.println(visitor + " executes: " + ih);
//				ih.accept(visitor);
//				visitor.printCurrentStack();
//			}
//			System.out.println("*** Finished execution of visitor: " + visitor + "\n\twith result: " + visitor.getReturnValue());
//			visitor.printCurrentConstraintStack();
//		}
//		System.out.println("*** No more frames on stack.");
//	}
//
//	public void executeFrame(Frame frame) {
//		System.out.println("**** EXECUTE FRAME FOR METHOD: " + frame.getMethod() + " AND STACK="+frame.getStack() + " AND LOCALVARS="+frame.getLocalVars() );
//		BaseVisitor visitor = new MethodVisitor(this, heap, frame, constraintManager);
//		while(visitor.getPC().valid()) {
//			InstructionHandle ih = visitor.getPC().getCurrentInstruction();
//			System.out.println(visitor + " executes: " + ih);
//			ih.accept(visitor);
//			visitor.printCurrentStack();
//		}
//		System.out.println("*** Finished execution of visitor: " + visitor + "\n\twith result: " + visitor.getReturnValue());
//		visitor.printCurrentConstraintStack();
//	}
//	
//	public void pushAndExecuteFrameafasdfas(Frame newFrame) {
//		executeFrame(newFrame);
//		if(newFrame.getCalledBy() != null) {
//			newFrame.getCalledBy().getPC();
//			executeFrame(newFrame.getCalledBy());
//		}
////		calledFrame.getCalledBy().getStack().push(new IntConstant(442));
//	}
//
//	
//	
//	
//	
//	
//	
//	
//	
//
//	public void run(BaseVisitor visitor) {
//		while(visitor.getPC().valid()) {
//			InstructionHandle ih = visitor.getPC().getCurrentInstruction();
//			System.out.println(visitor + " executes: " + ih);
//			ih.accept(visitor);
//			visitor.printCurrentStack();
//		}
//		System.out.println("*** Finished execution of visitor: " + visitor + "\n\twith result: " + visitor.getReturnValue());
//		visitor.printCurrentConstraintStack();
//	}
//	
//	public boolean executeNextChoice() {
//		if(this.choicePoints.size() > 0 ) {
//			try {
//				continueWithNextChoice();
//				return true;
//			} catch (ClassNotFoundException | IOException e) {
//				throw new SymbolicExecutionException("Error while trying to continue with next choice point.", e);
//			}
//		}
//		return false;
//	}
//
//	private void continueWithNextChoice() throws ClassNotFoundException, IOException {
//		ChoicePoint next = this.choicePoints.remove(0);
//		
//		System.out.println("*** Continue with choice point: " + next);
//		
//		SymbolicState state = SymbolicState.load(next.getState());
//		
//		// the frame in the state is the frame _BEFORE_ the choice point
//		// update to next instruction to be executed
//		state.getFrame().getPC().updatePC(next.getContinueWithPC().getPC());
//		// set additional constraints
//		ConstraintStack constraintStack = state.getConstraintStack();
//		constraintStack.addConstraint(next.getAdditionalConstraints());
//		ConstraintManager constraintManager = new ConstraintManager(constraintStack);
//
//		BaseVisitor visitor = new MethodVisitor(this, state.getHeap(), state.getFrame(), constraintManager);
//		run(visitor);
//		
//		// HIER FEHLT DANN DER FRAME VORHER... BESSER EINE FRAME ARCHITECTURE
//	}
//
//	public void addChoicePoint(ChoicePoint choicePoint) {
//		this.choicePoints.add(choicePoint);
//	}
//
//	public boolean hasMoreChoices() {
//		return this.choicePoints.size() > 0;
//	}
//
//	public BaseVisitor generateNextChoiceVisitor() throws ClassNotFoundException, IOException {
//		ChoicePoint next = this.choicePoints.remove(0);
//		SymbolicState state = SymbolicState.load(next.getState());
//		
//		// the frame in the state is the frame _BEFORE_ the choice point
//		// update to next instruction to be executed
//		state.getFrame().getPC().updatePC(next.getContinueWithPC().getPC());
//		// set additional constraints
//		ConstraintStack constraintStack = state.getConstraintStack();
//		constraintStack.addConstraint(next.getAdditionalConstraints());
//		ConstraintManager constraintManager = new ConstraintManager(constraintStack);
//		
//		BaseVisitor visitor = new MethodVisitor(this, state.getHeap(), state.getFrame(), constraintManager);
//		
//		return visitor;
//	}
//
//	


	
	
}
