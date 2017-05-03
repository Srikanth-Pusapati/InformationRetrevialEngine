import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ReadQuery {

	public static final String QUERY_NUMBER = "<num> Number:";
	public static final String QUERY_TITLE = "<title>";
	public static final String QUERY_DESC = "<desc>";
	private List<String> stopList;
	private Porter porter;
	private Map<Integer, String> queryMap;
	private Map<Integer, Map<Integer, Double>> pageRankMap;
	private Map<Integer, Double> innerNestedMap;
	private String wordsArray[];
	double doc[];
	double upper, dividend, wtd, wq;
	int documentNo;

	public ReadQuery() {
		stopList = new ArrayList<>();
		porter = new Porter();
		queryMap = new HashMap<Integer, String>();
		pageRankMap = new HashMap<>();
	}

	public static void main(String[] args) {
		ReadInputFiles readInputFiles = new ReadInputFiles();

		ReadQuery readQuery = new ReadQuery();
		readQuery.stopList = readInputFiles.loadStopList("./src/stopwordlist.txt");
		readInputFiles.setStopList(readQuery.stopList);
		readInputFiles.loadData("./src/ft911/");
		String path = "./src/topics.txt";
		readQuery.queryMap = readQuery.readContent(path);
		readQuery.processCosine(readInputFiles, readQuery, readQuery.queryMap);
		System.out.println("Done");
	}

	private void processCosine(ReadInputFiles readInputFiles, ReadQuery readQuery, Map<Integer, String> queryMap) {

		for (Entry<Integer, String> eachQuery : queryMap.entrySet()) {
			wordsArray = eachQuery.getValue().replaceAll("\\w*\\d\\w*", "").trim().split("\\s*[^a-z]\\s*");
			doc = new double[readInputFiles.getFrwdIndex().size() + 1];
			innerNestedMap = new TreeMap<Integer, Double>();
			for (String eachString : wordsArray) {
				if (!eachString.isEmpty() && !stopList.contains(eachString)) {
					int count = 0;

					for (int i = 0; i < wordsArray.length; i++) {
						if (eachString.equals(wordsArray[i]))
							++count;
					}

					wq = count / (Math.sqrt(wordsArray.length));
					String stemmedWord = readQuery.porter.stripAffixes(eachString);

					if (readInputFiles.getWordDict().containsKey(stemmedWord)) {
						for (Map.Entry<Integer, Integer> innerInvertedIndex : readInputFiles.getInvertedIndex()
								.get(readInputFiles.getWordDict().get(stemmedWord)).entrySet()) {
							upper = innerInvertedIndex.getValue();
							dividend = 0;
							documentNo = innerInvertedIndex.getKey();

							for (Map.Entry<Integer, Integer> innerFrwdIndex : readInputFiles.getFrwdIndex()
									.get(innerInvertedIndex.getKey()).entrySet()) {
								dividend += Math.pow(innerFrwdIndex.getValue(), 2);
							}
							wtd = upper / (Math.sqrt(dividend));
							doc[documentNo] += wq * wtd;
							innerNestedMap.put(documentNo, doc[documentNo]);
						}
					}

				}
				if (innerNestedMap.size() != 0)
					pageRankMap.put(eachQuery.getKey(), innerNestedMap);
				else {
					innerNestedMap.put(0, 0.0);
					pageRankMap.put(eachQuery.getKey(), innerNestedMap);
				}

			}
		}
		BufferedWriter printer;
		try {
			printer = new BufferedWriter(new FileWriter(new File("./src/output.txt")));

			for (Map.Entry<Integer, Map<Integer, Double>> rankedEntry : pageRankMap.entrySet()) {
				mapScoresDescending(rankedEntry.getValue(), rankedEntry.getKey(), 0, printer,
						readInputFiles.getFileDict());
			}
			printer.flush();
			printer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private Map<Integer, String> readContent(String path) {

		Map<Integer, String> queryMap = new HashMap<>();

		BufferedReader bufferedReader;
		try {
			bufferedReader = new BufferedReader(new FileReader(new File(path)));

			int queryNumber = 0;
			String qryString = null;
			String Line = "";
			String qryDesc = "";
			while ((Line = bufferedReader.readLine()) != null) {
				if (Line.startsWith(QUERY_NUMBER)) {
					queryNumber = Integer
							.parseInt(Line.substring(Line.indexOf(QUERY_NUMBER) + QUERY_NUMBER.length()).trim());
				}
				if (Line.contains(QUERY_TITLE)) {

					qryString = Line.substring(Line.indexOf(QUERY_TITLE) + QUERY_TITLE.length()).trim().toLowerCase();
				}
				if (Line.contains(QUERY_DESC)) {
					qryDesc = "";
					while (!(Line = bufferedReader.readLine()).contains("<narr>")) {
						qryDesc += " " + Line;
					}
				}
				if (queryNumber != 0 && qryString != null && qryString != "")
					queryMap.put(queryNumber, (qryString + qryDesc).toLowerCase());

			}

			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return queryMap;

	}

	private void mapScoresDescending(Map<Integer, Double> sortMap, int queryNo, int rankValue,
			BufferedWriter writerObject, Map<String, Integer> docFileIndex) {

		List<Entry<Integer, Double>> list = new LinkedList<>(sortMap.entrySet());
		Collections.sort(list, new Comparator<Entry<Integer, Double>>() {
			@Override
			public int compare(Map.Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
		try {
			for (Map.Entry<Integer, Double> entry : list) {
				for (Entry<String, Integer> fileEntry : docFileIndex.entrySet()) {
					if (fileEntry.getValue().equals(entry.getKey())) {
						// System.out.println("inside");
						writerObject.write(queryNo + "   \t  " + fileEntry.getKey() + "   \t  " + (++rankValue)
								+ "   \t  " + entry.getValue());
						writerObject.newLine();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
