package com.ten31f.autogatalog.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ten31f.autogatalog.domain.WatchURL;

public interface WatchURLRepo extends MongoRepository<WatchURL, String> {

}
