package com.roommake.admin.management.service;

import com.roommake.admin.management.dto.NoticeForm;
import com.roommake.admin.management.mapper.NoticeMapper;
import com.roommake.admin.management.vo.Notice;
import com.roommake.dto.Criteria;
import com.roommake.dto.ListDto;
import com.roommake.dto.Pagination;
import com.roommake.user.mapper.UserMapper;
import com.roommake.user.vo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;
    private final UserMapper userMapper;

    /**
     * 공지사항을 저장한다.
     *
     * @param form  저장할 공지사항 정보가 담긴 form객체
     * @param email 저장할 유저
     */
    public void createNotice(NoticeForm form, String email) {

        User user = userMapper.getUserByEmail(email);

        Notice notice = new Notice();
        notice.setTitle(form.getTitle());
        notice.setContent(form.getContent());
        notice.setPriority(form.getPriority());
        notice.setCreateByUser(user);
        notice.setUpdateByUser(user);

        noticeMapper.createNotice(notice);
    }

    /**
     * 공지사항을 수정한다.
     *
     * @param id    수정할 공지사항 id
     * @param form  수정할 공지사항 내용이 담긴 form객체
     * @param email 수정할 유저
     * @return 수정된 공지사항 객체
     */
    public Notice modifyNotice(int id, NoticeForm form, String email) {

        Notice notice = noticeMapper.getNoticeById(id);
        User user = userMapper.getUserByEmail(email);

        notice.setTitle(form.getTitle());
        notice.setContent(form.getContent());
        notice.setPriority(form.getPriority());
        notice.setUpdateByUser(user);
        notice.setUpdateDate(new Date());
        notice.setDeleteYn("N");
        noticeMapper.modifyNotice(notice);

        return notice;
    }

    /**
     * 공지사항을 삭제한다.
     *
     * @param id 삭제할 공지사항 id
     */
    public void deleteNotice(int id) {
        Notice notice = noticeMapper.getNoticeById(id);
        notice.setDeleteYn("Y");
        notice.setDeleteDate(new Date());
        noticeMapper.modifyNotice(notice);
    }

    /**
     * 페이징, 정렬, 검색, 필터링 된 공지사항 리스트를 반환한다.
     *
     * @param criteria 반환할 공지사항 리스트 조건이 담긴 객체
     * @return 공지사항 리스트
     */
    @Transactional(readOnly = true)
    public ListDto<Notice> getNotices(Criteria criteria) {

        int totalRows = noticeMapper.getTotalRows(criteria);

        Pagination pagination = new Pagination(criteria.getPage(), totalRows, criteria.getRows());

        criteria.setBegin(pagination.getBegin());
        criteria.setEnd(pagination.getEnd());

        List<Notice> noticeList = noticeMapper.getNotices(criteria);

        ListDto<Notice> dto = new ListDto<Notice>(noticeList, pagination);
        return dto;
    }

    /**
     * 공지사항 id를 받아 해당 공지사항을 반환한다.
     *
     * @param id 찾을 공지사항
     * @return 반환된 공지사항
     */
    @Transactional(readOnly = true)
    public Notice getNoticeById(int id) {
        return noticeMapper.getNoticeById(id);
    }
}
