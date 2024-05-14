package com.roommake.admin.Dashboard.service;

import com.roommake.admin.Dashboard.dto.DashboardDto;
import com.roommake.admin.Dashboard.dto.OrderStatusData;
import com.roommake.admin.Dashboard.mapper.DashboardMapper;
import com.roommake.admin.Dashboard.vo.SalesData;
import com.roommake.admin.management.dto.ComplaintDto;
import com.roommake.admin.management.service.ComplaintService;
import com.roommake.admin.management.service.QnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashBoardService {

    private final QnaService qnaService;
    private final DashboardMapper dashboardMapper;
    private final ComplaintService complaintService;
    private final VisitorService visitorService;

    LocalDate today = LocalDate.now();
    String todayStr = today.toString();
    LocalDate yesterDay = today.minusDays(1);
    String yesterDayStr = yesterDay.toString();

    public DashboardDto getAdminHomeDto() {
        DashboardDto dto = new DashboardDto();

        dto.setOrderStatusDataList(getOrderStatusData(todayStr));   // 주문상태별 건수 데이터

        dto.setSalesDataList(getSalesData(yesterDayStr, 6));        // 어제부터 일주일치 매출 데이터

        List<ComplaintDto> complaintDtos = new ArrayList<>();
        complaintDtos.addAll(complaintService.getBoardComplaints("N"));   // 미처리 게시글 신고
        complaintDtos.addAll(complaintService.getReplyComplaints("N"));   // 미처리 댓글 신고
        dto.setNoConfirmComplaints(complaintDtos);

        dto.setNoAnswerQnas(qnaService.getNoAnswerQnas());              // 미응답 문의사항

        dto.setNewUserCnt(dashboardMapper.getNewUserCnt(yesterDayStr));    // 신규가입자
        dto.setTotalUserCnt(dashboardMapper.getUserCntByDate(todayStr));    // 누적 가입자

        dto.setNewVisitorCnt(visitorService.getVisitorCount());             // 전날 방문자
        dto.setVisitorCounts(visitorService.getTotalVisitorCount());        // 누적 방문자수

        return dto;
    }

    /**
     * date부터 days만큼의 매출 정보를 반환한다.
     * 조회시작일자부터 포함이기 때문에 일주일을 구하고 싶다면 days를 6으로 설정한다.
     *
     * @param date 조회시작일자
     * @param days 조회하고 싶은 기간
     * @return 매출정보
     */
    @Transactional(readOnly = true)
    public List<SalesData> getSalesData(String date, int days) {
        long beforeTime = System.currentTimeMillis();

        List<SalesData> salesDataList = dashboardMapper.getSalesData(date, days);

        long afterTime = System.currentTimeMillis();
        long diffTime = afterTime - beforeTime;
        log.info("매출데이터 조회 시간: " + diffTime + "ms");

        return salesDataList;
    }

    /**
     * 주문상태별 건수
     *
     * @param date 상태 변동이 일어난 날짜
     * @return 해당 날짜에 발생한 건수
     */
    @Transactional(readOnly = true)
    public List<OrderStatusData> getOrderStatusData(String date) {
        long beforeTime = System.currentTimeMillis();

        List<OrderStatusData> orderStatusDataList = dashboardMapper.getOrderStatusData(date);

        long afterTime = System.currentTimeMillis();
        long diffTime = afterTime - beforeTime;
        log.info("주문상태별 건수 조회 시간: " + diffTime + "ms");

        return orderStatusDataList;
    }

    /**
     * 매일 오전 9시 전날 매출데이터를 저장한다.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void createSalesDate() {
        dashboardMapper.createSalesData(yesterDayStr);
        log.info("매출 데이터 저장");
    }
}
