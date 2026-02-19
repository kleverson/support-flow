package br.com.supportflow.SupportFlow.ticket.service;

import br.com.supportflow.SupportFlow.client.repository.ClientRepository;
import br.com.supportflow.SupportFlow.common.exception.BusinessException;
import br.com.supportflow.SupportFlow.common.security.ClientAccessGuard;
import br.com.supportflow.SupportFlow.common.security.ClientContextResolver;
import br.com.supportflow.SupportFlow.ticket.dto.CategoryCreateBody;
import br.com.supportflow.SupportFlow.ticket.dto.CategoryResponse;
import br.com.supportflow.SupportFlow.ticket.entity.Category;
import br.com.supportflow.SupportFlow.ticket.repository.CategoryRepository;
import br.com.supportflow.SupportFlow.user.entity.Role;
import br.com.supportflow.SupportFlow.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ClientContextResolver clientContextResolver;
    private final ClientAccessGuard clientAccessGuard;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            ClientContextResolver clientContextResolver,
            ClientAccessGuard clientAccessGuard,
            UserRepository userRepository,
            ClientRepository clientRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.clientContextResolver = clientContextResolver;
        this.clientAccessGuard = clientAccessGuard;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listByScope(String scope, String clientIdHeader, Authentication authentication) {
        String resolvedScope = scope == null ? "all" : scope.trim().toLowerCase();

        if ("global".equals(resolvedScope)) {
            return categoryRepository.findAllGlobal();
        }

        UUID clientId = clientContextResolver.resolveRequired(clientIdHeader);
        clientAccessGuard.assertUserHasAccess(authentication, clientId);

        return switch (resolvedScope) {
            case "client" -> categoryRepository.findAllByClientId(clientId);
            case "all" -> categoryRepository.findAllByGlobalOrClientId(clientId);
            default -> throw new BusinessException("INVALID_SCOPE", "Scope must be global, client or all", HttpStatus.BAD_REQUEST);
        };
    }

    @Transactional
    public CategoryResponse create(CategoryCreateBody body, String clientIdHeader, Authentication authentication) {
        if (body.global()) {
            var user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
            if (!Role.ADMIN.equals(user.getRole())) {
                throw new BusinessException("FORBIDDEN_GLOBAL_CATEGORY", "Only admin can create global categories", HttpStatus.FORBIDDEN);
            }
            if (categoryRepository.existsByTitleAndGlobalTrue(body.title())) {
                throw new BusinessException("ALREADY_EXISTS", "Global category already exists", HttpStatus.BAD_REQUEST);
            }

            Category category = new Category();
            category.setTitle(body.title().trim());
            category.setGlobal(true);
            category.setClient(null);
            return toResponse(categoryRepository.save(category));
        }

        UUID clientId = clientContextResolver.resolveRequired(clientIdHeader);
        clientAccessGuard.assertUserHasAccess(authentication, clientId);

        if (categoryRepository.existsByTitleAndClientId(body.title(), clientId)) {
            throw new BusinessException("ALREADY_EXISTS", "Category already exists for this client", HttpStatus.BAD_REQUEST);
        }

        Category category = new Category();
        category.setTitle(body.title().trim());
        category.setGlobal(false);
        category.setClient(clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException("CLIENT_NOT_FOUND", "Client not found", HttpStatus.NOT_FOUND)));

        return toResponse(categoryRepository.save(category));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getTitle());
    }
}
