import java.util.Scanner;

public class InfoRetrivealTest {

	public InfoRetrivealTest() {
		// TODO Auto-generated constructor stub
	}

	// Test
	public static void main(String args[]) {
		ReadInputFiles readInputFiles = new ReadInputFiles();

		readInputFiles.loadStopList("./src/stopwordlist.txt");

		String dataPath = new InfoRetrivealTest().readPath();
		System.out.println("Parsing the new content");

		readInputFiles.loadData(dataPath);
		readInputFiles.readinputFromUser(readInputFiles);

	}

	private String readPath() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter your Source files Path to parse: ");
		String parsePath = scanner.nextLine();

		return parsePath;
	}

}
