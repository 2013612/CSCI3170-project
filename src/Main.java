import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Scanner;

public class Main {
	public static Scanner reader = new Scanner(System.in);
	public static Connection conn = null;
	public static String dbAddress = "jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/group48";
	public static String dbUsername = "Group48";
	public static String dbPassword = "19980518";
	
	static void connect() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbAddress, dbUsername, dbPassword);
			System.out.println("Connected to MySQL successfully");
		} catch (ClassNotFoundException e) {
			System.out.println("[ERROR]: Java MySQL DB Driver not found!!");
			System.exit(0);
		} catch (SQLException e) {
			System.out.println(e);
		}
	}
	
    static void start() {
    	int identity;
    	do {
    		System.out.println("Welcome! Who are you?");
        	System.out.println("1. An administrator");
        	System.out.println("2. A passenager");
        	System.out.println("3. A driver");
        	System.out.println("4. A manager");
        	System.out.println("5. None of the above");
        	identity = readActionInt(1, 5);
        	
        	switch (identity) {
        		case 1:
        			systemAdministrator();
        			break;
        		case 2:
        			passenger();
        			break;
        		case 3:
        			driver();
        			break;
        		case 4:
        			manager();
        			break;
        	}
    	} while (identity != 5);
    }
    
    static int readActionInt(int lower, int upper) {
    	int input = -1;
    	do {
    		System.out.println(String.format("Please enter [%d-%d]", lower, upper));
    		input = readInt();   		
    	} while (!checkNumInRange(input, lower, upper));
    	
    	return input;
    }
    
    static int readInt() {
    	int input = -1;
    	if (reader.hasNextInt()) {
			input = reader.nextInt();
		}
    	
    	if (reader.hasNextLine()) {
    		reader.nextLine();
    	}
    	return input;
    }
    
    static boolean checkNumInRange(int input, int lower, int upper) {
    	if (input < lower || input > upper) {
			System.out.println("[ERROR] Invalid input.");
			return false;
		}
    	return true;
    }
    
    static String readString() {
    	String input = "";
    	if (reader.hasNext()) {
    		input = reader.next();
    	}
    	if (reader.hasNextLine()) {
    		reader.nextLine();
    	}
    	return input;
    }
    
    static String readLine() {
    	String input = "";
    	if (reader.hasNextLine()) {
    		input = reader.nextLine();
    	}
    	return input.trim();
    }
    
    static void systemAdministrator() {
    	int action = -1;
		do {
			System.out.println("Administrator, what would you like to do?");
			System.out.println("1. Create tables");
			System.out.println("2. Delete tables");
			System.out.println("3. Load data");
			System.out.println("4. Check data");
			System.out.println("5. Go back");
			
			action = readActionInt(1, 5);
			switch (action) {
				case 1:
					createTables();
					break;
				case 2:
					deleteTables();
					break;
				case 3:
					loadData();
					break;
				case 4:
					checkData();
					break;
			}
			
		} while (action != 5);
    }
    
    static void createTables() {
    	System.out.print("Processing...");
    	try {
    		String stmt = "CREATE TABLE vehicle(\r\n"
    				+ "	vehicle_id VARCHAR(6) PRIMARY KEY,\r\n"
    				+ "	model VARCHAR(30) NOT NULL,\r\n"
    				+ "	seats INT NOT NULL\r\n"
    				+ ");";
    		PreparedStatement pstmt = conn.prepareStatement(stmt);
    		pstmt.executeUpdate();
    		
    		stmt = "CREATE TABLE passenger(\r\n"
    				+ "	passenger_id INT PRIMARY KEY,\r\n"
    				+ "	passenger_name VARCHAR(30) NOT NULL\r\n"
    				+ ");";
    		pstmt = conn.prepareStatement(stmt);
    		pstmt.executeUpdate();
    		
    		stmt = "CREATE TABLE driver(\r\n"
    				+ "	driver_id INT PRIMARY KEY,\r\n"
    				+ "	driver_name VARCHAR(30) NOT NULL,\r\n"
    				+ "	vehicle_id VARCHAR(6) NOT NULL,\r\n"
    				+ "	driving_years INT NOT NULL,\r\n"
    				+ "	FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id)\r\n"
    				+ ");";
    		pstmt = conn.prepareStatement(stmt);
    		pstmt.executeUpdate();
    		
    		stmt = "CREATE TABLE taxi_stop(\r\n"
    				+ "	name VARCHAR(20) PRIMARY KEY,\r\n"
    				+ "	location_x INT NOT NULL,\r\n"
    				+ "	location_y INT NOT NULL\r\n"
    				+ ");";
    		pstmt = conn.prepareStatement(stmt);
    		pstmt.executeUpdate();
    		
    		stmt = "CREATE TABLE request(\r\n"
    				+ "	request_id INT PRIMARY KEY AUTO_INCREMENT,\r\n"
    				+ "	passenger_id INT NOT NULL,\r\n"
    				+ "	start_location VARCHAR(20) NOT NULL,\r\n"
    				+ "	destination VARCHAR(20) NOT NULL,\r\n"
    				+ "	model VARCHAR(30),\r\n"
    				+ "	passengers INT NOT NULL,\r\n"
    				+ "	taken BIT NOT NULL DEFAULT 0,\r\n"
    				+ "	driving_years INT DEFAULT 0,\r\n"
    				+ "	FOREIGN KEY (passenger_id) REFERENCES passenger(passenger_id),\r\n"
    				+ "	FOREIGN KEY (start_location) REFERENCES taxi_stop(name),\r\n"
    				+ "	FOREIGN KEY (destination) REFERENCES taxi_stop(name)\r\n"
    				+ ");";
    		pstmt = conn.prepareStatement(stmt);
    		pstmt.executeUpdate();
    		
    		stmt = "CREATE TABLE trip(\r\n"
    				+ "	trip_id INT PRIMARY KEY AUTO_INCREMENT,\r\n"
    				+ "	driver_id INT NOT NULL,\r\n"
    				+ "	passenger_id INT NOT NULL,\r\n"
    				+ "	start_location VARCHAR(20) NOT NULL,\r\n"
    				+ "	destination VARCHAR(20) NOT NULL,\r\n"
    				+ "	start_time DATETIME NOT NULL,\r\n"
    				+ "	end_time DATETIME,\r\n"
    				+ "	fee INT DEFAULT -1,\r\n"
    				+ "	FOREIGN KEY (driver_id) REFERENCES driver(driver_id),\r\n"
    				+ "	FOREIGN KEY (passenger_id) REFERENCES passenger(passenger_id),\r\n"
    				+ "	FOREIGN KEY (start_location) REFERENCES taxi_stop(name),\r\n"
    				+ "	FOREIGN KEY (destination) REFERENCES taxi_stop(name)\r\n"
    				+ ");";
    		pstmt = conn.prepareStatement(stmt);
    		pstmt.executeUpdate();
    	} catch (Exception e) {
    		System.out.println(e);
    	}
    	System.out.println("Done! Tables are created!");
    }
    
    static void deleteTables() {
    	System.out.println("Processing...Done!");
    	try {
    		String stmt = "SET FOREIGN_KEY_CHECKS = 0;";
    		PreparedStatement pstmt = conn.prepareStatement(stmt);
		    pstmt.executeUpdate();
    		ResultSet rs = conn.getMetaData().getTables(null, null, null,new String[] {"TABLE"});
    		while (rs.next()) {
    			//System.out.println(rs.getString("TABLE_NAME"));
			    stmt = "DROP TABLE IF EXISTS " + rs.getString("TABLE_NAME") + ";";
			    //System.out.println(stmt);
			    pstmt = conn.prepareStatement(stmt);
			    pstmt.executeUpdate();
    		}
    		rs.close();
    		stmt = "SET FOREIGN_KEY_CHECKS = 1;";
    		pstmt = conn.prepareStatement(stmt);
		    pstmt.executeUpdate();
    	} catch (Exception e) {
    		System.out.println(e);
    	}
    	System.out.println("Tables are deleted");	
    }
    
    static void loadData() {
    	System.out.println("Please enter the folder path");
    	String path = "/" + readLine() + "/";
    	if (path.equals("//")) path = "/";
    	path = System.getProperty("user.dir") + path;
    	//path = "D:\\CU\\CSCI\\CSCI 3170\\project\\Phase 2\\java program\\src\\test_data(2)\\test_data\\";
    	System.out.print("Processing...");
    	insertData(path, "vehicles");
    	insertData(path, "passengers");
    	insertData(path, "taxi_stops");
    	insertData(path, "drivers");
    	insertData(path, "trips");
    	System.out.println("Data is loaded!");
    }
    
    static void insertData(String path, String name) {
    	String csvFile = path + name + ".csv";
    	String line = "";
    	try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
    		while ((line = br.readLine()) != null) {
    			String[] record = line.split(",");
    			switch (name) {
    				case "drivers":
    					insertIntoDriver(record);
    					break;
    				case "vehicles":
    					insertIntoVehicle(record);
    					break;
    				case "passengers":
    					insertIntoPassenger(record);
    					break;
    				case "trips":
    					insertIntoTrip(record);
    					break;
    				case "taxi_stops":
    					insertIntoTaxiStop(record);
    					break;
    			}
    		}
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    static void insertIntoVehicle(String[] record) {
    	try {
    		String stmt = "INSERT INTO vehicle\r\n"
    				+ "(vehicle_id, model, seats)\r\n"
    				+ "VALUES\r\n"
    				+ "(?, ?, ?);";
    		PreparedStatement pstmt = conn.prepareStatement(stmt);
    		pstmt.setString(1, record[0]);
    		pstmt.setString(2, record[1]);
    		pstmt.setInt(3, Integer.parseInt(record[2]));
		    pstmt.executeUpdate();
    	} catch (Exception e) {
    		System.out.println(e);
    	}
    }
    
    static void insertIntoPassenger(String[] record) {
    	try {
    		String stmt = "INSERT INTO passenger\r\n"
    				+ "(passenger_id, passenger_name)\r\n"
    				+ "VALUES\r\n"
    				+ "(?, ?);";
    		PreparedStatement pstmt = conn.prepareStatement(stmt);
    		pstmt.setInt(1, Integer.parseInt(record[0]));
    		pstmt.setString(2, record[1]);
    		pstmt.executeUpdate();
    	} catch (Exception e) {
    		System.out.println(e);
    	}
    }
    
    static void insertIntoTaxiStop(String[] record) {
    	try {
    		String stmt = "INSERT INTO taxi_stop\r\n"
    				+ "(name, location_x, location_y)\r\n"
    				+ "VALUES\r\n"
    				+ "(?, ?, ?);";
    		PreparedStatement pstmt = conn.prepareStatement(stmt);
    		pstmt.setString(1, record[0]);
    		pstmt.setInt(2, Integer.parseInt(record[1]));
    		pstmt.setInt(3, Integer.parseInt(record[2]));
    		pstmt.executeUpdate();
    	} catch (Exception e) {
    		System.out.println(e);
    	}
    }
    
    static void insertIntoDriver(String[] record) {
    	try {
    		String stmt = "INSERT INTO driver \r\n"
    				+ "(driver_id, driver_name, vehicle_id, driving_years)\r\n"
    				+ "VALUES\r\n"
    				+ "(?, ?, ?, ?);";
    		PreparedStatement pstmt = conn.prepareStatement(stmt);
    		pstmt.setInt(1, Integer.parseInt(record[0]));
    		pstmt.setString(2, record[1]);
    		pstmt.setString(3, record[2]);
    		pstmt.setInt(4, Integer.parseInt(record[3]));
    		pstmt.executeUpdate();
    	} catch (Exception e) {
    		System.out.println(e);
    	}
    }
    
    static void insertIntoTrip(String[] record) {
    	try {
    		String stmt = "INSERT INTO trip\r\n"
    				+ "(trip_id, driver_id, passenger_id, start_time, end_time, start_location, destination, fee)\r\n"
    				+ "VALUES\r\n"
    				+ "(?, ?, ?, ?, ?, ?, ?, ?);";
    		PreparedStatement pstmt = conn.prepareStatement(stmt);
    		pstmt.setInt(1, Integer.parseInt(record[0]));
    		pstmt.setInt(2, Integer.parseInt(record[1]));
    		pstmt.setInt(3, Integer.parseInt(record[2]));
    		pstmt.setString(4, record[3]);
    		pstmt.setString(5, record[4]);
    		pstmt.setString(6, record[5]);
    		pstmt.setString(7, record[6]);
    		pstmt.setInt(8, Integer.parseInt(record[7]));
    		pstmt.executeUpdate();
    	} catch (Exception e) {
    		System.out.println(e);
    	}
    }
    
    static void checkData() {
    	System.out.println("Numbers of records in each table:");
    	try {
    		String stmt = "SELECT COUNT(*) from vehicle;";
        	PreparedStatement pstmt = conn.prepareStatement(stmt);
        	ResultSet rs = pstmt.executeQuery();
        	rs.next();
        	System.out.println(String.format("Vehicle: %d", rs.getInt(1)));
        	
        	stmt = "SELECT COUNT(*) from passenger;";
        	pstmt = conn.prepareStatement(stmt);
        	rs = pstmt.executeQuery();
        	rs.next();
        	System.out.println(String.format("Passenger: %d", rs.getInt(1)));
        	
        	stmt = "SELECT COUNT(*) from driver;";
        	pstmt = conn.prepareStatement(stmt);
        	rs = pstmt.executeQuery();
        	rs.next();
        	System.out.println(String.format("Driver: %d", rs.getInt(1)));
        	
        	stmt = "SELECT COUNT(*) from trip;";
        	pstmt = conn.prepareStatement(stmt);
        	rs = pstmt.executeQuery();
        	rs.next();
        	System.out.println(String.format("Trip: %d", rs.getInt(1)));
        	
        	stmt = "SELECT COUNT(*) from request;";
        	pstmt = conn.prepareStatement(stmt);
        	rs = pstmt.executeQuery();
        	rs.next();
        	System.out.println(String.format("Request: %d", rs.getInt(1)));
        	
        	stmt = "SELECT COUNT(*) from taxi_stop;";
        	pstmt = conn.prepareStatement(stmt);
        	rs = pstmt.executeQuery();
        	rs.next();
        	System.out.println(String.format("Taxi_Stop: %d", rs.getInt(1)));
    	} catch (Exception e) {
    		System.out.println(e);
    	}
    }
    
	static void passenger() {
		int action = -1;
		do {
			System.out.println("Passenger, what would you like to do?");
			System.out.println("1. Request a ride");
			System.out.println("2. Check trip records");
			System.out.println("3. Go back");
			
			action = readActionInt(1, 3);
			switch (action) {
				case 1:
					requestARide();
					break;
				case 2:
					checkTripRecords();
					break;
			}
		} while (action != 3);
	}
	
	static void requestARide() {
		int passengerID = -1;
		do {
			System.out.println("Please enter your ID");
			passengerID = readInt();
		} while (!checkPassengerID(passengerID));
		
		String stmt;
		PreparedStatement pstmt;
		try {
			stmt = "SELECT * from request\r\n"
					+ "WHERE passenger_id = ? and taken = 0";
			pstmt = conn.prepareStatement(stmt);
			pstmt.setInt(1, passengerID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				System.out.println("[ERROR] You have already placed a request which is still opened");
				return;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		
		int passengerNum = -1;
		do {
			System.out.println("Please enter the number of passengers.");
			passengerNum = readInt();
		} while (!checkNumInRange(passengerNum, 1, 8));
		
		String startLoc = "";
		String endLoc = "";
		do {
			do {
				System.out.println("Please enter the start location.");
				startLoc = readLine();
			} while (!checkLocation(startLoc));
			
			do {
				System.out.println("Please enter the destination.");
				endLoc = readLine();
			} while (!checkLocation(endLoc));
			
			if (startLoc.equals(endLoc)) {
				System.out.println("[ERROR] Start Location and destination are equal.");
			} else {
				break;
			}
		} while (true);
		
		String model = "";
		do {
			System.out.println("Please enter the model. (Press enter to skip)");
			model = readLine();
		} while (model != "" && !checkModel(model));
		
		int minDrivingYear = -1;
		String yearInString = "";
		do {
			System.out.println("Please enter the minimum driving years of the driver. (Press enter to skip)");
			yearInString = readLine();
			try {
				minDrivingYear = Integer.parseInt(yearInString);
			} catch (NumberFormatException e) {
				minDrivingYear = -1;
			}
		} while (yearInString.length() != 0 && !checkNumInRange(minDrivingYear, 0, Integer.MAX_VALUE));
		
		try {
			stmt = "SELECT COUNT(*) from driver d \r\n"
					+ "INNER JOIN vehicle v \r\n"
					+ "ON d.vehicle_id = v.vehicle_id\r\n"
					+ "WHERE v.model like ? and v.seats >= ? and driving_years >= ?;";
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, "%" + model + "%");
			pstmt.setInt(2, passengerNum);
			pstmt.setInt(3, minDrivingYear);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			int driverNum = rs.getInt(1);
			if (driverNum == 0) {
				System.out.println("No driver is able to take the request. Please adjust the criteria.");
			} else {
				stmt = "INSERT INTO request\r\n"
						+ "(passenger_id, start_location, destination, model, passengers, driving_years)\r\n"
						+ "VALUES\r\n"
						+ "(?, ?, ?, ?, ?, ?);";
				pstmt = conn.prepareStatement(stmt);
				pstmt.setInt(1, passengerID);
				pstmt.setString(2, startLoc);
				pstmt.setString(3, endLoc);
				pstmt.setString(4, model);
				pstmt.setInt(5, passengerNum);
				pstmt.setInt(6, minDrivingYear);
				pstmt.executeUpdate();
				System.out.println(String.format("Your request is placed. %d drivers are able to take the request.", driverNum));
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	static void checkTripRecords() {
		int passengerID = -1;
		do {
			System.out.println("Please enter your ID");
			passengerID = readInt();
		} while (!checkPassengerID(passengerID));
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate startDate = LocalDate.now();
		boolean transform = false;
		do {
			System.out.println("Please enter the start date.");
			try {
				startDate = LocalDate.parse(readLine(), formatter);
				transform = true;
			} catch (DateTimeParseException e) {
				System.out.println("[ERROR] Invalid input.");
			}
		} while (!transform);
		
		LocalDate endDate = LocalDate.now();
		transform = false;
		do {
			System.out.println("Please enter the end date.");
			try {
				endDate = LocalDate.parse(readLine(), formatter);
				transform = true;
			} catch (DateTimeParseException e) {
				System.out.println("[ERROR] Invalid input.");
			}
		} while (!transform);
		
		String endLoc = "";
		do {
			System.out.println("Please enter the destination.");
			endLoc = readLine();
		} while (!checkLocation(endLoc));
		
		try {
			String stmt = "SELECT * from trip t\r\n"
					+ "INNER JOIN driver d\r\n"
					+ "ON t.driver_id = d.driver_id\r\n"
					+ "INNER JOIN vehicle v\r\n"
					+ "ON d.vehicle_id = v.vehicle_id\r\n"
					+ "WHERE passenger_id = ? and start_time >= ? and end_time < ? and destination = ?\r\n"
					+ "ORDER BY end_time DESC;";
			PreparedStatement pstmt = conn.prepareStatement(stmt);
			pstmt.setInt(1, passengerID);
			pstmt.setString(2, startDate.toString());
			pstmt.setString(3, endDate.plusDays(1).toString());
			pstmt.setString(4, endLoc);
			ResultSet rs = pstmt.executeQuery();
			System.out.println("Trip_id, Driver Name, Vehicle ID, Vehicle Model, Start, End, Fee, Start Location, Destination");
			while (rs.next()) {
				System.out.print(rs.getInt("trip_id") + ", ");
				System.out.print(rs.getString("driver_name") + ", ");
				System.out.print(rs.getString("vehicle_id") + ", ");
				System.out.print(rs.getString("model") + ", ");
				System.out.print(rs.getString("start_time") + ", ");
				System.out.print(rs.getString("end_time") + ", ");
				System.out.print(rs.getInt("fee") + ", ");
				System.out.print(rs.getString("start_location") + ", ");
				System.out.println(rs.getString("destination"));
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	static boolean checkPassengerID(int passengerID) {
		try {
			String stmt = "SELECT passenger_id from passenger\r\n"
					+ "WHERE passenger_id = ?;";
			PreparedStatement pstmt = conn.prepareStatement(stmt);
			pstmt.setInt(1, passengerID);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next()) {
				System.out.println("[ERROR] Passenger ID does not exist.");
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}
	
	static boolean checkLocation(String location) {
		try {
			String stmt = "SELECT name from taxi_stop\r\n"
					+ "WHERE name = ?;";
			PreparedStatement pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, location);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next()) {
				System.out.println("[ERROR] Taxi stop does not exist.");
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}
	
	static boolean checkModel(String model) {
		try {
			String stmt = "SELECT DISTINCT model from vehicle\r\n"
					+ "WHERE model like ?;";
			PreparedStatement pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, "%" + model + "%");
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next()) {
				System.out.println("[ERROR] Model does not exist.");
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}
	
	static void driver() {
		int action = -1;
		do {
			System.out.println("Driver, what would you like to do?");
			System.out.println("1. Search requests");
			System.out.println("2. Take a request");
			System.out.println("3. Finish a trip");
			System.out.println("4. Go back");
			
			action = readActionInt(1, 4);
			switch (action) {
				case 1:
					searchRequests();
					break;
				case 2:
					takeARequest();
					break;
				case 3:
					finishATrip();
					break;
			}
		} while (action != 4);
	}
	
	static void searchRequests() {
		int driverID = -1;
		do {
			System.out.println("Please enter your ID.");
    		driverID = readInt();
		} while (!checkDriverID(driverID));
		
		int[] cood = {-1, -1};
		do {
			System.out.println("Please enter the coordinates of your location.");
    		if (reader.hasNextInt()) {
    			cood[0] = reader.nextInt();
    		}
    		if (reader.hasNextInt()) {
    			cood[1] = reader.nextInt();
    		}
    		readLine();
		} while (!(checkNumInRange(cood[0], 0 ,Integer.MAX_VALUE) && checkNumInRange(cood[1], 0 ,Integer.MAX_VALUE)));
		
		int maxDis = -1;
		do {
			System.out.println("Please enter the maximum distance from you to the passenger.");
    		maxDis = readInt();
		} while (!checkNumInRange(maxDis, 0, Integer.MAX_VALUE));
		
		try {
			String stmt = "SELECT * from request r\r\n"
					+ "INNER JOIN taxi_stop t\r\n"
					+ "ON start_location = name\r\n"
					+ "INNER JOIN passenger p\r\n"
					+ "ON r.passenger_id = p.passenger_id\r\n"
					+ "WHERE taken = 0 and (ABS(location_x - ?) + ABS(location_y - ?)) <= ?;";
			PreparedStatement pstmt = conn.prepareStatement(stmt);
			pstmt.setInt(1, cood[0]);
			pstmt.setInt(2, cood[1]);
			pstmt.setInt(3, maxDis);
			ResultSet rs = pstmt.executeQuery();
			System.out.println("request ID, passenger name, num of passengers, start location, destination");
			while (rs.next()) {
				System.out.print(rs.getInt("request_id") + ", ");
				System.out.print(rs.getString("passenger_name") + ", ");
				System.out.print(rs.getInt("passengers") + ", ");
				System.out.print(rs.getString("start_location") + ", ");
				System.out.println(rs.getString("destination"));
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	static void takeARequest() {
		int driverID = -1;
		do {
			System.out.println("Please enter your ID.");
    		driverID = readInt();
		} while (!checkDriverID(driverID));
		
		String stmt;
		PreparedStatement pstmt;
		ResultSet rs;
		try {
			stmt = "SELECT * from trip t\r\n"
					+ "INNER JOIN driver d\r\n"
					+ "ON t.driver_id = d.driver_id\r\n"
					+ "WHERE fee = -1;";
			pstmt = conn.prepareStatement(stmt);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				System.out.println("[ERROR] You need to finish the taken request before taking a new request.");
				return;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		
		int requestID = -1;
		do {
			System.out.println("Please enter the request ID.");
    		requestID = readInt();
		} while (!checkRequestID(requestID));
		
		try {
			stmt = "UPDATE request\r\n"
					+ "SET taken = 1\r\n"
					+ "WHERE request_id = ?;";
			pstmt = conn.prepareStatement(stmt);
			pstmt.setInt(1, requestID);
			pstmt.executeUpdate();
			
			stmt = "SELECT * from request\r\n"
					+ "WHERE request_id = ?;";
			pstmt = conn.prepareStatement(stmt);
			pstmt.setInt(1, requestID);
			rs = pstmt.executeQuery();
			rs.next();
			
			stmt = "INSERT INTO trip\r\n"
					+ "(driver_id, passenger_id, start_location, destination, start_time, fee)\r\n"
					+ "VALUES\r\n"
					+ "(?, ?, ?, ?, NOW(), -1);";
			pstmt = conn.prepareStatement(stmt);
			pstmt.setInt(1, driverID);
			pstmt.setInt(2, rs.getInt("passenger_id"));
			pstmt.setString(3, rs.getString("start_location"));
			pstmt.setString(4, rs.getString("destination"));
			pstmt.executeUpdate();
			
			stmt = "SELECT * from trip t\r\n"
					+ "INNER JOIN passenger p\r\n"
					+ "ON t.passenger_id = p.passenger_id\r\n"
					+ "WHERE trip_id = \r\n"
					+ "(SELECT MAX(trip_id) from trip);";
			pstmt = conn.prepareStatement(stmt);
			rs = pstmt.executeQuery();
			rs.next();
			System.out.println("Trip ID, Passenger name, Start");
			System.out.print(rs.getInt("trip_id") + ", ");
			System.out.print(rs.getString("passenger_name") + ", ");
			System.out.println(rs.getString("start_time"));
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	static void finishATrip() {
		int driverID = -1;
		do {
			System.out.println("Please enter your ID.");
    		driverID = readInt();
		} while (!checkDriverID(driverID));
		
		String stmt;
		PreparedStatement pstmt;
		ResultSet rs;
		int tripID = -1;
		try {
			stmt = "SELECT * from trip\r\n"
					+ "WHERE driver_id = ? and fee = -1;";
			pstmt = conn.prepareStatement(stmt);
			pstmt.setInt(1, driverID);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				tripID = rs.getInt("trip_id");
				System.out.println("Trip ID, Passenger ID, Start");
				System.out.print(tripID + ", ");
				System.out.print(rs.getString("passenger_id") + ", ");
				System.out.println(rs.getString("start_time"));
			} else {
				System.out.println("[ERROR] You does not take any request now.");
				return;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		
		System.out.println("Do you wish to finish the trip? [y/n]");
		String response = "";
		do {
    		response = readString();
    		if (!(response.equals("y") || response.equals("n"))) {
    			System.out.println("[ERROR] Invalid input.");
    		}
		} while (!(response.equals("y") || response.equals("n")));
		
		if (response.equals("y")) {
			try {
				stmt = "UPDATE trip\r\n"
						+ "SET end_time = NOW(), fee = FLOOR(TIME_TO_SEC(TIMEDIFF(NOW(), start_time))/60)\r\n"
						+ "WHERE trip_id = ?";
				pstmt = conn.prepareStatement(stmt);
				pstmt.setInt(1, tripID);
				pstmt.executeUpdate();
				
				stmt = "SELECT * from trip t\r\n"
						+ "INNER JOIN passenger p\r\n"
						+ "ON t.passenger_id = p.passenger_id\r\n"
						+ "WHERE trip_id = ?;";
				pstmt = conn.prepareStatement(stmt);
				pstmt.setInt(1, tripID);
				rs = pstmt.executeQuery();
				rs.next();
				System.out.println("Trip ID, Passenger name, Start, End, Fee");
				System.out.print(tripID + ", ");
				System.out.print(rs.getString("passenger_name") + ", ");
				System.out.print(rs.getString("start_time") + ", ");
				System.out.print(rs.getString("end_time") + ", ");
				System.out.println(rs.getInt("fee"));
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
	
	static boolean checkDriverID(int driverID) {
		try {
			String stmt = "SELECT driver_id from driver\r\n"
					+ "WHERE driver_id = ?;";
			PreparedStatement pstmt = conn.prepareStatement(stmt);
			pstmt.setInt(1, driverID);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next()) {
				System.out.println("[ERROR] Driver ID does not exist.");
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}
	
	static boolean checkRequestID(int requestID) {
		try {
			String stmt = "SELECT request_id, taken from request\r\n"
					+ "WHERE request_id = ?;";
			PreparedStatement pstmt = conn.prepareStatement(stmt);
			pstmt.setInt(1, requestID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				if (rs.getBoolean("taken")) {
					System.out.println("[ERROR] Request is already taken.");
					return false;
				}
				return true;
			} else {
				System.out.println("[ERROR] Request ID does not exist.");
				return false;
			}
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}
	
	static void manager() {
		int action = -1;
		do {
			System.out.println("Manager, what would you like to do?");
			System.out.println("1. Find trips");
			System.out.println("2. Go back");
			
			action = readActionInt(1, 2);
			if (action == 1) {
				findTrips();
			}
		} while (action != 2);
	}
	
	static void findTrips() {
		int minTraDis = -1;
		do {
			System.out.println("Please enter the minimum traveling distance.");
    		minTraDis = readInt();   		
		} while (!checkNumInRange(minTraDis, 0, Integer.MAX_VALUE));
		
		int maxTraDis = -1;
		do {
			System.out.println("Please enter the maximum traveling distance.");
			maxTraDis = readInt();
		} while (!checkNumInRange(maxTraDis, minTraDis, Integer.MAX_VALUE));
		
		try {
			String stmt = "SELECT * from trip t\r\n"
					+ "INNER JOIN passenger p\r\n"
					+ "ON t.passenger_id = p.passenger_id\r\n"
					+ "INNER JOIN driver d\r\n"
					+ "ON t.driver_id = d.driver_id\r\n"
					+ "INNER JOIN taxi_stop ts1\r\n"
					+ "ON t.start_location = ts1.name\r\n"
					+ "INNER JOIN taxi_stop ts2\r\n"
					+ "ON t.destination = ts2.name\r\n"
					+ "WHERE (ABS(ts1.location_x - ts2.location_x) + ABS(ts1.location_y - ts2.location_y)) BETWEEN ? and ?;";
			PreparedStatement pstmt = conn.prepareStatement(stmt);
			pstmt.setInt(1, minTraDis);
			pstmt.setInt(2, maxTraDis);
			ResultSet rs = pstmt.executeQuery();
			System.out.println("trip id, driver name, passenger name, start location, destination, duration");
			while (rs.next()) {
				System.out.print(rs.getInt("trip_id") + ", ");
				System.out.print(rs.getString("driver_name") + ", ");
				System.out.print(rs.getString("passenger_name") + ", ");
				System.out.print(rs.getString("start_location") + ", ");
				System.out.print(rs.getString("destination") + ", ");
				System.out.println(rs.getInt("fee"));
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
    
	public static void main(String[] args) {
		connect();
		start();
	}
}
