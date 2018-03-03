package at.ac.univie.a00908270.nncloud.storage;

import at.ac.univie.a00908270.nncloud.storage.data.StorageFileNotFoundException;
import at.ac.univie.a00908270.nncloud.storage.data.StorageService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class FileUploadTests {
	
	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private StorageService storageService;
	
	@Test
	public void shouldListAllFiles() throws Exception {
		given(this.storageService.loadAll())
				.willReturn(Stream.of(Paths.get("first.txt"), Paths.get("second.txt")));
		
		this.mvc.perform(get("/storage/")).andExpect(status().isOk())
				.andExpect(model().attribute("files",
						Matchers.contains("http://localhost/storage/files/first.txt",
								"http://localhost/storage/files/second.txt")));
	}
	
	@Test
	public void shouldSaveUploadedFile() throws Exception {
		MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt",
				"text/plain", "Spring Framework".getBytes());
		this.mvc.perform(fileUpload("/storage/").file(multipartFile))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", "/storage"));
		
		then(this.storageService).should().store(multipartFile);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void should404WhenMissingFile() throws Exception {
		given(this.storageService.loadAsResource("test.txt"))
				.willThrow(StorageFileNotFoundException.class);
		
		this.mvc.perform(get("/storage/files/test.txt")).andExpect(status().isNotFound());
	}
	
}
