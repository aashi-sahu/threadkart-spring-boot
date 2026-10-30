package com.threadkart.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CheckoutForm {

	@NotBlank(message = "Name is required")
	@Size(max = 100)
	private String fullName;

	@NotBlank(message = "Phone number is required")
	@Pattern(regexp = "[6-9]\\d{9}", message = "Enter a 10 digit mobile number")
	private String phone;

	@NotBlank(message = "Address is required")
	@Size(max = 200)
	private String addressLine1;

	@Size(max = 200)
	private String addressLine2;

	@NotBlank(message = "City is required")
	@Size(max = 60)
	private String city;

	@NotBlank(message = "State is required")
	@Size(max = 60)
	private String state;

	@NotBlank(message = "Pincode is required")
	@Pattern(regexp = "\\d{6}", message = "Pincode should be 6 digits")
	private String pincode;

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
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
}
