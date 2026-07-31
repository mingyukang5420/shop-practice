package com.skala.shop.domain.order;

import com.skala.shop.domain.member.Member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

	/** 내 주문 목록 조회(REQ-014)에 사용. 관리자 조회(REQ-016)는 회원 필터 없이 기본 findAll(Pageable)을 사용한다. */
	Page<Order> findAllByMember(Member member, Pageable pageable);
}
