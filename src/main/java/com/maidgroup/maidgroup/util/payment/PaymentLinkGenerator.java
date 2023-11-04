package com.maidgroup.maidgroup.util.payment;

import com.maidgroup.maidgroup.model.Invoice;
import com.squareup.square.SquareClient;
import com.squareup.square.exceptions.ApiException;
import com.squareup.square.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Component
public class PaymentLinkGenerator {
    @Value("${square.access-token}") //connects the program with your square account
    private String squareAccessToken;
    @Value("${square.location-id}") //connects the program with the specific project on your square account
    private String squareLocationId;
    /*@Value("${square.redirect-url}")
    private String redirectUrl;*/

    public String generatePaymentLink(Invoice invoice) {
        // Create Square client
        SquareClient squareClient = new SquareClient.Builder()
                .accessToken(squareAccessToken)
                .build();

        // Create line items from invoice items
        List<OrderLineItem> lineItems = invoice.getItems().stream()
                .map(item -> new OrderLineItem.Builder("1")
                        .name(item.getName())
                        .basePriceMoney(new Money.Builder()
                                .amount((long) (item.getPrice() * 100))
                                .currency("USD")
                                .build())
                        .build())
                .collect(Collectors.toList());

        // Create order
        Order order = new Order.Builder(squareLocationId)
                .lineItems(lineItems)
                .build();

        // Create checkout request
        CreateCheckoutRequest request = new CreateCheckoutRequest.Builder(
                UUID.randomUUID().toString(),
                new CreateOrderRequest.Builder()
                        .order(order)
                        .build())
                //.redirectUrl(redirectUrl)
                .build();

        try {
            // Create checkout
            CreateCheckoutResponse response = squareClient.getCheckoutApi().createCheckout(squareLocationId, request);
            return response.getCheckout().getCheckoutPageUrl();
        } catch (ApiException e) {
            throw new RuntimeException("Failed to create checkout", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}



