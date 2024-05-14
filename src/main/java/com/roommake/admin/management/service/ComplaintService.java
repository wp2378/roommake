package com.roommake.admin.management.service;

import com.roommake.admin.management.dto.ComplaintDto;
import com.roommake.admin.management.mapper.ComplaintMapper;
import com.roommake.admin.management.vo.Complaint;
import com.roommake.channel.enums.PostStatusEnum;
import com.roommake.channel.mapper.PostMapper;
import com.roommake.channel.mapper.PostReplyMapper;
import com.roommake.channel.service.PostService;
import com.roommake.channel.vo.ChannelPost;
import com.roommake.channel.vo.ChannelPostReply;
import com.roommake.community.enums.CommStatusEnum;
import com.roommake.community.mapper.CommunityMapper;
import com.roommake.community.mapper.CommunityReplyMapper;
import com.roommake.community.service.CommunityService;
import com.roommake.community.vo.Community;
import com.roommake.community.vo.CommunityReply;
import com.roommake.user.mapper.UserMapper;
import com.roommake.user.vo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ComplaintService {

    private final ComplaintMapper complaintMapper;
    private final UserMapper userMapper;
    private final CommunityService communityService;
    private final CommunityMapper communityMapper;
    private final PostService postService;
    private final PostMapper postMapper;
    private final PostReplyMapper postReplyMapper;
    private final CommunityReplyMapper communityReplyMapper;

    /**
     * 커뮤니티글, 채널글 신고 목록을 반환한다.
     * 삭제되지 않은 신고에 대해 반환.
     *
     * @param filt 신고처리 여부
     * @return 신고 목록
     */
    @Transactional(readOnly = true)
    public List<ComplaintDto> getBoardComplaints(String filt) {
        return complaintMapper.getBoardComplaints(filt);
    }

    /**
     * 커뮤니티글의 댓글, 채널글의 댓글 신고 목록을 반환한다.
     *
     * @param filt 신고처리 여부
     * @return 신고 목록
     */
    @Transactional(readOnly = true)
    public List<ComplaintDto> getReplyComplaints(String filt) {
        return complaintMapper.getReplyComplaint(filt);
    }

    /**
     * 신고를 확정 시키고, 콘텐츠와 작성자도 상황에 맞게 신고횟수를 누적시킨다.
     *
     * @param compalintTypeAndId 신고의 콘텐츠 유형과 신고 id
     */
    @Transactional
    public void confirmCompalint(String compalintTypeAndId) {
        String[] data = compalintTypeAndId.split("\\.");
        String type = data[0];
        int id = Integer.parseInt(data[1]);

        Complaint complaint = null;

        switch (type) {
            case "post":
                // 채널글 신고 확정
                complaint = complaintMapper.getPostComplaintById(id);
                complaint.setComplaintYn("Y");
                complaint.setUpdateDate(new Date());
                complaintMapper.modifyPostComplaint(complaint);

                // 포스트 신고수 누적
                ChannelPost post = postService.getPostByPostId(complaint.getContentId());
                post.setComplaintCount(post.getComplaintCount() + 1);
                postMapper.modifyPost(post);

                // 포스트 누적 신고수가 5 이상이면 작성자 신고수 카운트 +1
                if (post.getComplaintCount() >= 5) {
                    post.setStatus(PostStatusEnum.BLOCK.getStatus());
                    User user = userMapper.getUserById(post.getUser().getId());
                    user.setComplaintCount(user.getComplaintCount() + 1);
                    userMapper.modifyUserComplaintCount(user);
                }

                break;
            case "community":
                // 커뮤니티글 신고확정
                complaint = complaintMapper.getCommComplaintById(id);
                complaint.setComplaintYn("Y");
                complaint.setUpdateDate(new Date());
                complaintMapper.modifyCommComplaint(complaint);

                // 커뮤니티글 신고수 누적
                Community community = communityService.getCommunityByCommId(complaint.getContentId());
                community.setComplaintCount(community.getComplaintCount() + 1);
                communityMapper.modifyCommunity(community);

                // 커뮤니티 작성자 신고수 누적
                if (community.getComplaintCount() >= 5) {
                    community.setStatus(CommStatusEnum.BLOCK.getStatus());
                    User user = userMapper.getUserById(community.getUser().getId());
                    user.setComplaintCount(user.getComplaintCount() + 1);
                    userMapper.modifyUserComplaintCount(user);
                }
                break;
            case "postReply":
                // 채널 댓글 신고확정
                complaint = complaintMapper.getPostReplyComplaintById(id);
                complaint.setComplaintYn("Y");
                complaint.setUpdateDate(new Date());
                complaintMapper.modifyPostReplyComplaint(complaint);

                ChannelPostReply postReply = postService.getPostReplyByReplyId(complaint.getContentId());
                postReply.setComplaintCount(postReply.getComplaintCount() + 1);
                postReplyMapper.modifyReply(postReply);

                if (postReply.getComplaintCount() >= 5) {
                    postReply.setStatus(PostStatusEnum.BLOCK.getStatus());
                    User user = userMapper.getUserById(postReply.getUser().getId());
                    user.setComplaintCount(user.getComplaintCount() + 1);
                    userMapper.modifyUserComplaintCount(user);
                }
                break;
            case "commReply":
                // 커뮤니티 댓글 신고확정
                complaint = complaintMapper.getCommReplyComplaintById(id);
                complaint.setComplaintYn("Y");
                complaint.setUpdateDate(new Date());
                complaintMapper.modifyCommReplyComplaint(complaint);

                CommunityReply communityReply = communityService.getCommunityReplyByReplyId(complaint.getContentId());
                communityReply.setComplaintCount(communityReply.getComplaintCount() + 1);
                communityReplyMapper.modifyCommunityReply(communityReply);

                if (communityReply.getComplaintCount() >= 5) {
                    communityReply.setStatus(CommStatusEnum.BLOCK.getStatus());
                    User user = userMapper.getUserById(communityReply.getUser().getId());
                    user.setComplaintCount(user.getComplaintCount() + 1);
                    userMapper.modifyUserComplaintCount(user);
                }
                break;
        }
    }

    /**
     * 신고 내역을 취소한다.
     *
     * @param compalintTypeAndId 신고의 콘텐츠 유형과 신고 id
     */
    public void deleteCompalint(String compalintTypeAndId) {
        // 매개변수로 받은 complaintTypeAndId는 post.4과 같이 .으로 구분되어 있어 split을 통해 나누어 저장한다.
        String[] data = compalintTypeAndId.split("\\.");
        String type = data[0];
        int id = Integer.parseInt(data[1]);

        Complaint complaint = null;

        switch (type) {
            case "post":
                // 신고 취소
                complaint = complaintMapper.getPostComplaintById(id);
                if (complaint.getComplaintYn().equals("Y")) {
                    throw new RuntimeException("이미 처리 완료된 신고는 취소가 불가합니다.");
                }
                complaint.setComplaintYn("Y");
                complaint.setDeleteYn("Y");
                complaint.setUpdateDate(new Date());
                complaintMapper.modifyPostComplaint(complaint);
                break;
            case "community":
                // 신고 취소
                complaint = complaintMapper.getCommComplaintById(id);
                complaint.setComplaintYn("Y");
                complaint.setDeleteYn("Y");
                complaint.setUpdateDate(new Date());
                complaintMapper.modifyCommComplaint(complaint);
                break;
            case "postReply":
                // 신고 취소
                complaint = complaintMapper.getPostReplyComplaintById(id);
                complaint.setComplaintYn("Y");
                complaint.setDeleteYn("Y");
                complaint.setUpdateDate(new Date());
                complaintMapper.modifyPostReplyComplaint(complaint);
                break;
            case "commReply":
                // 신고 취소
                complaint = complaintMapper.getCommReplyComplaintById(id);
                complaint.setComplaintYn("Y");
                complaint.setDeleteYn("Y");
                complaint.setUpdateDate(new Date());
                complaintMapper.modifyCommReplyComplaint(complaint);
                break;
        }
    }
}
