import java.util.*;
import java.io.File;
public class Test{
	public static void main(String args[]){
		try{
			File file = new File("./TestCases.txt");
	        Scanner input = new Scanner(file);
	        while (input.hasNextLine()) {
	            String line = input.nextLine();
	            System.out.println(line);
	        }
	        input.close();
		}
		catch(Exception ex){
			System.out.println(ex);
		}
	}
}