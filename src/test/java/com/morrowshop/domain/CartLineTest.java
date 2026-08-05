package com.morrowshop.domain;
import static org.junit.jupiter.api.Assertions.assertEquals;import java.math.BigDecimal;import org.junit.jupiter.api.Test;
class CartLineTest { @Test void calculatesSubtotalWithoutFloatingPointError(){CartLine line=new CartLine();line.setPrice(new BigDecimal("12900.50"));line.setQuantity(3);assertEquals(new BigDecimal("38701.50"),line.getSubtotal());} }
