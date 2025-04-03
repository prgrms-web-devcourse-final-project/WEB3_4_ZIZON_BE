package com.ll.dopdang.domain.expert.entity;


import com.ll.dopdang.domain.category.entity.Category;
import com.ll.dopdang.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private String introduction;

    private int careerYears;

    private String certification;

    private Boolean gender; // 0 남자 1 여자

    @Column(nullable = false, length = 100)
    private String bankName; // 은행명

    @Column(nullable = false, length = 100)
    private String accountNumber; // 계좌번호

    private boolean isAvailability;

    private String sellerInfo;
}
