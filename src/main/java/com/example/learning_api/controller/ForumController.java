package com.example.learning_api.controller;

import com.example.learning_api.dto.request.forum.*;
import com.example.learning_api.dto.response.forum.GetForumCommentResponse;
import com.example.learning_api.dto.response.forum.GetForumDetailResponse;
import com.example.learning_api.dto.response.forum.GetForumsResponse;
import com.example.learning_api.model.ResponseAPI;
import com.example.learning_api.service.core.IForumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/forum")
public class ForumController {
    private final IForumService forumService;

    @PostMapping(path = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseAPI<String>> createForum(@ModelAttribute @Valid CreateForumRequest body) {
        try{
            forumService.createForum(body);
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .message("Create forum successfully")
                    .build();
            return ResponseEntity.ok(res);
        }
        catch (Exception e){
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(res);
        }
    }
    @PatchMapping(path = "/{forumId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseAPI<String>> updateForum(@ModelAttribute @Valid UpdateForumRequest body, @PathVariable String forumId) {
        try{
            body.setId(forumId);
            forumService.updateForum(body);
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .message("Update forum successfully")
                    .build();
            return ResponseEntity.ok(res);
        }
        catch (Exception e){
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(res);
        }
    }

    @DeleteMapping(path = "/{forumId}")
    public ResponseEntity<ResponseAPI<String>> deleteForum(@PathVariable String forumId) {
        try{
            forumService.deleteForum(forumId);
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .message("Delete forum successfully")
                    .build();
            return ResponseEntity.ok(res);
        }
        catch (Exception e){
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PostMapping(path = "/vote")
    public ResponseEntity<ResponseAPI<String>> voteForum(@RequestBody @Valid VoteRequest body) {
        try{
            forumService.voteForum(body);
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .message("Vote forum successfully")
                    .build();
            return ResponseEntity.ok(res);
        }
        catch (Exception e){
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(res);
        }
    }
    @GetMapping(path = "")
    public ResponseEntity<ResponseAPI<GetForumsResponse>> getForum(
            @RequestParam(name="name",required = false,defaultValue = "") String search,
            @RequestParam(name="page",required = false,defaultValue = "1") int page,
            @RequestParam(name="size",required = false,defaultValue = "10") int size
    ) {
        try{
            GetForumsResponse data = forumService.getForums(page-1, size, search);
            ResponseAPI<GetForumsResponse> res = ResponseAPI.<GetForumsResponse>builder()
                    .message("Get forum successfully")
                    .data(data)
                    .build();
            return ResponseEntity.ok(res);
        }
        catch (Exception e){
            ResponseAPI<GetForumsResponse> res = ResponseAPI.<GetForumsResponse>builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(res);
        }
    }
    @GetMapping(path = "/author/{authorId}")
    public ResponseEntity<ResponseAPI<GetForumsResponse>> getForumByAuthor(@PathVariable String authorId,
                                                                                     @RequestParam(name="page",required = false,defaultValue = "1") int page,
                                                                                     @RequestParam(name="size",required = false,defaultValue = "10") int size) {
        try{
            GetForumsResponse data = forumService.getForumByAuthor(authorId, page-1, size);
            ResponseAPI<GetForumsResponse> res = ResponseAPI.<GetForumsResponse>builder()
                    .message("Get forum by author successfully")
                    .data(data)
                    .build();
            return ResponseEntity.ok(res);
        }
        catch (Exception e){
            ResponseAPI<GetForumsResponse> res = ResponseAPI.<GetForumsResponse>builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping(path = "/tag")
    public ResponseEntity<ResponseAPI<GetForumsResponse>> getForumByTag(@RequestParam(name="search",required = false,defaultValue = "") String tag,
                                                                                  @RequestParam(name="page",required = false,defaultValue = "1") int page,
                                                                                  @RequestParam(name="size",required = false,defaultValue = "10") int size) {
        try{
            GetForumsResponse data = forumService.getForumByTag(tag, page-1, size);
            ResponseAPI<GetForumsResponse> res = ResponseAPI.<GetForumsResponse>builder()
                    .message("Get forum by tag successfully")
                    .data(data)
                    .build();
            return ResponseEntity.ok(res);
        }
        catch (Exception e){
            ResponseAPI<GetForumsResponse> res = ResponseAPI.<GetForumsResponse>builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(res);
        }
    }
    @GetMapping(path = "/{forumId}")
    public ResponseEntity<ResponseAPI<GetForumDetailResponse>> getForumDetail(@PathVariable String forumId) {
        try{
            GetForumDetailResponse data = forumService.getForumDetail(forumId);
            ResponseAPI<GetForumDetailResponse> res = ResponseAPI.<GetForumDetailResponse>builder()
                    .message("Get forum detail successfully")
                    .data(data)
                    .build();
            return ResponseEntity.ok(res);
        }
        catch (Exception e){
            ResponseAPI<GetForumDetailResponse> res = ResponseAPI.<GetForumDetailResponse>builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(res);
        }
    }



    @PostMapping(path= "/comment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseAPI<String>> createForumComment(@ModelAttribute @Valid CreateForumCommentRequest body) {
        try{
            forumService.createForumComment(body);
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .message("Create forum comment successfully")
                    .build();
            return ResponseEntity.ok(res);
        }
        catch (Exception e){
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PatchMapping(path = "/comment/{commentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseAPI<String>> updateForumComment(@ModelAttribute @Valid UpdateForumCommentRequest body, @PathVariable String commentId) {
        try{
            body.setId(commentId);
            forumService.updateForumComment(body);
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .message("Update forum comment successfully")
                    .build();
            return ResponseEntity.ok(res);
        }
        catch (Exception e){
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(res);
        }
    }

    @DeleteMapping(path = "/comment/{commentId}")
    public ResponseEntity<ResponseAPI<String>> deleteForumComment(@PathVariable String commentId) {
        try{
            forumService.deleteForumComment(commentId);
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .message("Delete forum comment successfully")
                    .build();
            return ResponseEntity.ok(res);
        }
        catch (Exception e){
            ResponseAPI<String> res = ResponseAPI.<String>builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping(path = "/comment/reply/{parentId}")
    public ResponseEntity<ResponseAPI<GetForumCommentResponse>> getReplyComments(@PathVariable String parentId,
                                                                                      @RequestParam(name="page",required = false,defaultValue = "1") int page,
                                                                                      @RequestParam(name="size",required = false,defaultValue = "10") int size) {
        try{
            GetForumCommentResponse data = forumService.getReplyComments(parentId, page-1, size);
            ResponseAPI<GetForumCommentResponse> res = ResponseAPI.<GetForumCommentResponse>builder()
                    .message("Get reply comments successfully")
                    .data(data)
                    .build();
            return ResponseEntity.ok(res);
        }
        catch (Exception e){
            ResponseAPI<GetForumCommentResponse> res = ResponseAPI.<GetForumCommentResponse>builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(res);
        }
    }


}
