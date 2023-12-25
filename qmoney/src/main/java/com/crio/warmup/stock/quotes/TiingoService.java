
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {


  private static RestTemplate restTemplate;

  public TiingoService(RestTemplate restTemplate) {
    TiingoService.restTemplate = restTemplate;
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {


   try{
        
    String url = buildUri(symbol, from, to, getToken());
    System.out.println(url);

   
    String apiresponse= restTemplate.getForObject(url, String.class);
   
    ObjectMapper om= getObjectMapper();

    TiingoCandle[] candle= om.readValue(apiresponse, TiingoCandle[].class);

    List<Candle> candles = new ArrayList<>();
  
    candles = Arrays.asList(candle);

    return candles;
   }catch(Exception e)
   {
     throw new StockQuoteServiceException(e);
   }

  }

  private ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
     
  }

  protected static String getToken() {
    String token = "885da87b807348a65bf70b2665b5d234d6c50585";
    return token;
  }


  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  // Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  // ./gradlew test --tests TiingoServiceTest


  // CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  // Write a method to create appropriate url to call the Tiingo API.
  protected static String buildUri(String symbol, LocalDate startDate, LocalDate endDate,
      String token) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate="
        + startDate + "&endDate=" + endDate + "&token=" + token + "";
    return uriTemplate;
  }




  // TODO: CRIO_TASK_MODULE_EXCEPTIONS
  //  1. Update the method signature to match the signature change in the interface.
  //     Start throwing new StockQuoteServiceException when you get some invalid response from
  //     Tiingo, or if Tiingo returns empty results for whatever reason, or you encounter
  //     a runtime exception during Json parsing.
  //  2. Make sure that the exception propagates all the way from
  //     PortfolioManager#calculateAnnualisedReturns so that the external user's of our API
  //     are able to explicitly handle this exception upfront.

  //CHECKSTYLE:OFF


}
