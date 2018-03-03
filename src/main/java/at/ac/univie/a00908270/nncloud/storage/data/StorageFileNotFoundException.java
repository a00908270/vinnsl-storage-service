package at.ac.univie.a00908270.nncloud.storage.data;

public class StorageFileNotFoundException extends StorageException {
	
	public StorageFileNotFoundException(String message) {
		super(message);
	}
	
	public StorageFileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}