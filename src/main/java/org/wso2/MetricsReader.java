package org.wso2;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingTimeWindowArrayReservoir;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jmx.JmxReporter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class MetricsReader {
    private static final MetricRegistry metricRegistry = new MetricRegistry();
    private static Timer timer;
    private static final JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build();


    private static long[] parseJSON(String jsonString){
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return new long[]{Long.parseLong(jsonObject.get("timestamp").toString().replace("\"", "")),
                    Long.parseLong(jsonObject.get("response_time").toString().replace("\"", ""))};

        }
        catch (IllegalStateException ex){
            System.out.println("Received an invalid JSON object");
        }

        return new long[]{-1, -1};
    }

    public static void main(String[] args){
        timer = metricRegistry.timer("response_times",
                () -> new Timer(new SlidingTimeWindowArrayReservoir(
                        20, TimeUnit.SECONDS)));

        jmxReporter.start();

        try(BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(System.in))){

            String line = bufferedReader.readLine();
            while(line != null) {
                if (!line.isEmpty()){
                    timer.update(parseJSON(line)[1], TimeUnit.MICROSECONDS);
                }
                line = bufferedReader.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
