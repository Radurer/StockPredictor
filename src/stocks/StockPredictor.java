package stocks;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.io.*;

public class StockPredictor {
	//the maximum bidirectional variation of a stock's predicted price, percentage wise
	private static final double DELTA_PERCENTAGE_VALUE = 0.025;
	private static final int MAX_FILES_TO_PROCESS = 2;
	
	private static final int INDEX_OF_SYMBOL = 0;
	private static final int INDEX_OF_DATE = 1;
	private static final int INDEX_OF_VALUE = 2;
	
	private static final int AMOUNT_OF_DATAPOINTS_TO_RETURN = 10;
	private static final int AMOUNT_OF_DATAPOINTS_TO_PREDICT = 3;
	private static final int SEASON_LENGTH = 4;
	
	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	
	private static final String outputDirectoryName = "Output";

	public static void main(String[] args) {
		
		int filesToProcess = 0;
		
		//i/o operations
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the path to the stock exchange folder: ");
		String folderPath = sc.nextLine();
		
		System.out.println("How many files to process?");
		//if the user doesn't provide a valid input, pester them until they do
		while(filesToProcess < 1 || filesToProcess > MAX_FILES_TO_PROCESS) {
			System.out.println("Please select a maximum of " + MAX_FILES_TO_PROCESS + " files: ");
			filesToProcess = sc.nextInt();
		}
		sc.close();
		
		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();
		
		int filesProcessed = 0;
		
		//starts processing the CSV files from the path provided, based on how many were requested
		int fileIndex = 0;
		
		if(listOfFiles.length > 0) {
			while(filesProcessed < filesToProcess) {
				File file = listOfFiles[fileIndex];
				
				if(file.isFile()) {
					++filesProcessed;
					String filePath = file.getPath();
					String fileName = file.getName();
					LocalDateTime now = LocalDateTime.now();
					String outputFileName = now.format(dateTimeFormatter) + "_predicted_" + fileName;
					
					List<List<String>> newList = new ArrayList<>();
					newList = csvToList(filePath);
					//if the file is not empty
					if(newList.size() != 0) {
						List<List<String>> chosenPoints = randomDataPoints(newList, AMOUNT_OF_DATAPOINTS_TO_RETURN);
						try {
							listToCSV(folderPath, outputFileName, forecastDataPoints(chosenPoints, AMOUNT_OF_DATAPOINTS_TO_PREDICT, SEASON_LENGTH));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					else {
						System.out.println("The file located at path " + filePath + " is empty.");
					}
					
				}
				
				++fileIndex;
			}
		}
		else {
			System.out.println("In the path " + folderPath + " there are no files.");
		}
		
		
	}
	
	//function responsible for parsing a csv file and returning a list
	public static List<List<String>> csvToList(String path) {
		List<List<String>> records = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		        String[] values = line.split(",");
		        records.add(Arrays.asList(values));
		    }
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return records;
	}
	
	public static void listToCSV(String path, String outputFileName, List<List<String>> list) throws IOException {
		boolean wasFileCreated = false;
		String outputDirectoryPathString = path + File.separator + outputDirectoryName;
		
		File outputDirectory = new File(outputDirectoryPathString);
		//assume the directory exists from previous executions. if not, create it.
		if(!outputDirectory.exists()) {
			try{
				outputDirectory.mkdir();
			}
			catch(Exception e) {
				System.out.println("csvToList(): Unable to create folder at path " + path);
				System.exit(-1);
			}
		}
		
		File csvFile = new File(outputDirectoryPathString + File.separator + outputFileName);


		FileWriter fileWriter = new FileWriter(csvFile);
		
		//iterates through the list, adds each element of the inner list to the string row of the CSV
		for(List<String> row : list) {
			StringBuilder csvLine = new StringBuilder();
			for(int i = 0; i < row.size(); ++i) {
				csvLine.append(row.get(i));
				if(i != row.size() - 1) {
					csvLine.append(',');
				}
			}
			csvLine.append("\n");
			
			fileWriter.write(csvLine.toString());
			
		}
		fileWriter.close();
		wasFileCreated = true;
		
		if(wasFileCreated) {
			System.out.println("Successfully created file " + outputFileName + " at path " + outputDirectoryPathString);
		}
	}
	
	//function that returns a given amount of data points after a certain date.
	//if there are less points that requested, returns the whole array
	public static List<List<String>> randomDataPoints(List<List<String>> dataPoints, int amount){
		if(dataPoints.size() > amount) {
			int maxStartIndex = dataPoints.size() - amount;
			int startIndex = (int)(Math.random() * (maxStartIndex - 1));
			
			List<List<String>> subList = dataPoints.subList(startIndex, startIndex + amount);
			
			return subList;
		}
		else {
			System.out.println("randomDataPoints(): Unable to provide " + amount + " datapoints, as the list contains only " + dataPoints.size() + " elements:");
			return dataPoints;
		}
	}
	
	public static List<List<String>> forecastDataPoints(List<List<String>> inputDataPoints, int amount, int seasonLength){
		
		if(seasonLength < 2 || seasonLength > inputDataPoints.size() / 2) {
			//not enough data to come up with a pattern
			System.out.println("forecastDataPoints(): Please adjust the season length so that there are at least 2 full seasons, containing at least 2 data points each.");
		}
		else {
			String symbol = "";
			LocalDate dateOfValue = LocalDate.EPOCH;
			
			//index for generating amount of points wanted
			int iteratorIndex = 0;
			//counter which keeps track of how many values which share 
			//the same season as the next point that needs to be predicted
			//have been computed so far.
			int currentSeasonCount = 0;
			
			while(iteratorIndex != amount) {
				double accumulatedValue = 0;
				int totalSeasonCount =  (int) Math.floor(inputDataPoints.size() / seasonLength);
				while(currentSeasonCount != totalSeasonCount) {
					//index used to get the values in the input array which are from the same season
					//if the input array is size 10, the seasonLength 3, then you know that the first value that needs
					//to be predicted (index 10) is based on the points in the same season, at indexes 1+3*0, 1+3*1, 1+3*2,
					//where 1 is arraySize mod seasonLength.
					int seasonalIndex = (inputDataPoints.size() % seasonLength) + (seasonLength * currentSeasonCount);
					//the predicted value will be a randomised value which
					//is +/- x% of the average values in the same season
					List<String> currentDataPoint = inputDataPoints.get(seasonalIndex);
					String currentSymbol = currentDataPoint.get(INDEX_OF_SYMBOL);			
					
					//making sure all symbols are the same.
					if(symbol.equals("")) {
						symbol = currentSymbol;
					}
					else if(!symbol.equals(currentSymbol)){
						System.out.println("forecastDataPoints(): Unexpected symbol " + currentSymbol + " found at line " + seasonalIndex + " of the CSV.");
						System.exit(-1);
					}
					else if(symbol.equals(currentSymbol)) {
						String currentValue = currentDataPoint.get(INDEX_OF_VALUE);
						accumulatedValue += Double.parseDouble(currentValue);
						++currentSeasonCount;
					}
					

				}
				//center value around which the new random value will be generated
				//with a minimum of CENTER_VALUE - DELTA, and a maximum of CENTER_VALUE + DELTA
				final int CENTER_VALUE = 1;
				double randomPercentage =  (CENTER_VALUE - DELTA_PERCENTAGE_VALUE) + (2 * DELTA_PERCENTAGE_VALUE * Math.random());
				double computedValue = (accumulatedValue / totalSeasonCount) * randomPercentage;
				
				List<String> tempList = new ArrayList<>();
				
				List<String> lastInputProvided = inputDataPoints.get(inputDataPoints.size() - 1);
				LocalDate dateOfLastInput = LocalDate.parse(lastInputProvided.get(INDEX_OF_DATE), dateFormatter);
				//the new datapoint is the next day after the last datapoint in the input.
				dateOfValue = dateOfLastInput.plusDays(1);
				
				//the symbol name
				tempList.add(symbol);
				//the date of the value
				tempList.add(dateOfValue.format(dateFormatter));
				//the value of the symbol
				tempList.add(Double.toString(computedValue));
				//in case more points are requested than provided, performs prediction using the new values, too
				inputDataPoints.add(tempList);
				
				//housekeeping to keep the algorithm going
				++iteratorIndex;
				currentSeasonCount = 0;
			}
			
		}
		return inputDataPoints;
	}
}
