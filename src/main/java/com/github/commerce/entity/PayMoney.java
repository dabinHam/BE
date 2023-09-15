package com.github.commerce.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "pay_moneys")
public class PayMoney {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "users_id", nullable = false)
    private User users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_histories_id")
    private ChargeHistory chargeHistories;

    @OneToOne(mappedBy = "payMoney")
    private Payment payment;

    @Column(name = "charge_pay_money_total")
    private Long chargePayMoneyTotal;

    @Column(name = "used_charge_pay_money")
    private Long usedChargePayMoney;

    @Column(name = "pay_money_balance")
    private Long payMoneyBalance;

    @Column(name = "point_balance")
    private Long pointBalance;

    @CreatedDate
    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "pg_payment_id")
    private String pgPaymentId;

    // 새롭게 생성 될꺼라 , 유지되는 값이 들어가야 함.
    public static PayMoney payMoney(PayMoney payMoney){

        return PayMoney.builder()
                .users(payMoney.getUsers())
                .chargeHistories(payMoney.getChargeHistories())
                .chargePayMoneyTotal(payMoney.getChargePayMoneyTotal())
                .pgPaymentId(payMoney.getPgPaymentId())
                .build();
    }
}
