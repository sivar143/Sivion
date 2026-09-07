package com.sivion.api.crm.web;

import com.sivion.api.crm.domain.*;
import com.sivion.api.crm.service.CrmService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/crm")
public class CrmController {
    private final CrmService service;

    public CrmController(CrmService service) { this.service = service; }

    @GetMapping("/customers")
    public List<Customer> customers(@RequestHeader(value="X-Tenant-ID", defaultValue="1") long tenantId, @RequestParam(required=false) String q) {
        return service.customers(tenantId, q);
    }

    @GetMapping("/customers/{id}")
    public Customer customer(@RequestHeader(value="X-Tenant-ID", defaultValue="1") long tenantId, @PathVariable long id) {
        return service.customer(tenantId, id);
    }

    @PostMapping("/customers")
    @ResponseStatus(HttpStatus.CREATED)
    public Customer createCustomer(@RequestHeader(value="X-Tenant-ID", defaultValue="1") long tenantId, @Valid @RequestBody Customer customer) {
        return service.saveCustomer(tenantId, customer);
    }

    @PutMapping("/customers/{id}")
    public Customer updateCustomer(@RequestHeader(value="X-Tenant-ID", defaultValue="1") long tenantId, @PathVariable long id, @Valid @RequestBody Customer customer) {
        return service.updateCustomer(tenantId, id, customer);
    }

    @GetMapping("/contacts")
    public List<Contact> contacts(@RequestHeader(value="X-Tenant-ID", defaultValue="1") long tenantId, @RequestParam(required=false) Long customerId) {
        return service.contacts(tenantId, customerId);
    }

    @PostMapping("/contacts")
    @ResponseStatus(HttpStatus.CREATED)
    public Contact createContact(@RequestHeader(value="X-Tenant-ID", defaultValue="1") long tenantId, @Valid @RequestBody Contact contact) {
        return service.saveContact(tenantId, contact);
    }

    @GetMapping("/leads")
    public List<Lead> leads(@RequestHeader(value="X-Tenant-ID", defaultValue="1") long tenantId, @RequestParam(required=false) String status) {
        return service.leads(tenantId, status);
    }

    @PostMapping("/leads")
    @ResponseStatus(HttpStatus.CREATED)
    public Lead createLead(@RequestHeader(value="X-Tenant-ID", defaultValue="1") long tenantId, @Valid @RequestBody Lead lead) {
        return service.saveLead(tenantId, lead);
    }

    @PatchMapping("/leads/{id}/status")
    public Lead updateLeadStatus(@RequestHeader(value="X-Tenant-ID", defaultValue="1") long tenantId, @PathVariable long id, @RequestBody Map<String,String> body) {
        return service.updateLeadStatus(tenantId, id, body.getOrDefault("status", "NEW"));
    }

    @GetMapping("/opportunities")
    public List<Opportunity> opportunities(@RequestHeader(value="X-Tenant-ID", defaultValue="1") long tenantId, @RequestParam(required=false) String stage) {
        return service.opportunities(tenantId, stage);
    }

    @PostMapping("/opportunities")
    @ResponseStatus(HttpStatus.CREATED)
    public Opportunity createOpportunity(@RequestHeader(value="X-Tenant-ID", defaultValue="1") long tenantId, @Valid @RequestBody Opportunity opportunity) {
        return service.saveOpportunity(tenantId, opportunity);
    }

    @PatchMapping("/opportunities/{id}/stage")
    public Opportunity updateOpportunityStage(@RequestHeader(value="X-Tenant-ID", defaultValue="1") long tenantId, @PathVariable long id, @RequestBody Map<String,String> body) {
        return service.updateOpportunityStage(tenantId, id, body.getOrDefault("stage", "QUALIFICATION"));
    }

    @GetMapping("/activities")
    public List<Activity> activities(@RequestHeader(value="X-Tenant-ID", defaultValue="1") long tenantId, @RequestParam(required=false) Long customerId) {
        return service.activities(tenantId, customerId);
    }

    @PostMapping("/activities")
    @ResponseStatus(HttpStatus.CREATED)
    public Activity createActivity(@RequestHeader(value="X-Tenant-ID", defaultValue="1") long tenantId, @Valid @RequestBody Activity activity) {
        return service.saveActivity(tenantId, activity);
    }
}
