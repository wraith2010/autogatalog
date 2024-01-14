package com.ten31f.autogatalog.repository;

import com.ten31f.autogatalog.domain.Gat;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface GatRepo extends MongoRepository<Gat, String> {

	@Query("{author : ?0}")
	List<Gat> findAllByAuthor(String author);

	@Query("{guid:?0}")
	Optional<Gat> findByGuid(String guid);

	@Query("{ imageFileObjectID: { $exists: false } }")
	List<Gat> findAllWithOutImage();

	@Query("{ fileObjectID: { $exists: false } }")
	List<Gat> findAllWithOutFile();

	boolean existsGatByFileObjectID(String fileObjectID);

	boolean existsGatByImagefileObjectID(String imagefileObjectID);
}
