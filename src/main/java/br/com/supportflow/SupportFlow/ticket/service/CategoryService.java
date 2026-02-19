package br.com.supportflow.SupportFlow.ticket.service;

import br.com.supportflow.SupportFlow.common.exception.BusinessException;
import br.com.supportflow.SupportFlow.ticket.dto.CategoryResponse;
import br.com.supportflow.SupportFlow.ticket.entity.Category;
import br.com.supportflow.SupportFlow.ticket.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> listAll(){

        try{

            return categoryRepository.findAllWithoutTickets();
        }catch (Exception ex){
            throw new BusinessException("ERROR_LIST", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }
}
