package br.com.supportflow.SupportFlow.user.service;

import br.com.supportflow.SupportFlow.common.dto.GenericResponse;
import br.com.supportflow.SupportFlow.common.exception.BusinessException;
import br.com.supportflow.SupportFlow.common.util.TokenGenerator;
import br.com.supportflow.SupportFlow.user.dto.UserEnable;
import br.com.supportflow.SupportFlow.user.dto.UserRegister;
import br.com.supportflow.SupportFlow.user.entity.Role;
import br.com.supportflow.SupportFlow.user.entity.User;
import br.com.supportflow.SupportFlow.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository _userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        _userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<User> getAll(String term, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        if (term == null || term.isEmpty()) {
            return _userRepository.findAll(pageable);
        } else {
            return _userRepository.findByNameContainingIgnoreCase(term, pageable);
        }
    }

    public GenericResponse register(UserRegister userRegister){
        try{

            var user = _userRepository.findByEmail(userRegister.email());
            if (user.isPresent()){
                throw new BusinessException("ALREADY_EXISTS", "User already registered!", HttpStatus.BAD_REQUEST);
            }

            var currentUser = new User();
            currentUser.setEmail(userRegister.email());
            currentUser.setName(userRegister.name());
            currentUser.setPassword(passwordEncoder.encode(userRegister.password()));
            currentUser.setActive(false);
            currentUser.setRole(Role.USER);
            currentUser.setToken(TokenGenerator.randomAlphaNumeric(20));
            _userRepository.save(currentUser);

            if(currentUser.getId() != null){
                return new GenericResponse("User registered successfully!");
            }else{
                throw new BusinessException("REGISTRATION_FAILED", "Failed to register user!", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }catch (Exception ex){
            throw new BusinessException("INVALID_CREDENTIALS", ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public GenericResponse userEnable(UserEnable userEnable){
        try{

            var user = _userRepository.findByToken(userEnable.token());
            if (user.isEmpty()){
                throw new BusinessException("ALREADY_EXISTS", "Invalid token!", HttpStatus.BAD_REQUEST);
            }

            if(user.isPresent() && user.get().isActive()){
                throw new BusinessException("ALREADY_ENABLE", "User already enable!", HttpStatus.BAD_REQUEST);
            }

            user.map(currentUser -> {
                currentUser.setActive(true);
                currentUser.setToken(TokenGenerator.randomAlphaNumeric(20));
                return _userRepository.save(currentUser);
            });

            return new GenericResponse("User enabled successfully!");

        }catch (Exception ex){
            System.out.println(ex.getMessage());
            throw new BusinessException("ERROR_ENABLE_USER", ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
