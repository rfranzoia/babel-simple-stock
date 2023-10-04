package com.franzoia.orderservice.model;

import com.franzoia.common.dto.OrderStatus;
import com.franzoia.common.util.audit.DefaultAuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order extends DefaultAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime creationDate;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Long quantityOrdered;

    @Column(nullable = false)
    private Long quantityFulfilled;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private OrderStatus status;
}
