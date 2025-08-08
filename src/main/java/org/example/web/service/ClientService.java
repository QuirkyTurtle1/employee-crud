package org.example.web.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.web.dto.client.ClientFilter;
import org.example.web.dto.client.ClientRequest;
import org.example.web.dto.client.ClientResponse;
import org.example.web.exception.DuplicateEmailException;
import org.example.web.exception.NotFoundException;
import org.example.web.mappers.ClientMapper;
import org.example.web.model.Client;
import org.example.web.repository.ClientRepository;
import org.example.web.util.SpecBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepository repo;
    private final ClientMapper mapper;


    public ClientResponse create(ClientRequest req) {
        if (repo.existsByEmailIgnoreCase(req.getEmail())) {
            throw new DuplicateEmailException(req.getEmail());
        }
        Client entity = mapper.toEntity(req);
        Client saved = repo.save(entity);
        return mapper.toResponse(saved);
    }

    public ClientResponse getOne(UUID id) {
        Client entity = repo.findById(id).orElseThrow(() -> new NotFoundException("Client", id));
        return mapper.toResponse(entity);
    }

    public ClientResponse update(UUID id, ClientRequest req) {
        Client entity = repo.findById(id).orElseThrow(() -> new NotFoundException("Client",id));
        if (!entity.getEmail().equalsIgnoreCase(req.getEmail())
                && repo.existsByEmailIgnoreCase(req.getEmail())) {
            throw new DuplicateEmailException(req.getEmail());
        }
        mapper.updateEntity(req, entity);
        return mapper.toResponse(entity);
    }

    public void delete(UUID id) {
        Client entity = repo.findById(id).orElseThrow(() -> new NotFoundException("Client", id));
        entity.getOrders().forEach(o -> o.setClient(null));
        repo.delete(entity);
    }

    public Page<ClientResponse> findAll (ClientFilter filter,
                                         Pageable pageable) {
        Specification<Client> spec = Specification
                .where(SpecBuilder.<Client>like("firstName", filter.firstName().orElse(null)))
                .and(SpecBuilder.like("lastName", filter.lastName().orElse(null)))
                .and(SpecBuilder.like("email", filter.email().orElse(null)))
                .and(SpecBuilder.like("phone", filter.phone().orElse(null)));

        return repo.findAll(spec, pageable).map(mapper::toResponse);
    }


}
