package org.mo.bots.data.cart;

public interface CartStore {

    Cart getCart(String id);

    void addItem(String userId, String itemId, int amount);

    void removeItem(String userId, String itemId);

}
