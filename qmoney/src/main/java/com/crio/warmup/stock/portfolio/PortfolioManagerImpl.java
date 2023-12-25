
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {



   private RestTemplate restTemplate;
   private  static StockQuotesService stockQuotesService;
  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  public PortfolioManagerImpl(StockQuotesService stockQuoteService) {
    PortfolioManagerImpl.stockQuotesService= stockQuoteService;
  }

  public PortfolioManagerImpl(RestTemplate restTemplate)
  {
     this.restTemplate= restTemplate;
  }


  // TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  // Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  // clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  // CHECKSTYLE:OFF

  

  public static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    Candle firstCandle = candles.get(0);
    return firstCandle.getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    Candle lastCandle = candles.get(candles.size() - 1);
    return lastCandle.getClose();
  }

  public static String getToken() {
    String token = "885da87b807348a65bf70b2665b5d234d6c50585";
    return token;
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate)  {

    List<AnnualizedReturn> annualizedReturns = portfolioTrades.stream().map(t -> {
      List<Candle> candles = new ArrayList<>();

      try {
        candles = stockQuotesService.getStockQuote(t.getSymbol(), t.getPurchaseDate(), endDate);
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      } catch (StockQuoteServiceException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      return calculateAnnualizedReturns(endDate, t, getOpeningPriceOnStartDate(candles),
          getClosingPriceOnEndDate(candles));
    }).sorted(getComparator()).collect(Collectors.toList());

    return annualizedReturns;
  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
      Double buyPrice, Double sellPrice) {

    Double totalReturns = (sellPrice - buyPrice) / buyPrice;

    Double total_num_years =
        (double) (ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate)) / 365.24;

    Double annualizedReturn = Math.pow(1 + totalReturns, 1 / total_num_years) - 1;

    return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturns);
  }

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
 

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  // CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Extract the logic to call Tiingo third-party APIs to a separate function.
  // Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {

    String url = buildUri(symbol, from, to, getToken());
    List<Candle> candles = new ArrayList<>();

    String apiresponse= restTemplate.getForObject(url, String.class);


    ObjectMapper om= new ObjectMapper();

    TiingoCandle[] candle= om.readValue(apiresponse, TiingoCandle[].class);
    candles = Arrays.asList(candle).stream().collect(Collectors.toList());

    return candles;

  }

  

  protected static String buildUri(String symbol, LocalDate startDate, LocalDate endDate,
      String token) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate="
        + startDate + "&endDate=" + endDate + "&token=" + token + "";
    return uriTemplate;
  }




  // ¶TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.



}
