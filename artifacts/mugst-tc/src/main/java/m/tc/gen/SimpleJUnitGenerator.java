package m.tc.gen;

import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import m.cstr.ConstraintSolution;
import m.tc.gen.meta.TestCaseGenerator;
import m.var.IntVariable;
import m.var.Variable;

public class SimpleJUnitGenerator extends TestCaseGenerator {

	private static int generatedMethods = 0;
	
	protected String indent = "\t";
	protected String testMethodName = "testMethod"+generatedMethods++;
	
	public SimpleJUnitGenerator(MethodGen mut, Variable[] methodArguments, Variable expectedReturnValue, ConstraintSolution solution) {
		super(mut, methodArguments, expectedReturnValue, solution);
	}

	@Override
	public void genearte() {
		StringBuilder sb = new StringBuilder();
		sb.append(indent+"public void " + testMethodName + "() {\n");
		
		String oldIndent = indent;
		indent+="\t";
		
		generateMUTArgs(indent, sb);
		
		String className = this.mut.getClassName();
		if(!this.mut.isStatic()) {
			sb.append(indent+className+ " cut = new " + className + "();\n");
			sb.append(indent+"cut.");
		} else {
			sb.append(indent+className+".");
		}
		
		sb.append(mut.getName() + "(");
		generateMUTArgsList(sb);
		sb.append(");\n");
		
		generateExpectedResultCheck(indent, sb);
		
		indent=oldIndent;
		
		
		System.out.println(sb.toString());
	}

	private void generateExpectedResultCheck(String indent, StringBuilder sb) {
		String res = "<result> : " + this.expectedReturnValue.getClass().getSimpleName();
		if(expectedReturnValue instanceof IntVariable) {
			Integer intValue = this.solution.getIntValue((IntVariable)expectedReturnValue);
			if(intValue != null) {
				res = intValue+"";
			}
		}
		sb.append(indent+"// expected result = " + res + "\n");
	}

	private void generateMUTArgs(String indent, StringBuilder sb) {
		String[] argNames = mut.getArgumentNames();
		Type[] argTypes = mut.getArgumentTypes();
		for(int i=0; i<argTypes.length; i++) {
			Variable argVar = this.methodArguments[i];
			if(argVar instanceof IntVariable) {
				Integer intValue = this.solution.getIntValue((IntVariable)argVar);
				if(intValue != null) {
					sb.append(indent+"// " + argNames[i]  + ": integer with value=" + intValue + "\n");
				}
			}
			sb.append(indent+argNames[i]+" = null; // argument variable: " + argVar + " -> type: " + argVar.getClass().getSimpleName() + "\n");
		}
	}
	
	private void generateMUTArgsList(StringBuilder sb) {
		int i=0;
		String[] argNames = mut.getArgumentNames();
		for(String argName : argNames) {
			sb.append(argName);
			if(i<(argNames.length-1)) {
				sb.append(", ");
			}
			i++;
		}
	}

}
