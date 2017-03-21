
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class ReadInputFiles {

	private int counter = 0;
	private static final String DOC_END_TAG = "</DOCNO>";
	private static final String DOC_START_TAG = "<DOCNO>";
	private static final String TEXT_END_TAG = "</TEXT>";

	private static final String TEXT_START_TAG = "<TEXT>";

	// private ReadInputDatatype readInputDatatype;
	private Set<String> content = new HashSet<>();
	private List<String> stopList = new ArrayList<>();
	private String line = null;
	private int index = 0;
	private HashMap<Integer, String> wordDict = new HashMap<>();
	private HashMap<Integer, String> fileDict = new HashMap<>();

	// Test
	public static void main(String args[]) {
		System.out.println("Started");
		ReadInputFiles readInputFiles = new ReadInputFiles();
		readInputFiles.loadData();

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("./parser_output.txt"));

			readInputFiles.writeContent(readInputFiles.wordDict, writer);
			readInputFiles.writeContent(readInputFiles.fileDict, writer);

			writer.flush();
			writer.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		System.out.println("Completed");

	}

	private void writeContent(HashMap<Integer, String> hashMap, BufferedWriter writer) {

		try {
			writer.write("-------------------------------------------");
			writer.newLine();
			for (Entry<Integer, String> entry : hashMap.entrySet()) {
				writer.write(entry.getValue() + " " + entry.getKey());
				writer.newLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return list of lists containing the content from the files.
	 */
	public void loadData() {
		List<String> filesList = loadFiles("./src/ft911/");

		for (String fileName : filesList) {
			readFileContent(fileName);
		}

		tokenizeContent(content);

	}

	/**
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
	 * 
	 * @param fileToRead
	 * @return list containing the data.
	 */
	private void readFileContent(String fileToRead) {
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
					// if (readInputDatatype == null) {
					// readInputDatatype = new ReadInputDatatype();
					// }
					// readInputDatatype.setIndex(++counter);
					// readInputDatatype.setmDocId(docNumber);
					fileDict.put(++counter, docNumber);

				} else if (line.contains(TEXT_START_TAG)) {
					while (!(textLine = bufferedReader.readLine()).contains(TEXT_END_TAG)) {
						content.add(" " + textLine);
						// readInputDatatype.setmDocText(readInputDatatype.getmDocText()
						// + " " + textLine);
					}
				}
				/*
				 * if (readInputDatatype != null && counter > 0 &&
				 * readInputDatatype.getmDocId() != null &&
				 * readInputDatatype.getmDocText() != null &&
				 * !readInputDatatype.getmDocText().equals("")) {
				 * readinputDataList.add(readInputDatatype); readInputDatatype =
				 * new ReadInputDatatype(); }
				 */
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

	/**
	 * 
	 * @param content
	 *            total word set that has to be tokenized
	 */
	public void tokenizeContent(Set<String> content) {

		Set<String> tokens = new HashSet<>();
		// Load stop list if its not yet loaded.
		if (stopList.isEmpty()) {
			stopList = loadStopList();
		}

		for (String data : content) {
			// Removing strings with numbers and then considering words
			// without punctuation.
			tokens.addAll(
					Arrays.asList((data.toLowerCase().replaceAll("\\w*\\d\\w*", "").trim().split("\\s*[^a-z]\\s*"))));
		}

		tokens = checkStopList(tokens);

		stemWords(tokens);
	}

	private void stemWords(Set<String> tokens) {
		Porter porter = new Porter();
		TreeSet<String> sortedTokens = new TreeSet<String>(Collator.getInstance());
		for (String stemWords : tokens) {
			if (!stemWords.isEmpty()) {
				sortedTokens.add(porter.stripAffixes(stemWords.trim()));
			}
		}

		for (String string : sortedTokens) {
			if (!string.isEmpty()) {
				wordDict.put(++index, string);
			}
		}

	}

	/**
	 * 
	 * @param tokens
	 *            The list of words after dividing into tokens.
	 * @return
	 */
	public Set<String> checkStopList(Set<String> tokensTOCheck) {
		tokensTOCheck.removeAll(stopList);
		return tokensTOCheck;

	}
}
