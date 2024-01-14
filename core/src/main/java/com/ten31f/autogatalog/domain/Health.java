package com.ten31f.autogatalog.domain;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.client.gridfs.model.GridFSFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "health")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Health {

	private long gatCount = 0;
	private int fileCount = 0;
	private int pendingDownloadCount = 0;

	private List<GridFSFile> orphans = null;
	private List<Gat> imagelessGats = null;
	private List<Gat> pendingDownload = null;

}
