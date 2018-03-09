//package w.bean;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.bcel.Repository;
//import org.apache.bcel.classfile.AnnotationEntry;
//import org.apache.bcel.classfile.JavaClass;
//import org.chocosolver.solver.search.loop.Reporting;
//
//import m.state.StateElement;
//import m.var.ObjectReference;
//import m.vm.heap.Heap;
//import m.vm.heap.SymbolicObject;
//import w.exception.AnalyzeException;
//
//public class BeanPool extends StateElement{
//
//	private static final long serialVersionUID = 1L;
//
//	private Map<String, SymbolicObject> requestBeans;
//	private Map<String, SymbolicObject> sessionsBeans;
//	
//	private final Heap heap;
//	private final BeanManager beanManager;
//	
//	public BeanPool(BeanManager beanManager, Heap heap) {
//		this.heap = heap;
//		this.beanManager = beanManager;
//		this.requestBeans = new HashMap<>();
//		this.sessionsBeans = new HashMap<>();
//	}
//	
//	public SymbolicObject getBean(String beanName, Heap heap) throws ClassNotFoundException {
//		String beanClassName = beanManager.getBeanClassName(beanName);
//		JavaClass beanClass = Repository.lookupClass(beanClassName);
//		BeanType beanType = getBeanType(beanClass);
//		switch(beanType) {
//			case REQUEST: {
//				SymbolicObject bean = requestBeans.get(beanName);
//				if(bean == null) {
//					bean = heap.newObject(beanClass);
//					requestBeans.put(beanName, bean);
//				}
//				return bean;
//			}
//			case SESSION: {
//				SymbolicObject bean = sessionsBeans.get(beanName);
//				if(bean == null) {
//					bean = heap.newObject(beanClass);
//					sessionsBeans.put(beanName, bean);
//				}
//				return bean;
//			}
//		}
//		throw new AnalyzeException("Could not identify bean type of class: " + beanClass);
//	}
//	
//	private BeanType getBeanType(JavaClass beanClass) {
//		for(AnnotationEntry a : beanClass.getAnnotationEntries()) {
//			if(a.getAnnotationType().equals("Ljavax/enterprise/context/RequestScoped;")) {
//				return BeanType.REQUEST;
//			}
//			if(a.getAnnotationType().equals("Ljavax/enterprise/context/SessionScoped;")) {
//				return BeanType.SESSION;
//			}
//		}
//		return BeanType.REQUEST; // REQUEST is the default scope
//	}
//
//	public void clearRequestBeans() {
//		this.requestBeans.clear();
//	}
//
//	
//	private enum BeanType {
//		REQUEST, SESSION
//	}
//
//
//	public BeanManager getBeanManager() {
//		return this.beanManager;
//	}
//}
