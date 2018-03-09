package m.javaee;

import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.JavaClass;

import m.vm.heap.Heap;
import m.vm.heap.SymbolicObject;

public class JavaEEHeap extends Heap {

	private static final long serialVersionUID = 1L;
	
	private Map<String, SymbolicObject> requestBeans;
	private Map<String, SymbolicObject> sessionsBeans;
	
	public JavaEEHeap() {
		this.requestBeans = new HashMap<>();
		this.sessionsBeans = new HashMap<>();
	}
	
	public SymbolicObject getBean(String beanName, JavaClass beanClass) {
		BeanType beanType = getBeanType(beanClass);
		switch(beanType) {
			case REQUEST: {
				SymbolicObject bean = requestBeans.get(beanName);
				if(bean == null) {
					bean = this.newObject(beanClass);
					requestBeans.put(beanName, bean);
				}
				return bean;
			}
			case SESSION: {
				SymbolicObject bean = sessionsBeans.get(beanName);
				if(bean == null) {
					bean = this.newObject(beanClass);
					sessionsBeans.put(beanName, bean);
				}
				return bean;
			}
		}
		throw new RuntimeException("Could not identify bean type of class: " + beanClass);
	}
	
	private BeanType getBeanType(JavaClass beanClass) {
		for(AnnotationEntry a : beanClass.getAnnotationEntries()) {
			if(a.getAnnotationType().equals("Ljavax/enterprise/context/RequestScoped;")) {
				return BeanType.REQUEST;
			}
			if(a.getAnnotationType().equals("Ljavax/enterprise/context/SessionScoped;")) {
				return BeanType.SESSION;
			}
		}
		return BeanType.REQUEST; // REQUEST is the default scope
	}

	public void clearRequestBeans() {
		this.requestBeans.clear();
	}

	
	private enum BeanType {
		REQUEST, SESSION
	}

	
}
