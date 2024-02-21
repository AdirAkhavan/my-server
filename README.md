# Multi-Threaded Web Server:

## Chosen design to implement our server:

The chosen design promotes modularity and separation of concerns, allowing for easy maintenance and future enhancements.
By using distinct classes, we achieve a clear and organized structure for our server implementation.
The use of a thread pool optimizes resource utilization, enabling the server to efficiently handle multiple requests simultaneously.
The configuration handling provided by the ConfigReader class ensures easy customization and maintenance of server settings.
The HTTPRequest and RequestHandler classes operate in coordination to process incoming requests effectively, allowing for extensibility and adaptability to different use cases.
Overall, this design prioritizes simplicity and maintainability for a robust multi-threaded web server implementation.

## Running the Server

To run the server, follow these steps:

Step 1: Navigate to the Source Directory.
Open your terminal and navigate to the source directory of the project using the cd command:

cd path/to/the/project/src

Step 2: Compile Java Files.
Compile the Java files using the javac command:

javac \*.java

Step 3: Launch the Server.
Execute the following command to start the server:

java MyServer

## ConfigReader class:

A class that enables reading and printing the properties from the config file.

## HTTPRequest class:

A class that represents an HTTP request, when the server receives a request, it is being converted to an HTTPRequest object.

## MultiThreadedWebServer class:

A class that oprates as the multi-threaded web server that receives all HTTP requests using a thread pool.

## MyServer class:

A class where a MultiThreadedWebServer instance is instantiated and then being run.

## RequestHandler class:

A class the handles all HTTP requests that are received by the server.

## PrintingColorProvider class:

Bonus class: A class the provides a printing color to each HTTPHandler object so the printing will be more clear through the multi-threaded web server operations.
Color printing is only used for the requests handling.
