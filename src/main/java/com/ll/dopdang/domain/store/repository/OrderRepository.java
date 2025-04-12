package com.ll.dopdang.domain.store.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.store.dto.DigitalContentProjection;
import com.ll.dopdang.domain.store.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query(value =
		"select o.id, o.order_number, o.total_amount, o.status, o.payment_method, o.created_at, "
			+ "p.id as product_id, p.title as product_title, p.price as product_price, "
			+ "p.thumbnail_image as product_thumbnail, p.product_type, "
			+ "oi.quantity, "
			+ "m.name as seller_name "
			+ "from order_item oi "
			+ "    left join product p on oi.product_id = p.id "
			+ "    left join `order` o on oi.order_id = o.id "
			+ "    left join expert e on p.expert_id = e.id "
			+ "    left join member m on e.member_id = m.id "
			+ "where o.member_id = :memberId "
			+ "order by o.created_at desc "
			+ "limit :#{#pageable.pageSize} offset :#{#pageable.offset}"
		, nativeQuery = true)
	List<Map<String, Object>> findAllOrderDetailsWithProductByMemberId(Long memberId, Pageable pageable);

	@Query(value = "SELECT COUNT(DISTINCT o.id) FROM `order` o WHERE o.member_id = :memberId", nativeQuery = true)
	Long countOrdersByMemberId(Long memberId);

	@Query(value = "select dc.id, dc.file_name as fileName, dc.file_url as fileUrl, "
		+ "dc.file_size as fileSize, dc.file_type as fileType, dc.download_limit as downloadLimit "
		+ "from digital_content dc "
		+ "left join order_item oi on dc.product_id = oi.product_id "
		+ "left join `order` o on oi.order_id = o.id "
		+ "where oi.id is not null and o.id = :id ",
		nativeQuery = true)
	List<DigitalContentProjection> findAllDigitalContentByOrderId(Long id);
}

// select dc.* from digital_content dc
// left join order_item oi on dc.product_id = oi.product_id
// left join `order` o on oi.order_id = o.id
// where oi.id is not null and o.id = :id

// 이걸 살리고, digital_content는 따로 쿼리문 작성해서 엮는걸로
// select *
// from order_item oi
//     left join product p on oi.product_id = p.id
//     left join `order` o on oi.order_id = o.id
// where o.member_id = 80

//select *
// from order_item oi
//     left join product p on oi.product_id = p.id
//     left join `order` o on oi.order_id = o.id
//     left join digital_content dc on p.id = dc.product_id
// where o.member_id = 80
