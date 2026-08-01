package com.example.gestionprojet.web;

import com.example.gestionprojet.entity.Film;
import com.example.gestionprojet.repository.FilmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/films")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FilmStreamController {

    private final FilmRepository filmRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/{id}/stream")
    public ResponseEntity<ResourceRegion> streamVideo(
            @PathVariable Long id,
            @RequestHeader HttpHeaders headers) throws IOException {

        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Film non trouvé"));

        if (film.getVideoFileName() == null) {
            return ResponseEntity.notFound().build();
        }

        UrlResource video = new UrlResource(
                Paths.get(uploadDir).resolve(film.getVideoFileName()).toUri());

        long contentLength = video.contentLength();
        List<HttpRange> ranges = headers.getRange();

        ResourceRegion region;
        if (ranges.isEmpty()) {
            long rangeLength = Math.min(1024 * 1024, contentLength);
            region = new ResourceRegion(video, 0, rangeLength);
        } else {
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(1024 * 1024, end - start + 1);
            region = new ResourceRegion(video, start, rangeLength);
        }

        return ResponseEntity
                .status(ranges.isEmpty() ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.valueOf("video/mp4"))
                .body(region);
    }
}