package com.ten31f.autogatalog.aws.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.ten31f.autogatalog.rds.domain.Gat;
import com.ten31f.autogatalog.rds.domain.GatView;

public interface IGatRepositroy extends JpaRepository<Gat, Long> {

	public Gat findOneByGuid(String guid);

	public List<Gat> findAllByAuthor(String author);

	public List<GatView> findTop10ByOrderByDownloadsDesc();

	public List<GatView> findTop10ByOrderByViewsDesc();

	public List<GatView> findAllProjectedBy();

	public Page<GatView> findAllProjectedBy(Pageable pageable);
	
	public List<GatView> findByTitleLikeIgnoreCase(@Param("searchString") String searchString);

}
