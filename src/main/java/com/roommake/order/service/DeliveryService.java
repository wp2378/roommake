package com.roommake.order.service;

import com.roommake.order.dto.DeliveryForm;
import com.roommake.order.mapper.DeliveryMapper;
import com.roommake.order.vo.Delivery;
import com.roommake.user.vo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryMapper deliveryMapper;

    /**
     * 로그인한 유저의 배송지 목록을 반환한다.
     *
     * @param userId 유저 번호
     * @return 로그인한 유저의 배송지 목록
     */
    @Transactional(readOnly = true)
    public List<Delivery> getDeliveriesByUserId(int userId) {
        return deliveryMapper.getDeliveriesByUserId(userId);
    }

    /**
     * 신규 배송지 정보가 저장된 DeliveryForm 객체를 전달받아서 배송지를 추가한다.
     *
     * @param form   신규 배송지 정보가 포함된 DeliveryForm 객체
     * @param userId 유저 번호
     */
    @Transactional
    public void createDelivery(DeliveryForm form, int userId) {
        User user = User.builder().id(userId).build();
        if ("Y".equals(form.getDefaultYn())) {
            deliveryMapper.updateDefaultDeliveryByUserId(user.getId());
        }

        Delivery delivery = Delivery.builder()
                .user(user)
                .name(form.getName())
                .recipient(form.getRecipient())
                .phone(form.getPhone())
                .address1(form.getAddress1())
                .address2(form.getAddress2())
                .zipcode(form.getZipcode())
                .defaultYn(form.getDefaultYn() == null ? "N" : form.getDefaultYn())
                .build();
        deliveryMapper.createDelivery(delivery);
    }

    /**
     * 지정된 배송지 번호에 해당하는 배송지의 삭제여부를 'Y'로 갱신한다.
     *
     * @param deliveryId 배송지 번호
     */
    public void deleteDelivery(int deliveryId) {
        deliveryMapper.deleteDelivery(deliveryId);
    }

    /**
     * 지정된 배송지 번호에 해당하는 배송지를 반환한다.
     *
     * @param deliveryId 배송지 번호
     * @return 배송지 번호에 해당하는 배송지 정보
     */
    @Transactional(readOnly = true)
    public Delivery getDeliveryById(int deliveryId) {
        return deliveryMapper.getDeliveryById(deliveryId);
    }

    /**
     * 지정된 배송지 번호에 해당하는 배송지의 정보를 변경한다.
     *
     * @param deliveryId 배송지 번호
     * @param form       수정할 배송지 정보가 포함된 DeliveryForm 객체
     */
    @Transactional
    public void modifyDelivery(int deliveryId, DeliveryForm form) {
        Delivery delivery = deliveryMapper.getDeliveryById(deliveryId);

        if ("Y".equals(form.getDefaultYn())) {
            deliveryMapper.updateDefaultDeliveryByUserId(delivery.getUser().getId());
        }

        delivery.setName(form.getName());
        delivery.setRecipient(form.getRecipient());
        delivery.setPhone(form.getPhone());
        delivery.setZipcode(form.getZipcode());
        delivery.setAddress1(form.getAddress1());
        delivery.setAddress2(form.getAddress2());
        delivery.setDefaultYn(form.getDefaultYn() == null ? "N" : form.getDefaultYn());
        deliveryMapper.modifyDelivery(delivery);
    }
}
