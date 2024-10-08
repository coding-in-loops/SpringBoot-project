package com.ecom.shopping_cart.model;

public class OrderRequest {

	private String firstName;
	
	private String lastName;
	
	private String email;
	
	private String mobileNumber;
	
	private String address;
	
	private String city;
	
	private String state;
	
	private String pincode;
	
	private String paymentType;
	
	public OrderRequest() {}

	public OrderRequest(String firstName, String lastName, String email, String mobileNumber,
			String address, String city, String state, String pincode,String paymentType) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.mobileNumber = mobileNumber;
		this.address = address;
		this.city = city;
		this.state = state;
		this.pincode = pincode;
		this.paymentType=paymentType;
	}


	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	@Override
	public String toString() {
		return "OrderRequest [firstName=" + firstName + ", lastName=" + lastName + ", email=" + email
				+ ", mobileNumber=" + mobileNumber + ", address=" + address + ", city=" + city + ", state=" + state
				+ ", pincode=" + pincode + ", paymentType=" + paymentType + "]";
	}
	
	
}
