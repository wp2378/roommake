package com.roommake.channel.controller;

import com.roommake.channel.dto.ChannelDto;
import com.roommake.channel.dto.PostDto;
import com.roommake.channel.dto.PostForm;
import com.roommake.channel.dto.PostReplyDto;
import com.roommake.channel.service.ChannelService;
import com.roommake.channel.service.PostService;
import com.roommake.channel.vo.ChannelParticipant;
import com.roommake.channel.vo.ChannelPost;
import com.roommake.channel.vo.ChannelPostReply;
import com.roommake.resolver.Login;
import com.roommake.user.security.LoginUser;
import com.roommake.utils.S3Uploader;
import com.roommake.utils.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/channel/post")
@RequiredArgsConstructor
@Tag(name = "채널글 API", description = "각 채널의 글에 대한 추가,변경,삭제,조회 API를 제공한다.")
public class PostController {

    private final PostService postService;
    private final S3Uploader s3Uploader;
    private final ChannelService channelService;

    @Operation(summary = "채널에 대한 전체글 조회", description = "해당 채널에 대한 채널정보, 전체글, 참가여부를 조회한다.")
    @GetMapping("/list/{channelId}")
    public String postList(@PathVariable("channelId") int channelId, Model model, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        ChannelDto dto = postService.getAllPostsByChannelId(channelId, email);

        model.addAttribute("channel", dto.getChannel());
        model.addAttribute("postList", dto.getChannelPosts());
        model.addAttribute("participant", dto.isParticipant());
        model.addAttribute("participantCount", dto.getChannelParticipantCount());
        model.addAttribute("postCount", dto.getChannelPostCount());

        return "channel/post/list";
    }

    @Operation(summary = "채널글 등록폼", description = "채널글 등록폼를 조회한다.")
    @GetMapping(path = "/create/{channelId}")
    @PreAuthorize("isAuthenticated()")
    public String createForm(@PathVariable("channelId") int channelId, @Login LoginUser loginUser, Model model) {
        ChannelParticipant channelParticipant = channelService.getChannelParticipant(channelId, loginUser.getId());
        if (channelParticipant == null) {
            throw new RuntimeException("채널에 가입하세요");
        }
        model.addAttribute("postForm", new PostForm());
        model.addAttribute("channelId", channelId);

        return "channel/post/form";
    }

    @Operation(summary = "채널글 등록", description = "채널글을 등록한다.")
    @PostMapping(path = "/create/{channelId}")
    @PreAuthorize("isAuthenticated()")
    public String createPost(@PathVariable("channelId") int channelId, @Valid PostForm postForm, BindingResult errors, @Login LoginUser loginUser) {
        if (errors.hasErrors()) {
            return "channel/post/form";
        }
        String image = s3Uploader.saveFile(postForm.getImageFile());
        postService.createPost(postForm, channelId, loginUser.getId(), image);

        return "redirect:/channel/post/list/{channelId}";
    }

    @Operation(summary = "채널글 상세", description = "채널글을 조회한다.")
    @GetMapping("/detail/{postId}")
    public String detailPost(@PathVariable("postId") int postId, Model model, Principal principal) {
        String email = principal != null ? principal.getName() : null;

        PostDto postDto = postService.getPostDetail(postId, email);
        String addBrContent = StringUtils.withBr(postDto.getPost().getContent());
        postDto.getPost().setContent(addBrContent);
        model.addAttribute("post", postDto.getPost());
        model.addAttribute("postLike", postDto.isLike());
        model.addAttribute("complaintCategories", postDto.getComplaintCategories());

        PostReplyDto replyDto = postService.getAllPostReplies(postId, email);
        model.addAttribute("totalReplyCount", replyDto.getTotalReplyCount());
        model.addAttribute("postReplies", replyDto.getPostReplies());

        return "channel/post/detail";
    }

    @Operation(summary = "채널글 수정폼", description = "채널글 수정폼를 조회한다.")
    @GetMapping(path = "/modify/{postId}")
    @PreAuthorize("isAuthenticated()")
    public String modifyForm(@PathVariable("postId") int postId, Model model, @Login LoginUser loginUser) {
        ChannelPost post = postService.getPostByPostId(postId);
        if (post.getUser().getId() != loginUser.getId()) {
            throw new RuntimeException("다른 사용자의 글은 수정할 수 없습니다.");
        }
        PostForm postForm = PostForm.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .build();
        String imageName = post.getImageName();
        model.addAttribute("postForm", postForm);
        model.addAttribute("imageName", imageName);
        model.addAttribute("postId", postId);

        return "channel/post/form";
    }

    @Operation(summary = "채널글 수정", description = "채널글을 수정한다.")
    @PostMapping(path = "/modify/{postId}")
    @PreAuthorize("isAuthenticated()")
    public String modifyPost(@PathVariable("postId") int postId, @Valid PostForm postForm, BindingResult errors, @Login LoginUser loginUser) {
        if (errors.hasErrors()) {
            return "channel/post/form";
        }
        ChannelPost post = postService.getPostByPostId(postId);
        if (post.getUser().getId() != loginUser.getId()) {
            throw new RuntimeException("다른 사용자의 글은 수정할 수 없습니다.");
        }
        String image = "";
        if (postForm.getImageFile() != null) {
            image = s3Uploader.saveFile(postForm.getImageFile());
        }
        postService.modifyPost(postForm, image, post);

        return "redirect:/channel/post/detail/{postId}";
    }

    @Operation(summary = "채널글 삭제", description = "채널글을 삭제한다.")
    @GetMapping(path = "/delete/{postId}")
    @PreAuthorize("isAuthenticated()")
    public String deletePost(@PathVariable("postId") int postId, @Login LoginUser loginUser) {
        ChannelPost post = postService.getPostByPostId(postId);
        if (post.getUser().getId() != loginUser.getId()) {
            throw new RuntimeException("다른 사용자의 글은 삭제할 수 없습니다.");
        }
        int channelId = post.getChannel().getId();
        postService.deletePost(post);

        return String.format("redirect:/channel/post/list/%d", channelId);
    }

    @Operation(summary = "채널글 좋아요", description = "채널글 좋아요를 추가한다.")
    @PostMapping(path = "/addLike")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public int addPostLike(@RequestParam("postId") int postId, @Login LoginUser loginUser) {
        int postLikeCount = postService.addPostLike(postId, loginUser.getId());

        return postLikeCount;
    }

    @Operation(summary = "채널글 좋아요 취소", description = "채널글 좋아요를 삭제한다.")
    @GetMapping(path = "/deleteLike")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public int deletePostLike(@RequestParam("postId") int postId, @Login LoginUser loginUser) {
        int postLikeCount = postService.deletePostLike(postId, loginUser.getId());

        return postLikeCount;
    }

    @Operation(summary = "채널글 신고", description = "채널글을 신고한다.")
    @PostMapping(path = "/complaint")
    @PreAuthorize("isAuthenticated()")
    public String addPostComplaint(@RequestParam("postId") int postId,
                                   @RequestParam("complaintCatId") int complaintCatId,
                                   @Login LoginUser loginUser) {
        postService.addPostComplaint(postId, complaintCatId, loginUser.getId());

        return String.format("redirect:/channel/post/list/%d", postId);
    }

    @Operation(summary = "댓글 등록", description = "채널글에 댓글을 등록한다.")
    @PostMapping(path = "/reply/create/{postId}")
    @PreAuthorize("isAuthenticated()")
    public String createPostReply(@PathVariable("postId") int postId,
                                  @RequestParam("content") String content,
                                  @RequestParam(name = "parentsReplyId", required = false, defaultValue = "0") int parentsReplyId,
                                  @Login LoginUser loginUser) {
        if (parentsReplyId == 0) {
            postService.createPostReply(postId, content, loginUser.getId());
        } else {
            postService.createPostReReply(postId, content, parentsReplyId, loginUser.getId());
        }
        return "redirect:/channel/post/detail/{postId}";
    }

    @Operation(summary = "댓글 조회", description = "댓글을 조회한다.")
    @GetMapping(path = "/reply/{replyId}")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public ChannelPostReply getPostReplyByReplyId(@PathVariable("replyId") int replyId) {
        return postService.getPostReplyByReplyId(replyId);
    }

    @Operation(summary = "댓글 수정", description = "댓글을 수정한다.")
    @PostMapping(path = "/reply/modify/{replyId}")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public ChannelPostReply modifyPostReplyByReplyId(@PathVariable("replyId") int replyId,
                                                     @RequestParam("content") String content,
                                                     @Login LoginUser loginUser) {
        ChannelPostReply postReply = postService.getPostReplyByReplyId(replyId);
        if (postReply.getUser().getId() != loginUser.getId()) {
            throw new RuntimeException("다른 사용자의 댓글은 수정할 수 없습니다.");
        }
        return postService.modifyReply(postReply, content);
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제한다.")
    @GetMapping(path = "/reply/delete/{replyId}")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public void deletePostReplyByReplyId(@PathVariable("replyId") int replyId, @Login LoginUser loginUser) {
        ChannelPostReply postReply = postService.getPostReplyByReplyId(replyId);
        if (postReply.getUser().getId() != loginUser.getId()) {
            throw new RuntimeException("다른 사용자의 댓글은 삭제할 수 없습니다.");
        }
        postService.deletePostReplyByReplyId(postReply);
    }

    @Operation(summary = "채널 댓글 신고", description = "채널 글 댓글을 신고한다.")
    @PostMapping(path = "/reply/complaint")
    @PreAuthorize("isAuthenticated()")
    public String addPostReplyComplaint(@RequestParam("postId") int postId,
                                        @RequestParam("replyId") int replyId,
                                        @RequestParam("complaintCatId") int complaintCatId,
                                        @Login LoginUser loginUser) {
        postService.addPostReplyComplaint(replyId, complaintCatId, loginUser.getId());

        return String.format("redirect:/channel/post/list/%d", postId);
    }
}
