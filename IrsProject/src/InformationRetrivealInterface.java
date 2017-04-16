import java.util.Scanner;

/**
 * 
 */

/**
 * @author Srikanth
 *
 */
public interface InformationRetrivealInterface {
	/**
	 * Method to load the stop list words.
	 * 
	 * @param stopListpath
	 *            path to the stop list.
	 */
	public void loadStopList(final String stopListpath);

	/**
	 * Method to parse the content; build the file, word dictionaries and
	 * indexers respectively.
	 * 
	 * @param filePath
	 *            path to the file that has to be parsed.
	 */
	public void loadData(final String filePath);

	/**
	 * Method to read word input from user and return the files that contain the
	 * word.
	 * 
	 * @param readInputFiles
	 * @param scanner
	 */
	public void readInputFromUser(final ReadInputFiles readInputFiles, final Scanner scanner);

}
