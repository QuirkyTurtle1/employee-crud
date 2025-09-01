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
import org.example.web.repository.OrderRepository;
import org.example.web.util.SpecBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepository repo;
    private final OrderRepository orderRepo;
    private final ClientMapper mapper;


    public ClientResponse create(ClientRequest req) {
        if (repo.existsByEmailIgnoreCase(req.getEmail())) {
            throw new DuplicateEmailException(req.getEmail());
        }
        Client saved = repo.save(mapper.toEntity(req));

        return mapper.toResponse(saved, Map.of(saved.getId(), 0L));
    }

    public ClientResponse getOne(UUID id) {
        Client entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Client", id));
        long cnt = repo.countOrdersByClientId(id);
        return mapper.toResponse(entity, Map.of(id, cnt));
    }

    public ClientResponse update(UUID id, ClientRequest req) {
        Client entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Client", id));

        if (!entity.getEmail().equalsIgnoreCase(req.getEmail())
                && repo.existsByEmailIgnoreCase(req.getEmail())) {
            throw new DuplicateEmailException(req.getEmail());
        }

        mapper.updateEntity(req, entity);
        long cnt = repo.countOrdersByClientId(id);
        return mapper.toResponse(entity, Map.of(id, cnt));
    }

    public void delete(UUID id) {
        Client entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Client", id));

        if (orderRepo.existsByClientId(id)) {
            throw new IllegalStateException("У клиента есть заказы — удаление запрещено");
        }
        repo.delete(entity);
    }

    public Page<ClientResponse> findAll (ClientFilter filter,
                                         Pageable pageable) {
        Specification<Client> spec = Specification
                .where(SpecBuilder.<Client>like("firstName", filter.firstName().orElse(null)))
                .and(SpecBuilder.like("lastName", filter.lastName().orElse(null)))
                .and(SpecBuilder.like("email", filter.email().orElse(null)))
                .and(SpecBuilder.like("phone", filter.phone().orElse(null)));

        Page<Client> page = repo.findAll(spec, pageable);
        List<UUID> ids = page.getContent().stream().map(Client::getId).toList();

        Map<UUID, Long> counts = ids.isEmpty() ? Map.of()
                : repo.countOrdersByClientIds(ids).stream()
                .collect(Collectors.toMap(ClientRepository.ClientOrderCount::getClientId,
                        ClientRepository.ClientOrderCount::getCnt));

        List<ClientResponse> content = page.getContent().stream()
                .map(c -> mapper.toResponse(c, counts))
                .toList();

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }


}
