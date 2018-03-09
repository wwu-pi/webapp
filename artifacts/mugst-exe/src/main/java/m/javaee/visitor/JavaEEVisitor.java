package m.javaee.visitor;

import java.util.HashSet;
import java.util.Set;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESTATIC;

import m.cstr.ConstraintManager;
import m.cstr.a.AllDifferent;
import m.exe.SymbolicExecutor;
import m.exe.ex.SymbolicExecutionException;
import m.exe.visitor.MethodVisitor;
import m.javaee.db.SymbolicObjectDatabase;
import m.javaee.var.SymbolicQuery;
import m.sym.var.VariableGenerator;
import m.var.IntVariable;
import m.var.ObjectReference;
import m.var.Variable;
import m.vm.Frame;
import m.vm.heap.Heap;
import m.vm.heap.SymbolicObject;

public class JavaEEVisitor extends MethodVisitor {
	
	protected SymbolicObjectDatabase database;

	public JavaEEVisitor(SymbolicExecutor symbolicExecutor, SymbolicObjectDatabase database, Heap heap, Frame frame, ConstraintManager constraintManager) {
		super(symbolicExecutor, heap, frame, constraintManager);
		this.database = database;
	}
	
	@Override
	public void visitINVOKESTATIC(INVOKESTATIC obj) {
		String className = obj.getClassName(cpg);
		String methodName = obj.getMethodName(cpg);
		String methodSignature = obj.getSignature(cpg);
		
		if(className.equals("java.lang.Long")
				&& methodName.equals("valueOf")
				&& methodSignature.equals("(J)Ljava/lang/Long;")) {
			pc.advance();
		} else {
			super.visitINVOKESTATIC(obj);
		}
	}
	
	@Override
	public void visitINVOKEINTERFACE(INVOKEINTERFACE obj) {
		String className = obj.getClassName(cpg);
		String methodName = obj.getMethodName(cpg);
		String methodSignature = obj.getSignature(cpg);
		
		if(className.equals("javax.persistence.EntityManager")
				&& methodName.equals("createQuery")
				&& methodSignature.equals("(Ljava/lang/String;Ljava/lang/Class;)Ljavax/persistence/TypedQuery;")) {
			ObjectReference resultClass = stack.pop(ObjectReference.class);
			ObjectReference qlString = stack.pop(ObjectReference.class);
			stack.pop(); // entity manager reference...
			stack.push(new SymbolicQuery("symbolic-query("+qlString+")", qlString, resultClass));
			pc.advance();
			return;
		}
		
		if(className.equals("javax.persistence.TypedQuery")
				&& methodName.equals("getResultList")
				&& methodSignature.equals("()Ljava/util/List;")) {
			JavaClass arrayListClass = null;
			try {
				arrayListClass = Repository.lookupClass(java.util.ArrayList.class.getName());
			} catch (ClassNotFoundException e) {
				throw new SymbolicExecutionException("Could not find java.util.ArrayList in class path!");
			}
			ObjectReference resultList = VariableGenerator.generateNewObjectReference("symbolic-query-result", arrayListClass, heap);
			SymbolicQuery symQuery = (SymbolicQuery)stack.pop(); // pop the query from the stack
			database.setQueryResult(symQuery, resultList);
			stack.push(resultList);
			pc.advance();
			return;
		}
		
		if(className.equals("javax.persistence.EntityManager")
				&& methodName.equals("persist")
				&& methodSignature.equals("(Ljava/lang/Object;)V")) {
			ObjectReference entityRef = stack.pop(ObjectReference.class);
			this.database.persist(entityRef.getClassName(), entityRef.getReferencedObject());
			
//			// currently, only for integer id values a uniquness is implemented
//			Set<IntVariable> idValues = new HashSet<>();
//			for(SymbolicObject e : this.database.selectAll(className)) {
//				Variable idVal = this.database.getId(e);
//				if(idVal instanceof IntVariable) {
//					idValues.add((IntVariable)idVal);
//				}
//			}
//			this.constraintManager.addConstraint(new AllDifferent(idValues));
			
			pc.advance();
			return;
		}
		
		if(className.equals("javax.persistence.EntityManager")
				&& methodName.equals("find")
				&& methodSignature.equals("(Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;")) {
			Variable id = stack.pop();
			Variable type = stack.pop();
			stack.pop(); // Entity Manager reference
			SymbolicObject entityObj = this.database.getObjectById(type, id);
			if(entityObj != null) {
				stack.push(new ObjectReference("findEntity"+entityObj.hashCode(), entityObj));
			} else {
				stack.push(new ObjectReference("findEntity_NULL", null));
			}
			
			pc.advance();
			return;
		}
		
		if(className.equals("javax.persistence.EntityManager")
				&& methodName.equals("flush")
				&& methodSignature.equals("()V")) {
			pc.advance();
			return;
		}
		
		throw new SymbolicExecutionException("Implement me");
	}

}
