
import java.util.HashMap;

public class TestClass {
	
	static HashMap<Integer, Integer> testMap = new HashMap<Integer, Integer>();

	public static void main(String[] args) {
		
		int size = 0;
		for(int x = 0; x < 5; x++) {
			for(int y = 0; y < 5; y++) {
				size++;
				if(size > 10) {
					break;
				}
			}
		}
		System.out.println(size);
	}
}
