package com.mb.brokerageprovider.data.entity;

import com.mb.brokerageprovider.enums.OrderStatus;
import com.mb.brokerageprovider.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
@SQLRestriction("deleted=false")
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE orders SET deleted=true WHERE id=?")
public class Order extends BaseEntity {

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.INITIATED;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderType type;

    private String productCode;

    private Long quantity;

    @Transient
    private Long userId;
}
