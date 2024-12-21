package com.ten31f.autogatalog.aws.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ten31f.autogatalog.rds.domain.Gat;

public interface IGatRepositroy extends JpaRepository<Gat, Long> {

	
	public List<Gat> findTop10ByOrderByDownloadsDesc();
	
	public List<Gat> findTop10ByOrderByViewsDesc();

	public Gat findOneByGuid(String guid);
	
	public List<Gat> findAllByAuthor(String author);
	
	
}
