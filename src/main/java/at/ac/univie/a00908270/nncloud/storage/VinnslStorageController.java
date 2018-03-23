package at.ac.univie.a00908270.nncloud.storage;

import at.ac.univie.a00908270.nncloud.storage.data.StorageFileNotFoundException;
import com.mongodb.gridfs.GridFSDBFile;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FilenameUtils;
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
import java.net.HttpURLConnection;
import java.net.URL;
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
	@ApiOperation("List all Files by FileId")
	public String listUploadedFiles(Model model) {
		
		model.addAttribute("files", getFiles().stream().map(
				path -> {
					return MvcUriComponentsBuilder.fromMethodName(VinnslStorageController.class,
							"serveFile", path.getId().toString(), false).build().toString();
				})
				.collect(Collectors.toList()));
		
		return "uploadForm";
	}
	
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation("List all Files")
	public ResponseEntity<Model> listUploadedFilesJson(Model model) {
		
		model.addAttribute("files", getFiles().stream()
				.map(file -> file.getId().toString())
				.collect(Collectors.toList()));
		
		return ResponseEntity.ok(model);
	}
	
	@GetMapping("/files/name/{filename:.+}")
	@ResponseBody
	@ApiOperation("Download File by Original Filename")
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
	
	@GetMapping("/files/{fileId:.+}")
	@ResponseBody
	@ApiOperation("Download or Show File by FileID")
	public HttpEntity<byte[]> serveFile(@PathVariable String fileId,
										@RequestParam(value = "download", required = false) boolean download) {
		try {
			Optional<GridFSDBFile> optionalCreated = maybeLoadFileById(fileId);
			if (optionalCreated.isPresent()) {
				GridFSDBFile file = optionalCreated.get();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				file.writeTo(os);
				
				if (download) {
					return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=\"" + file.getFilename() + "\"").body(os.toByteArray());
				} else {
					
					HttpHeaders headers = new HttpHeaders();
					headers.add(HttpHeaders.CONTENT_TYPE, file.getContentType());
					return new HttpEntity<>(os.toByteArray(), headers);
				}
				
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.IM_USED);
		}
	}
	
	@PostMapping
	@ApiOperation("Handle File Upload from HTML Form")
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
								   RedirectAttributes redirectAttributes) {
		try {
			String uuidFilename = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType()).getId().toString();
			String absolutePath = MvcUriComponentsBuilder.fromMethodName(VinnslStorageController.class, "serveFile", uuidFilename, false).build().toString();
			
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
	@ApiOperation("Upload MultipartFile")
	public ResponseEntity<?> handleRestFileUpload(@RequestParam("file") MultipartFile file) {
		Map<String, String> response = new HashMap<>();
		
		try {
			String uuidFilename = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType()).getId().toString();
			String absolutePath = MvcUriComponentsBuilder.fromMethodName(VinnslStorageController.class, "serveFile", uuidFilename, false).build().toString();
			response.put("file", uuidFilename);
			
			return ResponseEntity.ok(response);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation("Upload File by URL")
	public ResponseEntity<?> handleRestFileUploadFromUrl(@RequestParam("url") URL url) {
		Map<String, String> response = new HashMap<>();
		
		try {
			String extension = FilenameUtils.getExtension(url.toString());
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			
			String uuidFilename = gridFsTemplate.store(connection.getInputStream(), "upload." + extension, connection.getContentType()).getId().toString();
			
			connection.disconnect();
			
			response.put("file", uuidFilename);
			
			return ResponseEntity.ok(response);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@DeleteMapping("/files/{fileId:.+}")
	@ApiOperation("Delete File by FileID")
	public ResponseEntity deleteFile(@PathVariable String fileId) {
		gridFsTemplate.delete(new Query(GridFsCriteria.where("_id").is(fileId)));
		return ResponseEntity.ok().build();
	}
	
	
	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}
	
	//TODO move into service
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
