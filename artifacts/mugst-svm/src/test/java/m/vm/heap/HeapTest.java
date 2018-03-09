package m.vm.heap;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.junit.BeforeClass;
import org.junit.Test;

import m.var.IntVariable;

public class HeapTest {

	protected static JavaClass stringClass;
	
	@BeforeClass
	public static void init() throws ClassNotFoundException {
		stringClass = Repository.lookupClass(String.class.getName());
	}
	
	@Test
	public void copyHeap() throws IOException {
		Heap heap = new Heap();
		assertEquals(0, heap.getHeapObjects().size());
		SymbolicObject string1 = heap.newObject(stringClass);
		SymbolicObject string2 = heap.newObject(stringClass);
		SymbolicObject string3 = heap.newObject(stringClass);
		assertEquals(3, heap.getHeapObjects().size());
		
		assertTrue(string1.getValue("foobar") == null);
		
		IntVariable intVar = new IntVariable("foooooo");
		string1.setValue("foobar", intVar);
		assertEquals(intVar, string1.getValue("foobar"));
		
		
	}
}
