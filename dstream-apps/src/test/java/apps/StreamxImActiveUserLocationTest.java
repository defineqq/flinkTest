package apps;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.earthchen.ipplus360.awdb.db.AWReader;
import io.github.earthchen.ipplus360.awdb.exception.IpTypeException;
import junit.framework.TestCase;
import org.apache.flink.streaming.api.transformations.SideOutputTransformation;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.FileSystem;

public class StreamxImActiveUserLocationTest {

    @Test
    public void run() {
        try {
            //demo_IP_city_single_WGS84_awdb.awdb
            //IPv4城市库中文版txt格式demo.txt
            String ips = "154.53.57.207";
            System.out.println("ips：" + ips);
            String[] ipArray = ips.split(",");
            String filePath = "/Users/joyme/IP_city_single_WGS84.awdb";

//            URL url = new URL("http://localhost:8000/IP_city_single_WGS84.awdb");
//            InputStream is = url.openStream();

            File file = new File(filePath);

            int ipCount = 0;
            for (String ip :
                    ipArray) {

                try (AWReader awReader = new AWReader(file);) {
                    InetAddress address = InetAddress.getByName(ip);
                    JsonNode record = awReader.get(address);
                    if (record != null) {
//                    JsonNode continent = record.get("continent");
//                    if (continent != null) {
//                        System.out.println(continent.asText());
//                    }
//
//                    JsonNode country = record.get("country");
//                    if (country != null) {
//                        System.out.println(country.asText());
//                    }

                        JsonNode city = record.get("city");
                        if (city != null) {
                            System.out.println(city.asText());
                            String location = record.get("latwgs").textValue() + "," + record.get("lngwgs").textValue();
                            System.out.println(location instanceof String);
                            System.out.println("location:" + location);
                        }

//                    JsonNode accuracy = record.get("accuracy");
//                    if (accuracy != null) {
//                        System.out.println(accuracy.asText());
//                    }
                    } else {
                        System.out.println(ip + " is invalid");
                    }
                }


            }


        } catch (IpTypeException exception) {
            System.out.println(exception.getMessage());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}