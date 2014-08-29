package com.sj.weixin.web.entity;

import java.util.ArrayList;
import java.util.List;

public class AddsBook {
	String name;
	String id;
	List<XCard> xCardList = new ArrayList<XCard>();
	List<Folder> folderList = new ArrayList<Folder>();
	
	public void addXCard(XCard xCard)
	{
		
		this.xCardList.add(xCard);
	}
	
	public void addFolder(Folder folder)
	{
		
		this.folderList.add(folder);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<XCard> getxCardList() {
		return xCardList;
	}
	public void setxCardList(List<XCard> xCardList) {
		this.xCardList = xCardList;
	}
	public List<Folder> getFolderList() {
		return folderList;
	}
	public void setFolderList(List<Folder> folderList) {
		this.folderList = folderList;
	}
	
	
}
