package org.wso2;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingTimeWindowArrayReservoir;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jmx.JmxReporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class MetricsReader {
    private static final MetricRegistry metricRegistry = new MetricRegistry();
    private static Timer timer;
    private static final JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build();

    public static void main(String[] args){
        timer = metricRegistry.timer("response_times",
                () -> new Timer(new SlidingTimeWindowArrayReservoir(
                        20, TimeUnit.SECONDS)));

        jmxReporter.start();

        try(BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(System.in))){

            String line = bufferedReader.readLine();
            String[] temp;
            while(line != null) {
                if (!line.isEmpty()){
                    temp = line.split(" ");
                    if (temp.length==3)
                        timer.update(Long.parseLong(temp[2]), TimeUnit.MICROSECONDS);
                    else
                        System.out.println("Invalid log format");
                }
                line = bufferedReader.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
