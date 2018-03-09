package w.tc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.RuntimeErrorException;

import java.util.Scanner;
import java.util.Set;

import m.cstr.ConstraintManager;
import m.cstr.ConstraintSolution;
import m.cstr.a.AllDifferent;
import m.cstr.meta.BoolOp;
import m.cstr.p.IntXopY;
import m.javaee.db.SymbolicObjectDatabase;
import m.var.ArrayReference;
import m.var.CharConstant;
import m.var.CharVariable;
import m.var.IntConstant;
import m.var.IntVariable;
import m.var.LongConstant;
import m.var.LongVariable;
import m.var.ObjectReference;
import m.var.Variable;
import m.vm.heap.CopiedSymbolicObject;
import m.vm.heap.Heap;
import m.vm.heap.SymbolicObject;
import w.exception.SymbolicExecutionException;
import w.exe.action.CommandButtonClick;
import w.exe.action.LinkClick;
import w.exe.action.TextInput;
import w.exe.action.assertion.AssertDataTableEntries;
import w.exe.action.assertion.AssertInputFieldValue;
import w.exe.action.assertion.meta.Assertion;
import w.exe.action.meta.Action;
import w.exe.cov.Coverage;
import w.sym.state.SymbolicState;
import w.xhtml.ui.WebPage;
import w.xhtml.ui.component.DataTableColumn;
import w.xhtml.ui.component.OutputText;
import w.xhtml.ui.component.meta.UIComponent;

public class TestCaseGenerator {
	
	private static int i = 0;

	private final File outputDirectory;
	
	private File statsFile;
		
	public TestCaseGenerator(String outputDirectoryPath) {
		File outputDirectory = new File(outputDirectoryPath);
		if(!outputDirectory.exists()) {
			Scanner in = new Scanner(System.in);
			System.out.println("**********************************************");
			System.out.println("*** Output directory should be in : " + outputDirectoryPath);
			boolean isOk = false;
			boolean create = false;
			while(!isOk) {
				System.out.print("\tOutput directory does not exists, create it? y/n: ");
				String input = in.nextLine();
				if(input != null) {
					if(input.equals("yes") || input.equals("y")) {
						create = true; isOk = true;
					}
					if(input.equals("no") || input.equals("n")) {
						create = false; isOk = true;
					}
				}
			}
			in.close();
			System.out.println("**********************************************");
			if(!create) {
				System.out.println("** Good bye!");
				System.exit(0);
			} else {
				Path p = Paths.get(outputDirectory.getAbsolutePath());
				try {
					Files.createDirectories(p);
				} catch (IOException e) {
					throw new SymbolicExecutionException("Could not create output directory.", e);
				}
			}
		}
		if(outputDirectory.exists() && !outputDirectory.isDirectory()) {
			System.out.println("*** Path to output directory is not a directory: " + outputDirectoryPath);
			System.exit(0);
		}
		
		Path genTestsPath = Paths.get(outputDirectoryPath, "generated-tests", "w", "tc");
		int i = 0;
		while(genTestsPath.toFile().exists()) {
			genTestsPath = Paths.get(outputDirectoryPath, "generated-tests-"+(i), "w", "tc");
			i++;
		}	
		try {
			Files.createDirectories(genTestsPath);
		} catch (IOException e) {
			throw new SymbolicExecutionException("Could not create output directory.", e);
		}
		this.outputDirectory = genTestsPath.toFile();
	}

	public void generate(SymbolicState state) {
		
		// currently, only for integer id values a uniqueness is implemented
		Set<IntVariable> idValues = new HashSet<>();
		for(String className : state.getDatabase().getObjectDB().keySet()) {
			for(SymbolicObject e : state.getDatabase().selectAll(className)) {
				Variable idVal = state.getDatabase().getId(e);
				if(idVal instanceof IntVariable) {
					idValues.add((IntVariable)idVal);
					state.getConstraints().addConstraint(new IntXopY((IntVariable)idVal, new IntConstant(0), BoolOp.GE));
				}
			}
		}
		state.getConstraints().addConstraint(new AllDifferent(idValues));
		
		ConstraintManager cm = new ConstraintManager(state.getConstraints());
		ConstraintSolution sol = cm.solve();
		
		StringBuilder sbHead = createHead(state);
		StringBuilder sbDB = createDB(state, sol);
		StringBuilder sbTestMethod = createMethod(state, sol);
		StringBuilder sbButtom = createBottom();
		
		StringBuilder all = new StringBuilder();
		all.append(sbHead);
		all.append(sbDB);
		all.append(sbTestMethod);
		all.append(sbButtom);
		
		Path testCaseFilePath = Paths.get(this.outputDirectory.getAbsolutePath(), "TestCase"+i+".java");
		writeFile(testCaseFilePath, all);
		
		Path webAppTestCasePath = Paths.get(this.outputDirectory.getAbsolutePath(), "WebappTest.java");
		writeFile(webAppTestCasePath, createWebappTestFile());
		
		Path statsPath = Paths.get(this.outputDirectory.getAbsolutePath(), "stats.txt");
		writeCoverageFile(statsPath, createStats("TestCase"+i, state.getCoverage()));
	}
	
	private void writeCoverageFile(Path statsPath, StringBuilder createStats) {
		this.statsFile = statsPath.toFile();
		if(!statsFile.exists()) {
			StringBuilder sbInit = new StringBuilder();
			sbInit.append("* Stats file *\n");
			writeFile(statsPath, sbInit);
		}
		try {
		    Files.write(statsPath, createStats.toString().getBytes(), StandardOpenOption.APPEND);
		}catch (IOException e) {
		    e.printStackTrace();
		}
	}

	private int c = 0;
	private Map<String, Integer> pages = new HashMap<>();
	
	private Integer[] visited;
	
	private StringBuilder createStats(String testCaseName, Coverage coverage) {
		visited = new Integer[3];
		StringBuilder sb = new StringBuilder();
		sb.append(testCaseName + ", ");
		for(Entry<WebPage, Integer> entry : coverage.getVisitedPageCounter().entrySet()) {
			if(!pages.containsKey(entry.getKey().toString())) {
				pages.put(entry.getKey().toString(), c++);
			}
			int nr = pages.get(entry.getKey().toString());
			visited[nr] = entry.getValue();
		}
		for(int i=0; i<visited.length; i++) {
			if(visited[i] != null) {
				sb.append(visited[i]+", ");
			} else {
				sb.append(" - , ");
			}
		}
		sb.append("\n");
		return sb;
	}
	
	public void writeStatsEnd(Duration executionTime, int maxSteps) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n* Total Execution time (in seconds): "+executionTime.getSeconds()+" for max-steps: " + maxSteps + "pages: " + pages + " \n\n");
		Path p=Paths.get(this.statsFile.getAbsolutePath());
		writeCoverageFile(p, sb);
	}

	private void writeFile(Path path, StringBuilder sb) {
		assert(!path.toFile().exists());
		File testCaseFile = path.toFile();
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(testCaseFile));
			writer.write(sb.toString());
		} catch(Exception e) {
			throw new SymbolicExecutionException("Could not write test case to: " + testCaseFile, e);
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					throw new SymbolicExecutionException("Could not close file writer.", e);
				}
		}
	}
	
	private StringBuilder createBottom() {
		StringBuilder sb = new StringBuilder();
		sb.append("}\n");
		return sb;
	}

	private StringBuilder createDB(SymbolicState state, ConstraintSolution sol) {
		StringBuilder sb = new StringBuilder();
		SymbolicObjectDatabase db = state.getDatabase();
		sb.append("\t/**\n\t * Required Database Entities \n\t **/\n");
		for(Entry<String, Set<CopiedSymbolicObject>> entry : db.getRequiredObjects().entrySet()) {
			String entityType = entry.getKey();
			Set<CopiedSymbolicObject> entities = entry.getValue();
			int size = entities != null ? entities.size() : 0;
			sb.append("\t// Entities of type: " + entityType + " ("+size+")\n");
			int ei = 0;
			for(CopiedSymbolicObject copiedEntity : entities) {
				SymbolicObject entity;
				try {
					entity = copiedEntity.load();
				} catch (ClassNotFoundException | IOException e1) {
					throw new SymbolicExecutionException("Could not load copied object!", e1);
				}
				sb.append("\t// "+entityType+"("+ (ei++) + "): ");
				for(Entry<String, Variable> e : entity.getValues().entrySet()) {
					sb.append(e.getKey()+"="+getValueFromSolution(sol,e.getValue())+", ");
				}
				sb.append("\n");
			}
		}
		return sb;
	}

	private String getValueFromSolution(ConstraintSolution sol, Variable value) {
		if(value instanceof CharConstant) {
			return ""+(char)((CharConstant)value).getIntValue();
		}
		if(value instanceof IntVariable || value instanceof LongVariable) {
			Integer val = sol.getIntValue((IntVariable)value);
			if(val == null) return "0";
			else return ""+val;
		}
		if(value instanceof IntConstant || value instanceof LongConstant) {
			return ""+((IntConstant)value).getIntValue();
		}
		if(value instanceof ObjectReference) {
			return buildObject(sol, (ObjectReference)value);
		}
		if(value instanceof ArrayReference) {
			return buildArray(sol, (ArrayReference)value);
		}
		throw new RuntimeException("dont now what to do with: " +value);
	}

	private String buildArray(ConstraintSolution sol, ArrayReference value) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		Integer lengthValue = sol.getIntValue(value.getLength());
		int length = 0;
		if(lengthValue != null) {
			length = lengthValue.intValue();
		}
		for(int i=0; i<length; i++) {
			Variable var = value.getElement(i);
			sb.append(getValueFromSolution(sol, var)+ " ");
		}
		sb.append("]");
		return sb.toString();
	}

	private String buildObject(ConstraintSolution sol, ObjectReference ref) {
		if(ref.getClassName().equals(String.class.getName())) {
			return buildStringObject(sol, ref);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(ref.getName() + "(");
		for(Entry<String, Variable> e : ref.getReferencedObject().getValues().entrySet()) {
			sb.append(e.getKey()+"="+getValueFromSolution(sol,e.getValue())+", ");
		}
		sb.append(")");
		return sb.toString();
	}
	
	private String buildStringObjectFromArrayValueRef(ConstraintSolution sol, ArrayReference valueArray) {
		Integer lengthValue = sol.getIntValue(valueArray.getLength());
		int length = 0;
		if(lengthValue != null) {
			length = lengthValue.intValue();
		}
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<length; i++) {
			Variable var = valueArray.getElement(i);
			if(var != null) {
				if(var instanceof CharConstant) {
					CharConstant cc = (CharConstant)var;
					sb.append((char)cc.getIntValue());
				} else if (var instanceof CharVariable) {
					CharVariable cv = (CharVariable)var;
					Integer val = sol.getIntValue((IntVariable)cv);
					if(val == null) sb.append("?");
					else sb.append((char)val.intValue());
				} else{
					throw new SymbolicExecutionException("Types in array of a string must be characters, but was: " + var);
				}
			}
		}
		return "\""+ sb.toString() + "\"";
	}
		
	private String buildStringObject(ConstraintSolution sol, ObjectReference ref) {
		ArrayReference valueArray = (ArrayReference)ref.getValue("value");
		if(valueArray == null) {
			return "null";
		}
		return buildStringObjectFromArrayValueRef(sol, valueArray);
	}

	private StringBuilder createMethod(SymbolicState state, ConstraintSolution sol) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t@Test public void testIt() throws Throwable {\n");
		sb.append("\t\t// assert that we are on the start page (given in webapptest)\n");
		for(Action action : state.getActionSequence().getActions()) {
			sb.append(buildAction(state.getHeap(), sol, action, "\t\t"));
			if(action.getAssertions().size() > 0) {
				sb.append(buildAssertions(action.getAssertions(), sol));
			}
			System.out.println("******* and action in selenium:    " + sb.toString());
		}
		sb.append("\t}\n");
		return sb;
	}
	
	private StringBuilder buildAssertions(Set<Assertion> assertions, ConstraintSolution sol) {
		StringBuilder sb = new StringBuilder();
		for(Assertion a : assertions) {
			if(a instanceof AssertDataTableEntries) {
				sb.append(buildAssertDataTableEntries((AssertDataTableEntries)a, sol));
			}
			else if(a instanceof AssertInputFieldValue) {
				sb.append(buildAssertInputFieldValues((AssertInputFieldValue)a, sol));
			}
		}
		return sb;
	}

	private StringBuilder buildAssertInputFieldValues(AssertInputFieldValue a, ConstraintSolution sol) {
		StringBuilder sb = new StringBuilder();
		
		SymbolicObject symObj = null;
		try {
			symObj = a.getStringValue().load();
		} catch (ClassNotFoundException | IOException e) {
			throw new SymbolicExecutionException("Could not load object");
		}
		
		ArrayReference arrayValueRef = (ArrayReference)symObj.getValue("value");
		if(arrayValueRef != null) {
			String checkVal = buildStringObjectFromArrayValueRef(sol, arrayValueRef);
			if(checkVal != null) {
				sb.append("\t\ttry{\n\t\t\tdriver.findElement(By.xpath(\"//input[@id='"+a.getInputText().getId()+"', contains(text(), '");
				sb.append(checkVal);
				sb.append(")')]\"));\n");
				sb.append("\t\t} catch(NoSuchElementException e) {\n\t\t\tfail();\n\t\t}\n");
			}
		}
		
		return sb;
	}

	private StringBuilder buildAssertDataTableEntries(AssertDataTableEntries a, ConstraintSolution sol) {
		StringBuilder sb = new StringBuilder();
		for(CopiedSymbolicObject copiedObj : a.getElements()) {
			SymbolicObject symObj = null;
			try {
				symObj = copiedObj.load();
			} catch (ClassNotFoundException | IOException e) {
				throw new SymbolicExecutionException("Could not load object");
			}
			for(DataTableColumn column : a.getDataTable().getColumns()) {
				for(UIComponent ui : column.getUiComponents()) {
					if(ui instanceof OutputText) {
						OutputText output = (OutputText)ui;
						String value = format(output.getValue());
						if(value.startsWith("#{") && value.endsWith("}")) {
							value = value.substring(2, value.length()-1);
							String[] s = value.split("\\.");
							if(s != null && s.length > 1) {
								String fieldName = s[1];
								Variable entryValue = symObj.getValue(fieldName);
								String checkVal = null;
								if(entryValue instanceof ObjectReference) {
									checkVal = buildStringObject(sol, (ObjectReference)entryValue);
									checkVal = checkVal.substring(1,checkVal.length()-1);
								} else if(entryValue instanceof IntVariable){
									Integer val = sol.getIntValue((IntVariable)entryValue);
									if(val != null) {
										checkVal = ""+val.intValue();
									}
								}
								
								if(checkVal != null) {
									sb.append("\t\ttry{\n\t\t\tdriver.findElement(By.xpath(\"//tr/td[contains(text(), '");
									sb.append(checkVal);
									sb.append(")')]\"));\n");
									sb.append("\t\t} catch(NoSuchElementException e) {\n\t\t\tfail();\n\t\t}\n");
								}
								
								System.out.println("ASSERT THAT VALUE IN TABLE IS THAT VALUE HERE: " + entryValue);
							}
						}
					}
				}
			}
		}
		return sb;
	}

	private StringBuilder createWebappTestFile() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("package w.tc;\n\n");
		sb.append("import org.junit.AfterClass;\n");
		sb.append("import org.junit.BeforeClass;\n");
		sb.append("import org.openqa.selenium.WebDriver;\n");
		sb.append("import org.openqa.selenium.chrome.ChromeDriver;\n");
		sb.append("import org.openqa.selenium.support.ui.WebDriverWait;\n\n");
		sb.append("public class WebappTest {\n\n");
		sb.append("\tprotected static WebDriver driver;\n");
		sb.append("\tprotected static WebDriverWait wait; \n\n");
		sb.append("\t@BeforeClass public static void openBrowser() {\n");
		sb.append("\t\tString chromeDriverPath = System.getProperty(\"webdriver.chrome.driver\");\n");
		sb.append("\t\tString startURL = System.getProperty(\"start.url\");\n");
		sb.append("\t\tif(chromeDriverPath == null) throw new RuntimeException(\"Please set the system property webdriver.chrome.driver to point to the chrome-driver file, e.g.:   -Dwebdriver.chrome.driver=/foo/bar/chromedriver-2.34.exe\");\n");
		sb.append("\t\tif(startURL == null) throw new RuntimeException(\"Please set the system property start.url to point to the start page, e.g.:   -Dstart.url=http://localhost:8080/\");\n");
		sb.append("\t\tdriver = new ChromeDriver();\n");
		sb.append("\t\tdriver.get(startURL);\n");
		sb.append("\t\twait = new WebDriverWait(driver, 10);\n");
		sb.append("\t}\n");
		sb.append("\t@AfterClass public static void closeBrowser() {\n");
		sb.append("\t\tdriver.close();\n");
		sb.append("\t}\n");
		sb.append("}\n");

		return sb;
	}
	
	private StringBuilder createHead(SymbolicState state) {
		StringBuilder sb = new StringBuilder();
		sb.append("package w.tc;\n\n");
		sb.append("import org.junit.Test;\n");
		sb.append("import org.openqa.selenium.*;\n");
		sb.append("import w.tc.WebappTest;\n\n");
		sb.append("/**\n");
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
		sb.append(" * Generated at: " + timeStamp + "\n");
		sb.append(" * constraints: " + state.getConstraints().getConstraints() +"\n");
		sb.append(" * heap: " + state.getHeap() + "\n");
		sb.append(" * author: Andreas Fuchs \n");
		
		sb.append(" * visited pages: \n");
		for(Entry<WebPage, Integer> entry : state.getCoverage().getVisitedPageCounter().entrySet()) {
			sb.append("    * " + entry.getKey() + ": " + entry.getValue() + "\n");
		}
		
		sb.append(" */\n");
		sb.append("public class TestCase"+(i++)+" extends WebappTest {\n\n");
		return sb;
	}

	private StringBuilder buildAction(Heap heap, ConstraintSolution sol, Action action, String indent) {
		StringBuilder sb = new StringBuilder();
		if(action instanceof CommandButtonClick) {
			String buttonValue = format(((CommandButtonClick)action).getButton().getValue());
			sb.append(indent);
			sb.append("driver.findElement(By.xpath(\"//input[@type='submit' and @value='");
			sb.append(buttonValue);
			sb.append("']\")).click();\n");
		} else if(action instanceof TextInput) {
			TextInput txtInput = (TextInput)action;
			String formId = format(txtInput.getForm().getId());
			String inputId = format(txtInput.getInput().getId());
			String inputTextValue = generateTextInputValue(sol, getSymbolicObjectFromHeap(heap, txtInput.getStringRef()));
			sb.append(indent);
			sb.append("driver.findElement(By.xpath(\"//input[@id='");
			sb.append(formId+":"+inputId);
			sb.append("']\")).sendKeys(\"");
			sb.append(inputTextValue);
			sb.append("\");\n");
		} else if(action instanceof LinkClick) {
			LinkClick linkClick = (LinkClick)action;
			String linkText = linkClick.getLink().getValue();
			sb.append(indent);
			sb.append("driver.findElement(By.xpath(\"//a[text()='");
			sb.append(format(linkText));
			sb.append("']\")).click();\n");
		}
		return sb;
	}

	private SymbolicObject getSymbolicObjectFromHeap(Heap heap, ObjectReference stringRef) {
		for(SymbolicObject heapobj : heap.getHeapObjects()) {
			if(heapobj.equals(stringRef.getReferencedObject())) {
				return heapobj;
			}
		}
		return null;
	}

	private String generateTextInputValue(ConstraintSolution sol, SymbolicObject stringRef) {		
		ArrayReference arrayRef = (ArrayReference)stringRef.getValue("value");
		if(arrayRef == null) {
			// could be anything, here we return 'foobar'
			return "foobar";
		}
		
		Integer lengthVal = sol.getIntValue(arrayRef.getLength());
		if(lengthVal == null) {
			throw new RuntimeException("If an array reference has been created, it should have at least the length 0!");
		}
		
		int length = lengthVal.intValue();

		char[] stringValues = new char[length];
		
		for(int i=0; i<length; i++) {
			Variable var = arrayRef.getElement(i);
			if(var != null) {
				if(var instanceof CharVariable) {
					CharVariable cv = (CharVariable)var;
					int val = sol.getIntValue(cv);
					stringValues[i] = (char)val; 
				} else if(var instanceof CharConstant) {
					CharConstant cc = (CharConstant)var;
					stringValues[i] = (char)cc.getIntValue();; 
				} else {
					throw new RuntimeException("Charcter variable must be either CharVariable or CharConstant, but was: " + var);
				}
			} else {
				stringValues[i] = '?';
			}
		}
		
		return new String(stringValues);
	}
	
	private String format(String value) {
		if(value.startsWith("\"")) value = value.substring(1);
		if(value.endsWith("\"")) value = value.substring(0, value.length()-1);
		return value;
	}


}
