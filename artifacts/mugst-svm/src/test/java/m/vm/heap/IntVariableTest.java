package m.vm.heap;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import m.var.IntVariable;

public class IntVariableTest {

	@Test
	public void test1() throws IOException {
		IntVariable var = new IntVariable("foobar");
		assertEquals("foobar", var.toString());
	}
}
