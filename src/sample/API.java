package sample;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class API {

    private LocalDate start;

    private LocalDate end;

    private List<APIData> values = new ArrayList<>();

    public API(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;

        initialize();
    }

    private void initialize() {
        for (LocalDate date = start; !date.isEqual(end.plusDays(1)); date = date.plusDays(1)) {
            System.out.println(date);
            ObjectMapper mapper = new ObjectMapper();
            try {
                APIData apiData = mapper.readValue(download(date), APIData.class);
                System.out.println(apiData.getDate());
                if (apiData.parseDate().compareTo(date) == 0) {
                    values.add(apiData);
                }
                Thread.sleep(200);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private BufferedReader download(LocalDate date) throws IOException {
        String day = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        URL url = new URL("http://api.fixer.io/" + day + "?base=PLN");
        URLConnection urlCon = url.openConnection();
        return new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
    }

    public List<APIData> getValues() {
        return values;
    }

    public Optional<APIData> getMin(String currency) {
        return values.stream().min((a, b) -> {
            return a.getRates().get(currency).compareTo(b.getRates().get(currency));
        });
    }

    public Optional<APIData> getMax(String currency) {
        return values.stream().max((a, b) -> {
            return a.getRates().get(currency).compareTo(b.getRates().get(currency));
        });
    }
}
