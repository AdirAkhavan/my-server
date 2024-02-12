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

## Chosen design to implement our server:

The chosen design promotes modularity and separation of concerns, allowing for easy maintenance and future enhancements.
By using distinct classes, we achieve a clear and organized structure for our server implementation.
The use of a thread pool optimizes resource utilization, enabling the server to efficiently handle multiple requests simultaneously.
The configuration handling provided by the ConfigReader class ensures easy customization and maintenance of server settings.
The HTTPRequest and RequestHandler classes operate in coordination to process incoming requests effectively, allowing for extensibility and adaptability to different use cases.
Overall, this design prioritizes simplicity and maintainability for a robust multi-threaded web server implementation.
