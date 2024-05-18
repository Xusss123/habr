package karm.van.habr.controller;

import karm.van.habr.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/resume_v1")
public class CommentController {
    private final CommentService commentService;

    @Async
    @PostMapping("/create-comment")
    public CompletableFuture<ResponseEntity<String>> createComment(@RequestParam(name = "text") String text,
                                                                   @RequestParam(name = "cardId") Long id,
                                                                   Authentication authentication){
        return CompletableFuture.supplyAsync(()->{
            try {
                commentService.createComment(text,id,authentication);
                return ResponseEntity.ok("Успех");
            }catch (Exception e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        });
    }

    @Async
    @DeleteMapping("/delete-comment")
    public CompletableFuture<ResponseEntity<String>> deleteComment(@RequestParam(name = "commentId") Long commentId){
        return CompletableFuture.supplyAsync(()->{
            try {
                commentService.deleteComment(commentId);
                return ResponseEntity.ok("Успех");
            }catch (Exception e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        });
    }

    @Async
    @PatchMapping("/patch-comment")
    public CompletableFuture<ResponseEntity<String>> patchComment(@RequestParam(name = "commentId") Long commentId,
                                                                  @RequestParam(name = "text") String commentText) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                commentService.patchComment(commentId, commentText);
                return ResponseEntity.ok("Успех");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        });
    }

}