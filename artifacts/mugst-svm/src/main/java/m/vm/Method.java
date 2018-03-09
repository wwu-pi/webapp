package m.vm;

import java.io.Serializable;

import m.state.StateElement;

public class Method extends StateElement implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected final String className;
	protected final String methodName;
	protected final String methodSignature;
	
	public Method(String className, String methodName, String methodSignature) {
		this.className = className;
		this.methodName = methodName;
		this.methodSignature = methodSignature;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getMethodSignature() {
		return methodSignature;
	}
	
	@Override
	public String toString() {
		return this.className + " # " + this.methodName + " " + this.methodSignature;
	}
		
}
