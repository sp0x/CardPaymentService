package com.cardService.payment;

import java.util.Map;

public interface IBankResponseParser {
    Map<String, String> parse(String payload);
}
