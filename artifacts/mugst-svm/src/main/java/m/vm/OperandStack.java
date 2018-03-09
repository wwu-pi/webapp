package m.vm;

import java.io.Serializable;
import java.util.Stack;

import m.state.StateElement;
import m.var.Variable;


public class OperandStack extends StateElement implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Stack<Variable> stack;
	
	public OperandStack() {
		this.stack = new Stack<Variable>();
	}
	
	public void push(Variable variable) {
		this.stack.push(variable);
	}

	public Variable pop() {
		return this.stack.pop();
	}
	
	public Variable peek() {
		return this.stack.peek();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T pop(Class<T> clazz) {
		Variable var = this.stack.pop();
		if(var != null && clazz.isAssignableFrom(var.getClass())) {
			return (T)var;
		}
		throw new RuntimeException("Element on stack was not of type " + clazz + ", but rather: "+ var);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T peek(Class<T> clazz) {
		Variable var = this.stack.peek();
		if(var != null && clazz.isAssignableFrom(var.getClass())) {
			return (T)var;
		}
		throw new RuntimeException("Element on stack was not of type " + clazz + ", but rather: "+ var);
	}

	public boolean isEmpty() {
		return this.stack.isEmpty();
	}
	
	@Override
	public String toString() {
		return this.stack.toString();
	}
}
