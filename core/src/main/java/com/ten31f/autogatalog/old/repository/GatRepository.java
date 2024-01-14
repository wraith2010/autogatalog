package com.ten31f.autogatalog.old.repository;

public class GatRepository extends AbstractMongoRepository {

	public static final String COLLECTION_GATS = "gats";

	public GatRepository(String databaseURL) {
		super(databaseURL);
	}

	

//	public boolean isPresent(ObjectId objectId) {
//
//		return (getCollection().countDocuments(Filters.eq("imageFileObjectID", objectId)) > 0)
//				|| (getCollection().countDocuments(Filters.eq("fileObjectID", objectId)) > 0);
//	}

}
