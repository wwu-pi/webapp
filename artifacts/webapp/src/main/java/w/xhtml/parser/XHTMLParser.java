package w.xhtml.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import antlr4.xml.XMLLexer;
import antlr4.xml.XMLParser;
import antlr4.xml.XMLParser.AttributeContext;
import antlr4.xml.XMLParser.DocumentContext;
import antlr4.xml.XMLParser.ElementContext;
import antlr4.xml.XMLParserBaseListener;
import w.exception.AnalyzeException;
import w.xhtml.ui.WebPage;
import w.xhtml.ui.component.CommandButton;
import w.xhtml.ui.component.DataTable;
import w.xhtml.ui.component.DataTableColumn;
import w.xhtml.ui.component.Form;
import w.xhtml.ui.component.InputText;
import w.xhtml.ui.component.Link;
import w.xhtml.ui.component.OutputText;

public class XHTMLParser extends XMLParserBaseListener {

	/**
	 * The XHTML file to parse.
	 */
	protected final File xhtmlFile;
	
	/**
	 * The abstract representation of the XHTML web page to be parsed.
	 */
	protected final WebPage page;
	
	/**
	 * The latest parsed data-table.
	 */
	protected DataTable currentTable;
	
	/**
	 * The latest parsed form.
	 */
	protected Form currentForm;
	
	/**
	 * The latest parsed column
	 */
	protected DataTableColumn currentColumn;
	
	public XHTMLParser(File xhtmlFile) {
		this.page = new WebPage(xhtmlFile.getName());
		this.xhtmlFile = xhtmlFile;
		this.currentTable = null;
	}
	
	public void parse() throws IOException {
		System.out.println("*** Start parsing web page: " + xhtmlFile);
		Path filePath = xhtmlFile.toPath();
		XMLLexer lexer = new XMLLexer(CharStreams.fromPath(filePath));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		XMLParser parser = new XMLParser(tokens);
		DocumentContext documentContext = parser.document();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(this, documentContext);
		System.out.println("\tSuccessfully parsed page: " + xhtmlFile);
	}
	
	public WebPage getPage() {
		return this.page;
	}
	
	@Override
	public void enterElement(ElementContext ctx) {
		String ctxName = ctx.Name().get(0).getText().toLowerCase();
		
		if(ctxName.equals("h:dataTable".toLowerCase())) {
			DataTable dataTable = createDataTable(ctx);
			this.currentTable = dataTable;
			page.addDataTable(dataTable);
			return;
		}
		
		if(ctxName.equals("h:form".toLowerCase())) {
			Form form = createForm(ctx);
			this.currentForm = form;
			page.addForm(form);
			return;
		}
		
		if(ctxName.equals("h:column".toLowerCase())) {
			DataTableColumn column = new DataTableColumn();
			if(this.currentTable != null) {
				this.currentTable.addColumn(column);
				this.currentColumn = column;
			} else {
				throw new AnalyzeException("Data column must be placed inside a <h:dataTable..>! Please check XHTML file: " + this.xhtmlFile);
			}
			return;
		}
		
		if(ctxName.equals("h:inputText".toLowerCase())) {
			InputText input = createInputText(ctx);
			if(this.currentForm != null) {
				page.addFormInput(this.currentForm, input);
			} else {
				throw new AnalyzeException("Input text must be placed inside a <h:form..>! Please check XHTML file: " + this.xhtmlFile);
			}
			return;
		}
		
		if(ctxName.equals("h:outputText".toLowerCase())) {
			OutputText output = createOutputText(ctx);
			if(this.currentColumn != null) {
				this.currentColumn.addComponent(output);
			}
			return;
		}
		
		if(ctxName.equals("h:commandButton".toLowerCase())) {
			CommandButton button = createCommandButton(ctx);
			if(this.currentTable != null) {
				page.addDataTableButton(this.currentTable, button);
			}
			if(this.currentForm != null) {
				page.addFormButton(this.currentForm, button);
			} else {
				throw new AnalyzeException("Command button must be placed inside a <h:form..>! Please check XHTML file: " + this.xhtmlFile);
			}
			return;
		}
		
		if(ctxName.equals("h:link".toLowerCase())) {
			Link link = createLink(ctx);
			if(this.currentTable != null) {
				page.addDataTableLink(this.currentTable, link);
			} else {
				page.addSimpleLink(link);
			}
			return;
		}
	}
	
	
	@Override
	public void exitElement(ElementContext ctx) {
		String ctxName = ctx.Name().get(0).getText().toLowerCase();
		
		if(ctxName.equals("h:dataTable".toLowerCase())) {
			this.currentTable = null;
		}
		
		if(ctxName.equals("h:form".toLowerCase())) {
			this.currentForm = null;
		}
		
		if(ctxName.equals("h:column".toLowerCase())) {
			this.currentColumn = null;
		}
	}
	

	private InputText createInputText(ElementContext ctx) {
		Map<String, String> attValues = parseElement(ctx, "id", "value", "required");
		return new InputText(attValues.get("id"), attValues.get("value"), attValues.get("required"));
	}
	
	private OutputText createOutputText(ElementContext ctx) {
		Map<String, String> attValues = parseElement(ctx, "id", "value");
		return new OutputText(attValues.get("id"), attValues.get("value"));
	}
	
	private Form createForm(ElementContext ctx) {
		Map<String, String> attValues = parseElement(ctx, "id");
		return new Form(attValues.get("id"));
	}
	
	private Link createLink(ElementContext ctx) {
		Map<String, String> attValues = parseElement(ctx, "outcome", "value");
		return new Link(attValues.get("value"), attValues.get("outcome"));
	}
	
	private CommandButton createCommandButton(ElementContext ctx) {
		Map<String, String> attValues = parseElement(ctx, "action", "value", "disabled");
		return new CommandButton(attValues.get("action"), attValues.get("value"), attValues.get("disabled"));
	}
	
	private DataTable createDataTable(ElementContext ctx) {
		Map<String, String> attValues = parseElement(ctx, "id", "value", "var");
		return new DataTable(attValues.get("id"), attValues.get("value"), attValues.get("var"));
	}

	private Map<String, String> parseElement(ElementContext ctx, String... attributes) {
		Map<String, String> attributeValues = new HashMap<>();
		if(ctx.attribute() != null) {
			for(AttributeContext att : ctx.attribute()) {
				String attName = att.Name().getText();
				for(int i=0;i<attributes.length;i++) {
					if(attName.equals(attributes[i])) {
						String value = att.STRING().getText();
						attributeValues.put(attName, value);
					}
				}
			}
		}		
		return attributeValues;
	}
}
