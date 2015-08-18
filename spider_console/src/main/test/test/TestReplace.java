package test;

public class TestReplace {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String query="3-13MM* ? Titanium Coated 11Step Drill 1/4\" HSS Hex Shank 3/4/5/6/7/8/9/10/11/12/13";
		System.err.println(query.replaceAll("\\*|\\(|\\)|\"|-|\\?", " "));
	}

}
