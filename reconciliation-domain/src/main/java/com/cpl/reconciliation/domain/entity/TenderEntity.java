//package com.cpl.reconciliation.domain.entity;
//
//import java.io.Serializable;
//import lombok.Getter;
//import lombok.Setter;
//
//import javax.persistence.*;
//import java.util.Objects;
//
//@Getter
//@Setter
//@Entity
//@Table(name = "tender")
//public class TenderEntity implements Serializable {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    protected Long id;
//    private String rrn;
//    private String name;
//    private double amount;
//    @Column(name = "description", length = 1024)
//    private String description;
//    @ManyToOne
//    @JoinColumn(name = "order_id")
//    private OrderEntity order;
//
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        TenderEntity that = (TenderEntity) o;
//        return Objects.equals(name, that.name);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(name);
//    }
//}
