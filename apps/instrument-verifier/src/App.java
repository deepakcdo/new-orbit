import java.io.FileReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.opencsv.CSVReader;

import org.bson.Document;
import org.bson.types.Decimal128;

public class App {
    // Mapping between Mongo field names and CSV field names
    public static Map<String, String[]> csvToMongoMapping = new HashMap<>();

    public static void main(String[] args) throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        buildCsvMongoMapping();

        List<String> csvFieldNames = getBondCsvFieldNames();
        List<List<String>> csvData = getBondCsvData();

        MongoClientURI uri = new MongoClientURI(
    "mongodb://admin:Thermalsight1@cluster0-shard-00-00.x1ane.azure.mongodb.net:27017,cluster0-shard-00-01.x1ane.azure.mongodb.net:27017,cluster0-shard-00-02.x1ane.azure.mongodb.net:27017/finance?ssl=true&replicaSet=atlas-60jw2s-shard-0&authSource=admin&retryWrites=true&w=majority");

        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase database = mongoClient.getDatabase("finance");

        MongoCollection<Document> bonds = database.getCollection("bonds");

        List<Document> documents = bonds.find().into(new ArrayList<>());

        List<String> errorMessages = new ArrayList<>();

        for (int i = 0; i < documents.size(); i++) {
            // Turns a line of CSV data into Mongo document format so that it can be compared against the document read from the database
            Document csvDocument = buildMongoDocument(csvData.get(i), csvFieldNames, 1);

            String errorMsg = checkDocument(documents.get(i), csvDocument, "");
            if (errorMsg != null) {
                errorMessages.add("Document " + i + " is incorrect: " + errorMsg);
            }
        }

        if (errorMessages.size() == 0) {
            System.out.println("All documents are correct!");
        }
        else {
            for (String msg : errorMessages) {
                System.out.println(msg);
            }
        }
        
        mongoClient.close();
    }


    // Checks a MongoDB document against its corresponding CSV line (where the line is passed into this function in document format)
    // Returns error message, null if everything is correct
    public static String checkDocument(Document mongoDocument, Document csvDocument, String documentName) {
        // The Mongo document from the database has an extra _id field so should have exactly 1 more field than the CSV version
        if (documentName.equals("") && csvDocument.size() != mongoDocument.size() - 1) {
            return "Wrong number of fields";
        }

        // This is the test for nested document
        if (!documentName.equals("") && csvDocument.size() != mongoDocument.size()) {
            return "Key " + documentName + ": Wrong number of fields";
        }

        // Check each entry of the Mongo document and compare it to the corresponding CSV entry
        for (Map.Entry<String, Object> entry : mongoDocument.entrySet()) {
            String key = entry.getKey();
            // In the case of nested document, we recurse so that the inner fields can be checked one by one
            if (entry.getValue().getClass().equals(Document.class) && csvDocument.get(key).getClass().equals(Document.class)) {
                String errorMsg = checkDocument((Document)entry.getValue(), (Document)csvDocument.get(key), key);
                if (errorMsg != null) {
                    return errorMsg;
                }
            }
            else if (!key.equals("_id") && !entry.getValue().equals(csvDocument.get(key))) {
                return "Wrong value for key: " + (documentName.equals("") ? "" : (documentName + ".")) + 
                    key + "; Expected " + csvDocument.get(key).toString() + ", Actual " + entry.getValue().toString();
            }
        }

        return null;
    }

    // Builds a MongoDB document from a single line of CSV data
    public static Document buildMongoDocument(List<String> csvLine, List<String> csvFieldNames, int recurseLevel) {
        Document document = new Document();
        for (int i = 0; i < csvLine.size(); i ++) {
            String csvValue = csvLine.get(i).trim();
            String csvFieldName = csvFieldNames.get(i).trim();

            if (csvFieldName.equals("dayCount") && recurseLevel > 0) {
                Document nestedDocument = buildMongoDocument(csvLine.subList(i, i + 3), csvFieldNames.subList(i, i + 3), 0);
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

            if (mongoFieldName.equals("isFloat")) {
                mongoValue = !csvValue.equals("FIXED");
            }
            else if (mongoDataType.equals("date")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                // Creates Date object from LocalDateTime, since Mongo driver works with Date
                mongoValue = java.sql.Timestamp.valueOf(LocalDateTime.parse(csvValue, formatter));
            }
            else if (mongoFieldName.equals("endOfMonthRule")) {
                mongoValue = csvValue.equals("Y");
            }
            else {
                switch (mongoDataType) {
                    case "int": mongoValue = Integer.parseInt(csvValue); break;
                    case "string": mongoValue = csvValue; break;
                    case "long": mongoValue = Long.parseLong(csvValue); break;
                    case "decimal": mongoValue = new Decimal128(new BigDecimal(csvValue)); break;
                    default: mongoValue = csvValue; break;
                }
            }
            document.append(mongoFieldName, mongoValue);

        }
        return document;
    }


    public static List<String> getBondCsvFieldNames() throws Exception {
        // Read the first line of the csv which contains the field names
        List<String> csvFieldNames = null;
        try (CSVReader csvReader = new CSVReader(new FileReader("bonds.csv"));) {
            final String[] fieldNames = csvReader.readNext();
            csvFieldNames = Arrays.asList(fieldNames);
        }
        // Without this line the first field name always has a char value of -1 at the beginning, which needs to be removed
        csvFieldNames.set(0, csvFieldNames.get(0).substring(1));

        return csvFieldNames;
    }

    public static List<List<String>> getBondCsvData() throws Exception {
        // Read the csv values into a 2-dimensional table of strings
        final List<List<String>> csvData = new ArrayList<List<String>>();

        try (CSVReader csvReader = new CSVReader(new FileReader("bonds.csv"));) {
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
    
}
