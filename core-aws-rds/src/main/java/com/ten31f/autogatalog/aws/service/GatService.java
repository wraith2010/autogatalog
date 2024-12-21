package com.ten31f.autogatalog.aws.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ten31f.autogatalog.aws.repository.IGatRepositroy;
import com.ten31f.autogatalog.rds.domain.Gat;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class GatService {

	private IGatRepositroy gatRepositroy;

	public List<Gat> findAll() {
		return getGatRepositroy().findAll();
	}
	
	public Page<Gat> findAll(Pageable pageable) {
		return getGatRepositroy().findAll(pageable);
	}
	
	public long count() {
		return getGatRepositroy().count();
	}

	@Transactional
	public List<Gat> mostDownloaded() {
		return getGatRepositroy().findTop10ByOrderByDownloadsDesc();
	}

	@Transactional
	public List<Gat> mostViews() {
		return getGatRepositroy().findTop10ByOrderByViewsDesc();
	}

	@Transactional
	public Gat findByGuid(String guid) {
		return getGatRepositroy().findOneByGuid(guid);
	}

	@Transactional
	public List<Gat> findByAuthor(String author) {
		return getGatRepositroy().findAllByAuthor(author);
	}

	@Transactional
	public Gat save(Gat gat) {
		return getGatRepositroy().save(gat);
	}

	private IGatRepositroy getGatRepositroy() {
		return gatRepositroy;
	}
}
