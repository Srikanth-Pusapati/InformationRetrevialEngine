import java.util.List;

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
	 * @return 
	 */
	public List<String> loadStopList(final String stopListpath);

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
	 * @param searchTerm
	 */
	public void readInputFromUser(final ReadInputFiles readInputFiles, final String searchTerm);

}
