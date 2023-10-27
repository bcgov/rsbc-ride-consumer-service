package bcgov.rsbc.ride;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.Objects;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CsvProcessorService {
    private static final Logger logger = Logger.getLogger(CsvProcessorService.class);

    @ConfigProperty(name = "ride.geolocation.processed.file")
    private String processedFilepath;

    @ConfigProperty(name = "ride.geolocation.decode.method")
    private String decodeMethod;

    @ConfigProperty(name = "ride.geolocation.failed.file")
    private String failedFilepath;

    @Transactional
    public void processCsv(String csvFilePath) throws Exception {
        logger.info("Processing CSV file: " + csvFilePath);
        try (Reader in = new FileReader(csvFilePath)) {
            Iterable<CSVRecord> records = CSVFormat.EXCEL.withFirstRecordAsHeader().parse(in);
            FileWriter csvWriter = new FileWriter(processedFilepath,true);
            csvWriter.append("date");
            csvWriter.append(",");
            csvWriter.append("ticket");
            csvWriter.append(",");
            csvWriter.append("lat");
            csvWriter.append(",");
            csvWriter.append("long");
            csvWriter.append("\n");
//            csvWriter.flush();
//            csvWriter.close();

            FileWriter failedcsvWriter = new FileWriter(failedFilepath,true);
            failedcsvWriter.append("date");
            failedcsvWriter.append(",");
            failedcsvWriter.append("ticket");
            failedcsvWriter.append(",");
            failedcsvWriter.append("lat");
            failedcsvWriter.append(",");
            failedcsvWriter.append("long");
            failedcsvWriter.append("\n");
//            failedcsvWriter.flush();
//            failedcsvWriter.close();

            for (CSVRecord record : records) {
                try{
                    String xvalue = record.get("x");
                    String yvalue = record.get("y");
                    String ticket = record.get("ticket");
                    String dateval=record.get("date");


    //                process x and y values
                    if (Objects.equals(decodeMethod, "BCALBERS")) {
                        ConverterBCAlbersToWGS84 converterBCAlbersToWGS84 = new ConverterBCAlbersToWGS84();
                        double[] respval = converterBCAlbersToWGS84.convert(xvalue, yvalue);
                        double latvalue = respval[0];
                        double longvalue = respval[1];
//                        csvWriter = new FileWriter(processedFilepath, true);
                        csvWriter.append(dateval);
                        csvWriter.append(",");
                        csvWriter.append(ticket);
                        csvWriter.append(",");
                        csvWriter.append(String.valueOf(latvalue));
                        csvWriter.append(",");
                        csvWriter.append(String.valueOf(longvalue));
                        csvWriter.append("\n");
//                        csvWriter.flush();
//                        csvWriter.close();
                    } else if (Objects.equals(decodeMethod, "UTM")) {
                        ConverterUTMToWGS84 converterUTMToWGS84 = new ConverterUTMToWGS84();
                        double[] respval = converterUTMToWGS84.convert(xvalue, yvalue);
                        double latvalue = respval[0];
                        double longvalue = respval[1];
//                        csvWriter = new FileWriter(processedFilepath, true);
                        csvWriter.append(dateval);
                        csvWriter.append(",");
                        csvWriter.append(ticket);
                        csvWriter.append(",");
                        csvWriter.append(String.valueOf(latvalue));
                        csvWriter.append(",");
                        csvWriter.append(String.valueOf(longvalue));
                        csvWriter.append("\n");
//                        csvWriter.flush();
//                        csvWriter.close();
                    }
            }catch(Exception e){
                    logger.error("Error processing record: " + record.toString());
                    logger.error(e.getMessage());
//                    csvWriter.flush();
//                    csvWriter.close();

                    logger.error("Writing failed record to file: " + failedFilepath);

                    String xvalue = record.get("x");
                    String yvalue = record.get("y");
                    String ticket = record.get("ticket");
                    String dateval=record.get("date");
                    ConverterUTMToWGS84 converterUTMToWGS84 = new ConverterUTMToWGS84();
                    double[] respval = converterUTMToWGS84.convert(xvalue, yvalue);
                    double latvalue = respval[0];
                    double longvalue = respval[1];
                    failedcsvWriter = new FileWriter(failedFilepath, true);
                    failedcsvWriter.append(dateval);
                    failedcsvWriter.append(",");
                    failedcsvWriter.append(ticket);
                    failedcsvWriter.append(",");
                    failedcsvWriter.append(String.valueOf(latvalue));
                    failedcsvWriter.append(",");
                    failedcsvWriter.append(String.valueOf(longvalue));
                    failedcsvWriter.append("\n");
//                    failedcsvWriter.flush();
//                    failedcsvWriter.close();
                }

            }
            csvWriter.flush();
            csvWriter.close();
            failedcsvWriter.flush();
            failedcsvWriter.close();
        }
        logger.info("Completed processing CSV file: " + csvFilePath);


    }

}
