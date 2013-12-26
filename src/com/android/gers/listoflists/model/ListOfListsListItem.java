package com.android.gers.listoflists.model;

public class ListOfListsListItem {
	private long id;
	private long listId;
	private String name;
	private double rating;
	
	public boolean equals(Object obj)
	{
		// if the two objects are equal in reference, they are equal
		if (this == obj) {
			return true;
		} else if (obj instanceof ListOfListsListItem) {
			ListOfListsListItem item = (ListOfListsListItem) obj;
			if (	id == item.id
				 && listId == item.listId
				 && name.equals(item.name)
				 && rating == item.rating
			    )
			{
				return true;
			}
		}

		return false;
	}
	
	public int hashCode() {
		return (int)id;
	}

	public ListOfListsListItem(long listId, String name, double rating) {
		this(-1, listId, name, rating);
	}

	public ListOfListsListItem(long id, long listId, String name, double rating) {
		this.id = id;
		this.listId = listId;
		this.name = name;
		this.rating = rating;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getListId() {
		return listId;
	}

	public void setListId(long listId) {
		this.listId = listId;
	}

	public Double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public String getDisplayName() {
		return getName();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	@Override
	public String toString() {
		return String.format("id %d,  listId %d, rating %s",
							id, listId, rating);
	}	
}
