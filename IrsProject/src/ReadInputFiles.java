
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ReadInputFiles implements InformationRetrivealInterface {

	private static final String DOC_END_TAG = "</DOCNO>";
	private static final String DOC_START_TAG = "<DOCNO>";
	private static final String TEXT_END_TAG = "</TEXT>";
	private static final String TEXT_START_TAG = "<TEXT>";

	private List<String> stopList;
	private String line;
	private int index, counter;
	private Map<String, Integer> wordDict;
	private Map<String, Integer> fileDict;
	private Porter porter;
	private String stemmedWord;
	// Map<DocID,<WordId,FreqCount>>
	private Map<Integer, Map<Integer, Integer>> frwdIndex;
	// Map <wordId,FreqCount>
	private Map<Integer, Integer> termIndex;
	private int wordId;
	private int docId;

	// wordID - docId - freqCount
	private Map<Integer, Map<Integer, Integer>> invertedIndex;
	// docId, freqCount
	private Map<Integer, Integer> docIndex;

	public ReadInputFiles() {
		super();
		counter = 0;
		index = 0;
		stopList = new ArrayList<>();
		line = null;
		wordDict = new TreeMap<>();
		fileDict = new HashMap<>();
		porter = new Porter();
		frwdIndex = new HashMap<>();
		invertedIndex = new HashMap<>();

	}

	/**
	 * @return the stopList
	 */
	public List<String> getStopList() {
		return stopList;
	}

	/**
	 * @param stopList
	 *            the stopList to set
	 */
	public void setStopList(List<String> stopList) {
		this.stopList = stopList;
	}

	/**
	 * @param wordDict
	 *            the wordDict to set
	 */
	public void setWordDict(Map<String, Integer> wordDict) {
		this.wordDict = wordDict;
	}

	/**
	 * @return the wordDict
	 */
	public Map<String, Integer> getWordDict() {
		return wordDict;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index
	 *            the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return the fileDict
	 */
	public Map<String, Integer> getFileDict() {
		return fileDict;
	}

	/**
	 * @param fileDict
	 *            the fileDict to set
	 */
	public void setFileDict(Map<String, Integer> fileDict) {
		this.fileDict = fileDict;
	}

	/**
	 * @return the frwdIndex
	 */
	public Map<Integer, Map<Integer, Integer>> getFrwdIndex() {
		return frwdIndex;
	}

	/**
	 * @return the invertedIndex
	 */
	public Map<Integer, Map<Integer, Integer>> getInvertedIndex() {
		return invertedIndex;
	}

	/**
	 * @param frwdIndex
	 *            the frwdIndex to set
	 */
	public void setFrwdIndex(Map<Integer, Map<Integer, Integer>> frwdIndex) {
		this.frwdIndex = frwdIndex;
	}

	/**
	 * @param invertedIndex
	 *            the invertedIndex to set
	 */
	public void setInvertedIndex(Map<Integer, Map<Integer, Integer>> invertedIndex) {
		this.invertedIndex = invertedIndex;
	}

	// Test
	public static void main(String args[]) {
		Timestamp inTime = new Timestamp(System.currentTimeMillis());
		System.out.println("Started at " + inTime);
		ReadInputFiles readInputFiles = new ReadInputFiles();

		readInputFiles.setStopList(readInputFiles.loadStopList("./src/stopwordlist.txt"));

		readInputFiles.loadData("./src/ft911/");
		readInputFiles.writeToFile(readInputFiles);
		readInputFiles.writeIndexToFile(readInputFiles);
		System.out.println("size of forward Index is " + readInputFiles.frwdIndex.size());
		System.out.println("size of Inverted Index is " + readInputFiles.invertedIndex.size());
		Timestamp outTime = new Timestamp(System.currentTimeMillis());
		Long dur = (outTime.getTime() - inTime.getTime()) / 1000;
		System.out.println("Completed at " + outTime + " duration is around " + dur + " seconds");

	}

	private void writeIndexToFile(final ReadInputFiles readInputFiles) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("./forwardIndexoutput.txt"));

			readInputFiles.writeIndexToFile(readInputFiles.frwdIndex, writer);
			writer.flush();
			writer.close();

			writer = new BufferedWriter(new FileWriter("./InverseIndexoutput.txt"));

			readInputFiles.writeIndexToFile(readInputFiles.invertedIndex, writer);
			writer.flush();
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void writeIndexToFile(final Map<Integer, Map<Integer, Integer>> invertedIndex,
			final BufferedWriter writer) {

		try {
			for (Entry<Integer, Map<Integer, Integer>> entry : invertedIndex.entrySet()) {
				writer.write(entry.getKey() + " " + entry.getValue());
				writer.newLine();
			}
			writer.write("-------------------------------------------");
			writer.newLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see InformationRetrivealInterface#readInputFromUser(ReadInputFiles,
	 * java.util.Scanner)
	 */
	public void readInputFromUser(final ReadInputFiles readInputFiles, String searchTerm) {

		// String searchTerm = term.toLowerCase();
		searchTerm = searchTerm.toLowerCase().trim();
		if (!searchTerm.isEmpty()
				&& searchTerm.contentEquals(searchTerm.toLowerCase().replaceAll("\\w*\\d\\w*", "").trim())
				&& !stopList.contains(searchTerm)) {
			searchTerm = porter.stripAffixes(searchTerm);
			if (readInputFiles.wordDict.containsKey(searchTerm)) {
				wordId = readInputFiles.wordDict.get(searchTerm);
				System.out.println("The entered search after stemming is valid");

				// WordId : DocId : FreqCount
				for (Entry<Integer, Map<Integer, Integer>> wordEntry : readInputFiles.invertedIndex.entrySet()) {
					if (wordEntry.getKey() == wordId) {
						for (Entry<Integer, Integer> docEntry : wordEntry.getValue().entrySet()) {
							for (Entry<String, Integer> fileEntry : readInputFiles.fileDict.entrySet()) {
								if (docEntry.getKey().equals(fileEntry.getValue())) {
									System.out.println("The document the word after stemming " + searchTerm
											+ " is present in " + fileEntry.getKey() + " and freq. count is "
											+ docEntry.getValue());
								}
							}
						}
					}
				}

			} else {
				System.out.println("Entered word after stemming " + searchTerm + " is not present in the document");
			}
		} else {
			System.out.println("The entered Search term is invalid or is a stop list word.");
		}

	}

	/**
	 * write word dictionary and file dictionary to a file.
	 * 
	 * @param readInputFiles
	 *            class object to retrieve the final dictionary objects.
	 */
	private void writeToFile(final ReadInputFiles readInputFiles) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("./parser_output.txt"));

			readInputFiles.writeContent(readInputFiles.wordDict, writer, false);
			readInputFiles.writeContent(readInputFiles.fileDict, writer, true);

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
	private void writeContent(final Map<String, Integer> mapToPrint, final BufferedWriter writer, final boolean check) {

		try {
			// checking if we need to sort the map and print the content.
			if (check) {
				List<Map.Entry<String, Integer>> list = new LinkedList<>(mapToPrint.entrySet());
				Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
					@Override
					public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
						return (o1.getValue()).compareTo(o2.getValue());
					}
				});
				for (Map.Entry<String, Integer> entry : list) {
					writer.write(entry.getKey() + " " + entry.getValue());
					writer.newLine();
				}
			} else {
				for (Entry<String, Integer> entry : mapToPrint.entrySet()) {
					writer.write(entry.getKey() + " " + entry.getValue());
					writer.newLine();
				}
				writer.write("-------------------------------------------");
				writer.newLine();
			}
		} catch (

		IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see InformationRetrivealInterface#loadData(java.lang.String)
	 */
	@Override
	public void loadData(final String filePath) {
		List<String> filesList = loadFiles(filePath);

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
	 * @param stopListPath
	 *            path to the stop words
	 */
	@Override
	public List<String> loadStopList(final String stopListPath) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(stopListPath));
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
	 *            file to parse.
	 * 
	 */
	private void processFileContent(final String fileToRead) {
		BufferedReader bufferedReader = null;
		String docNumber = null;
		String textLine = null;

		try {
			bufferedReader = new BufferedReader(new FileReader(fileToRead));
			while ((line = bufferedReader.readLine()) != null) {
				if (line.contains(DOC_START_TAG)) {
					docNumber = line.substring(line.indexOf(DOC_START_TAG) + DOC_START_TAG.length(),
							line.indexOf(DOC_END_TAG));
					fileDict.put(docNumber, ++counter);

				} else if (line.contains(TEXT_START_TAG)) {
					termIndex = new TreeMap<>();
					while (!(textLine = bufferedReader.readLine()).contains(TEXT_END_TAG)) {
						for (String token : textLine.toLowerCase().replaceAll("\\w*\\d\\w*", "").trim()
								.split("\\s*[^a-z]\\s*")) {
							if (!token.isEmpty() && !stopList.contains(token)) {
								stemmedWord = porter.stripAffixes(token.trim());
								if (!stemmedWord.isEmpty()) {
									if (!wordDict.containsKey(stemmedWord)) {
										wordDict.put(stemmedWord, ++index);

									}
									// forward Index DocId - WordId - freqCount
									// getting the wordId
									wordId = wordDict.get(stemmedWord);
									// check if wordId already exists in the
									// term dictionary. if present increase the
									// count, else create a new entry in the
									// term.
									if (!termIndex.isEmpty() && termIndex.containsKey(wordId)) {
										termIndex.put(wordId, termIndex.get(wordId) + 1);
									} else {
										termIndex.put(wordId, 1);
									}

									// Inverted Index wordID - docId -freqCount
									docId = fileDict.get(docNumber);
									docIndex = new TreeMap<>();

									// check if the inverted index has the word.
									// if it contains then update the document
									// index.
									// else insert the new document index.
									if (invertedIndex.containsKey(wordId)) {
										docIndex = invertedIndex.get(wordId);
										if (docIndex.containsKey(docId)) {
											docIndex.put(docId, docIndex.get(docId) + 1);
										} else {
											docIndex.put(docId, 1);
										}
										invertedIndex.put(wordId, docIndex);

									} else {
										if (docIndex.isEmpty()) {
											docIndex.put(docId, 1);
										}
										invertedIndex.put(wordId, docIndex);
									}
								}
							}
						}
					}

					if (!frwdIndex.containsKey(docNumber)) {
						frwdIndex.put(fileDict.get(docNumber), termIndex);
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
