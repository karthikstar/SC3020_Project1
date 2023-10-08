# SC3020_Project1
### Project Overview 
This project focuses on the design and implementation of a database management system's storage and indexing component using the Java programming language. Efficient data storage and retrieval are essential for the performance and scalability of database systems. The key feature of this project is hence the integration of a B+ tree index structure, which excels in maintaining balance for consistent performance. They store data in sorted order, enabling efficient range queries. The fanout structure of B+ Trees minimizes disk I/O, resulting in swift data retrieval. This data structure is especially well-suited for handling large datasets, making it indispensable for ensuring the efficiency of a database system.

### Project Structure 
Our project is structured into three primary packages:

Utils - This package houses the DataInitialiser class, responsible for initializing data from a text file, parsing it, and preparing it for storage within the database.

Database - The Database package manages the storage infrastructure of our database management system. It handles data blocks, records, addresses, and disk management to ensure efficient data storage and retrieval.

BplusTree - This package encompasses the core components related to the B+ tree implementation. It includes classes and methods for constructing and managing B+ trees, as well as support for search, insertion, and deletion operations.

## Team members
This project was developed and maintained by the following team members:
Amabel Lim Hui Xin   
Elangovan Karthikeyan   
Tan Puay Jun, Klaus  
Tay Jia Ying, Denise   

## Installation guide
### Prerequisites
1. Ensure that you have the Java Development Kit (JDK) installed on your system.
2. Verify that you have a compatible Integrated Development Environment (IDE) installed (e.g., Eclipse, IntelliJ IDEA, or Visual Studio Code).

### Project Setup
3. Clone or download the project's source code from the project repository on GitHub.

### Running the Project
4. Open the `Main.java` file located in the `src` folder in your preferred IDE.
5. Run the `Main.java` file.

### Usage
6. Experiment results can be viewed in the terminal window's output.



Done using Java (JDK 18)
