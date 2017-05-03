import java.util.Scanner;

public class InfoRetrivealTest {

	public InfoRetrivealTest() {
		// TODO Auto-generated constructor stub
	}

	// Test
	public static void main(String args[]) {
		ReadInputFiles readInputFiles = new ReadInputFiles();

		readInputFiles.loadStopList("./src/stopwordlist.txt");

		Scanner scanner = new Scanner(System.in);

		String dataPath = new InfoRetrivealTest().readPath(scanner);
		System.out.println("Parsing the new content");

		readInputFiles.loadData(dataPath);
		System.out.print("Enter your word to search: ");
		String searchTerm = scanner.nextLine();
		readInputFiles.readInputFromUser(readInputFiles, searchTerm);
		scanner.close();

	}

	private String readPath(final Scanner scanner) {
		System.out.print("Enter your Source files Path to parse: ");
		String parsePath = scanner.nextLine();
		return parsePath;
	}

}
