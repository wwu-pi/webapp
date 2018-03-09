package m.cstr;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import m.cstr.meta.BoolOp;
import m.cstr.p.IntXopY;
import m.state.CopiedSymbolicState;
import m.state.SymbolicState;
import m.var.IntConstant;
import m.var.IntVariable;

public class ConstraintManagerTest {

	@Test
	public void testSatisfiablity() {
		IntVariable x = new IntVariable("x");
		IntVariable y = new IntVariable("y");
		
		ConstraintStack cs = new ConstraintStack();
		// x >= 0
		cs.addConstraint(new IntXopY(x, new IntConstant(0), BoolOp.GE));
		// x = y
		cs.addConstraint(new IntXopY(x, y, BoolOp.EQ));
		// y > 40
		cs.addConstraint(new IntXopY(y, new IntConstant(40), BoolOp.GT));
		
		// generate constraint manager from the three constraints above
		ConstraintManager cm = new ConstraintManager(cs);
		
		// x > 50
		IntXopY c1 = new IntXopY(x, new IntConstant(50), BoolOp.GT);
		assertTrue(cm.isSatisfiable(c1));
		// only the three initial constraints must be in constraint store
		assertEquals(3, cm.getConstraintStack().constraints.size());
		assertFalse(cm.getConstraintStack().constraints.contains(c1));
		
		// x < 30 -> cannot work!
		IntXopY c2 = new IntXopY(x, new IntConstant(30), BoolOp.LT);
		assertFalse(cm.isSatisfiable(c2));
		// only the three initial constraints must be in constraint store
		assertEquals(3, cm.getConstraintStack().constraints.size());
		assertFalse(cm.getConstraintStack().constraints.contains(c1));
		assertFalse(cm.getConstraintStack().constraints.contains(c2));
		
		// solve the current solution
		ConstraintSolution solution = cm.solve();
		int xValue = solution.getIntValue(x);
		int yValue = solution.getIntValue(y);
		
		assertEquals(xValue, yValue);
		assertTrue(xValue > 40);
		assertTrue(yValue > 40);
	}
	
	
	@Test
	public void testCopyState() throws IOException, ClassNotFoundException {
		IntVariable x = new IntVariable("x");
		IntVariable y = new IntVariable("y");
		
		ConstraintStack cs = new ConstraintStack();
		ConstraintManager cm = new ConstraintManager(cs);
		SymbolicState state = new SymbolicState(null, null, null, cs);
		
		// we add three constraints...
		// x >= 0
		Constraint c1 = new IntXopY(x, new IntConstant(0), BoolOp.GE);
		cm.addConstraint(c1);
		// x = y
		Constraint c2 = new IntXopY(x, y, BoolOp.EQ);
		cm.addConstraint(c2);
		// y > 40
		Constraint c3 = new IntXopY(y, new IntConstant(40), BoolOp.GT);
		cm.addConstraint(c3);
		
		// a new branching constraint comes...
		// x > 50
		IntXopY branchingTrue = new IntXopY(x, new IntConstant(50), BoolOp.GT);
		IntXopY branchingFalse = branchingTrue.negate();
		
		// both constraints can be satisfied
		assertTrue(cm.isSatisfiable(branchingTrue));
		assertTrue(cm.isSatisfiable(branchingFalse));
		
		// constraint stack still has the initial three constraints
		assertEquals(3, state.getConstraintStack().constraints.size());
		
		// we copy the state
		CopiedSymbolicState copiedState = state.copy();
		
		// continue with true branch...
		cm.addConstraint(branchingTrue);
		assertEquals(4, state.getConstraintStack().constraints.size());

		// check solution for TRUE branch...
		ConstraintSolution solutionTRUE = cm.solve();
		int xValueTRUE = solutionTRUE.getIntValue(x);
		int yValueTRUE = solutionTRUE.getIntValue(y);
		
		assertEquals(xValueTRUE, yValueTRUE);
		assertTrue(xValueTRUE > 40);
		assertTrue(yValueTRUE > 40);
		assertTrue(xValueTRUE > 50);
		assertTrue(yValueTRUE > 50);
		
		// now, we create new constraint manager from the copied state
		SymbolicState restoredState = SymbolicState.load(copiedState);
		assertEquals(3, restoredState.getConstraintStack().constraints.size());
		assertTrue(restoredState.getConstraintStack().constraints.contains(c1));
		assertTrue(restoredState.getConstraintStack().constraints.contains(c2));
		assertTrue(restoredState.getConstraintStack().constraints.contains(c3));
		cm = new ConstraintManager(restoredState.getConstraintStack());
		
		// we want to continue execution on FALSE branch...
		cm.addConstraint(branchingFalse);
		assertEquals(4, restoredState.getConstraintStack().constraints.size());
		assertEquals(4, cm.getConstraintStack().constraints.size());
		
		// check solution for FALSE branch...
		ConstraintSolution solutionFALSE = cm.solve();
		int xValueFALSE = solutionFALSE.getIntValue(x);
		int yValueFALSE = solutionFALSE.getIntValue(y);
		
		assertEquals(xValueFALSE, yValueFALSE);
		assertTrue(xValueFALSE > 40);
		assertTrue(yValueFALSE > 40);
		assertTrue(xValueFALSE <= 50);
		assertTrue(yValueFALSE <= 50);
	}
}
