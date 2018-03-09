package m;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Set;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.apache.commons.io.FilenameUtils;

import m.cli.CLI;
import m.cstr.ConstraintManager;
import m.cstr.ConstraintStack;
import m.exe.SymbolicExecutor;
import m.exe.ex.SymbolicExecutionException;
import m.exe.visitor.BaseVisitor;
import m.exe.visitor.MethodVisitor;
import m.state.SymbolicState;
import m.sym.var.VariableGenerator;
import m.tc.TestCase;
import m.tc.gen.meta.TestCaseGenerator;
import m.var.ObjectReference;
import m.var.Variable;
import m.vm.Frame;
import m.vm.LocalVariables;
import m.vm.Method;
import m.vm.heap.Heap;


public class Main {

	public static void main(String[] args) throws Exception {
		CLI cli = new CLI(args);
		cli.parse();
		
		System.out.println("*****************************");
		System.out.println("*         M U G S T         *");
		System.out.println("*****************************\n");
		
		new Main().startSymbolicExecution(cli.getClassPath(), cli.getCUT(), cli.getMUTName(), cli.getMUTSignature());
	}

	public Set<TestCase> startSymbolicExecution(String classPath, String cut, String mutName, String mutSignature) throws ClassFormatException, IOException, ClassNotFoundException {
		String pattern = Pattern.quote(System.getProperty("path.separator"));
		String[] classPaths = classPath.split(pattern);
		System.out.println("*** Analyze additional class path: " + Arrays.toString(classPaths));
		for(String cp : classPaths) {
			analyzeClassPath(new File(cp));
		}		
		
		System.out.println("*** Start with symbolic execution...");
		System.out.println("    ...class under test:  " + cut);
		System.out.println("    ...method under test: " + mutName + "  " + mutSignature);
		
		return startSymbolicExecution(cut, mutName, mutSignature);
	}

	private static Set<TestCase> startSymbolicExecution(String cut, String mutName, String mutSignature) throws ClassNotFoundException, IOException {
		Instant start = Instant.now(); // measure the execution time -> START
		
		Heap heap = new Heap();
		Method method = new Method(cut, mutName, mutSignature);
		
		MethodGen methodGen = getMethodGen(method);
		
		Stack<Frame> stackFrame = new Stack<>();
		Frame frame = new Frame(method, methodGen.getMaxLocals());
		buildLocalVariables(frame, heap);
		ConstraintManager initialConstraintManager = new ConstraintManager(new ConstraintStack());
		
		
		SymbolicExecutor symbolicExecutor = new SymbolicExecutor(heap, stackFrame, initialConstraintManager);
		
		symbolicExecutor.start(frame);
		
		Instant end = Instant.now(); // measure the execution time -> END
		
		Duration executionTime = Duration.between(start, end);
		
		System.out.println("******************************************************************************");
		System.out.println("Statistics:");
		System.out.println("\texecution time in nano: " + executionTime.getNano());
		System.out.println("\texecution time in sec:  " + executionTime.getSeconds());
		System.out.println("\tgenerated test cases:   " + SymbolicExecutor.getTestCases().size());
		
		return SymbolicExecutor.getTestCases();
		
//		SymbolicExecutor symbolicExecutor = new SymbolicExecutor();
//		
//		BaseVisitor visitor = new MethodVisitor(symbolicExecutor, heap, frame, initialConstraintManager);
//		
//		symbolicExecutor.run(visitor);
//		
//		System.out.println("\n** EXECTUED THE FIRST \n");
//		
//		int i = 0;
//		while(symbolicExecutor.hasMoreChoices()) {
//			BaseVisitor nextVisitor = symbolicExecutor.generateNextChoiceVisitor();
//			symbolicExecutor.run(nextVisitor);
//			System.out.println("\n** EXECTUED THE #"+i+" VISITOR \n");
//			i++;
//		}
//		
////		while(symbolicExecutor.executeNextChoice()) {
////			System.out.println("blub\n\n");
////		}
	}
	
	private static void buildLocalVariables(Frame frame, Heap heap) throws ClassNotFoundException {
		MethodGen methodGen = getMethodGen(frame.getMethod());
		int offset = 0;
		if(!methodGen.isStatic()) {
			offset = 1;
			ObjectReference thisReference = VariableGenerator.generateNewObjectReference("this", Repository.lookupClass(methodGen.getClassName()), heap);
			frame.getLocalVars().set(0, thisReference);
		}
		Type[] argTypes = methodGen.getArgumentTypes();
		for(int i=0; i<argTypes.length; i++) {
			Variable argVar = VariableGenerator.generateNewVariable("arg"+i, argTypes[i], heap);
			frame.getLocalVars().set(i+offset, argVar);
		}
	}

	private static LocalVariables buildLocalVariables(Method method, Heap heap) throws ClassNotFoundException {
		MethodGen methodGen = getMethodGen(method);
		LocalVariables lv = new LocalVariables(methodGen.getMaxLocals());
		
		int offset = 0;
		if(!methodGen.isStatic()) {
			offset = 1;
			ObjectReference thisReference = VariableGenerator.generateNewObjectReference("this", Repository.lookupClass(methodGen.getClassName()), heap);
			lv.set(0, thisReference);
		}
		
		Type[] argTypes = methodGen.getArgumentTypes();
		for(int i=0; i<argTypes.length; i++) {
			Variable argVar = VariableGenerator.generateNewVariable("arg"+i, argTypes[i], heap);
			lv.set(i+offset, argVar);
		}
		
		return lv;
	}
	
	private static MethodGen getMethodGen(Method method) throws ClassNotFoundException {
		JavaClass jc = Repository.lookupClass(method.getClassName());
		for(org.apache.bcel.classfile.Method m : jc.getMethods()) {
			if(m.getName().equals(method.getMethodName())
			&& m.getSignature().equals(method.getMethodSignature())) {
				return new MethodGen(m, jc.getClassName(), new ConstantPoolGen(jc.getConstantPool()));
			}
		}
		throw new SymbolicExecutionException("Could not find method in class path: " + method);
	}

	private static void analyzeClassPath(File f) throws ClassFormatException, IOException {
		if(!f.exists()) throw new SymbolicExecutionException("Given class path does not exists!");

		if(f.isDirectory()) {
			for(File ff : f.listFiles()) {
				analyzeClassPath(ff);
			}
		} else {
			String extension = FilenameUtils.getExtension(f.getName());
			if(extension.equals("class")) {
				InputStream is = new FileInputStream(f);
				ClassParser cp = new ClassParser(is, f.getName());
				JavaClass jc = cp.parse();
				Repository.addClass(jc);
				System.out.println("   * class successfully added to internal repository:        " + jc.getClassName());
			} else if(extension.equals("jar")) {
				JarFile jarFile = new JarFile(f);
				Enumeration<JarEntry> entries = jarFile.entries();
				while(entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					if (!entry.getName().endsWith(".class")) {
		                continue;
		            }
					ClassParser parser = new ClassParser(f.getAbsolutePath(), entry.getName());
					JavaClass javaClass = parser.parse();
					Repository.addClass(javaClass);
				}
				System.out.println("   * successfully added classes from jar file:        " + f.getName());
				jarFile.close();
			}
		}
	}
}
