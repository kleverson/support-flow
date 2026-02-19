package br.com.supportflow.SupportFlow.client.service;

import br.com.supportflow.SupportFlow.client.dto.ClientPostBody;
import br.com.supportflow.SupportFlow.client.entity.Client;
import br.com.supportflow.SupportFlow.client.repository.ClientRepository;
import br.com.supportflow.SupportFlow.common.dto.GenericResponse;
import br.com.supportflow.SupportFlow.common.exception.BusinessException;
import br.com.supportflow.SupportFlow.common.util.Base64Utils;
import br.com.supportflow.SupportFlow.common.util.StorageFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClientService {
    private final ClientRepository clientRepository;
    private final StorageFile storageFile;


    public ClientService(ClientRepository clientRepository, StorageFile storageFile) {
        this.clientRepository = clientRepository;
        this.storageFile = storageFile;
    }



    public Client get(UUID id) {
        return clientRepository.findById(id).orElseThrow(() -> new BusinessException("NOT_FOUND", "Client not found!", HttpStatus.NOT_FOUND));
    }

    public Page<Client> getAll(String term, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String searchTerm = term == null ? "" : term.trim();

        Page<Client> clients = searchTerm.isBlank()
                ? clientRepository.findAll(pageable)
                : clientRepository.findByNameContainingIgnoreCase(
                searchTerm,
                pageable
        );

        return clients;
    }

    @Transactional
    public GenericResponse create(ClientPostBody clientPostBody) {
        try {
            var client = new Client();

            if(!clientPostBody.brand().isEmpty()){
                String contentType = Base64Utils.resolveContentType(clientPostBody.brand());
                String file = storageFile.uploadFile(clientPostBody.brand(), "clients/brands", contentType);

                client.setBrand(file);
            }

            client.setName(clientPostBody.name());
            client.setEmail(clientPostBody.email());
            client.setPhone(clientPostBody.phone());
            clientRepository.save(client);

            if (client.getId() != null) {
                return new GenericResponse("Client created successfully!");
            } else {
                throw new BusinessException("CREATION_FAILED", "Failed to create client!", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            throw new BusinessException("PERSIST_ERROR", ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public GenericResponse update(UUID id, ClientPostBody clientPostBody) {
        try {
            var currentClient = clientRepository.findById(id).orElseThrow(() -> new BusinessException("NOT_FOUND", "Client not found!", HttpStatus.NOT_FOUND));
            currentClient.setName(clientPostBody.name());
            currentClient.setEmail(clientPostBody.email());
            currentClient.setPhone(clientPostBody.phone());
            clientRepository.save(currentClient);

            return new GenericResponse("Client updated successfully!");
        } catch (Exception ex) {
            throw new BusinessException("PERSIST_ERROR", ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public GenericResponse deleteOrRestore(UUID id) {
        try {
            var currentClient = clientRepository.findById(id).orElseThrow(() -> new BusinessException("NOT_FOUND", "Client not found!", HttpStatus.NOT_FOUND));

            currentClient.setActive(!currentClient.isActive());
            clientRepository.save(currentClient);

            return new GenericResponse(currentClient.isActive() ? "Client restored successfully!" : "Client deleted successfully!");
        } catch (Exception ex) {
            throw new BusinessException("PERSIST_ERROR", ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
