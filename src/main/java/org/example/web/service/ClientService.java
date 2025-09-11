package org.example.web.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.web.dto.client.ClientFilter;
import org.example.web.dto.client.ClientRequest;
import org.example.web.dto.client.ClientResponse;
import org.example.web.exception.ClientInUseException;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepository repo;
    private final OrderRepository orderRepo;
    private final ClientMapper mapper;


    public ClientResponse create(ClientRequest req) {
        log.debug("Client create start: firstName={}, lastName={}", req.getFirstName(), req.getLastName());
        if (repo.existsByEmailIgnoreCase(req.getEmail())) {
            throw new DuplicateEmailException(req.getEmail());
        }
        Client saved = repo.save(mapper.toEntity(req));
        log.info("Client created: id={}", saved.getId());

        return mapper.toResponse(saved, Map.of(saved.getId(), 0L));
    }

    public ClientResponse getOne(UUID id) {
        log.debug("Client getOne start: id={}", id);
        Client entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Client", id));
        long cnt = repo.countOrdersByClientId(id);
        return mapper.toResponse(entity, Map.of(id, cnt));
    }

    public ClientResponse update(UUID id, ClientRequest req) {
        log.debug("Client update start: id={}, newFirstName={}, newLastName={}",
                id, req.getFirstName(), req.getLastName());
        Client entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Client", id));

        if (!entity.getEmail().equalsIgnoreCase(req.getEmail())
                && repo.existsByEmailIgnoreCase(req.getEmail())) {
            throw new DuplicateEmailException(req.getEmail());
        }

        mapper.updateEntity(req, entity);
        log.info("Client updated: id={}, firstName={}, lastName={}, email={}",
                id, entity.getFirstName(), entity.getLastName(), entity.getEmail());
        long cnt = repo.countOrdersByClientId(id);
        return mapper.toResponse(entity, Map.of(id, cnt));
    }

    public void delete(UUID id) {
        log.debug("Client delete start: id={}", id);
        Client entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Client", id));

        if (orderRepo.existsByClientId(id)) {
            throw new ClientInUseException(id);
        }
        repo.delete(entity);
        log.info("Client deleted: id={}", id);
    }

    public Page<ClientResponse> findAll (ClientFilter filter,
                                         Pageable pageable) {
        log.debug("Find clients start: filter={}", filter);
        Specification<Client> spec = Specification
                .where(SpecBuilder.<Client>like("firstName", filter.firstName()))
                .and(SpecBuilder.like("lastName", filter.lastName()))
                .and(SpecBuilder.like("email", filter.email()))
                .and(SpecBuilder.like("phone", filter.phone()));

        Page<Client> page = repo.findAll(spec, pageable);
        log.debug("Clients page loaded: number={}, returned={}, total={}",
                page.getNumber(), page.getNumberOfElements(), page.getTotalElements());
        List<UUID> ids = page.getContent().stream().map(Client::getId).toList();

        Map<UUID, Long> counts = ids.isEmpty() ? Map.of()
                : repo.countOrdersByClientIds(ids).stream()
                .collect(Collectors.toMap(ClientRepository.ClientOrderCount::getClientId,
                        ClientRepository.ClientOrderCount::getCnt));

        List<ClientResponse> content = page.getContent().stream()
                .map(c -> mapper.toResponse(c, counts))
                .toList();

        log.debug("Clients mapped: {}", content.size());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }


}
