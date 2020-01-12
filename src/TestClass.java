import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TestClass {
	
	static HashMap<Integer, Integer> testMap = new HashMap<Integer, Integer>();

	public static void main(String[] args) {
		
		testMap.put(17, 1);
		testMap.put(6, 3);
		
		testMap.remove(5);
		
		System.out.println(testMap);
	}
}
