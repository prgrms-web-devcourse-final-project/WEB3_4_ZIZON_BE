package com.ll.dopdang.domain.review.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.ll.dopdang.domain.expert.entity.Expert;
import com.ll.dopdang.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewStats extends BaseEntity {

	@Id
	private Long expertId;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "expert_id")
	private Expert expert;

	@Column(nullable = false, precision = 3, scale = 2)
	private BigDecimal averageScore = BigDecimal.ZERO;

	@Column(nullable = false)
	private int reviewCount = 0;

	public static ReviewStats of(Expert expert, BigDecimal averageScore, int reviewCount) {
		ReviewStats stats = new ReviewStats();
		stats.expert = expert;
		stats.averageScore = averageScore;
		stats.reviewCount = reviewCount;
		return stats;
	}

	public void update(BigDecimal newAverageScore, int newReviewCount) {
		this.averageScore = newAverageScore;
		this.reviewCount = newReviewCount;
	}

	public void addReview(BigDecimal score) {
		int newCount = this.reviewCount + 1;
		BigDecimal newAverage = this.averageScore
			.multiply(BigDecimal.valueOf(this.reviewCount))
			.add(score)
			.divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);

		update(newAverage, newCount);
	}

	public void removeReview(BigDecimal score) {
		int newCount = this.reviewCount - 1;

		if (newCount == 0) {
			update(BigDecimal.ZERO, 0);
		} else {
			BigDecimal newAverage = this.averageScore
				.multiply(BigDecimal.valueOf(this.reviewCount))
				.subtract(score)
				.divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);

			update(newAverage, newCount);
		}
	}

}
