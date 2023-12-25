
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.crio.warmup.stock.portfolio.PortfolioManagerImpl;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;



public class PortfolioManagerApplication {


  private static RestTemplate restTemplate= new RestTemplate();
  static PortfolioManager portfolioManager= PortfolioManagerFactory.getPortfolioManager(restTemplate);

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.


  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File filename= resolveFileFromResources(args[0]);
  
    ObjectMapper om = getObjectMapper();
    PortfolioTrade[] trade= om.readValue(filename, PortfolioTrade[].class);
    List<String> symbols= new ArrayList<>();
    symbols= Arrays.asList(trade).stream().map(PortfolioTrade::getSymbol).collect(Collectors.toList());
    return symbols;
 }



  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/harshitgupta1732000-ME_QMONEY_V2/qmoney/bin/main/trades.json";
     String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@3bf7ca37";
     String functionNameFromTestFileInStackTrace = "mainReadFile";
     String lineNumberFromTestFileInStackTrace = "31";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }


  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {

        List<PortfolioTrade> trade= readTradesFromJson(args[0]);
        List<String> res= new ArrayList<>();
        res= trade.stream()
                  .map(t->{
                    List<Candle> candles= fetchCandles(t, LocalDate.parse(args[1]), getToken());
                    return new TotalReturnsDto(t.getSymbol(), candles.get(candles.size()-1).getClose());
                  }).sorted().map(TotalReturnsDto::getSymbol).collect(Collectors.toList());

       return res;
  }

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
        File file= resolveFileFromResources(filename);
        ObjectMapper om = getObjectMapper();

        PortfolioTrade[] trade= om.readValue(file, PortfolioTrade[].class);
        List<PortfolioTrade> trades= new ArrayList<>();
        trades= Arrays.asList(trade).stream().collect(Collectors.toList());
        return trades; 
  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate localDate, String token) {
      String symbol= trade.getSymbol();
      LocalDate startDate= trade.getPurchaseDate();
      LocalDate endDate= localDate;
  
      String url= "https://api.tiingo.com/tiingo/daily/"+ symbol+"/prices?startDate="+startDate+"&endDate="+endDate+"&token="+token+"";

      return url;
  }
  // // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
      Candle firstCandle= candles.get(0);
      return firstCandle.getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    Candle lastCandle= candles.get(candles.size()-1);
     return lastCandle.getClose();
  }

  public static String getToken(){
    String token= "885da87b807348a65bf70b2665b5d234d6c50585";
    return token;
  }

  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    RestTemplate restTemplate = new RestTemplate();
    String url= prepareUrl(trade, endDate, token);
    List<Candle> candles= new ArrayList<>();

    TiingoCandle[] candle= restTemplate.getForObject(url,  TiingoCandle[].class);

    candles= Arrays.asList(candle).stream().collect(Collectors.toList());
     return candles;
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {

        List<PortfolioTrade> trade= readTradesFromJson(args[0]);
        List<AnnualizedReturn> annualizedReturns = trade.stream().map(t -> {
                                                                List<Candle> candles= new ArrayList<>();
                                                                candles = fetchCandles(t,LocalDate.parse(args[1]), getToken());
                                                                return calculateAnnualizedReturns(LocalDate.parse(args[1]), t, getOpeningPriceOnStartDate(candles), getClosingPriceOnEndDate(candles));
                                                                }).sorted()
                                                                .collect(Collectors.toList());
     return annualizedReturns;
  }

  // // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // //  Return the populated list of AnnualizedReturn for all stocks.
  // //  Annualized returns should be calculated in two steps:
  // //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  // //      1.1 Store the same as totalReturns
  // //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  // //      The formula is:
  // //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  // //      2.1 Store the same as annualized_returns
  // //  Test the same using below specified command. The build should be successful.
  // //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {


        Double totalReturns= (sellPrice-buyPrice)/buyPrice;

        Double total_num_years= (double)(ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate))/365.24;

        Double annualizedReturn= Math.pow(1+totalReturns, 1/total_num_years)-1;
        
      
        
      return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturns);
  }

























































  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       
       LocalDate endDate = LocalDate.parse(args[1]);
      //  String contents = readFileAsString(file);
      //  ObjectMapper objectMapper = getObjectMapper();
      File filename= resolveFileFromResources(file);
    
      ObjectMapper om = getObjectMapper();
      PortfolioTrade[] trade= om.readValue(filename, PortfolioTrade[].class);
      
  
      return portfolioManager.calculateAnnualizedReturn(Arrays.asList(trade), endDate);
  }


  



 


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

   //  printJsonObject(mainReadFile(args));

    // printJsonObject(resolveFileFromResources(args[0]));
    
  
    //  printJsonObject(mainReadQuotes(args));

    //printJsonObject(mainCalculateSingleReturn(args));
  
    printJsonObject(mainCalculateReturnsAfterRefactor(args));

  }




}








  


