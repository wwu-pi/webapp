package m.exe.visitor;

import java.util.HashSet;
import java.util.Set;

import org.apache.bcel.Const;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ARETURN;
import org.apache.bcel.generic.ARRAYLENGTH;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.CALOAD;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IADD;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.INSTANCEOF;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.IRETURN;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.ISUB;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LDC2_W;
import org.apache.bcel.generic.LLOAD;
import org.apache.bcel.generic.LRETURN;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.Type;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

import m.cstr.Constraint;
import m.cstr.ConstraintManager;
import m.cstr.meta.BoolOp;
import m.cstr.p.IntXopY;
import m.exe.SymbolicExecutor;
import m.exe.cp.ChoicePoint;
import m.exe.ex.SymbolicExecutionException;
import m.state.CopiedSymbolicState;
import m.sym.var.VariableGenerator;
import m.var.ArrayReference;
import m.var.IntConstant;
import m.var.IntVariable;
import m.var.LongConstant;
import m.var.LongVariable;
import m.var.ObjectReference;
import m.var.SumVariable;
import m.var.Variable;
import m.vm.Frame;
import m.vm.LocalVariables;
import m.vm.Method;
import m.vm.PC;
import m.vm.heap.Heap;
import m.vm.heap.SymbolicObject;

public class MethodVisitor extends BaseVisitor {
	
	public MethodVisitor(SymbolicExecutor symbolicExecutor, Heap heap, Frame frame, ConstraintManager constraintManager) {
		super(symbolicExecutor, heap, frame, constraintManager);
	}

	// -- LOAD INSTRUCTIONS ----------------------------------------------------------------------------
	@Override
	public void visitILOAD(ILOAD obj) {
		stack.push(localVariables.get(obj.getIndex()));
		pc.advance();
	}
	
	@Override
	public void visitALOAD(ALOAD obj) {
		stack.push(localVariables.get(obj.getIndex()));
		pc.advance();
	}
	
    @Override
    public void visitLLOAD( final LLOAD obj ) {
    	stack.push(localVariables.get(obj.getIndex()));
		pc.advance();
    }
	
	@Override
	public void visitCALOAD(CALOAD obj) {
		IntVariable indexVar = stack.pop(IntVariable.class);
		ArrayReference arrayRef = stack.pop(ArrayReference.class);
		
		if(indexVar instanceof IntConstant) {
			int index = ((IntConstant)indexVar).getIntValue();
			Variable var = arrayRef.getElement(index);
			if(var == null) {
				Type charType = BasicType.getType(Const.T_CHAR);
				var = VariableGenerator.generateNewVariable(arrayRef+".element#"+index, charType, heap);
				arrayRef.setElement(index, var);
			}
			stack.push(var);
		} else {
			throw new SymbolicExecutionException("Symbolic index access to element in array not supported yet");
		}
		
		pc.advance();
	}
	
	// --------------------------------------------------------------------------------------------------

	
	// -- STORE INSTRUCTIONS ----------------------------------------------------------------------------

	@Override
	public void visitASTORE(ASTORE obj) {
		this.localVariables.set(obj.getIndex(), stack.pop());
		pc.advance();
	}
	
	@Override
	public void visitISTORE(ISTORE obj) {
		this.localVariables.set(obj.getIndex(), stack.pop());
		pc.advance();
	}
	
	// --------------------------------------------------------------------------------------------------
	
	@Override
	public void visitGETFIELD(GETFIELD obj) {
		ObjectReference objRef = stack.pop(ObjectReference.class);
		String fieldName = obj.getFieldName(cpg);
		Variable value = objRef.getValue(fieldName);
		if(value == null) {
			String fieldVariableName = objRef.getName() + "." + fieldName;
			value = VariableGenerator.generateNewVariable(fieldVariableName, obj.getFieldType(cpg), heap);
			
			// if it is an array type -> set constraint: length>=0
			if(value instanceof ArrayReference) {
				ArrayReference arrRef = (ArrayReference)value;
				constraintManager.addConstraint(new IntXopY(arrRef.getLength(), new IntConstant(0), BoolOp.GE));
			}
			
			objRef.setValue(fieldName, value);
		}
		stack.push(value);
		pc.advance();
	}
	
	@Override
	public void visitPUTFIELD(PUTFIELD obj) {
		Variable value = stack.pop();
		ObjectReference ref = stack.pop(ObjectReference.class);
		String fieldName = obj.getFieldName(cpg);
		ref.setValue(fieldName, value);
		pc.advance();
	}
	
	@Override
	public void visitARRAYLENGTH(ARRAYLENGTH obj) {
		ArrayReference arrayRef = stack.pop(ArrayReference.class);
		stack.push(arrayRef.getLength());
		pc.advance();
	}
	
	// --------------------------------------------------------------------------------------------------

	// -- NEW INSTRUCTION   -----------------------------------------------------------------------------

	@Override
	public void visitNEW(NEW obj) {
		ObjectType objType = obj.getLoadClassType(cpg);
		String className = objType.getClassName();
		JavaClass jc;
		try {
			jc = Repository.lookupClass(className);
		} catch (ClassNotFoundException e) {
			throw new SymbolicExecutionException("Could not find class: " + className);
		}
		SymbolicObject symObj = heap.newObject(jc);
		stack.push(new ObjectReference("new_"+className+"."+symObj.hashCode(), symObj));
		pc.advance();
	}
	
	// --------------------------------------------------------------------------------------------------

	@Override
	public void visitDUP(DUP obj) {
		Variable dupVar = stack.pop();
		stack.push(dupVar);
		stack.push(dupVar);
		pc.advance();
	}
	
	// -- CONSTANT INSTRUCTIONS -------------------------------------------------------------------------
	
    @Override
    public void visitLDC2_W(LDC2_W obj) {
    	int value = obj.getValue(cpg).intValue();
    	stack.push(new LongConstant(value));
		pc.advance();
    }
    
	@Override
	public void visitBIPUSH(BIPUSH obj) {
		int value = obj.getValue().intValue();
		stack.push(new IntConstant(value));
		pc.advance();
	}
	
	@Override
	public void visitICONST(ICONST obj) {
		int value = obj.getValue().intValue();
		stack.push(new IntConstant(value));
		pc.advance();
	}
	
	@Override
	public void visitLDC(LDC obj) {
		Type type = obj.getType(cpg);
		
		if(type == Type.INT) {
			int value = (int)obj.getValue(cpg);
			stack.push(new IntConstant(value));
			pc.advance();
			return;
		}
		
		if(type instanceof ObjectType) {
			ObjectType objType = (ObjectType)type;
			if(objType.getClassName().equals(String.class.getName())) {
				String stringValue = (String)obj.getValue(cpg);
				SymbolicObject stringConstantObj = heap.getStringConstantObject(stringValue);
				
				// add constraints on the value array of the string constant
				ArrayReference valueArray = (ArrayReference) stringConstantObj.getValue("value");
				IntXopY lengthConstraint = new IntXopY(valueArray.getLength(), new IntConstant(stringValue.length()), BoolOp.EQ);
				constraintManager.addConstraint(lengthConstraint);
//				for(int i=0; i<stringValue.length(); i++) {
//					CharVariable ele = (CharVariable)valueArray.getElement(i);
//					IntXopY elementConstraint = new IntXopY(ele, new CharConstant((int)stringValue.charAt(i)), BoolOp.EQ);
//					constraintManager.addConstraint(elementConstraint); 
//				}
				
				stack.push(new ObjectReference("STRING_CONST_("+stringValue+")", stringConstantObj));
				pc.advance();
				return;
			} else if(objType.getClassName().equals(Class.class.getName())) {
				ObjectType objClassType = (ObjectType)obj.getValue(cpg);
				String className = objClassType.getClassName();
				SymbolicObject classConstantObj = heap.getClassConstantObject(className);
				stack.push(new ObjectReference("CLASS_CONST_("+className+")", classConstantObj));
				pc.advance();
				return;
			} else {
				throw new SymbolicExecutionException("LDC for Object-Type must be String or Class, but was: " + objType.getClassName());
			}
		}
		
		throw new SymbolicExecutionException("LDC not supported for type: " + type);
	}
	
	// --------------------------------------------------------------------------------------------------

	
	// -- ARITHM INSTRUCTIONS ---------------------------------------------------------------------------
	
	@Override
	public void visitIINC(IINC obj) {
		IntVariable intVar = (IntVariable)localVariables.get(obj.getIndex());
		IntVariable increment = null;
		if(intVar instanceof IntConstant) {
			IntConstant intConst = (IntConstant)intVar;
			int incrementValue = intConst.getIntValue() + obj.getIncrement();
			increment = new IntConstant(incrementValue);
		} else {
			increment = new SumVariable(intVar, new IntConstant(obj.getIncrement()));
		}
		localVariables.set(obj.getIndex(), increment);
		pc.advance();
	}
	
	@Override
	public void visitIADD(IADD obj) {
		IntVariable val2 = stack.pop(IntVariable.class);
		IntVariable val1 = stack.pop(IntVariable.class);
		SumVariable sum = new SumVariable(val1, val2);
		stack.push(sum);
		pc.advance();
	}
	
	@Override
	public void visitISUB(ISUB obj) {
		IntVariable val2 = stack.pop(IntVariable.class);
		IntVariable val1 = stack.pop(IntVariable.class);
		val2.negate();
		SumVariable sum = new SumVariable(val1, val2);
		stack.push(sum);
		pc.advance();
	}
	
	// --------------------------------------------------------------------------------------------------

	
	// -- IF INSTRUCTIONS -------------------------------------------------------------------------------
	
	@Override
	public void visitIfInstruction(IfInstruction obj) {
		handleIfInstruction(obj.getOpcode(), obj.getTarget());
	}
	
	private void handleIfInstruction(short opcode, InstructionHandle target) {
		switch(opcode) {
			case org.apache.bcel.Const.IFEQ : { handleIfCond(BoolOp.EQ, target); return; }
			case org.apache.bcel.Const.IFNE : { handleIfCond(BoolOp.NEQ, target); return; }
		
			case org.apache.bcel.Const.IF_ACMPEQ : { handleIfACMP(BoolOp.EQ,  target); return; }
			case org.apache.bcel.Const.IF_ACMPNE : { handleIfACMP(BoolOp.NEQ, target); return; }
			
			case org.apache.bcel.Const.IF_ICMPEQ : { handleIfICMP(BoolOp.EQ,  target); return; }
			case org.apache.bcel.Const.IF_ICMPGE : { handleIfICMP(BoolOp.GE,  target); return; }
			case org.apache.bcel.Const.IF_ICMPGT : { handleIfICMP(BoolOp.GT,  target); return; }
			case org.apache.bcel.Const.IF_ICMPLE : { handleIfICMP(BoolOp.LE,  target); return; }
			case org.apache.bcel.Const.IF_ICMPLT : { handleIfICMP(BoolOp.LT,  target); return; }
			case org.apache.bcel.Const.IF_ICMPNE : { handleIfICMP(BoolOp.NEQ, target); return; }
		}
		throw new SymbolicExecutionException("Opcode: " + opcode + " for if-instruction not supported yet");
	}
	
	private void handleIfACMP(BoolOp op, InstructionHandle target) {
		ObjectReference objRef2 = stack.pop(ObjectReference.class);
		ObjectReference objRef1 = stack.pop(ObjectReference.class);
		
		if(!objRef1.hasValues()) {
			// object1 has not been initialized yet, we can set it to be the object referenced by object2
			
		}
		
		else if(!objRef2.hasValues()) {
			// object2 has not been initialized yet, we can set it to be the object referenced by object1
			
		}
		
		else {
			// both object1 and object2 are initialized already, no aliasing supported here...
			
		}
		
		// WORKAROUND FOR NOW: no aliasing supported..
		boolean equals = objRef1 == objRef2;
		if(op == BoolOp.EQ) {
			if(equals) {
				pc.setInstruction(target); return;
			} else {
				pc.advance();  return;
			}
		} else if(op == BoolOp.NEQ) {
			if(!equals) {
				pc.setInstruction(target); return;
			} else {
				pc.advance();  return;
			}
		}
		throw new SymbolicExecutionException("ACMP must be either EQ or NEQ!");
	}

	private void handleIfCond(BoolOp op, InstructionHandle target) {
		IntVariable var1 = stack.pop(IntVariable.class);
		IntConstant var2 = new IntConstant(0);
		
		handleIfIntegerComparison(var1, op, var2, target);		
	}
	
	private void handleIfICMP(BoolOp op, InstructionHandle target) {
		IntVariable var2 = stack.pop(IntVariable.class);
		IntVariable var1 = stack.pop(IntVariable.class);
		
		handleIfIntegerComparison(var1, op, var2, target);
	}
	
	private void handleIfIntegerComparison(IntVariable var1, BoolOp op, IntVariable var2, InstructionHandle target) {
		if(handleIfIntegerConstantComparison(var1, op, var2, target)) {
			return;
		}
		
		IntXopY trueConstraint = new IntXopY(var1, var2, op);
		IntXopY falseConstraint = trueConstraint.negate();
		boolean trueSatisfiable = this.constraintManager.isSatisfiable(trueConstraint);
		boolean falseSatisfiable = this.constraintManager.isSatisfiable(falseConstraint);
		
		if(!trueSatisfiable && !falseSatisfiable) {
			// cannot continue this branch, go back to last choice point...
			pc.invalidate();
			return;
		}
		
		if(trueSatisfiable && !falseSatisfiable) {
			// can only continue true branch
			this.constraintManager.addConstraint(trueConstraint);
			pc.setInstruction(target);
			return;
		}
		
		if(!trueSatisfiable && falseSatisfiable) {
			// can only continue false branch
			this.constraintManager.addConstraint(falseConstraint);
			pc.advance();
			return;
		}
		
		if(trueSatisfiable && falseSatisfiable) {
			// can continue on both branches -> create choice point
			// we continue execution with the true branch, and create a choice point for the false branch
			CopiedSymbolicState copiedState = this.symbolicExecutor.copyState(frame);
			PC continuePC = new PC(pc.getNextInstruction().getPosition());
			Set<Constraint> additionalConstraints = new HashSet<>();
			additionalConstraints.add(falseConstraint);
			ChoicePoint choicePoint = new ChoicePoint(copiedState, this.frame.getMethod(), continuePC, additionalConstraints);
			this.symbolicExecutor.addChoicePoint(choicePoint);
			
			
			// first, continue with the true branch
			this.constraintManager.addConstraint(trueConstraint);
			pc.setInstruction(target);
			return;
		}
	}
	
	private boolean handleIfIntegerConstantComparison(IntVariable var1, BoolOp op, IntVariable var2, InstructionHandle target) {
		// if both integer variables are constants -> we can check it without a constraint solver
		if(var1 instanceof IntConstant && var2 instanceof IntConstant) {
			IntConstant const1 = (IntConstant)var1;
			IntConstant const2 = (IntConstant)var2;
			
			boolean c = false;
			switch(op) {
				case EQ: { c = const1.getIntValue() == const2.getIntValue(); break; }
				case GE: { c = const1.getIntValue() >= const2.getIntValue(); break; }
				case GT: { c = const1.getIntValue() >  const2.getIntValue(); break; }
				case LE: { c = const1.getIntValue() <= const2.getIntValue(); break; }
				case LT: { c = const1.getIntValue() <  const2.getIntValue(); break; }
				case NEQ:{ c = const1.getIntValue() != const2.getIntValue(); break; }
			}
			
			if(c) {
				pc.setInstruction(target);
			} else {
				pc.advance();
			}
			
			return true;
		}
		return false;
	}
	
	// --------------------------------------------------------------------------------------------------
	
	
	// -- INVOKE INSTRUCTIONS----------------------------------------------------------------------------
	
	@Override
	public void visitINVOKEVIRTUAL(INVOKEVIRTUAL obj) {
		handleInvokeInstruction(obj);
	}
	
	@Override
	public void visitINVOKESTATIC(INVOKESTATIC obj) {
		handleInvokeInstruction(obj);
	}
	
	@Override
	public void visitINVOKESPECIAL(INVOKESPECIAL obj) {
		handleInvokeInstruction(obj);
	}
	
	public void handleInvokeInstruction(InvokeInstruction obj) {
		String methodName = obj.getMethodName(cpg);
		String methodSigature = obj.getSignature(cpg);
		String className = obj.getClassName(cpg);
		
		Method invokedMethod = new Method(className, methodName, methodSigature);
		MethodGen calledMethod = null;
		try {
			calledMethod = getMethodGen(invokedMethod);
		} catch (ClassNotFoundException e) {
			throw new SymbolicExecutionException("Could not find method: : " + methodName + " " + methodSigature + " in class: " + className, e);
		}
		
		// build called frame
		Frame calledFrame = new Frame(invokedMethod, calledMethod.getMaxLocals());
		
		// build the local variables
		LocalVariables invokedMethodLV = calledFrame.getLocalVars();
		int consumes = obj.consumeStack(cpg);
		for(int i=(consumes-1); i>=0; i--) {
			Variable var = stack.pop();
			invokedMethodLV.set(i, var);
			if(var instanceof LongVariable) {
				invokedMethodLV.set(i-1, var);
				i--;
			}
		}

		pc.advance();
		this.isInterrupted = true;
		
		symbolicExecutor.pushFrame(frame);
		symbolicExecutor.pushFrame(calledFrame);
		
		
		
		
		
//		BaseVisitor invokedVisitor = new MethodVisitor(symbolicExecutor, heap, calledFrame, constraintManager);
//		
//		symbolicExecutor.run(invokedVisitor);
//		
////		if(invokedVisitor.returnValue != null && invokedVisitor.returnValue instanceof VariableReturn) {
////			VariableReturn varRetVal = (VariableReturn)invokedVisitor.returnValue;
////			this.stack.push(varRetVal.getReturnValue());
////		}
//		
//		pc.advance();
	}
	
	// --------------------------------------------------------------------------------------------------

	
	
	// -- RETURN INSTRUCTIONS----------------------------------------------------------------------------

	@Override
	public void visitIRETURN(IRETURN obj) {
		IntVariable retVal = stack.pop(IntVariable.class);
		while(!stack.isEmpty()) {
			stack.pop();
		}
		this.returnValue = retVal;
		pc.invalidate();
	}
	
	@Override
	public void visitARETURN(ARETURN obj) {
		ObjectReference retVal = stack.pop(ObjectReference.class);
		while(!stack.isEmpty()) {
			stack.pop();
		}
		this.returnValue = retVal;
		pc.invalidate();
	}
	
	@Override
	public void visitLRETURN(LRETURN obj) {
		IntVariable retVal = stack.pop(IntVariable.class);
		while(!stack.isEmpty()) {
			stack.pop();
		}
		this.returnValue = retVal;
		pc.invalidate();
	}
	
	@Override
	public void visitRETURN(RETURN obj) {
		while(!stack.isEmpty()) {
			stack.pop();
		}
		this.returnValue = null;
		pc.invalidate();
	}
	
	// --------------------------------------------------------------------------------------------------
	
	
	
	
	
	
	
	
	@Override
	public void visitGOTO(GOTO obj) {
		pc.setInstruction(obj.getTarget());
	}
	
	
	
	
	
	
	@Override
	public void visitINSTANCEOF(INSTANCEOF obj) {
		Variable var = stack.pop();
		if(var instanceof ObjectReference) {
			ObjectReference objRef = (ObjectReference)var;
			ObjectType objType = obj.getLoadClassType(cpg);
			
			String S = objRef.getClassName();
			String T = objType.getClassName();
			
			try {
				JavaClass jcS = Repository.lookupClass(S);
				JavaClass jcT = Repository.lookupClass(T);
				
				if(jcS.instanceOf(jcT)) {
					stack.push(new IntConstant(1));
				} else {
					stack.push(new IntConstant(0));
				}
				pc.advance();				
			} catch (ClassNotFoundException e) {
				throw new SymbolicExecutionException("Error while looking up classes S="+S+", and T="+T, e);
			}			
		} else if (var instanceof ArrayReference) {
			throw new SymbolicExecutionException("Implement array referene instanceof instruction!");
		} else {
			throw new SymbolicExecutionException("Variable must be Objectref or Arrayref, but was: " +var);
		}
	}
	
	@Override
	public void visitCHECKCAST(CHECKCAST obj) {
		Variable var = stack.pop(Variable.class);
		
		if(var instanceof ObjectReference) {
			ObjectReference objRef = (ObjectReference)var;
			ObjectType objType = obj.getLoadClassType(cpg);
			
			String S = objRef.getClassName();
			String T = objType.getClassName();
			
			try {
				JavaClass jcS = Repository.lookupClass(S);
				JavaClass jcT = Repository.lookupClass(T);
				
				if(jcS.instanceOf(jcT)) {
					stack.push(var);
					pc.advance();
				} else {
					throw new SymbolicExecutionException("here we must throw a ClassCastException symbolically");
				}			
			} catch (ClassNotFoundException e) {
				throw new SymbolicExecutionException("Error while looking up classes S="+S+", and T="+T, e);
			}			
		} else if (var instanceof ArrayReference) {
			throw new SymbolicExecutionException("Implement array referene instanceof instruction!");
		} else {
			throw new SymbolicExecutionException("Variable must be Objectref or Arrayref, but was: " +var);
		}
	}
}
