package org.mo.bots.data.cart;

import java.util.HashMap;
import java.util.Map;

public class RuntimeCartStore implements CartStore {

    public Map<String, Cart> data = new HashMap<>();

    @Override
    public Cart getCart(String id) {
        return data.get(id);
    }

}
