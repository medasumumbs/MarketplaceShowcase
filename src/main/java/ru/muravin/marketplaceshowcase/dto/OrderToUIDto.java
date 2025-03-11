package ru.muravin.marketplaceshowcase.dto;

import lombok.Data;
import ru.muravin.marketplaceshowcase.models.Order;
import ru.muravin.marketplaceshowcase.models.OrderItem;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderToUIDto {
    private Long id;

    private Double sum = Double.valueOf(0);

    private List<OrderItemToUIDto> orderItems = new ArrayList<>();

    private LocalDateTime orderDate;

    public OrderToUIDto(Order order) {
        this.id = order.getId();
        this.orderDate = order.getOrderDate();
        for (OrderItem orderItem : order.getOrderItems()) {
            orderItems.add(new OrderItemToUIDto(orderItem,this));
            sum += orderItem.getPrice() * orderItem.getQuantity();
        }

    }
}
