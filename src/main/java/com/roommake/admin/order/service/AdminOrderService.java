package com.roommake.admin.order.service;

import com.roommake.admin.order.dto.OrderHistoryResponseDto;
import com.roommake.admin.order.mapper.AdminOrderMapper;
import com.roommake.admin.refund.AdminRefundDto;
import com.roommake.order.vo.Exchange;
import com.roommake.order.vo.Order;
import com.roommake.order.vo.OrderCancel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    private final AdminOrderMapper adminOrderMapper;

    @Transactional
    public void updateExchange(int id) {
        adminOrderMapper.updateExchangeApprovalYn(id);
    }

    public List<OrderHistoryResponseDto> getAllOrders() {
        return adminOrderMapper.getAllOrders();
    }

    public int updateOrderStatus(Order order) {
        return adminOrderMapper.updateOrderStatus(order);
    }

    public List<AdminRefundDto> getAllRefund() {
        return adminOrderMapper.getRefund();
    }

    public List<Exchange> getAllExchanges() {
        return adminOrderMapper.getAllExchanges();
    }

    public List<OrderCancel> getAllorderCancel() {
        return adminOrderMapper.getAllorderCancels();
    }
}
