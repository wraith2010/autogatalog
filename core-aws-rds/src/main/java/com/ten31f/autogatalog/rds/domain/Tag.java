package com.ten31f.autogatalog.rds.domain;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tags")
public class Tag {

	public Tag(String value) {
		setValue(value);
	}
	
	@Id
	private String value;

	@ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
	private Set<Gat> gats;

}
