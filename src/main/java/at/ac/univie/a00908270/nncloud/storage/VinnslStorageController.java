package at.ac.univie.a00908270.nncloud.storage;

import at.ac.univie.a00908270.nncloud.storage.data.StorageFileNotFoundException;
import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/storage")
public class VinnslStorageController {
	
	private final GridFsTemplate gridFsTemplate;
	
	@Autowired
	public VinnslStorageController(GridFsTemplate gridFsTemplate) {
		this.gridFsTemplate = gridFsTemplate;
	}
	
	@GetMapping
	public String listUploadedFiles(Model model) throws IOException {
		
		model.addAttribute("files", getFiles().stream().map(
				path -> {
					return MvcUriComponentsBuilder.fromMethodName(VinnslStorageController.class,
							"serveFile", path.getId().toString()).build().toString();
				})
				.collect(Collectors.toList()));
		
		return "uploadForm";
	}
	
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Model> listUploadedFilesJson(Model model) throws IOException {
		
		model.addAttribute("files", getFiles().stream()
				.map(file -> file.getId().toString())
				.collect(Collectors.toList()));
		
		return ResponseEntity.ok(model);
	}
	
	@GetMapping("/files/name/{filename:.+}")
	@ResponseBody
	public HttpEntity<byte[]> serveFileByName(@PathVariable String filename) {
		
		try {
			Optional<GridFSDBFile> optionalCreated = maybeLoadFile(filename);
			if (optionalCreated.isPresent()) {
				GridFSDBFile file = optionalCreated.get();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				file.writeTo(os);
				
				return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + file.getFilename() + "\"").body(os.toByteArray());
				
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.IM_USED);
		}
	}
	
	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public HttpEntity<byte[]> serveFile(@PathVariable String filename) {
		try {
			Optional<GridFSDBFile> optionalCreated = maybeLoadFileById(filename);
			if (optionalCreated.isPresent()) {
				GridFSDBFile file = optionalCreated.get();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				file.writeTo(os);
				/*HttpHeaders headers = new HttpHeaders();
				headers.add(HttpHeaders.CONTENT_TYPE, created.getContentType());
				return new HttpEntity<>(os.toByteArray(), headers);*/
				
				return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + file.getFilename() + "\"").body(os.toByteArray());
				
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.IM_USED);
		}
	}
	
	@GetMapping("/files/show/{filename:.+}")
	@ResponseBody
	public HttpEntity<byte[]> showFile(@PathVariable String filename) {
		try {
			Optional<GridFSDBFile> optionalCreated = maybeLoadFileById(filename);
			if (optionalCreated.isPresent()) {
				GridFSDBFile file = optionalCreated.get();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				file.writeTo(os);
				HttpHeaders headers = new HttpHeaders();
				headers.add(HttpHeaders.CONTENT_TYPE, file.getContentType());
				return new HttpEntity<>(os.toByteArray(), headers);
				
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.IM_USED);
		}
	}
	
	@PostMapping
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
								   RedirectAttributes redirectAttributes) {
		try {
			String uuidFilename = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType()).getId().toString();
			String absolutePath = MvcUriComponentsBuilder.fromMethodName(VinnslStorageController.class, "serveFile", uuidFilename).build().toString();
			
			redirectAttributes.addFlashAttribute("message",
					"You successfully uploaded " + uuidFilename + "!");
			
			return "redirect:/storage";
			
		} catch (IOException e) {
			redirectAttributes.addFlashAttribute("message",
					"Error uploading file");
			
			return "redirect:/storage";
		}
	}
	
	@PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> handleRestFileUpload(@RequestParam("file") MultipartFile file) {
		Map<String, String> response = new HashMap<>();
		
		try {
			String uuidFilename = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType()).getId().toString();
			String absolutePath = MvcUriComponentsBuilder.fromMethodName(VinnslStorageController.class, "serveFile", uuidFilename).build().toString();
			response.put("file", absolutePath);
			
			return ResponseEntity.ok(response);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@DeleteMapping("/files/{filename:.+}")
	public ResponseEntity deleteFile(@PathVariable String filename) {
		gridFsTemplate.delete(new Query(GridFsCriteria.where("_id").is(filename)));
		return ResponseEntity.ok().build();
	}
	
	
	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}
	
	
	private List<GridFSDBFile> getFiles() {
		return gridFsTemplate.find(null);
	}
	
	private Optional<GridFSDBFile> maybeLoadFile(String name) {
		GridFSDBFile file = gridFsTemplate.findOne(getFileByNameQuery(name));
		return Optional.ofNullable(file);
	}
	
	private Optional<GridFSDBFile> maybeLoadFileById(String id) {
		GridFSDBFile file = gridFsTemplate.findOne(getFileByIdQuery(id));
		return Optional.ofNullable(file);
	}
	
	private static Query getFileByNameQuery(String name) {
		return Query.query(GridFsCriteria.whereFilename().is(name));
	}
	
	private static Query getFileByIdQuery(String id) {
		return Query.query(GridFsCriteria.where("_id").is(id));
	}
}
