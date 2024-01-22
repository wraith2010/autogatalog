package com.ten31f.autogatalog.repository;

import com.ten31f.autogatalog.domain.Gat;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface GatRepo extends MongoRepository<Gat, String> {

	@Query("{author : ?0}")
	List<Gat> findAllByAuthor(String author);

	@Query("{guid : ?0}")
	Optional<Gat> findByGuid(String guid);

	@Query("{ imagefileObjectID: { $exists: false } }")
	List<Gat> findAllWithOutImage();

	@Query("{ fileObjectID: { $exists: false } }")
	List<Gat> findAllWithOutFile();

	boolean existsGatByFileObjectID(String fileObjectID);

	boolean existsGatByImagefileObjectID(String imagefileObjectID);

	@Query("{ $text: { $search: ?0 } }")
	List<Gat> search(String searchString);

	@Aggregation(pipeline = { "{ '$group': { '_id' : '$tags' } }" })
	List<String> findDistinctTags();

	@Query("{'tags': ?0 }")
	List<Gat> findByTag(String tag);

	@Query("{'tags':{$nin:['NFPM']}}")
	List<Gat> findForFontPage(Pageable pageable);

}
