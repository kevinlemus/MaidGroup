package com.maidgroup.maidgroup.util.square.mock;

import com.squareup.square.SquareClient;
import com.squareup.square.api.OrdersApi;

public class SquareClientWrapperImpl implements SquareClientWrapper{
    private final SquareClient squareClient;

    public SquareClientWrapperImpl(SquareClient squareClient) {
        this.squareClient = squareClient;
    }

    @Override
    public OrdersApi getOrdersApi() {
        return squareClient.getOrdersApi();
    }
}
