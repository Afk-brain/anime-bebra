package org.mo.bots.data.cart;

import java.util.HashMap;
import java.util.Map;

public class RuntimeCartStore implements CartStore {

    public Map<String, Cart> data = new HashMap<>();

    @Override
    public Cart getCart(String id) {
        return data.get(id);
    }

    @Override
    public void addItem(String userId, String itemId) {
        Cart cart = getCart(userId);
        if(cart == null) {
            cart = new Cart();
        }
        cart.addItem(itemId, 1);
        data.put(userId, cart);
    }

}
