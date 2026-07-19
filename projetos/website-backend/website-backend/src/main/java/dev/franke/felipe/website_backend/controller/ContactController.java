package dev.franke.felipe.website_backend.controller;

import dev.franke.felipe.website_backend.dto.ContactRequest;
import dev.franke.felipe.website_backend.dto.ContactResponse;
import dev.franke.felipe.website_backend.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse saveContact(@Valid @RequestBody ContactRequest contactRequest) {
        return contactService.saveContact(contactRequest);
    }
}
