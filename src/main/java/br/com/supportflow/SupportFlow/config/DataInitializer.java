package br.com.supportflow.SupportFlow.config;

import br.com.supportflow.SupportFlow.ticket.entity.Category;
import br.com.supportflow.SupportFlow.ticket.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initCategories(CategoryRepository categoryRepository){
        return args -> {
            String defaultCategory = "UNCATEGORIZED";

            if(!categoryRepository.existsByTitleAndGlobalTrue(defaultCategory)){
                Category category = new Category();
                category.setTitle(defaultCategory);
                category.setGlobal(true);
                categoryRepository.save(category);
                System.out.println("Default Category CREATED");
            }else{
                System.out.println("Default Category already exists");
            }
        };
    }

}
