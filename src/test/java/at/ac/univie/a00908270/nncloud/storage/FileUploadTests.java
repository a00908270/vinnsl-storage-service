package at.ac.univie.a00908270.nncloud.storage;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class FileUploadTests {
	
	/*@Autowired
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
	}*/
	
}
