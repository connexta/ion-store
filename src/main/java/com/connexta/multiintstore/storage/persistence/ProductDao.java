package com.connexta.multiintstore.storage.persistence;

import com.connexta.multiintstore.storage.persistence.models.Product;

import java.util.Optional;

public class ProductDao implements Dao<Product> {
    @Override
    public Optional get(long id) {
        return Optional.empty();
    }

    @Override
    public void save(Product product) {

    }

    @Override
    public void delete(Product product) {

    }
}
