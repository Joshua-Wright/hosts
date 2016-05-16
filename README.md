# hosts
joins and formats hosts files  
uses Java 8 parallel stream functions

## Usage:
`java -jar <this .jar file> <path to URL file>`  
URL file should be a file with one remote url per line

## Optional Parameters:
| Parameter   | Description |
| ----------- | ----------- |
| `-h`        | Specify the hostname to use |
| ` -nh`      | do not print the header |
| `-w <file>` | path to whitelist file |
