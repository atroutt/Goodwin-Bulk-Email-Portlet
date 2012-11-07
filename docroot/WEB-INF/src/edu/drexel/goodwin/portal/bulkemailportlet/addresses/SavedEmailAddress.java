package edu.drexel.goodwin.portal.bulkemailportlet.addresses;

public class SavedEmailAddress {
	private long id;
	private String address;
	private String label;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return label + " <" + address + ">";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SavedEmailAddress))
			return false;
		
		SavedEmailAddress other = (SavedEmailAddress) obj;
		
		return label.equals(other.getLabel()) && address.equals(other.getAddress()) && id == other.getId();
	}
}
