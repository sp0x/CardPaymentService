package com.cardService;

import com.cardService.payment.Payment;
import org.junit.Test;

import static org.junit.Assert.*;

public class PaymentTest {

    @Test
    public void getDescription() {
        Payment p = new Payment();
        p.setDescription("desc");
        assertEquals(p.getDescription(), "desc");
    }

    @Test
    public void setDescription() {
        Payment p = new Payment();
        p.setDescription("desc");
        assertEquals(p.getDescription(), "desc");
    }
}