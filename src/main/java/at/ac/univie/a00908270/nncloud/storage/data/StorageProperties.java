package at.ac.univie.a00908270.nncloud.storage.data;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {
	
	/**
	 * Folder location for storing files
	 */
	private String location = "upload-dir";
	
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
}
