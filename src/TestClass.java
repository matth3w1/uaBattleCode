import java.util.Arrays;

public class TestClass {

	public static void main(String[] args) {
		int[] arr = {1, 2, 3, 4, 5, 6, 7};
		int[] test = new int[10];
		int index = 0;
		
		for(int i = 0; i < arr.length; i++) {
			if(arr[i] % 2 == 0) {
				test[index++] = arr[i];
			}
		}
		
		System.out.println(Arrays.toString(test));
	}
}
