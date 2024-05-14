package com.roommake.cs.controller;

import com.roommake.admin.management.dto.QnaForm;
import com.roommake.admin.management.service.FaqService;
import com.roommake.admin.management.service.NoticeService;
import com.roommake.admin.management.service.QnaService;
import com.roommake.admin.management.vo.Faq;
import com.roommake.admin.management.vo.FaqCategory;
import com.roommake.admin.management.vo.Notice;
import com.roommake.admin.management.vo.QnaCategory;
import com.roommake.dto.Criteria;
import com.roommake.dto.ListDto;
import com.roommake.dto.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RequestMapping("/cs")
@Controller
@RequiredArgsConstructor
public class CsController {

    private final NoticeService noticeService;
    private final FaqService faqService;
    private final QnaService qnaService;

    @Cacheable(cacheNames = "getFaqCategories")
    @ModelAttribute("faqCategories")
    public List<FaqCategory> getFaqCategories() {
        log.info("faq 카테고리 목록 cache 저장");
        return faqService.getFaqCategories();
    }

    @GetMapping("/notice/list")
    public String noticeList(@RequestParam(name = "page", required = false, defaultValue = "1") int page,
                             @RequestParam(name = "rows", required = false, defaultValue = "10") int rows,
                             @RequestParam(name = "sort", required = false, defaultValue = "date") String sort,
                             @RequestParam(name = "opt", required = false) String opt,
                             @RequestParam(name = "keyword", required = false) String keyword,
                             Model model) {

        Criteria criteria = new Criteria();

        criteria.setPage(page);
        criteria.setRows(rows);
        criteria.setSort(sort);

        if (StringUtils.hasText(opt) && StringUtils.hasText(keyword)) {
            criteria.setOpt(opt);
            criteria.setKeyword(keyword);
        }

        ListDto<Notice> dto = noticeService.getNotices(criteria);
        model.addAttribute("noticeList", dto.getItems());
        model.addAttribute("paging", dto.getPaging());
        model.addAttribute("criteria", criteria);

        return "cs/notice/list";
    }

    @GetMapping("/notice/detail/{id}")
    public String detail(@PathVariable("id") int id, Model model) {

        Notice notice = noticeService.getNoticeById(id);
        model.addAttribute("notice", notice);
        return "cs/notice/detail";
    }

    @GetMapping("/faq/list")
    public String fnqList(@RequestParam(name = "page", required = false, defaultValue = "1") int page,
                          @RequestParam(name = "sort", required = false, defaultValue = "date") String sort,
                          @RequestParam(name = "filt", required = false, defaultValue = "all") String filt,
                          @RequestParam(name = "opt", required = false) String opt,
                          @RequestParam(name = "keyword", required = false) String keyword,
                          Model model) {

        Criteria criteria = new Criteria();

        criteria.setPage(page);
        criteria.setSort(sort);
        criteria.setFilt(filt);

        if (StringUtils.hasText(opt) && StringUtils.hasText(keyword)) {
            criteria.setOpt(opt);
            criteria.setKeyword(keyword);
        }

        ListDto<Faq> dto = faqService.getAllFaqs(criteria);
        model.addAttribute("faqs", dto.getItems());
        model.addAttribute("paging", dto.getPaging());
        model.addAttribute("criteria", criteria);

        return "cs/faq/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/qna/form")
    public String qnaForm(Model model) {

        List<QnaCategory> qnaCategories = qnaService.getQnaCategories();
        model.addAttribute("qnaCategories", qnaCategories);

        return "cs/qna/form";
    }

    @PostMapping("/qna/create")
    @PreAuthorize("isAuthenticated()")
    public String createQna(QnaForm form, Principal principal, Model model) {
        String email = principal.getName();
        qnaService.createQna(form, email);

        // model에 message라는 이름으로 alert창에 띄울 메세지 설정
        model.addAttribute("message", new Message("문의사항 작성이 완료되었습니다."));
        return "cs/qna/form";
    }
}
