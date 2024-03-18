# StockPredictor
Stock Predictor is a evaluatory exercise which aims to predict future stock prices based on seasonality.

## Setup & Running
The program structure is simple, containing a single file, `StockPredictor.java` which is the program that needs to be executed. In order to have an expected output, please prepare a folder structure for the `.csv` files that you plan to test with this program.
Once executing the program, you will be requested the system path to the directory which contains one (or multiple) `.csv` files, which will be processed.

## Configuration
Multiple parameters are configurable in this program. They are defined as constants in the header of the file. Below is a short explanation of each constant's purpose:
`DELTA_PERCENTAGE_VALUE` represents a fractional value that is used in order to predict a new price of a stock. 

## Output
The program will output a new `.csv` file that contains the selected data points, together with the predicted data points. The file will be located in a new directory created during the runtime. This directory is located at the same path provided in the program execution.
