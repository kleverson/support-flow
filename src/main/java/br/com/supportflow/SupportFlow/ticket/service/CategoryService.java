package br.com.supportflow.SupportFlow.ticket.service;

import br.com.supportflow.SupportFlow.ticket.entity.Category;
import br.com.supportflow.SupportFlow.ticket.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }


}
