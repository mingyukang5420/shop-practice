package com.skala.shop.domain.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

	/** keyword(title/author 부분일치, 대소문자 무관)와 categoryId는 각각 독립적으로 선택 가능하다. */
	@Query("SELECT b FROM Book b WHERE "
			+ "(:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
			+ "AND (:categoryId IS NULL OR b.category.id = :categoryId)")
	Page<Book> search(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, Pageable pageable);
}
