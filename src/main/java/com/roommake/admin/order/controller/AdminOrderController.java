package com.roommake.admin.order.controller;

import com.roommake.admin.order.dto.AdminExchangeDto;
import com.roommake.admin.order.dto.ItemCancelDto;
import com.roommake.admin.order.dto.ItemReturnDto;
import com.roommake.admin.order.dto.OrderHistoryResponseDto;
import com.roommake.admin.order.service.AdminOrderService;
import com.roommake.admin.refund.AdminRefundDto;
import com.roommake.order.vo.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {
    private final AdminOrderService adminOrderService;

    //교환페이지
    @GetMapping("/exchange")
    public String exchange(Model model) {
        List<AdminExchangeDto> exchanges = adminOrderService.getAllExchanges();
        model.addAttribute("exchanges", exchanges);

        return "admin/order/exchange";
    }

    //교환 상세페이지
    @GetMapping("/exchange-detail/{id}")
    public String exchangedetail(Model model, @PathVariable Long id) {
        AdminExchangeDto exchangeById = adminOrderService.getExchangeById(id);
        model.addAttribute("exchange", exchangeById);
        return "admin/order/exchange-detail";
    }

    /**
     * @param id 교환 상세페이지
     * @return
     */
    @GetMapping("/exchange/{id}")
    public String updateExchangeApprovalYn(@PathVariable int id) {
        adminOrderService.updateExchangeApprovalYn(id);
        return "redirect:/admin/order/exchange";
    }

    /**
     * @param model 반품
     * @return
     */
    @GetMapping("/refund")
    public String refund(Model model) {
        List<AdminRefundDto> refund = adminOrderService.getAllRefund();
        model.addAttribute("refunds", refund);

        return "admin/order/refund";
    }

    //반품페이지
    @GetMapping("/item-return")
    public String returnpage(Model model) {
        List<ItemReturnDto> itemReturnDtoList = adminOrderService.getAllItemReturn();
        model.addAttribute("itemReturnList", itemReturnDtoList);
        return "admin/order/item-return";
    }

    /**
     * @param ItemReturnDtoList 반품업데이트
     * @return
     */
    @PostMapping("/updateReturn")
    @ResponseBody
    public int updateReturn(@RequestBody List<ItemReturnDto> ItemReturnDtoList) {
        String itemReturnStatus = "반품완료";
        String itemReturnYn = "Y";

        List<Integer> itemReturnId = ItemReturnDtoList.stream()
                .map(ItemReturnDto::getItemReturnId)
                .toList();

        int updateReturnYnResult = adminOrderService.updateReturnYn(itemReturnStatus, itemReturnYn, itemReturnId);
        return updateReturnYnResult;
    }

    // 주문 취소 내역
    @GetMapping("/orderCancel")
    public String orderCannel(Model model) {
        List<ItemCancelDto> orderCancel = adminOrderService.getAllorderCancel();
        model.addAttribute("orderCancels", orderCancel);
        return "admin/order/orderCancel";
    }

    /**
     * @param order 배송상태값 업데이트
     */
    @PostMapping("/updateDeliveryNo")
    @ResponseBody
    public void updateDeliveryNo(@RequestBody Order order) {
        adminOrderService.createDeliveryNo(order);
    }
    
    @PostMapping("/updateItem")
    @ResponseBody
    public void orderItem(@RequestBody Order order) {
        adminOrderService.updateOrderStatus(order);
    }

    /**
     * @param model 주문내역리스트
     * @return
     */
    @GetMapping("/item")
    public String order(Model model) {
        List<OrderHistoryResponseDto> orders = adminOrderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "admin/order/item";
    }
}

