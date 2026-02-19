package br.com.supportflow.SupportFlow.ticket.repository;

import br.com.supportflow.SupportFlow.ticket.dto.CategoryResponse;
import br.com.supportflow.SupportFlow.ticket.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

import java.util.List;
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByTitle(String name);

    Optional<Category> findByTitle(String name);

    @Query("SELECT new br.com.supportflow.SupportFlow.ticket.dto.CategoryResponse(c.id, c.title) " +
            "FROM Category c ")
    List<CategoryResponse> findAllWithoutTickets();

}
