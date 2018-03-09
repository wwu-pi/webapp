package m.sym.var;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

import m.exe.ex.SymbolicExecutionException;
import m.var.ArrayReference;
import m.var.CharVariable;
import m.var.IntVariable;
import m.var.LongVariable;
import m.var.ObjectReference;
import m.var.Variable;
import m.vm.heap.Heap;

public class VariableGenerator {
	
	/**
	 * Generate a new object-reference in the given heap.
	 */
	public static ObjectReference generateNewObjectReference(String variableName, JavaClass javaClass, Heap heap) {
		return new ObjectReference(variableName, heap.newObject(javaClass)); 
	}
	
	public static ArrayReference generateNewArrayReference(String variableName, Heap heap, String arrayType, int dimensions) {
		return new ArrayReference(variableName, heap.newArray(arrayType, dimensions));
	}

	/**
	 * Generate a new variable of the given type with the given name.
	 */
	public static Variable generateNewVariable(String name, Type type, Heap heap) {
		if(type == Type.INT) {
			return new IntVariable(name);
		}
		
		if(type == Type.LONG) {
			return new LongVariable(name);
		}
		
		if(type == Type.CHAR) {
			return new CharVariable(name);
		}
		
		if(type instanceof ObjectType) {
			ObjectType objType = (ObjectType)type;
			JavaClass javaClass = null;
			try {
				javaClass = Repository.lookupClass(objType.getClassName());
			} catch (ClassNotFoundException e) {
				throw new SymbolicExecutionException("Could not find class with name: " + objType.getClassName());
			}
			return generateNewObjectReference(name, javaClass, heap);
		}
		
		if(type instanceof ArrayType) {
			ArrayType arrType = (ArrayType)type;
			System.out.println("\n\n IRGENDWO HIER NOCH DIE LAENGE ALS CONSTRAINT MIT DRAUF PACKEN!!!!");

			return generateNewArrayReference(name, heap, getArrayType(arrType), arrType.getDimensions());
//			return generateNewArrayReference(name, null, heap);
		}
		
		throw new SymbolicExecutionException("Variable not supported for type: " +type);
	}

	private static String getArrayType(ArrayType arrType) {
		if(arrType.getBasicType().getType() == Type.CHAR.getType()) {
			return "char";
		}
		throw new SymbolicExecutionException("Get the array type!");
	}

	
}
