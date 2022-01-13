package org.mo.bots.data;

import org.mo.bots.data.objects.Category;
import org.mo.bots.data.objects.Product;

public interface DataProvider {

    Category[] getCategories();

    Product[] getProductsByCategory(String groupId);

    Product getProductById(String id);

}
