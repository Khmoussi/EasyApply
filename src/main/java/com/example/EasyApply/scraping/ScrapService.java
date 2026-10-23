package com.example.EasyApply.scraping;

import com.example.EasyApply.Data.CompanyDetailsRequest;
import com.example.EasyApply.Data.CompanyDetailsResponse;
import com.example.EasyApply.Services.AutoEmailService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScrapService {

    private final AutoEmailService autoEmailService;

    private final ArrayList<String> combinations = new ArrayList<>(Arrays.asList(
            "",
            "/contact.html",
            "/contact.php",
            "/contact-us",
            "/contact",
            "/contactus",
            "/fr/contact"
    ));

    public ScrapService(AutoEmailService autoEmailService) {
        this.autoEmailService = autoEmailService;
    }

    String prepareHtml(String url) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
        try {
            response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity httpEntity = response.getEntity();
                return EntityUtils.toString(httpEntity, "utf-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private Document getHtmlDocument(String html) {
        return Jsoup.parse(html);
    }

    public CompanyDetailsResponse scrapeEmailsFromCompanies(ArrayList<CompanyDetailsRequest> request) {
        ConcurrentHashMap<String, String> failedMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, CompanyDetailsRequest> successMap = new ConcurrentHashMap<>();
        int poolSize = 10;
        int maxQueueSize = 100;
        ExecutorService executorService = new ThreadPoolExecutor(
                poolSize,
                poolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(maxQueueSize)
        );

        request.forEach(item -> executorService.submit(() -> {
            ArrayList<String> emails = this.scrapeEmails(item.getWebsite());
            if (emails.isEmpty()) {
                failedMap.put(item.getWebsite(), item.getName());
            } else {
                successMap.put(emails.get(0), item);
            }
        }));
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(30, TimeUnit.MINUTES)) {
                System.err.println("Some tasks were not completed within the timeout.");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        CompanyDetailsResponse result = new CompanyDetailsResponse(
                new HashMap<>(successMap),
                new HashMap<>(failedMap)
        );
        autoEmailService.sendForScrapedCompanies(result.getSuccessMap());
        return result;
    }

    public ArrayList<String> scrapeEmails(String url) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < combinations.size(); i++) {
            String html = prepareHtml(url + combinations.get(i));
            Document document = getHtmlDocument(html);
            list = prepareHrefs(document);
            if (list.isEmpty()) {
                list = prepareTexts(document);
            }
            if (!list.isEmpty()) {
                return list;
            }
        }
        return list;
    }

    public ArrayList<String> prepareHrefs(Document document) {
        System.out.println("1st step : fetching hrefs");
        ArrayList<String> list = new ArrayList<>();
        document.getElementsByAttribute("href").forEach(elt -> {
            String href = elt.attr("href");
            String email = getEmailFromHref(href);
            if (!email.isEmpty()) {
                list.add(email);
            }
        });
        return list;
    }

    public ArrayList<String> prepareTexts(Document document) {
        System.out.println("2nd step : fetching text");
        ArrayList<String> list = new ArrayList<>();
        String text = document.text();
        if (!text.isEmpty()) {
            list = findEmailInText(text);
        }
        return list;
    }

    ArrayList<String> findEmailInText(String text) {
        ArrayList<String> list = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (detectEmailsInToken(token)) {
                list.add(token);
            }
        }
        return list;
    }

    boolean detectEmailsInToken(String token) {
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(token);
        return matcher.matches();
    }

    String getEmailFromHref(String href) {
        String emailRegex = "([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})";
        Pattern pattern = Pattern.compile(emailRegex);
        if (href.startsWith("mailto:")) {
            Matcher matcher = pattern.matcher(href.replace("mailto:", ""));
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return "";
    }
}
