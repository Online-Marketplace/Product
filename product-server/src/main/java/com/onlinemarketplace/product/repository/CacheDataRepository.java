package com.onlinemarketplace.product.repository;

import com.onlinemarketplace.product.model.CacheData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CacheDataRepository extends CrudRepository<CacheData, String> {
}