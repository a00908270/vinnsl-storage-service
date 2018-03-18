package at.ac.univie.a00908270.nncloud.storage;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileUploadIntegrationTests {
	
	/*@Autowired
	private TestRestTemplate restTemplate;
	
	@MockBean
	private StorageService storageService;
	
	@LocalServerPort
	private int port;
	
	@Test
	public void shouldUploadFile() throws Exception {
		ClassPathResource resource = new ClassPathResource("testupload.txt", getClass());
		
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		map.add("file", resource);
		ResponseEntity<String> response = this.restTemplate.postForEntity("/storage/", map,
				String.class);
		
		assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.FOUND);
		assertThat(response.getHeaders().getLocation().toString())
				.startsWith("http://localhost:" + this.port + "/storage");
		then(storageService).should().store(any(MultipartFile.class));
	}
	
	@Test
	public void shouldDownloadFile() throws Exception {
		ClassPathResource resource = new ClassPathResource("testupload.txt", getClass());
		given(this.storageService.loadAsResource("testupload.txt")).willReturn(resource);
		
		ResponseEntity<String> response = this.restTemplate
				.getForEntity("/storage/files/{filename}", String.class, "testupload.txt");
		
		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
				.isEqualTo("attachment; filename=\"testupload.txt\"");
		assertThat(response.getBody()).isEqualTo("Spring Framework");
}*/
	
}
