package com.program.order_service.service;


import com.program.order_service.dto.InventoryResponse;
import com.program.order_service.dto.OrderLineItemsDto;
import com.program.order_service.dto.OrderRequest;
import com.program.order_service.model.Order;
import com.program.order_service.model.OrderLineItems;
import com.program.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;
    private final DiscoveryClient discoveryClient;

    public void placeOrder(OrderRequest orderRequest) throws IllegalAccessException {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream()
                .map(this::mapToDto).toList();
        order.setOrderLineItemsList(orderLineItems);

        // lấy ra domain

        URI uri = discoveryClient.getInstances("inventory-service")
                        .stream().map(ServiceInstance::getUri).findFirst().orElse(null);

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                        .map(OrderLineItems::getSkuCode).toList();

        InventoryResponse [] inventoryResponses = webClient.get()
                        .uri(uri + "/api/inventory",
                                uriBuilder -> uriBuilder.queryParam("skuCodes", skuCodes).build())
                        .retrieve()
                        .bodyToMono(InventoryResponse[].class)
                        .block();

        boolean allProductsInStock = Arrays.stream(Objects.requireNonNull(inventoryResponses)).allMatch(InventoryResponse::getIsInStock);

        if (allProductsInStock) {
            orderRepository.save(order);
        }
        else {
            throw new IllegalAccessException("Product is not in stock, please try again later");
        }


    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
