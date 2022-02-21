package org.mo.bots.data.cart;

import org.mo.bots.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Cart {

    public List<Pair<String, Integer>> data;

    public Cart() {
        data = new ArrayList<>();
    }

    public void addItem(String id, int quantity) {
        for(Pair<String, Integer> item : data) {
            if(item.key.equals(id)) {
                item.value += quantity;
                return;
            }
        }
        Pair<String, Integer> item = new Pair<>(id, quantity);
        data.add(item);
    }

    public void removeItem(String id) {
        for(Pair<String, Integer> item : data) {
            if(item.key.equals(id)) {
                item.value--;
                if(item.value == 0) {
                    data.remove(item);
                }
                return;
            }
        }
    }

    public boolean isEmpty() {
        return data.size() == 0;
    }

}
