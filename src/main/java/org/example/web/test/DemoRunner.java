package org.example.web.test;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.web.model.Client;
import org.example.web.model.Order;
import org.example.web.model.OrderStatus;
import org.example.web.repository.ClientRepository;

import org.springframework.boot.CommandLineRunner;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DemoRunner implements CommandLineRunner {
    private final ClientRepository clientRepository;


    @Transactional
    @Override
    public void run(String... args) throws Exception {
        Client anna = Client.builder()
                .firstName("Kostya")
                .lastName("Chochua")
                .email("chochua@mail.ru")
                .phone("+79185585445")
                .build();

        Order order = Order.builder()
                .status(OrderStatus.NEW)
                .client(anna)
                .build();

        anna.getOrders().add(order);
        clientRepository.save(anna);
        long orderCount = clientRepository.findById(anna.getId())
                .orElseThrow()
                .getOrders().size();
        System.out.println("Заказов у Кости в БД: " + orderCount);
    }
}
