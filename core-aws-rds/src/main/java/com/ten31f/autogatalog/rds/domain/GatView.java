package com.ten31f.autogatalog.rds.domain;

import java.net.URL;
import java.util.Set;

public interface GatView {

	public long getId();

	public String getGuid();

	public String getTitle();

	public String getAuthor();

	public String getS3URLFile();

	public String getS3URLImage();

	public int getDownloads();

	public int getViews();

	public URL getLinkURL();

	public Set<Tag> getTags();

}
