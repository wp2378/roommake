package com.roommake.admin.management.service;

import com.roommake.admin.management.dto.BannerForm;
import com.roommake.admin.management.enums.BannerStatusEnum;
import com.roommake.admin.management.mapper.BannerMapper;
import com.roommake.admin.management.vo.Banner;
import com.roommake.dto.Criteria;
import com.roommake.dto.ListDto;
import com.roommake.dto.Pagination;
import com.roommake.user.vo.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerMapper bannerMapper;

    /**
     * 새로 만들 배너 정보를 form으로 입력받아 저장한다.
     *
     * @param bannerForm 저장할 배너 정보가 담긴 form객체
     * @param imageName  배너에 저장될 imageName
     * @param userId     저장한 유저
     */
    public void createBanner(BannerForm bannerForm, String imageName, int userId) {
        User user = User.builder().id(userId).build();

        String originName = "";
        if (!bannerForm.getImageFile().isEmpty()) {
            originName = bannerForm.getImageFile().getOriginalFilename();
        }

        Banner banner = Banner.builder()
                .user(user)
                .description(bannerForm.getDescription())
                .startDate(bannerForm.getStartDate())
                .endDate(bannerForm.getEndDate())
                .status(getBannerStatus(bannerForm, new Date()))
                .imageOriginName(originName)
                .imageUploadName(imageName)
                .url(bannerForm.getUrl())
                .build();
        bannerMapper.createBanner(banner);
        System.out.println(banner);
    }

    public Banner getBannerById(int id) {

        return bannerMapper.getBannerById(id);
    }

    /**
     * 배너를 수정한다.
     *
     * @param id         수정할 배너 id
     * @param bannerForm 수정될 내용이 담긴 form 객체
     * @param imageName  수정될 imageName
     * @param userId     수정한 유저
     * @return 수정된 배너 객체
     */
    public Banner modifyBanner(int id, BannerForm bannerForm, String imageName, int userId) {

        Banner banner = bannerMapper.getBannerById(id);
        User user = User.builder().id(userId).build();

        banner.setStartDate(bannerForm.getStartDate());
        banner.setEndDate(bannerForm.getEndDate());

        Date currentDate = new Date();
        banner.setStatus(getBannerStatus(bannerForm, currentDate));

        if (bannerForm.getImageFile() != null) {
            banner.setImageOriginName(bannerForm.getImageFile().getOriginalFilename());
            banner.setImageUploadName(imageName);
        }
        banner.setDescription(bannerForm.getDescription());
        banner.setUrl(bannerForm.getUrl());
        banner.setDeleteYn("N");
        banner.setUser(user);
        bannerMapper.modifyBanner(banner);

        return banner;
    }

    /**
     * form객체와 현재 날짜를 받아서 배너 상태를 반환해준다.
     *
     * @param form startDate와 endDate가 담긴 form객체
     * @param date 현재 날짜
     * @return 배너 상태
     */
    private String getBannerStatus(BannerForm form, Date date) {

        if (form.getStartDate().after(date)) {
            return BannerStatusEnum.EXPECT.getStatus();
        } else if (form.getEndDate().before(date)) {
            return BannerStatusEnum.END.getStatus();
        } else {
            return BannerStatusEnum.ACTIVE.getStatus();
        }
    }

    /**
     * id를 받아서 해당 배너를 삭제한다.
     *
     * @param id 삭제할 배너 아이디
     */
    public void deleteBanner(int id) {

        Banner banner = bannerMapper.getBannerById(id);
        banner.setDeleteYn("Y");
        banner.setDeleteDate(new Date());
        bannerMapper.modifyBanner(banner);
    }

    /**
     * 페이징, 검색, 정렬, 필터링 등 조건에 맞는 배너 리스트를 반환한다.
     *
     * @param criteria 배너를 가져올 조건이 담긴 객체
     * @return 배너 리스트
     */
    public ListDto<Banner> getBanners(Criteria criteria) {

        int totalRows = bannerMapper.getTotalRows(criteria);
        Pagination pagination = new Pagination(criteria.getPage(), totalRows, criteria.getRows());

        criteria.setBegin(pagination.getBegin());
        criteria.setEnd(pagination.getEnd());

        List<Banner> bannerList = bannerMapper.getBanners(criteria);
        ListDto<Banner> dto = new ListDto<>(bannerList, pagination);
        return dto;
    }

    /**
     * 삭제되지 않은 배너 반환한다.
     *
     * @return 배너 리스트
     */
    public List<Banner> getAllBanners() {

        return bannerMapper.getAllBanners();
    }

    /**
     * 매일 새벽 3시 게시 시작일, 종료일에 맞추어 배너 상태를 변경한다.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void modifyBannerStatus() {

        bannerMapper.modifyBannerStatus();

        log.info("배너 상태 업데이트");
    }
}
