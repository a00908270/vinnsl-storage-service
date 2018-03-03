package at.ac.univie.a00908270.nncloud.storage.data;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {
	
	void init();
	
	String store(MultipartFile file);
	
	String storeFromUrl(URL url);
	
	Stream<Path> loadAll();
	
	Path load(String filename);
	
	Resource loadAsResource(String filename);
	
	void deleteAll();
	
}
