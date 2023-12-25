
package com.crio.warmup.stock.exception;

public class StockQuoteServiceException extends Exception {

  public StockQuoteServiceException(Exception e) {
    super(e);
  }

  public StockQuoteServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
