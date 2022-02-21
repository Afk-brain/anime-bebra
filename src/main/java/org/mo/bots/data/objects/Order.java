package org.mo.bots.data.objects;

public class Order {

    public String spot_id = "1";
    public String phone = "+380957777777";
    public Product[] products;
    static class Product {
        public String product_id = "169";
        public int count = 1;
    }

}
