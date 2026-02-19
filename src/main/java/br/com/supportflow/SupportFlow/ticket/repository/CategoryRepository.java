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
    boolean existsByTitleAndGlobalTrue(String name);

    Optional<Category> findByTitle(String name);
    Optional<Category> findByTitleAndGlobalTrue(String name);

    @Query("SELECT new br.com.supportflow.SupportFlow.ticket.dto.CategoryResponse(c.id, c.title) " +
            "FROM Category c ")
    List<CategoryResponse> findAllWithoutTickets();

    @Query("SELECT new br.com.supportflow.SupportFlow.ticket.dto.CategoryResponse(c.id, c.title) " +
            "FROM Category c WHERE c.global = true")
    List<CategoryResponse> findAllGlobal();

    @Query("SELECT new br.com.supportflow.SupportFlow.ticket.dto.CategoryResponse(c.id, c.title) " +
            "FROM Category c WHERE c.client.id = :clientId")
    List<CategoryResponse> findAllByClientId(UUID clientId);

    @Query("SELECT new br.com.supportflow.SupportFlow.ticket.dto.CategoryResponse(c.id, c.title) " +
            "FROM Category c WHERE c.global = true OR c.client.id = :clientId")
    List<CategoryResponse> findAllByGlobalOrClientId(UUID clientId);

    boolean existsByTitleAndClientId(String title, UUID clientId);

}
