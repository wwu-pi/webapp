package m.jee.ejb;

public class LoginBean {
	
	private int magicNumber;
	private static final int MORE = 42;
	
	public LoginBean() {
		this.magicNumber = 3;
	}
	
	public boolean login(String username, String password) {
		return username.equals("a");
	}
	
	public int max(int x, int y) {
		return foo(x,y);
//		if(x > y) {
//			return foo(x, y);
//		}
//		return y;
	}
	
	private int foo2(int x, int y) {
		return x+y;
	}

	private int foo(int x, int y) {
		if(x > 42) {
			if(y < 2) {
				return x;
			}
		}
		return y+42;
	}

	public int max2(int x, int y) {
		if(multiply(x) > FoobarClass.magic(y)) {
			return x;
		}
		return y + magicNumber + MORE;
	}
	
	private int multiply(int x) {
		return x + 42;
	}
	
	public boolean simpleTest(int[] foobar, String x) {
		if(x.equals("x")) {
			return true;
		}
		return false;
	}
	
}
