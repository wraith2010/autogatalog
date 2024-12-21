package com.ten31f.autogatalog.aws.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ten31f.autogatalog.rds.domain.Tag;

public interface ITagRepositroy extends JpaRepository<Tag, String> {	

}
