package model;

public class Item 
{
	public int item_id;
	public String name;
	public String description;
	public String category;
	public String condition;
	public String city;
	public String state;
	public String zipCode;
	

	public Item(String name, String description, String category, String condition, String city, String state,
			String zipCode) {
		super();
		this.name = name;
		this.description = description;
		this.category = category;
		this.condition = condition;
		this.city = city;
		this.state = state;
		this.zipCode = zipCode;
	}



	public Item(int item_id, String name, String description, String category, String condition, String city,
			String state, String zipCode) {
		super();
		this.item_id = item_id;
		this.name = name;
		this.description = description;
		this.category = category;
		this.condition = condition;
		this.city = city;
		this.state = state;
		this.zipCode = zipCode;
	}



	public boolean isMissingAnyNN()
	{
		if(name == null || category == null || condition == null || city == null || state == null || zipCode == null)
			return true;
		else
			return false;
	}
	
	
	
	public int getItem_id() {
		return item_id;
	}

	public void setItem_id(int item_id) {
		this.item_id = item_id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}



	public String getCity() {
		return city;
	}



	public void setCity(String city) {
		this.city = city;
	}



	public String getState() {
		return state;
	}



	public void setState(String state) {
		this.state = state;
	}



	public String getZipCode() {
		return zipCode;
	}



	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}



	
	
}
