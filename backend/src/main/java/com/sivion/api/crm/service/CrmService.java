package com.sivion.api.crm.service;

import com.sivion.api.crm.domain.*;
import com.sivion.api.crm.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class CrmService {
    private static final Set<String> CUSTOMER_STATUSES = Set.of("ACTIVE", "INACTIVE");
    private static final Set<String> LEAD_STATUSES = Set.of("NEW", "CONTACTED", "QUALIFIED", "CONVERTED");
    private static final Set<String> LEAD_SOURCES = Set.of("WEBSITE", "REFERRAL", "EMAIL", "PHONE", "CAMPAIGN", "OTHER");
    private static final Set<String> LEAD_RATINGS = Set.of("HOT", "WARM", "COLD");
    private static final Set<String> OPPORTUNITY_STAGES = Set.of("QUALIFICATION", "NEEDS_ANALYSIS", "PROPOSAL", "NEGOTIATION", "CLOSED_WON", "CLOSED_LOST");
    private static final Set<String> ACTIVITY_TYPES = Set.of("NOTE", "CALL", "EMAIL", "MEETING", "TASK");

    private final CustomerRepository customers;
    private final ContactRepository contacts;
    private final LeadRepository leads;
    private final OpportunityRepository opportunities;
    private final ActivityRepository activities;

    public CrmService(CustomerRepository customers, ContactRepository contacts, LeadRepository leads,
                      OpportunityRepository opportunities, ActivityRepository activities) {
        this.customers = customers;
        this.contacts = contacts;
        this.leads = leads;
        this.opportunities = opportunities;
        this.activities = activities;
    }

    public List<Customer> customers(long tenantId, String q) {
        requireTenant(tenantId);
        return q == null || q.isBlank()
                ? customers.findByTenantIdOrderByNameAsc(tenantId)
                : customers.findByTenantIdAndNameContainingIgnoreCaseOrderByNameAsc(tenantId, q.trim());
    }

    public Customer customer(long tenantId, long id) {
        requireTenant(tenantId);
        return customers.findById(id)
                .filter(x -> tenantId == x.getTenantId())
                .orElseThrow(() -> new NoSuchElementException("Customer not found"));
    }

    public Customer saveCustomer(long tenantId, Customer customer) {
        requireTenant(tenantId);
        validateCustomer(customer);
        customer.setTenantId(tenantId);
        if (customer.getCode() == null || customer.getCode().isBlank()) {
            customer.setCode("CUS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        ensureUniqueCustomerCode(tenantId, customer.getCode(), null);
        return customers.save(customer);
    }

    public Customer updateCustomer(long tenantId, long id, Customer input) {
        Customer existing = customer(tenantId, id);
        validateCustomer(input);
        String code = input.getCode() == null || input.getCode().isBlank() ? existing.getCode() : input.getCode();
        ensureUniqueCustomerCode(tenantId, code, id);
        existing.setCode(code);
        existing.setName(input.getName());
        existing.setEmail(input.getEmail());
        existing.setPhone(input.getPhone());
        existing.setStatus(input.getStatus());
        return customers.save(existing);
    }

    public List<Contact> contacts(long tenantId, Long customerId) {
        requireTenant(tenantId);
        if (customerId == null) return contacts.findByTenantIdOrderByFirstNameAsc(tenantId);
        customer(tenantId, customerId);
        return contacts.findByTenantIdAndCustomerIdOrderByFirstNameAsc(tenantId, customerId);
    }

    public Contact saveContact(long tenantId, Contact contact) {
        requireTenant(tenantId);
        if (contact.getCustomerId() == null) throw new IllegalArgumentException("customerId is required");
        customer(tenantId, contact.getCustomerId());
        if (contact.getFirstName() == null || contact.getFirstName().isBlank()) throw new IllegalArgumentException("firstName is required");
        if (contact.getEmail() != null && !contact.getEmail().isBlank() && !contact.getEmail().contains("@")) throw new IllegalArgumentException("email must be valid");
        contact.setTenantId(tenantId);
        return contacts.save(contact);
    }

    public Lead lead(long tenantId, long id) {
        requireTenant(tenantId);
        return leads.findById(id)
                .filter(x -> tenantId == x.getTenantId())
                .orElseThrow(() -> new NoSuchElementException("Lead not found"));
    }

    public List<Lead> leads(long tenantId, String status) {
        requireTenant(tenantId);
        if (status == null || status.isBlank()) return leads.findByTenantIdOrderByCreatedAtDesc(tenantId);
        requireOneOf("status", status, LEAD_STATUSES);
        return leads.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, status);
    }

    public Lead saveLead(long tenantId, Lead lead) {
        requireTenant(tenantId);
        validateLead(lead);
        lead.setTenantId(tenantId);
        if (lead.getLeadNumber() == null || lead.getLeadNumber().isBlank()) {
            lead.setLeadNumber("LEAD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        return leads.save(lead);
    }

    public Lead updateLeadStatus(long tenantId, long id, String status) {
        Lead lead = lead(tenantId, id);
        requireOneOf("status", status, LEAD_STATUSES);
        lead.setStatus(status);
        return leads.save(lead);
    }

    public Opportunity opportunity(long tenantId, long id) {
        requireTenant(tenantId);
        return opportunities.findById(id)
                .filter(x -> tenantId == x.getTenantId())
                .orElseThrow(() -> new NoSuchElementException("Opportunity not found"));
    }

    public List<Opportunity> opportunities(long tenantId, String stage) {
        requireTenant(tenantId);
        if (stage == null || stage.isBlank()) return opportunities.findByTenantIdOrderByCreatedAtDesc(tenantId);
        requireOneOf("stage", stage, OPPORTUNITY_STAGES);
        return opportunities.findByTenantIdAndStageOrderByCreatedAtDesc(tenantId, stage);
    }

    public Opportunity saveOpportunity(long tenantId, Opportunity opportunity) {
        requireTenant(tenantId);
        validateOpportunity(opportunity);
        opportunity.setTenantId(tenantId);
        if (opportunity.getCustomerId() != null) customer(tenantId, opportunity.getCustomerId());
        if (opportunity.getLeadId() != null) lead(tenantId, opportunity.getLeadId());
        if (opportunity.getOpportunityNumber() == null || opportunity.getOpportunityNumber().isBlank()) {
            opportunity.setOpportunityNumber("OPP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (opportunity.getProbability() == null) opportunity.setProbability(BigDecimal.ZERO);
        validateProbability(opportunity.getProbability());
        return opportunities.save(opportunity);
    }

    public Opportunity updateOpportunityStage(long tenantId, long id, String stage) {
        Opportunity opportunity = opportunity(tenantId, id);
        requireOneOf("stage", stage, OPPORTUNITY_STAGES);
        opportunity.setStage(stage);
        if ("CLOSED_WON".equals(stage)) opportunity.setProbability(BigDecimal.valueOf(100));
        if ("CLOSED_LOST".equals(stage)) opportunity.setProbability(BigDecimal.ZERO);
        return opportunities.save(opportunity);
    }

    public List<Activity> activities(long tenantId, Long customerId) {
        requireTenant(tenantId);
        if (customerId == null) return activities.findByTenantIdOrderByCreatedAtDesc(tenantId);
        customer(tenantId, customerId);
        return activities.findByTenantIdAndCustomerIdOrderByCreatedAtDesc(tenantId, customerId);
    }

    public Activity saveActivity(long tenantId, Activity activity) {
        requireTenant(tenantId);
        if (activity.getSubject() == null || activity.getSubject().isBlank()) throw new IllegalArgumentException("subject is required");
        requireOneOf("type", activity.getType(), ACTIVITY_TYPES);
        if (activity.getCustomerId() != null) customer(tenantId, activity.getCustomerId());
        if (activity.getLeadId() != null) lead(tenantId, activity.getLeadId());
        if (activity.getOpportunityId() != null) opportunity(tenantId, activity.getOpportunityId());
        if (activity.getCustomerId() == null && activity.getLeadId() == null && activity.getOpportunityId() == null) {
            throw new IllegalArgumentException("activity must reference a customer, lead, or opportunity");
        }
        activity.setTenantId(tenantId);
        return activities.save(activity);
    }

    private void validateCustomer(Customer customer) {
        if (customer.getName() == null || customer.getName().isBlank()) throw new IllegalArgumentException("name is required");
        requireOneOf("status", customer.getStatus(), CUSTOMER_STATUSES);
    }

    private void validateLead(Lead lead) {
        if (lead.getName() == null || lead.getName().isBlank()) throw new IllegalArgumentException("name is required");
        requireOneOf("source", lead.getSource(), LEAD_SOURCES);
        requireOneOf("status", lead.getStatus(), LEAD_STATUSES);
        requireOneOf("rating", lead.getRating(), LEAD_RATINGS);
        if (lead.getEstimatedValue() != null && lead.getEstimatedValue().signum() < 0) throw new IllegalArgumentException("estimatedValue cannot be negative");
    }

    private void validateOpportunity(Opportunity opportunity) {
        if (opportunity.getName() == null || opportunity.getName().isBlank()) throw new IllegalArgumentException("name is required");
        requireOneOf("stage", opportunity.getStage(), OPPORTUNITY_STAGES);
        if (opportunity.getAmount() != null && opportunity.getAmount().signum() < 0) throw new IllegalArgumentException("amount cannot be negative");
    }

    private void validateProbability(BigDecimal probability) {
        if (probability.signum() < 0 || probability.compareTo(BigDecimal.valueOf(100)) > 0) throw new IllegalArgumentException("probability must be between 0 and 100");
    }

    private void ensureUniqueCustomerCode(long tenantId, String code, Long existingId) {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("code is required");
        customers.findByTenantIdAndCode(tenantId, code).ifPresent(found -> {
            if (!Objects.equals(found.getId(), existingId)) throw new IllegalArgumentException("customer code already exists");
        });
    }

    private void requireOneOf(String field, String value, Set<String> allowed) {
        if (value == null || !allowed.contains(value)) throw new IllegalArgumentException(field + " must be one of " + allowed);
    }

    private void requireTenant(long tenantId) {
        if (tenantId <= 0) throw new IllegalArgumentException("tenantId must be positive");
    }
}
