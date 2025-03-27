package ru.muravin.marketplaceshowcase.dto;

import lombok.Data;
import ru.muravin.marketplaceshowcase.models.Order;
import ru.muravin.marketplaceshowcase.models.OrderItem;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class OrderToUIDto {
    private Long id;

    private Double sum = Double.valueOf(0);

    private List<OrderItemToUIDto> orderItems = new ArrayList<>();

    private LocalDateTime orderDate;

    public OrderToUIDto(Order order, List<OrderItemToUIDto> orderItems) {
        this.id = order.getId();
        this.orderDate = order.getOrderDate();
        for (OrderItemToUIDto orderItem : orderItems) {
            orderItem.setOrder(this);
            this.orderItems.add(orderItem);
            sum += orderItem.getPrice() * orderItem.getQuantity();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrderToUIDto that = (OrderToUIDto) o;
        return Objects.equals(id, that.id) && Objects.equals(sum, that.sum) && Objects.equals(orderItems, that.orderItems) && Objects.equals(orderDate, that.orderDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sum, orderItems, orderDate);
    }
}
