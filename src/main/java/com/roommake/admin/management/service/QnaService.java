package com.roommake.admin.management.service;

import com.roommake.admin.management.dto.QnaForm;
import com.roommake.admin.management.mapper.QnaMapper;
import com.roommake.admin.management.vo.Qna;
import com.roommake.admin.management.vo.QnaCategory;
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
public class QnaService {

    private final QnaMapper qnaMapper;
    private final UserMapper userMapper;
    private final MailService mailService;

    /**
     * 문의사항번호, 답변내용, 답변자를 받아서 문의사항 답변을 등록한다.
     *
     * @param id     문의사항 id
     * @param answer 등록될 답변 내용
     * @param email  답변자 email
     * @return 수정된 값이 담긴 qna 객체
     */
    public Qna updateAnswer(int id, String answer, String email) throws Exception {

        long beforeTime = System.currentTimeMillis();
        Qna qna = qnaMapper.getQnaById(id);
        User user = userMapper.getUserByEmail(email);

        // 답변 등록 알림 메일 설정 부분
        String to = qna.getUser().getEmail();               // 답변 알림 받을 이메일
        String subject = "문의사항 답변이 등록되었습니다.";      // 메일 발송시 제목
        Map<String, Object> content = new HashMap<>();      // 메일 콘텐츠를 담을 Map 생성
        content.put("title", qna.getTitle());               // html 템플릿에 적용될 콘텐츠 담기
        content.put("answer", answer);                      // html 템플릿에 적용될 콘텐츠 담기

        mailService.sendEmail(to, subject, "answer-email", content);   // 메일 발송시 필요한 정보를 전달한다.

        // 문의사항 답변 등록
        qna.setAnswer(answer);
        qna.setAnswerWriter(user);
        qna.setAnswerYn("Y");
        qna.setUpdateDate(new Date());

        qnaMapper.updateAnswer(qna);

        // 실행시간을 보기 위해 log 출력
        long afterTime = System.currentTimeMillis();
        long diffTime = afterTime - beforeTime;
        log.info("답변 등록 후 메일발송 총 실행 시간: " + diffTime + "ms");      // 메일발송 및 답변 등록에 걸린 시간 조회

        return qna;
    }

    /**
     * 문의사항 전체 리스트 조회
     *
     * @param criteria 전체 조회할 리스트 기준
     * @return 반환된 목록
     */
    @Transactional(readOnly = true)
    public ListDto<Qna> getQnas(Criteria criteria) {

        int totalRows = qnaMapper.getTotalRows(criteria);

        Pagination pagination = new Pagination(criteria.getPage(), totalRows, criteria.getRows());

        criteria.setBegin(pagination.getBegin());
        criteria.setEnd(pagination.getEnd());

        List<Qna> qnas = qnaMapper.getQnas(criteria);

        ListDto<Qna> dto = new ListDto<>(qnas, pagination);

        return dto;
    }

    /**
     * 미응답 문의사항 리스트 조회
     *
     * @return 반환된 미응답리스트
     */
    @Transactional(readOnly = true)
    public List<Qna> getNoAnswerQnas() {
        return qnaMapper.getNoAnswerQnas();
    }

    /**
     * 문의 사항 1개 조회
     *
     * @param id 조회할 문의사항 번호
     * @return 조회된 문의사항
     */
    @Transactional(readOnly = true)
    public Qna getQnaById(int id) {

        return qnaMapper.getQnaById(id);
    }

    /**
     * 문의사항 카테고리 리스트 조회
     *
     * @return 문의사항 카테고리 리스트
     */
    @Transactional(readOnly = true)
    public List<QnaCategory> getQnaCategories() {

        return qnaMapper.getQnaCategories();
    }

    /**
     * 문의사항 카테고리 번호로 카테고리 정보 조회
     *
     * @param qnaCatId 조회할 카테고리 아이디
     * @return 문의사항 카테고리
     */
    @Transactional(readOnly = true)
    public QnaCategory getQnaCategory(int qnaCatId) {

        return qnaMapper.getQnaCategory(qnaCatId);
    }

    /**
     * 문의사항을 등록한다.
     *
     * @param form  저장할 문의사항 정보가 담긴 form객체
     * @param email 문의사항을 남기는 유저
     */
    public void createQna(QnaForm form, String email) {
        Qna qna = new Qna();

        User user = userMapper.getUserByEmail(email);

        qna.setCategory(QnaCategory.builder().id(form.getCategoryId()).build());
        if (form.getSecret() != null) {
            qna.setPrivateYn(form.getSecret());
        } else {
            qna.setPrivateYn("N");
        }
        qna.setTitle(form.getTitle());
        qna.setContent(form.getContent());
        qna.setUser(user);
        qnaMapper.createQna(qna);
    }

    /**
     * 유저 번호로 답변완료 문의내역 조회
     *
     * @param userId 조회할 유저번호
     * @return 답변완료 문의내역 리스트
     */
    @Transactional(readOnly = true)
    public List<Qna> getAnswerQnasByUserId(int userId, Pagination pagination) {
        // offset은 배열 인덱스 번호로 찾기 때문에 -1 한 값을 start로 전달한다.
        int start = pagination.getBegin() - 1;
        return qnaMapper.getAnswerQnasByUserId(userId, start);
    }

    /**
     * 유저 번호로 미답변 문의내역 ㅈ회
     *
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public List<Qna> getNoAnswerQnasByUserId(int userId, Pagination pagination) {
        // offset은 배열 인덱스 번호로 찾기 때문에 -1 한 값을 start로 전달한다.
        int start = pagination.getBegin() - 1;
        return qnaMapper.getNoAnswerQnasByUserId(userId, start);
    }

    /**
     * 문의 내역 삭제
     *
     * @param qnaId 삭제할 문의내역
     */
    public void deleteQna(int qnaId) {
        Qna qna = getQnaById(qnaId);
        qna.setDeleteYn("Y");
        qna.setDeleteDate(new Date());
        qnaMapper.updateAnswer(qna);
    }

    /**
     * 답변 여부에 따라 총 문의내역 개수를 반환한다.
     *
     * @param userId
     * @param answerYn
     * @return
     */
    public int getTotalQnaRowsByUserId(int userId, String answerYn) {
        return qnaMapper.getTotalQnaRowsByUserId(userId, answerYn);
    }
}
