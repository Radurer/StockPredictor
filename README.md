# StockPredictor
Stock Predictor is a evaluatory exercise which aims to predict future stock prices based on seasonality.



## Flow
The program starts by parsing the `.csv` files available in the directory provided as input (up to a maximum specified amount of files). The files are expected to contain three columns each, the symbol name, the value of the stock and the date at which the value is represented. The result of the parsing is stored in an `ArrayList` of `ArrayList`s.

Then, the program chooses a number of random consecutive points from this data set, and attempts to predict a set number of points in the future, consecutive with the last data point in the subset selected.
The prediction of the stock value is done by splitting the subset into seasons, based on an input season length. This season length represents the algebraic period of relevant data points in the subset. For example, in a data set containing 36 points, representing monthly sales figures, some valid season lengths could be 3 (which would impose a new prediction based on components of quaterly figures), or 12 (which predicts new points based on components of yearly figures)

Based on the season length provided the program obtains all the relevant points of the "same coordinates" from different seasons, averages them and obtains a random figure within a specific threshold of that average figure (+/- delta)
Then, after obtaining the data point, it is appended to the original data set, so it can be used for future predictions which may use the same position inside the season. Once the program obtains the amount of points specified by the user, it converts the `ArrayList` to a `.csv` file, and stores it in a directory in the same location as the files that were processed. 

## Configuration
Multiple parameters are configurable in this program. They are defined as constants in the header of the file. Below is a short explanation of each constant's purpose, which can be correlated to the flow explained above:

`DELTA_PERCENTAGE_VALUE` represents the fractional value that is used in order to construct a numerical range from which a new price of a stock is randomly selected.

`MAX_FILES_TO_PROCESS` represents the maximum amount of `.csv` files the program will process for the given input location.

`INDEX_OF_SYMBOL`, `INDEX_OF_DATE` and `INDEX_OF_VALUE` represent the expected order of the columns in the `.csv` file, where `0` is interpreted as the first column.

`AMOUNT_OF_DATAPOINTS_TO_RETURN` represents the number of random consecutive data points which will be selected from each `.csv` file, before making a prediction.

`AMOUNT_OF_DATAPOINTS_TO_PREDICT` represents the amount of data points that the program will predict for each provided stock file.

`SEASON_LENGTH` is a constant representing the algebraic period (seasonality) of the data points in the file.

`dateFormatter` is the expected date format for the date column in the `.csv` files.

`dateTimeFormatter` is used for obtaining a unique file output name.

`outputDirectoryName` is the name of the directory which will store the outputted `.csv` files. This directory is located at the same path provided by the user, as input, when requesting the `.csv` file parsing.

## Setup & Running
The program structure is simple, containing a single file, `StockPredictor.java` which is the program that needs to be executed. In order to have an expected output, please prepare a folder structure for the `.csv` files that you plan to test with this program.
Once executing the program, you will be requested the system path to the directory which contains one (or multiple) `.csv` files, which will be processed.



## Output
The program will output a new `.csv` file that contains the selected data points, together with the predicted data points. The file will be located in a new directory created during the runtime. This directory is located at the same path provided in the program execution.
