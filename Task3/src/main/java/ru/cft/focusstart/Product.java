package ru.cft.focusstart;

import java.util.concurrent.atomic.AtomicInteger;

class Product {
    private static AtomicInteger productIdGenerator = new AtomicInteger(1);
    private int productID;

    Product() {
        productID = productIdGenerator.getAndIncrement();
    }

    int getProductID() {
        return productID;
    }
}
