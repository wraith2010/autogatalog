package com.ten31f.autogatalog.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ten31f.autogatalog.domain.Health;

public interface HealthRepo extends MongoRepository<Health, String> {

}
