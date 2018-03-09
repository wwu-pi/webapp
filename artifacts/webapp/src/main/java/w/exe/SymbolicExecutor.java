package w.exe;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.chocosolver.solver.variables.IntVar;

import m.cstr.ConstraintManager;
import m.cstr.a.AllDifferent;
import m.cstr.meta.BoolOp;
import m.cstr.p.IntXopY;
import m.javaee.JavaEEHeap;
import m.javaee.db.SymbolicObjectDatabase;
import m.javaee.var.SymbolicQuery;
import m.tc.TestCase;
import m.var.ArrayReference;
import m.var.CharConstant;
import m.var.IntConstant;
import m.var.IntVariable;
import m.var.LongVariable;
import m.var.ObjectReference;
import m.var.Variable;
import m.vm.Frame;
import m.vm.heap.SymbolicArray;
import m.vm.heap.SymbolicObject;
import w.bean.BeanManager;
import w.exception.SymbolicExecutionException;
import w.exe.action.CommandButtonClick;
import w.exe.action.LinkClick;
import w.exe.action.TextInput;
import w.exe.action.assertion.AssertDataTableEntries;
import w.exe.action.assertion.AssertInputFieldValue;
import w.exe.action.meta.Action;
import w.sym.state.CopiedState;
import w.sym.state.SymbolicState;
import w.tc.TestCaseGenerator;
import w.xhtml.WebPageManager;
import w.xhtml.ui.WebPage;
import w.xhtml.ui.component.CommandButton;
import w.xhtml.ui.component.DataTable;
import w.xhtml.ui.component.Form;
import w.xhtml.ui.component.InputText;
import w.xhtml.ui.component.Link;

public class SymbolicExecutor {

	private static int MAX_STEPS;

	protected final TestCaseGenerator tcgen;
	
	protected final WebPageManager webPageManager;
	protected final BeanManager beanManager;
	
	protected final Stack<SymbolicState> states;
	
	protected SymbolicObjectDatabase database;
	
	// coverage strategies
//	protected final CoverPages coverPages;
	
	public SymbolicExecutor(WebPageManager webPageManager, SymbolicObjectDatabase database, BeanManager beanManager, String outputDirectory, int M) {
		MAX_STEPS = M;
		this.webPageManager = webPageManager;
		this.beanManager = beanManager;
		this.database = database;
		this.tcgen = new TestCaseGenerator(outputDirectory);
		this.states = new Stack<>();
//		this.coverPages = new CoverPages(1, webPageManager); // cover every page at least 1 times
	}

	public void start(WebPage startPage) {
		Instant start = Instant.now(); // measure the execution time -> START
		
		System.out.println("*** Start symbolic execution on page: " + startPage);
		
		SymbolicState startState = new SymbolicState(startPage, database, webPageManager, beanManager);
		states.push(startState);
		
		run();
		
		Instant end = Instant.now(); // measure the execution time -> END
		Duration executionTime = Duration.between(start, end);
		this.tcgen.writeStatsEnd(executionTime, MAX_STEPS);
	}
	
	
	private void run() {
		System.out.println("\n-----------------------------------------------------------\n");
		while(!this.states.isEmpty()) {
			SymbolicState nextState = this.states.pop();
			
 			if(nextState.getSteps() > MAX_STEPS) {
				System.out.println("########### STOP EXECUTION - MAXIMUM STEPS");
				generateTestCase(nextState);
				continue;
			}
			
			System.out.println("################ Execute state: " + nextState);
			try {
				execute(nextState);
			} catch (Exception e) {
				throw new SymbolicExecutionException("Could not execute state: " + nextState, e);
			}
		}
	}

	private void execute(SymbolicState state) throws IOException, ClassNotFoundException {
		System.out.println("Current page: " +state.getCurrentPage());
		
		checkAssertions(state);
		
		Set<LinkClick> uncoveredSimpleLinkClicks = getUncoveredLinkClicks(state);
		
		Set<CommandButtonClick> uncoveredFormCommandButtonClicks = getUncoveredFormCommandButtonClicks(state);
		
		Set<CommandButtonClick> uncoveredDataTableCommandButtonClicks = getUncoveredDataTableCommandButtonClicks(state);
		
		if(    uncoveredSimpleLinkClicks.size()             == 0 
			&& uncoveredFormCommandButtonClicks.size()      == 0
			&& uncoveredDataTableCommandButtonClicks.size() == 0) {
			
			System.out.println("******* FINISHED : " + state);
			System.out.println("*********** TC: " + state.getActionSequence());
			
			generateTestCase(state);
			
			return;
		}
		
		CopiedState copiedState = CopiedState.copyState(state); // state before executing any action
		
		handleSimpleLinks(uncoveredSimpleLinkClicks, copiedState);

		handleFormButtonClicks(uncoveredFormCommandButtonClicks, copiedState);
		
		handleDataTableButtonClicks( uncoveredDataTableCommandButtonClicks, copiedState);
	}


	private void checkAssertions(SymbolicState state) {
		System.out.println(state.getActionSequence());
		if(state.getPreviousPage() != null) {
			for(DataTable dt : state.getPreviousPage().getDataTables()) {
				Action lastAction = state.getActionSequence().getActions().getLast(); // the action that brought us here
				lastAction.addAssertion(new AssertDataTableEntries(dt, dt.getData()));
				System.out.println(dt.getData());
				System.out.println("***");
//				throw new RuntimeException("BUILD ASSERTION HERE");
			}
			
			for(Entry<Form, Set<InputText>> e : state.getPreviousPage().getFormInputs().entrySet()) {
				for(InputText inputText : e.getValue()) {
					String value = inputText.getValue();
					if(value != null && value.length() > 3 && format(value).startsWith("#{") && format(value).endsWith("}")) {
						// get the bean that has this value
						value = format(value).substring(2, format(value).length()-1);
						
						String[] p = value.split("\\.");
						
						String beanName   = p[0];
						String fieldName  = p[1];
						SymbolicObject beanObj;
						try {
							JavaClass beanClass = Repository.lookupClass(beanManager.getBeanClassName(beanName));
							beanObj = state.getHeap().getBean(beanName, beanClass);
						} catch (ClassNotFoundException e2) {
							throw new SymbolicExecutionException("Could not find bean: " + beanName, e2);
						}
						ObjectReference inputValue = (ObjectReference)beanObj.getValue(fieldName);
						if(p.length > 2) {
							inputValue = (ObjectReference)inputValue.getValue(p[2]);
						}
						if(inputValue != null) {
							Action lastAction = state.getActionSequence().getActions().getLast();
							lastAction.addAssertion(new AssertInputFieldValue(inputText, inputValue));
						}
					}
				}
			}
		}
	}

	private void generateTestCase(SymbolicState state) {
		tcgen.generate(state);
	}

	public Set<LinkClick> getUncoveredLinkClicks(SymbolicState state) {
		Set<LinkClick> uncoveredClicks = new HashSet<>();
		for(Link simpleLink : state.getCurrentPage().getSimpleLinks()) {
			LinkClick nextAction = new LinkClick(simpleLink);
			if(!state.getCoverage().isCovered(state.getCurrentPage(), nextAction, state.getConstraints())) {
				uncoveredClicks.add(nextAction);
			} else {
				System.out.println("LINK CLICK ALREADY COVERED: " + nextAction);
			}
		}
		return uncoveredClicks;
	}

	private Set<CommandButtonClick> getUncoveredFormCommandButtonClicks(SymbolicState state) {
		Set<CommandButtonClick> uncoveredClicks = new HashSet<>();
		
		for(Form form : state.getCurrentPage().getForms()) {
			Set<CommandButton> formButtons = state.getCurrentPage().getFormButtons().get(form);
			if(formButtons == null) continue;
			for(CommandButton cmdBtn : formButtons) {
				if(!isDataTableCommandButton(state.getCurrentPage(), cmdBtn)) {
					CommandButtonClick nextAction = new CommandButtonClick(form, cmdBtn);
					if(!state.getCoverage().isCovered(state.getCurrentPage(), nextAction, state.getConstraints())) {
						uncoveredClicks.add(nextAction);
					} else {
						System.out.println("BUTTON CLICK ALREADY COVERED: " + nextAction);
					}
				}
			}
		}
		
		return uncoveredClicks;
	}
		
	private boolean isDataTableCommandButton(WebPage webPage, CommandButton commandButton) {
		for(DataTable dataTable : webPage.getDataTables()) {
			for(CommandButton tblBtn : webPage.getDataTableButtons().get(dataTable)) {
				if(tblBtn.equals(commandButton)) return true;
			}
		}
		return false;
	}

	private Set<CommandButtonClick> getUncoveredDataTableCommandButtonClicks(SymbolicState state) {
		Set<CommandButtonClick> uncoveredClicks = new HashSet<>();
		
		for(Form form : state.getCurrentPage().getForms()) {
			Set<CommandButton> formButtons = state.getCurrentPage().getFormButtons().get(form);
			if(formButtons == null) continue;
			for(CommandButton cmdBtn : formButtons) {
				
				for(DataTable dataTable : state.getCurrentPage().getDataTables()) {
					for(CommandButton tblBtn : state.getCurrentPage().getDataTableButtons().get(dataTable)) {
						if(cmdBtn == tblBtn) {
							CommandButtonClick nextAction = new CommandButtonClick(form, cmdBtn);
							if(!state.getCoverage().isCovered(state.getCurrentPage(), nextAction, state.getConstraints())) {
								uncoveredClicks.add(nextAction);
							} else {
								System.out.println("BUTTON CLICK ALREADY COVERED: " + nextAction);
							}
						}
					}
				}
			}
		}
		
		return uncoveredClicks;
	}
	
	
	
	
	
	private void handleSimpleLinks(Set<LinkClick> linkClicks, CopiedState copiedState) throws ClassNotFoundException, IOException {
		for(LinkClick click : linkClicks) {
			SymbolicState state = CopiedState.loadState(copiedState);
			state.updateCopiedStateId();
			
			WebPage linkedPage = state.getWebPageManager().getPageByName(click.getLink().getOutcome());
						
			state.getCoverage().addCoverage(state.getCurrentPage(), click, state.getConstraints());
			state.getCoverage().addVisitedPage(linkedPage);
			
			state.addAction(click);
			state.updateCurrentPage(linkedPage);
			this.states.push(state);	
			
			System.out.println("*** linking to page: " + linkedPage);
		}
	}


	private void handleFormButtonClicks(Set<CommandButtonClick> buttonClicks, CopiedState copiedState) throws ClassNotFoundException, IOException {
		JavaClass stringClass = Repository.lookupClass(String.class.getName());
		
		for(CommandButtonClick click : buttonClicks) {
			SymbolicState state = CopiedState.loadState(copiedState);
			state.updateCopiedStateId();
			
			Map<String, Variable> beanInitializationValues = new HashMap<>();
			// add required input insertion actions
			Set<InputText> inputTexts = state.getCurrentPage().getFormInputs().get(click.getForm());
			if(inputTexts != null) {
				for(InputText input : inputTexts) {
					if(input.isRequired()) {
						SymbolicObject stringSymObj = state.getHeap().newObject(stringClass);
						SymbolicArray stringSymObjValue = state.getHeap().newArray("char", 1);
						ArrayReference refValue = new ArrayReference("INPUT_("+input.hashCode()+").value", stringSymObjValue);
						stringSymObj.setValue("value", refValue);
						state.getConstraints().addConstraint(new IntXopY(refValue.getLength(), new IntConstant(0), BoolOp.GE));
						ObjectReference stringRef = new ObjectReference("INPUT_("+input.hashCode()+")", stringSymObj);
						beanInitializationValues.put(format(input.getValue()), stringRef);
						TextInput textInput = new TextInput(input, click.getForm(), stringRef);
						state.addAction(textInput);
						state.getCoverage().addCoverage(state.getCurrentPage(), textInput, state.getConstraints());
					}
				}
			}
		
			String action = click.getButton().getAction();
			if(isBeanMethodCall(action)) {
				String[] p = parseBeanMethodCall(action);
				String beanName   = p[0];
				String methodName = p[1];
				String arguments  = p[2];
				
				JavaClass beanClass = Repository.lookupClass(beanManager.getBeanClassName(beanName));
				SymbolicObject beanObj = state.getHeap().getBean(beanName, beanClass);
				for(Entry<String, Variable> beanValue : beanInitializationValues.entrySet()) {
					if(beanValue.getKey().startsWith("#{") && beanValue.getKey().endsWith("}")) {
						String[] s = beanValue.getKey().substring(2, beanValue.getKey().length()-1).split("\\.");
						String valueBeanName = s[0];
						String valueFieldName= s[1];
						if(beanName.equals(valueBeanName)) {
							beanObj.setValue(valueFieldName, beanValue.getValue());
						}
					}
				}
				
				Variable[] argVariables = null;
				if(arguments != null) {
					String[] s = arguments.split("\\.");
					if(s.length == 2) {
						JavaClass argBeanClass = Repository.lookupClass(beanManager.getBeanClassName(s[0]));
						SymbolicObject argBeanName = state.getHeap().getBean(s[0], argBeanClass);
						argVariables = new Variable[]{argBeanName.getValue(s[1])};
						if(argVariables[0] == null) {
							throw new RuntimeException("Should not be null");
						}
					}				
				}
				handleBeanExecution(state, click, beanName, methodName, argVariables);
			} else {
				WebPage linkedPage = state.getWebPageManager().getPageByName(action);
				
				state.getCoverage().addCoverage(state.getCurrentPage(), click, state.getConstraints());
				state.getCoverage().addVisitedPage(linkedPage);
				
				state.addAction(click);
				state.updateCurrentPage(linkedPage);
				this.states.push(state);	
				
				System.out.println("*** clicking to page: " + linkedPage);
			}
		}
	}	

	private void handleBeanExecution(SymbolicState state, Action action, String beanName, String methodName, Variable... arguments) throws ClassNotFoundException, IOException {
		CopiedState copiedStateBeforeSymExe = CopiedState.copyState(state);
		symbolicallyExecuteBean(state, beanName, methodName, arguments);
		
		for(TestCase tc : m.exe.SymbolicExecutor.getTestCases()) {
			m.javaee.state.JavaEESymbolicState symExeState = m.javaee.state.JavaEESymbolicState.load(tc.getCopiedState());
			System.out.println("\tconstraints: " + symExeState.getConstraintStack());
			System.out.println("\texpectedResult: " + tc.getExpectedReturnValue());
			System.out.println("\theap: " + symExeState.getHeap());
			System.out.println("\theap objects:");
			for(SymbolicObject symobj : symExeState.getHeap().getHeapObjects()) {
				System.out.println("\t\t"+symobj);
				if(symobj.getJavaClassName().equals("java.lang.String") && symobj.getValue("value") != null) {
					System.out.println("\t\t\tvalue = "+((ArrayReference)symobj.getValue("value")).getReferencedArray());
				}
			}
			
			SymbolicState stateBeforeExe = CopiedState.loadState(copiedStateBeforeSymExe);
			// add constraints from the symbolic execution of the backing bean!
			stateBeforeExe.getConstraints().addConstraint(symExeState.getConstraintStack().getConstraints());
			stateBeforeExe.updateHeap(symExeState.getHeap());
			
			
			String returnedValue = getReturnedValue(tc);
			WebPage linkedPage = stateBeforeExe.getWebPageManager().getPageByName(returnedValue);
			
			stateBeforeExe.getCoverage().addCoverage(stateBeforeExe.getCurrentPage(), action, stateBeforeExe.getConstraints());
			stateBeforeExe.getCoverage().addVisitedPage(linkedPage);
			
			stateBeforeExe.addAction(action);
			stateBeforeExe.updateCurrentPage(linkedPage);
			addAssertions(action, linkedPage, stateBeforeExe);

			
			this.states.push(stateBeforeExe);
			
			
			System.out.println("*** bean method navigates to page: " + linkedPage);
		}
	}

	private void handleDataTableButtonClicks(Set<CommandButtonClick> buttonClicks, CopiedState copiedState) throws ClassNotFoundException, IOException {
	
	for(CommandButtonClick click : buttonClicks) {
			SymbolicState state = CopiedState.loadState(copiedState);
			state.updateCopiedStateId();
			
			DataTable dataTable = null;
			for(Entry<DataTable, Set<CommandButton>> e : state.getCurrentPage().getDataTableButtons().entrySet()) {
				for(CommandButton cb : e.getValue()) {
					if(cb.equals(click.getButton())) {
						dataTable = e.getKey(); break;
					}
				}
			}
		
			String[] p = parseBeanMethodCall(dataTable.getValue());
			String beanName   = p[0];
			String methodName = p[1];
			String arguments  = p[2];
			
			symbolicallyExecuteBean(state, beanName, methodName, null);
			
			for(TestCase tc : m.exe.SymbolicExecutor.getTestCases()) {
				ObjectReference returnValue = (ObjectReference)tc.getExpectedReturnValue();
				SymbolicQuery query = state.getDatabase().getQuery(returnValue);
				ArrayReference classNameValueArray = ((ArrayReference)((ObjectReference)query.getResultClass().getValue("name")).getValue("value"));
				StringBuffer sb = new StringBuffer();
				for(int i=0; i<1000; i++) {
					Variable var = classNameValueArray.getElement(i);
					if(var == null) break;
					CharConstant cc = (CharConstant)var;
					sb.append((char)cc.getIntValue());
				}
				String className = sb.toString();
				
				JavaClass entityJC = Repository.lookupClass(className);
				SymbolicObject entityObj = state.getHeap().newObject(entityJC);
				
				// check the disabled condition
				if(click.getButton().getDisabled() != null) {
					String disabledCondition = format(click.getButton().getDisabled());
					disabledCondition = disabledCondition.substring(2, disabledCondition.length()-1);
					String[] parts = disabledCondition.split(" ");
					String lhs = parts[0];
					String boolOp = parts[1];
					String rhs = parts[2];
					
					String[] lhsSplit = lhs.split("\\.");
					if(lhsSplit != null && lhsSplit.length == 2) {
						if(lhsSplit[0].equals(format(dataTable.getVar()))) {
							// there is a condition on the variable
//							Variable var = entityObj.getValue(lhsSplit[1]);
							if(boolOp.equals("!=")) {
								String stringValue = rhs.substring(1, rhs.length()-1);
								SymbolicObject stringConstObj = state.getHeap().getStringConstantObject(stringValue);
								
								ArrayReference valueArray = (ArrayReference) stringConstObj.getValue("value");
								IntXopY lengthConstraint = new IntXopY(valueArray.getLength(), new IntConstant(stringValue.length()), BoolOp.EQ);
								state.getConstraints().addConstraint(lengthConstraint);
								
								ObjectReference stringConstant = new ObjectReference(stringConstObj.toString(), stringConstObj);
								entityObj.setValue(lhsSplit[1], stringConstant);
							}
						}
					}
				}
				
				// add required attributes to entity
				// add id value
				entityObj.setValue("id", new LongVariable(entityObj.hashCode()+"_ID"));
				
				state.getDatabase().addRequiredObject(className, entityObj);
				state.getDatabase().persist(className, entityObj); // we need to operate on it
				
				dataTable.addData(entityObj);

				// now execute the command button to see where it goes to...
				executeDataTableCommandButton(CopiedState.copyState(state), click, new ObjectReference("var"+entityObj.hashCode(), entityObj));
			}
		}
	}
	
	private void executeDataTableCommandButton(CopiedState copiedState, CommandButtonClick click, Variable... arguments) throws ClassNotFoundException, IOException {
		SymbolicState state = CopiedState.loadState(copiedState);
		state.updateCopiedStateId();
		
		String action = click.getButton().getAction();
		if(isBeanMethodCall(action)) {
			String[] p = parseBeanMethodCall(action);
			String beanName   = p[0];
			String methodName = p[1];
//			String arguments  = p[2];

			handleBeanExecution(state, click, beanName, methodName, arguments);
		} else {
			WebPage linkedPage = state.getWebPageManager().getPageByName(action);
			
			state.getCoverage().addCoverage(state.getCurrentPage(), click, state.getConstraints());
			state.getCoverage().addVisitedPage(linkedPage);
			
			state.addAction(click);
			state.updateCurrentPage(linkedPage);
			
			this.states.push(state);	
			
			System.out.println("*** clicking to page: " + linkedPage);
		}
	}
	
	private void addAssertions(Action action, WebPage page, SymbolicState state) {
		// assert form inputs
		for(Form form : page.getFormInputs().keySet()) {
			for(InputText inputText : page.getFormInputs().get(form)) {
				String value = inputText.getValue();
				if(value != null && value.length() > 3 && format(value).startsWith("#{") && format(value).endsWith("}")) {
					// get the bean that has this value
					value = format(value).substring(2, format(value).length()-1);
					
					String[] p = value.split("\\.");
					
					String beanName   = p[0];
					String fieldName  = p[1];
					SymbolicObject beanObj;
					try {
						JavaClass beanClass = Repository.lookupClass(beanManager.getBeanClassName(beanName));
						beanObj = state.getHeap().getBean(beanName, beanClass);
					} catch (ClassNotFoundException e) {
						throw new SymbolicExecutionException("Could not find bean: " + beanName, e);
					}
					ObjectReference inputValue = (ObjectReference)beanObj.getValue(fieldName);
					if(p.length > 2) {
						inputValue = (ObjectReference)inputValue.getValue(p[2]);
					}
					if(inputValue != null) {
						action.addAssertion(new AssertInputFieldValue(inputText, inputValue));
					}
				}
			}
		}
		
		// assert data table entries
		for(DataTable dataTable : page.getDataTables()) {
			
			dataTable.getData();
			
//			symbolicallyExecuteBean(state, beanName, methodName, null);
//			
//			for(TestCase tc : m.exe.SymbolicExecutor.getTestCases()) {
//				tc.getExpectedReturnValue();
//			}
		}
	}

	private String getReturnedValue(TestCase tc) {
		Variable expectedReturn = tc.getExpectedReturnValue();
		
		if(expectedReturn instanceof ObjectReference) {
			ObjectReference retObjRef = (ObjectReference)expectedReturn;
			if(retObjRef.getClassName().equals(String.class.getName())) {
				Variable retValueArray = retObjRef.getValue("value");
				if(retValueArray != null ) {
					ArrayReference arrayRef = (ArrayReference)retValueArray;
					IntVariable arrayLengthVar = arrayRef.getLength();
					int length = tc.getSolution().getIntValue(arrayLengthVar);
					char[] stringValues = new char[length];
					for(int i=0; i<length; i++) {
						CharConstant var = (CharConstant)arrayRef.getElement(i); // only constants are supported
						stringValues[i] = (char)var.getIntValue();
					}
					return new String(stringValues);
				}
			}
		}
		return null;
	}

	private boolean isBeanMethodCall(String action) {
		action = format(action);
		return action.startsWith("#{") && action.endsWith("}");
	}
	
	private String format(String action) {
		if(action.startsWith("\"")) action = action.substring(1);
		if(action.endsWith("\"")) action = action.substring(0, action.length()-1);
		return action;
	}
	
	/**
	 * return 0=class-name, 1=method-name, 2=arguments (comma separated)
	 * @param beanMethodCall
	 * @return
	 */
	private String[] parseBeanMethodCall(String beanMethodCall) {
		if(beanMethodCall.startsWith("\"")) beanMethodCall = beanMethodCall.substring(1);
		if(beanMethodCall.endsWith("\"")) beanMethodCall = beanMethodCall.substring(0, beanMethodCall.length()-1);
		
		if(!beanMethodCall.startsWith("#{")) throw new SymbolicExecutionException("A bean method call must have the format:   #{beanName.method(arg1,arg2)} but it had: " +beanMethodCall);
		
		beanMethodCall = beanMethodCall.substring(2, beanMethodCall.length()-1);
		
		
		String args = null;
		if(beanMethodCall.contains("(") && beanMethodCall.contains(")")) {
			String[] s = beanMethodCall.split("\\(");
			String end = s[s.length-1];
			s = end.split("\\)");
			args = s[0];
			int cutIdx = beanMethodCall.indexOf('(');
			beanMethodCall = beanMethodCall.substring(0, cutIdx);
		}
		
		String[] s = beanMethodCall.split("\\.");
				
		String cut = s[0];
		String mut = s[1];
		
		return new String[]{cut, mut, args};
	}
	
	

	private void symbolicallyExecuteBean(SymbolicState state, String beanName, String methodName, Variable... arguments) {
		String beanClassName = beanManager.getBeanClassName(beanName);
		JavaClass beanClass = null;
		try {
			beanClass = Repository.lookupClass(beanClassName);
		} catch (ClassNotFoundException e) {
			throw new SymbolicExecutionException("Could not find bean class: " + beanClassName);
		}
		String get_methodName = "get"+methodName.substring(0, 1).toUpperCase()+methodName.substring(1);
		org.apache.bcel.classfile.Method beanMethod = null;
		for(org.apache.bcel.classfile.Method m : beanClass.getMethods()) {
			if(m.getName().equals(methodName) || m.getName().equals(get_methodName)) {
				beanMethod = m;
				break;
			}
		}
		if(beanMethod == null) 
			throw new RuntimeException("Bean method is null for: beanName="+beanName+", and beanMethod="+beanMethod);
		// start the symbolic execution of the bean method
		MethodGen methodGen = new MethodGen(beanMethod, beanClass.getClassName(), new ConstantPoolGen(beanClass.getConstantPool()));
		m.vm.Method method = new m.vm.Method(methodGen.getClassName(), methodGen.getMethod().getName(), methodGen.getMethod().getSignature());
		int maxLocals = methodGen.getMaxLocals();
		
		SymbolicObject beanObject = state.getHeap().getBean(beanName, beanClass);
		ObjectReference beanRef = new ObjectReference("BEAN_REF("+beanName+")", beanObject);
		
		SymbolicObjectDatabase db = state.getDatabase();
		
		JavaEEHeap heap = state.getHeap();
		ConstraintManager constraintManager = new ConstraintManager(state.getConstraints());
		
		Frame frame = new Frame(method, maxLocals);
		frame.getLocalVars().set(0, beanRef);
		if(arguments != null) {
			for(int i=0; i<arguments.length; i++) {
				frame.getLocalVars().set(i+1, arguments[i]);
			}
		}
		
		m.exe.SymbolicExecutor.resetTestCases();
		m.exe.SymbolicExecutor symbolicExecutor = 
				new m.javaee.exe.JavaEESymbolicExecutor(db, heap, new Stack<>(), constraintManager);
		symbolicExecutor.start(frame);
	}

}
