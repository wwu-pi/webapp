package w.exe;

import w.exception.SymbolicExecutionException;

public class Foobar {

	public static void main(String[] args) {
		String testString = "\"#{issueEdit.submit}\"";
		System.out.println(testString);
		String[] parsed = parseBeanMethodCall(testString);
		for(int i=0; i<parsed.length; i++) {
			System.out.println(parsed[i]);
		}
	}
	
	private static String[] parseBeanMethodCall(String beanMethodCall) {
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
}
