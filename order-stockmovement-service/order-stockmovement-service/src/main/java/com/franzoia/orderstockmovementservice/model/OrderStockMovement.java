package com.franzoia.orderstockmovementservice.model;

import com.franzoia.common.dto.OrderStockMovementKey;
import com.franzoia.common.util.audit.DefaultAuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_stock_movements")
public class OrderStockMovement extends DefaultAuditableEntity {

    @Id
    private OrderStockMovementKey key;

    @Column(nullable = false)
    private Long quantityUsed;

}
