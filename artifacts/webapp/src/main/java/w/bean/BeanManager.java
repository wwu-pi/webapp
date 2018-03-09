package w.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

import m.state.StateElement;

public class BeanManager extends StateElement {

	private static final long serialVersionUID = 1L;

	/**
	 * The class path (.class files) of the bean-classes.
	 */
	protected final String classPath;
	
	/**
	 * A map containing all found bean classes in the given class path. <br>
	 * key = beanName, value = parsed java class <br>
	 */
	protected Map<String, String> beanClasses;
	
	private static final String ANNO_NAMED = "Ljavax/inject/Named;";
	private static final String ANNO_MANAGED_BEAN = "Ljavax/faces/bean/ManagedBean;";
	
	public BeanManager(String classPath) throws ClassFormatException, IOException {
		this.beanClasses = new HashMap<>();
		this.classPath = classPath;
		analyzeClassPath();
	}
	
	public String getBeanClassName(String beanName) {
		return this.beanClasses.get(beanName);
	}
	
	private void analyzeClassPath() throws ClassFormatException, IOException {
		System.out.println("*** Start searching for backing beans in class path: " + this.classPath);
		createBackingBeanMap(new File(classPath));
	}

	private void createBackingBeanMap(File f) throws ClassFormatException, IOException {
		if(f.isDirectory()) {
			for(File ff : f.listFiles()) {
				createBackingBeanMap(ff);
			}
		} else if(!f.getName().endsWith(".class")) {
			return;
		} else {
			InputStream is = new FileInputStream(f);
			ClassParser cp = new ClassParser(is, f.getName());
			JavaClass jc = cp.parse();
			for(AnnotationEntry a : jc.getAnnotationEntries()) {
				if(a.getAnnotationType().equals(ANNO_NAMED) || a.getAnnotationType().equals(ANNO_MANAGED_BEAN)) {
					String beanName = null;
					if(a.getElementValuePairs().length == 0) {
						String[] splitted = jc.getClassName().split("\\.");
						String className = splitted[splitted.length-1];						
						beanName = className.substring(0,1).toLowerCase() + className.substring(1);
					} else {
						beanName = a.getElementValuePairs()[0].getValue().stringifyValue();
					}
					
					System.out.println("\tfound backing bean with name="+beanName + " in class " + jc.getClassName());
					this.beanClasses.put(beanName, jc.getClassName());
				}
			}
		}
	}
}
