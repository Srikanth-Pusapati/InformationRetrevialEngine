
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ReadInputFiles {

	private static final String DOC_END_TAG = "</DOCNO>";
	private static final String DOC_START_TAG = "<DOCNO>";
	private static final String TEXT_END_TAG = "</TEXT>";
	private static final String TEXT_START_TAG = "<TEXT>";

	private List<String> stopList;
	private String line;
	private int index, counter;
	private Map<Integer, String> wordDict;
	private Map<Integer, String> fileDict;
	private Porter porter;
	// private TreeSet<String> sortedTokens;
	private String stemmedWord;
	// private Iterator<Entry<String, Map<String, Integer>>> mapIt;
	private Map<String, Map<String, Integer>> frwdIndex;
	private Map<String, Integer> termIndex;

	public ReadInputFiles() {
		super();
		counter = 0;
		index = 0;
		stopList = new ArrayList<>();
		line = null;
		wordDict = new TreeMap<>();
		fileDict = new HashMap<>();
		porter = new Porter();
		// sortedTokens = new TreeSet<>(Collator.getInstance());
		// forwardIndex = new HashMap<>();
		frwdIndex = new HashMap<>();
		termIndex = new TreeMap<>();

	}

	// Test
	public static void main(String args[]) {
		System.out.println("Started at " + new Timestamp(System.currentTimeMillis()));
		ReadInputFiles readInputFiles = new ReadInputFiles();
		if (readInputFiles.stopList.isEmpty()) {
			readInputFiles.stopList = readInputFiles.loadStopList();
		}
		readInputFiles.loadData();
		// readInputFiles.mapIt =
		// readInputFiles.frwdIndex.entrySet().iterator();
		//
		// while (readInputFiles.mapIt.hasNext()) {
		// System.out.println(readInputFiles.mapIt.next());
		// }
		// for (String string : readInputFiles.sortedTokens) {
		// if (!string.isEmpty()) {
		// readInputFiles.wordDict.put(++readInputFiles.index, string);
		// }
		// }
		readInputFiles.writeToFile(readInputFiles);
		System.out.println("Completed at " + new Timestamp(System.currentTimeMillis()));

	}

	private void writeToFile(ReadInputFiles readInputFiles) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("./parser_output.txt"));

			readInputFiles.writeContent(readInputFiles.wordDict, writer);
			readInputFiles.writeContent(readInputFiles.fileDict, writer);

			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to write the content to the output file.
	 * 
	 * @param mapToPrint
	 *            hash map write onto a file.
	 * @param writer
	 *            buffered writer object.
	 */
	private void writeContent(Map<Integer, String> mapToPrint, BufferedWriter writer) {

		try {
			writer.write("-------------------------------------------");
			writer.newLine();
			for (Entry<Integer, String> entry : mapToPrint.entrySet()) {
				writer.write(entry.getValue() + " " + entry.getKey());
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Method call to load all the files
	 */
	public void loadData() {
		List<String> filesList = loadFiles("./src/ft911/");

		for (String fileName : filesList) {
			processFileContent(fileName);
		}
	}

	/**
	 * Method to load the files present in a folder.
	 * 
	 * @param path
	 *            to the folder containing the files.
	 * @return list of files to be read.
	 */
	private List<String> loadFiles(final String path) {
		List<String> filesToLoad = new ArrayList<>();
		File folder = new File(path);
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isFile()) {
				filesToLoad.add(fileEntry.getAbsolutePath());
			}
		}
		return filesToLoad;

	}

	/**
	 * 
	 * @return list containing the stop words
	 */
	private List<String> loadStopList() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("./src/stopwordlist.txt"));
			while ((line = reader.readLine()) != null) {
				stopList.add(line.trim());
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stopList;
	}

	/**
	 * Method to process the file content.
	 * 
	 * @param fileToRead
	 * @return list containing the data.
	 */
	private void processFileContent(String fileToRead) {
		BufferedReader bufferedReader = null;
		String docNumber = null;
		String textLine = null;
		// List<ReadInputDatatype> readinputDataList = new ArrayList<>();

		try {
			bufferedReader = new BufferedReader(new FileReader(fileToRead));
			while ((line = bufferedReader.readLine()) != null) {
				if (line.contains(DOC_START_TAG)) {
					docNumber = line.substring(line.indexOf(DOC_START_TAG) + DOC_START_TAG.length(),
							line.indexOf(DOC_END_TAG));
					fileDict.put(++counter, docNumber);

				} else if (line.contains(TEXT_START_TAG)) {
					termIndex = new HashMap<>();

					while (!(textLine = bufferedReader.readLine()).contains(TEXT_END_TAG)) {
						for (String token : textLine.toLowerCase().replaceAll("\\w*\\d\\w*", "").trim()
								.split("\\s*[^a-z]\\s*")) {
							if (!token.isEmpty() && !stopList.contains(token)) {
								stemmedWord = porter.stripAffixes(token.trim());
								// sortedTokens.add(stemmedWord);
								wordDict.put(++index, stemmedWord);
								// Doc 1 - cow 2; moon 4; sum 10;
								// inside Doc 1
								if (!termIndex.isEmpty() && termIndex.containsKey(stemmedWord)) {

									termIndex.put(stemmedWord, termIndex.get(stemmedWord) + 1);
								} else {
									termIndex.put(stemmedWord, 1);
								}

							}
						}
					}

					if (!frwdIndex.containsKey(docNumber)) {

						frwdIndex.put(docNumber, termIndex);
					}
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
