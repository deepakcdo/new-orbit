import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.opencsv.CSVReader;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Field;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.bson.types.Decimal128;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogManager;

public class App {
    // Maps the CSV field names to the MongoDB field names and data types
    public static Map<String, String[]> csvToMongoMapping = new HashMap<>();
    public final static Logger LOGGER = Logger.getLogger(App.class.getName());
    public static Thread main;
    public static List<String> failedDocumentErrorMsgs = new ArrayList<>();

    public static String volumePath = System.getenv("filePath");
    public static String mongoUri = System.getenv("mongoUri");

    public static void main(final String[] args) throws Exception {
        LOGGER.info("Env variable volumePath is set to " + volumePath);
        LOGGER.info("Env variable mongoUri is set to " + mongoUri);

        main = Thread.currentThread();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        LOGGER.setLevel(Level.INFO);
        LOGGER.info("mongo-importer has started running");

        FileHandler handler = new FileHandler("/" + volumePath + "/log.txt", true);
        LOGGER.addHandler(handler);

        buildCsvMongoMapping();

        MongoClientURI uri = new MongoClientURI(mongoUri);

        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase database = mongoClient.getDatabase("finance");

        MongoCollection<Document> bonds = database.getCollection("bonds");
        LOGGER.info("Database connection established");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                LOGGER.info("Terminating the application gracefully...");
                main.interrupt();
                try {
                    main.join();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        // On startup, we want to look for csv files and push them all to the database
        LOGGER.info("Looking for csv files in mongo-importer-mnt volume...");
        File folder = new File("/" + volumePath);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.getName().contains(".csv") && file.getName().length() == 12) {
                updateDatabase(bonds, file.getName());
            }
        }

        // Monitor changes to bonds.csv and update database whenever there is a change
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path path = Paths.get("/" + volumePath);

        path.register(
  watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

        WatchKey key;

        try {
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    String fileName = event.context().toString();
                    if (fileName.contains(".csv") && fileName.length() == 12) {
                        Thread.sleep(5000);
                        LOGGER.info("Processing " + fileName);
                        updateDatabase(bonds, fileName);
                    }
                }
                key.reset();
            }
        }
        catch (InterruptedException e) {
            LOGGER.info("Interrupted exception handled");
        }
        catch (Exception e) {
            LOGGER.info("Exception handled");
        }

        LOGGER.info("Shutting down database connection gracefully...");
        // Close the DB connection
        mongoClient.close();
        LOGGER.info("Database connection terminated. Graceful shutdown complete.");
    }

    public static void updateDatabase(MongoCollection<Document> bonds, String fileName) throws Exception {
        failedDocumentErrorMsgs.clear();
        LOGGER.info("Updating database using data from " + fileName);
        List<String> csvFieldNames = getBondCsvFieldNames(fileName);
        List<List<String>> csvData = getBondCsvData(fileName);
        int numUpdated = 0;
        int numInserted = 0;
        int numFailed = 0;
        // Insert the first 100 lines of the bond CSV data into the local Mongo database
        for (int i = 0; i < csvData.size(); i++) {
            Document document = buildMongoDocument(i, csvData.get(i), csvFieldNames, 1);
            if (document == null) {
                numFailed ++;
                continue;
            }
            Document findQuery = new Document();
            findQuery.append("isin", document.get("isin"));
            if (bonds.find(findQuery).first() != null) {
                Document updateQuery = new Document();
                updateQuery.append("isin", document.get("isin"));
                Document updateDocument = new Document();
                updateDocument.append("$set", document);
                boolean updateSuccessful = true;
                try {
                    bonds.updateOne(updateQuery, updateDocument);
                }
                catch (MongoException e) {
                    updateSuccessful = false;
                    failedDocumentErrorMsgs.add("Could not write bond with ISIN " + document.get("isin") + " to the database. Reason: " + e.getMessage());
                    numFailed ++;
                }
                if (updateSuccessful) {
                    numUpdated ++;
                }
            }
            else {
                boolean insertSuccessful = true;
                try {
                    bonds.insertOne(document);
                }
                catch (MongoException e) {
                    insertSuccessful = false;
                    failedDocumentErrorMsgs.add("Could not write bond with ISIN " + document.get("isin") + " to the database. Reason: " + e.getMessage());
                    numFailed ++;
                }
                if (insertSuccessful) {
                    numInserted ++;
                }
            }
        }

        String logMsg = "Database update successful\n";
        logMsg += "Updated " + Integer.toString(numUpdated) + " documents in the database\n";
        logMsg += "Inserted " + Integer.toString(numInserted) + " new documents into the database\n";
        logMsg += Integer.toString(numFailed) + " documents failed: \n";
        for (String errorMsg : failedDocumentErrorMsgs) {
            logMsg += errorMsg + "\n";
        }
        LOGGER.info(logMsg);
    }

    public static List<String> getBondCsvFieldNames(String fileName) throws Exception {
        // Read the first line of the csv which contains the field names
        List<String> csvFieldNames = null;
        try (CSVReader csvReader = new CSVReader(new FileReader("/" + volumePath + "/" + fileName));) {
            final String[] fieldNames = csvReader.readNext();
            csvFieldNames = Arrays.asList(fieldNames);
        }
        // Without this line the first field name always has a char value of -1 at the beginning, which needs to be removed
        csvFieldNames.set(0, csvFieldNames.get(0).substring(1));

        return csvFieldNames;
    }

    public static List<List<String>> getBondCsvData(String fileName) throws Exception {
        // Read the csv values into a 2-dimensional table of strings
        final List<List<String>> csvData = new ArrayList<List<String>>();

        try (CSVReader csvReader = new CSVReader(new FileReader("/" + volumePath + "/" + fileName));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                csvData.add(Arrays.asList(values));
            }
        }

        // Remove the first line which contains the headings
        csvData.remove(0);

        return csvData;
    }

    // Initializes csvToMongoMapping with the appropriate mappings from CSV to MongoDB
    public static void buildCsvMongoMapping() {
        csvToMongoMapping.put("number_of_trades", new String[] {"numberOfTrades", "int"});
        csvToMongoMapping.put("ISIN", new String[] {"isin", "string"});
        csvToMongoMapping.put("instrumentId", new String[] {"instrumentId", "long"});
        csvToMongoMapping.put("instrumentType", null);
        csvToMongoMapping.put("currency", new String[] {"currency", "string"});
        csvToMongoMapping.put("longDescription", new String[] {"longDescription", "string"});
        csvToMongoMapping.put("maturityTypeValue", new String[] {"maturityType", "string"});
        csvToMongoMapping.put("detailedCouponValue", new String[] {"isFloat", "bool"});
        csvToMongoMapping.put("calculationTypeValue", new String[] {"calculationType", "string"});
        csvToMongoMapping.put("firstCouponDate", new String[] {"firstCouponDate", "date"});
        csvToMongoMapping.put("penultimateCouponDate", new String[] {"penultimateCouponDate", "date"});
        csvToMongoMapping.put("maturityDate", new String[] {"maturityDate", "date"});
        csvToMongoMapping.put("earliestMaturityDate", new String[] {"earliestMaturityDate", "date"});
        csvToMongoMapping.put("paymentDate", new String[] {"paymentDate", "date"});
        csvToMongoMapping.put("interestAccDate", new String[] {"interestAccDate", "date"});
        csvToMongoMapping.put("parAmount", new String[] {"parAmount", "decimal"});
        csvToMongoMapping.put("minPiece", new String[] {"minPiece", "decimal"});
        csvToMongoMapping.put("minIncrement", new String[] {"minIncrement", "decimal"});
        csvToMongoMapping.put("marketSectorValue", new String[] {"marketSector", "string"});
        csvToMongoMapping.put("firstConformingDate", new String[] {"firstConformingDate", "date"});
        csvToMongoMapping.put("lastConformingDate", new String[] {"lastConformingDate", "date"});
        csvToMongoMapping.put("conformantPeriodId", new String[] {"conformantPeriodId", "long"});
        csvToMongoMapping.put("couponCurrency", new String[] {"couponCurrency", "string"});
        csvToMongoMapping.put("endOfMonthRule", new String[] {"endOfMonthRule", "bool"});
        csvToMongoMapping.put("roundingForCoupon", new String[] {"roundingForCoupon", "int"});
        csvToMongoMapping.put("settlementDays", new String[] {"settlementDays", "int"});
        csvToMongoMapping.put("lookupCouponTypeValue", null);
        csvToMongoMapping.put("freqValue", new String[] {"freq", "string"});
        csvToMongoMapping.put("YieldConvType", new String[] {"yieldConvType", "string"});
        csvToMongoMapping.put("dayCount", new String[] {"default", "string"});
        csvToMongoMapping.put("dayCountDiscount", new String[] {"discount", "string"});
        csvToMongoMapping.put("dayCountFunding", new String[] {"funding", "string"});
        csvToMongoMapping.put("coupon", new String[] {"coupon", "decimal"});
    }

    // Get the MongoDB field name corresponding to the CSV field name
    public static String getMongoFieldName(String csvFieldName) {
        String[] mapping = csvToMongoMapping.get(csvFieldName);
        if (mapping != null) {
            return csvToMongoMapping.get(csvFieldName)[0];
        }
        return null;
    }

    // Get the MongoDB data type corresponding to the CSV field name
    public static String getMongoDataType(String csvFieldName) {
        String[] mapping = csvToMongoMapping.get(csvFieldName);
        if (mapping != null) {
            return csvToMongoMapping.get(csvFieldName)[1];
        }
        return null;
    }


    // Builds a MongoDB document from a single line of CSV data
    public static Document buildMongoDocument(int lineNumber, List<String> csvLine, List<String> csvFieldNames, int recurseLevel) {
        Document document = new Document();
        for (int i = 0; i < csvLine.size(); i ++) {
            String csvValue = csvLine.get(i).trim();
            String csvFieldName = csvFieldNames.get(i).trim();

            if (csvFieldName.equals("dayCount") && recurseLevel > 0) {
                Document nestedDocument = buildMongoDocument(lineNumber, csvLine.subList(i, i + 3), csvFieldNames.subList(i, i + 3), 0);
                if (nestedDocument == null) {
                    return null;
                }
                document.append("dayCount", nestedDocument);
                i += 2;
                continue;
            }

            String mongoFieldName = getMongoFieldName(csvFieldName);
            if (mongoFieldName == null) {
                continue;
            }
            String mongoDataType = getMongoDataType(csvFieldName);

            Object mongoValue = null;

            // Boolean flag to indicate whether there was an error when converting the CSV field to Mongo field
            boolean csvDataInvalid = false;
            String errorMsg = "Failed to build Mongo document from bond with ISIN " + csvLine.get(1) + ". ";

            if (mongoFieldName.equals("isFloat")) {
                if (csvValue.equals("FLOAT")) {
                    mongoValue = true;
                }
                else if (csvValue.equals("FIXED")) {
                    mongoValue = false;
                }
                else {
                    csvDataInvalid = true;
                    errorMsg += "CSV value for field " + csvFieldName + " should either be FIXED or FLOAT. Current value is " + csvValue;
                }
            }
            else if (mongoDataType.equals("date")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                // Creates Date object from LocalDateTime, since Mongo driver works with Date
                try {
                    mongoValue = java.sql.Timestamp.valueOf(LocalDateTime.parse(csvValue, formatter));
                }
                catch (DateTimeParseException e) {
                    csvDataInvalid = true;
                    errorMsg += "CSV value for field " + csvFieldName + " is not in the required date time format of dd/MM/yyyy HH:mm. Current value is " + csvValue;
                }
                
            }
            else if (mongoDataType.equals("bool")) {
                if (csvValue.equals("Y")) {
                    mongoValue = true;
                }
                else if (csvValue.equals("N")) {
                    mongoValue = false;
                }
                else {
                    csvDataInvalid = true;
                    errorMsg += "CSV value for field " + csvFieldName + " should either be Y or N. Current value is " + csvValue;
                }
            }
            else {
                try {
                    switch (mongoDataType) {
                        case "int": mongoValue = Integer.parseInt(csvValue); break;
                        case "string": mongoValue = csvValue; break;
                        case "long": mongoValue = Long.parseLong(csvValue); break;
                        case "decimal": mongoValue = new Decimal128(new BigDecimal(csvValue)); break;
                        default: mongoValue = csvValue; break;
                    }
                }
                catch (NumberFormatException e) {
                    csvDataInvalid = true;
                    errorMsg += "CSV value for field " + csvFieldName + " could not be converted into numeric datatype. Current value is " + csvValue;
                }
            }
            if (csvDataInvalid) {
                failedDocumentErrorMsgs.add(errorMsg);
                return null;
            }
            document.append(mongoFieldName, mongoValue);

        }
        return document;
    }
}