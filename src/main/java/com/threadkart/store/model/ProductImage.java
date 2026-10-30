package com.threadkart.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

/**
 * Images are saved in the database instead of the file system.
 * Free hosts like Render wipe the disk on every deploy, so files in a folder would disappear.
 */
@Entity
public class ProductImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Lob
	@Column(nullable = false, columnDefinition = "LONGBLOB")
	private byte[] data;

	@Column(length = 50)
	private String contentType;

	public ProductImage() {
	}

	public ProductImage(byte[] data, String contentType) {
		this.data = data;
		this.contentType = contentType;
	}

	public Long getId() {
		return id;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
