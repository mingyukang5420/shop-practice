package com.skala.shop.domain.cart;

import java.util.List;
import java.util.Optional;

import com.skala.shop.domain.book.Book;
import com.skala.shop.domain.member.Member;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	/** 동일 도서 재담기 시 수량 합산 대상을 찾기 위해 사용(기능명세서 3.1). */
	Optional<CartItem> findByMemberAndBook(Member member, Book book);

	List<CartItem> findAllByMember(Member member);
}
