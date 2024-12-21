package com.ten31f.autogatalog.aws.service;

import java.util.List;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ten31f.autogatalog.aws.repository.IGatRepositroy;
import com.ten31f.autogatalog.aws.repository.ITagRepositroy;
import com.ten31f.autogatalog.rds.domain.Gat;
import com.ten31f.autogatalog.rds.domain.GatView;
import com.ten31f.autogatalog.rds.domain.Tag;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class GatService {

	private IGatRepositroy gatRepositroy;
	private ITagRepositroy tagRepositroy;

	public List<Gat> findAll() {
		return getGatRepositroy().findAll();
	}

	public Page<Gat> findAll(Pageable pageable) {
		return getGatRepositroy().findAll(pageable);
	}

	public List<Tag> findAllTags() {
		return getTagRepositroy().findAll();
	}

	@Transactional
	public Tag findTagByID(String id, boolean fetchGats) {

		Optional<Tag> tagOptional = getTagRepositroy().findById(id);

		if (tagOptional.isEmpty())
			return null;

		Tag tag = tagOptional.get();

		if (fetchGats) {
			Hibernate.initialize(tag.getGats());
		}

		return tag;
	}

	public long count() {
		return getGatRepositroy().count();
	}

	@Transactional
	public List<GatView> mostDownloaded() {
		return getGatRepositroy().findTop10ByOrderByDownloadsDesc();
	}

	@Transactional
	public List<GatView> mostViews() {
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

		if (gat.getTags() != null && !gat.getTags().isEmpty()) {
			getTagRepositroy().saveAll(gat.getTags());
		}

		return getGatRepositroy().save(gat);
	}

	@Transactional
	public List<GatView> findAllLightWeight() {
		return getGatRepositroy().findAllProjectedBy();
	}

	@Transactional
	public Page<GatView> findAllLightWeight(Pageable pageable) {
		return getGatRepositroy().findAllProjectedBy(pageable);
	}

	public Optional<Tag> findById(String id) {
		return getTagRepositroy().findById(id);
	}

	@Transactional
	public List<GatView> search(String searchString) {
		return getGatRepositroy().findByDescriptionLike(searchString);
	}
	
	private IGatRepositroy getGatRepositroy() {
		return gatRepositroy;
	}

	private ITagRepositroy getTagRepositroy() {
		return tagRepositroy;
	}

}
