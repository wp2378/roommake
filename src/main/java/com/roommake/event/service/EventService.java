package com.roommake.event.service;

import com.roommake.admin.management.mapper.BannerMapper;
import com.roommake.admin.management.vo.Banner;
import com.roommake.dto.Criteria;
import com.roommake.event.mapper.EventMapper;
import com.roommake.user.enums.PointReasonEnum;
import com.roommake.user.vo.PlusPointHistory;
import com.roommake.user.vo.PointType;
import com.roommake.user.vo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final BannerMapper bannerMapper;
    private final EventMapper eventMapper;

    /**
     * 정렬 및 옵션에 따라 전체 배너를 조회한다.
     *
     * @param criteria 필터, 시작, 끝
     * @return 배너 목록
     */
    @Transactional(readOnly = true)
    public List<Banner> getBanners(Criteria criteria) {
        criteria.setBegin(1);
        criteria.setEnd(10);
        List<Banner> bannerList = bannerMapper.getBanners(criteria);
        return bannerList;
    }

    /**
     * 유저 아이디와 오늘 날짜로 출석체크 여부를 확인하고, 출석체크시 10 포인트를 추가한다.
     *
     * @param userId 유저 아이디
     * @return 출석체크 실패 또는 성공 결과 메세지
     */
    public String addDailyCheckPoint(int userId) {
        if (eventMapper.getAttendance(userId) != null) {
            return "이미 출석체크를 하셨습니다.";
        } else {
            eventMapper.createAttendance(userId);

            PlusPointHistory plusPointHistory = PlusPointHistory.builder()
                    .amount(10)
                    .user(new User(userId))
                    .pointType(PointType.getPointType(4))
                    .pointReason(PointReasonEnum.DAILY_CHECK.getReason())
                    .build();
            eventMapper.addDailyCheckPoint(plusPointHistory);
            eventMapper.addDailyCheckPointToUser(plusPointHistory);
            return "출석체크가 완료되었습니다. 10 포인트가 적립되었습니다.";
        }
    }
}
