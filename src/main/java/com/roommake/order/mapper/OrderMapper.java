package com.roommake.order.mapper;

import com.roommake.cart.dto.CartCreateForm;
import com.roommake.cart.dto.CartItemDto;
import com.roommake.order.dto.OrderDto;
import com.roommake.order.dto.OrderItemDto;
import com.roommake.order.vo.Delivery;
import com.roommake.order.vo.Order;
import com.roommake.order.vo.OrderItem;
import com.roommake.order.vo.Payment;
import com.roommake.user.vo.MinusPointHistory;
import com.roommake.user.vo.UserGrade;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    CartItemDto getProductsByDetailId(CartCreateForm cartCreateForm);

    Delivery getDefaultDeliveryByUserId(int id);

    CartItemDto getProductDetailByDetailId(int id);

    int createOrder(Order order);

    void createOrderItem(OrderItem orderItem);

    void createPayment(Payment payment);

    void createMinusPointHistory(MinusPointHistory history);

    void minusPointToUser(@Param("amount") int amount, @Param("userId") int userId);

    OrderDto getOrderById(int orderId);

    Payment getPaymentByOrderId(int orderId);

    Delivery getDeliveryByOrderId(int orderId);

    List<OrderItemDto> getItemsByOrderId(int orderId);

    void updateConfirmOrderItemById(int id);

    void createPlusPointHistory(@Param("amount") int amount,
                                @Param("userId") int userId,
                                @Param("typeId") int typeId,
                                @Param("pointReason") String pointReason);

    void addPointToUser(@Param("amount") int amount, @Param("userId") int userId);

    UserGrade getUserGradeById(int id);

    void updateConfirmOrder();

    List<Order> getOrdersByStatus();
}
