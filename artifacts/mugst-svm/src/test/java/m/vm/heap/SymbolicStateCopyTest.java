package m.vm.heap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.junit.Test;

import m.javaee.JavaEEHeap;
import m.state.CopiedSymbolicState;
import m.state.SymbolicState;
import m.var.IntVariable;

public class SymbolicStateCopyTest {

	@Test
	public void testCopyState() throws ClassNotFoundException, IOException {
		JavaClass stringClass = Repository.lookupClass(String.class.getName());

		SymbolicState state = new SymbolicState(new JavaEEHeap(), null, null, null);
		
		Heap heap = state.getHeap();
		assertEquals(0, heap.getHeapObjects().size());
		SymbolicObject string1 = heap.newObject(stringClass);
		SymbolicObject string2 = heap.newObject(stringClass);
		SymbolicObject string3 = heap.newObject(stringClass);
		assertEquals(3, heap.getHeapObjects().size());
		
		assertTrue(string1.getValue("foobar") == null);
		
		IntVariable intVar = new IntVariable("a");
		string1.setValue("foobar", intVar);
		assertEquals(intVar, string1.getValue("foobar"));
		
		CopiedSymbolicState copiedState = state.copy(); // -------- COPY STATE HERE
		
		SymbolicObject string4 = heap.newObject(stringClass);
		SymbolicObject string5 = heap.newObject(stringClass);
		assertEquals(5, heap.getHeapObjects().size());
		
		IntVariable intVar_changed = new IntVariable("b");
		string1.setValue("foobar", intVar_changed);
		assertEquals(intVar_changed, string1.getValue("foobar"));
		
		
		SymbolicState state2 = SymbolicState.load(copiedState);
		heap = state2.getHeap();
		assertEquals(3, heap.getHeapObjects().size());
		
		assertTrue(heap.getHeapObjects().contains(string1));
		
		for(SymbolicObject obj : heap.getHeapObjects()) {
			if(obj == string1) {
				assertEquals(intVar, string1.getValue("foobar"));
				assertNotEquals(intVar_changed, string1.getValue("foobar"));
			}
		}
	}
}
