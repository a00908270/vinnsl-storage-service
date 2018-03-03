package at.ac.univie.a00908270.nncloud.storage;

import at.ac.univie.a00908270.nncloud.storage.data.StorageFileNotFoundException;
import at.ac.univie.a00908270.nncloud.storage.data.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/storage")
public class VinnslStorageController {
	
	private final StorageService storageService;
	
	@Autowired
	public VinnslStorageController(StorageService storageService) {
		this.storageService = storageService;
	}
	
	@GetMapping
	public String listUploadedFiles(Model model) throws IOException {
		
		model.addAttribute("files", storageService.loadAll().map(
				path -> {
					return MvcUriComponentsBuilder.fromMethodName(VinnslStorageController.class,
							"serveFile", path.getFileName().toString()).build().toString();
				})
				.collect(Collectors.toList()));
		
		return "uploadForm";
	}
	
	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
		
		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}
	
	@PostMapping
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
								   RedirectAttributes redirectAttributes) {
		
		String uuidFilename;
		uuidFilename = storageService.store(file);
		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + uuidFilename + "!");
		
		return "redirect:/storage";
	}
	
	@PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> handleRestFileUpload(@RequestParam("file") MultipartFile file) {
		Map<String, String> response = new HashMap<>();
		String uuidFilename = storageService.store(file);
		String absolutePath = MvcUriComponentsBuilder.fromMethodName(VinnslStorageController.class, "serveFile", uuidFilename).build().toString();
		response.put("file", absolutePath);
		
		return ResponseEntity.ok(response);
	}
	
	@GetMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> handleRestUrlUpload(@RequestParam("url") URL url) {
		Map<String, String> response = new HashMap<>();
		String uuidFilename = storageService.storeFromUrl(url);
		String absolutePath = MvcUriComponentsBuilder.fromMethodName(VinnslStorageController.class, "serveFile", uuidFilename).build().toString();
		response.put("file", absolutePath);
		
		return ResponseEntity.ok(response);
	}
	
	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}
	
}
