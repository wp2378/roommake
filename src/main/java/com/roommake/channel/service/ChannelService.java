package com.roommake.channel.service;

import com.roommake.channel.dto.ChannelForm;
import com.roommake.channel.dto.ChannelInfoDto;
import com.roommake.channel.enums.PostStatusEnum;
import com.roommake.channel.mapper.ChannelMapper;
import com.roommake.channel.mapper.PostMapper;
import com.roommake.channel.vo.Channel;
import com.roommake.channel.vo.ChannelParticipant;
import com.roommake.channel.vo.ChannelPost;
import com.roommake.dto.Criteria;
import com.roommake.dto.ListDto;
import com.roommake.dto.Pagination;
import com.roommake.email.service.MailService;
import com.roommake.user.mapper.UserMapper;
import com.roommake.user.vo.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelMapper channelMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final MailService mailService;

    /**
     * 천체 채널 목록을 조회한다.
     *
     * @param criteria 정렬 및 페이징 정보
     * @return 채널정보, 총 참여자수, 총 글개수가 포함된 전체 채널 목록
     */
    @Transactional(readOnly = true)
    public ListDto<ChannelInfoDto> getAllChannels(Criteria criteria) {
        int totalRows = channelMapper.getTotalRows(criteria);

        Pagination pagination = new Pagination(criteria.getPage(), totalRows, criteria.getRows());
        criteria.setBegin(pagination.getBegin());
        criteria.setEnd(pagination.getEnd());

        List<ChannelInfoDto> channelList = channelMapper.getAllChannels(criteria);
        return new ListDto<ChannelInfoDto>(channelList, pagination);
    }

    /**
     * 로그인한 유저가 활동중인 채널 목록을 조회한다.
     *
     * @param userId 로그인한 유저 아이디
     * @return 활동중인 채널 목록
     */
    @Transactional(readOnly = true)
    public List<Channel> getChannelsByUserId(int userId) {
        return channelMapper.getChannelsByUserId(userId);
    }

    /**
     * 채널을 조회한다.
     *
     * @param channelId 채널 아이디
     * @return 채널 정보
     */
    @Transactional(readOnly = true)
    public Channel getChannelByChannelId(int channelId) {
        return channelMapper.getChannelByChannelId(channelId);
    }

    /**
     * 채널을 추가한다.
     *
     * @param form      채널 등록폼
     * @param imageName 이미지 이름
     * @param userId    채널을 등록한 유저 아이디
     */
    public void createChannel(ChannelForm form, String imageName, int userId) {
        // 시작 시간 측정
        long beforeTime = System.currentTimeMillis();
        User user = userMapper.getUserById(userId);

        // 답변 등록 알림 메일 설정
        String to = user.getEmail();                                       // 답변 알림 받을 이메일(채널 생성자 이메일주소)
        String subject = "채널이 생성되었습니다.";                             // 메일 발송시 제목
        Map<String, Object> content = new HashMap<>();                      // 메일 콘텐츠를 담을 Map 생성
        content.put("title", form.getTitle());                              // html 템플릿에 적용될 콘텐츠 담기
        mailService.sendEmail(to, subject, "channel-create-email", content);                          // 메일 발송시 필요한 정보를 전달한다.

        Channel channel = Channel.builder()
                .user(user)
                .title(form.getTitle())
                .description(form.getDescription())
                .imageName(imageName)
                .build();
        channelMapper.createChannel(channel);

        ChannelParticipant participant = new ChannelParticipant();
        participant.toParticipant(channel.getId(), userId);
        channelMapper.createChannelParticipant(participant);

        // 실행시간을 보기 위해 log 출력
        long afterTime = System.currentTimeMillis();
        long diffTime = afterTime - beforeTime;
        // 메일발송 및 채널 생성, 참여자 등록에 걸린 시간 조회
        log.info("채널 생성 후 메일발송 총 실행 시간: " + diffTime + "ms");
    }

    /**
     * 채널 정보를 수정한다.
     *
     * @param channelForm 채널 수정폼
     * @param imageName   채널 수정시 업로드한 이미지 이름
     * @param channel     수정하려는 채널 정보
     */
    public void modifyChannel(ChannelForm channelForm, String imageName, Channel channel) {
        channel.setTitle(channelForm.getTitle());
        channel.setDescription(channelForm.getDescription());
        channel.setImageName(imageName);
        channel.setUpdateDate(new Date());
        channelMapper.modifyChannel(channel);
    }

    /**
     * 채널을 삭제한다.
     *
     * @param channel 삭제할 채널정보
     */
    public void deleteChannel(Channel channel) {
        // 1. 채널을 삭제한다.
        channel.setDeleteYn("Y");
        channel.setDeleteDate(new Date());
        channelMapper.modifyChannel(channel);

        // 2. 채널과 관련된 글이 숨겨진다. (block)
        List<ChannelPost> postList = channelMapper.getAllChannelPosts(channel.getId());
        for (ChannelPost post : postList) {
            post.setStatus(PostStatusEnum.BLOCK.getStatus());
            postMapper.modifyPost(post);
        }
    }

    /**
     * 채널 참여자를 조회한다.
     *
     * @param channelId 채널 아이디
     * @param userId    유저 아이디
     */
    @Transactional(readOnly = true)
    public ChannelParticipant getChannelParticipant(int channelId, int userId) {
        ChannelParticipant participant = new ChannelParticipant();
        participant.toParticipant(channelId, userId);
        return channelMapper.getChannelParticipant(participant);
    }

    /**
     * 로그인한 유저가 채널에 참여한다.
     *
     * @param channelId 채널 아이디
     * @param userId    유저 아이디
     */
    public void createChannelParticipant(int channelId, int userId) {
        ChannelParticipant participant = new ChannelParticipant();
        participant.toParticipant(channelId, userId);
        channelMapper.createChannelParticipant(participant);
    }

    /**
     * 로그인한 유저가 채널 참여를 취소한다.
     *
     * @param channelId 채널 아이디
     * @param userId    유저 아이디
     */
    public void deleteChannelParticipant(int channelId, int userId) {
        ChannelParticipant participant = new ChannelParticipant();
        participant.toParticipant(channelId, userId);
        channelMapper.deleteChannelParticipant(participant);
    }
}
