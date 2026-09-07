package com.sivion.api.crm.service;

import com.sivion.api.crm.domain.*; import com.sivion.api.crm.repo.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;

@Service @Transactional
public class CrmService {
 private final CustomerRepository customers; private final ContactRepository contacts; private final LeadRepository leads; private final OpportunityRepository opportunities; private final ActivityRepository activities;
 public CrmService(CustomerRepository c,ContactRepository ct,LeadRepository l,OpportunityRepository o,ActivityRepository a){customers=c;contacts=ct;leads=l;opportunities=o;activities=a;}
 public List<Customer> customers(long t,String q){return q==null||q.isBlank()?customers.findByTenantIdOrderByNameAsc(t):customers.findByTenantIdAndNameContainingIgnoreCaseOrderByNameAsc(t,q);}
 public Customer customer(long t,long id){return customers.findById(id).filter(x->x.getTenantId()==t).orElseThrow(()->new NoSuchElementException("Customer not found"));}
 public Customer saveCustomer(long t,Customer x){x.setTenantId(t); if(x.getCode()==null||x.getCode().isBlank()) x.setCode("CUS-"+UUID.randomUUID().toString().substring(0,8).toUpperCase()); return customers.save(x);}
 public List<Contact> contacts(long t,Long customerId){return customerId==null?contacts.findByTenantIdOrderByFirstNameAsc(t):contacts.findByTenantIdAndCustomerIdOrderByFirstNameAsc(t,customerId);}
 public Contact saveContact(long t,Contact x){customer(t,x.getCustomerId());x.setTenantId(t);return contacts.save(x);}
 public List<Lead> leads(long t,String status){return status==null||status.isBlank()?leads.findByTenantIdOrderByCreatedAtDesc(t):leads.findByTenantIdAndStatusOrderByCreatedAtDesc(t,status);}
 public Lead saveLead(long t,Lead x){x.setTenantId(t);if(x.getLeadNumber()==null||x.getLeadNumber().isBlank())x.setLeadNumber("LEAD-"+UUID.randomUUID().toString().substring(0,8).toUpperCase());return leads.save(x);}
 public List<Opportunity> opportunities(long t,String stage){return stage==null||stage.isBlank()?opportunities.findByTenantIdOrderByCreatedAtDesc(t):opportunities.findByTenantIdAndStageOrderByCreatedAtDesc(t,stage);}
 public Opportunity saveOpportunity(long t,Opportunity x){x.setTenantId(t);if(x.getCustomerId()!=null)customer(t,x.getCustomerId());if(x.getOpportunityNumber()==null||x.getOpportunityNumber().isBlank())x.setOpportunityNumber("OPP-"+UUID.randomUUID().toString().substring(0,8).toUpperCase());return opportunities.save(x);}
 public List<Activity> activities(long t,Long customerId){return customerId==null?activities.findByTenantIdOrderByCreatedAtDesc(t):activities.findByTenantIdAndCustomerIdOrderByCreatedAtDesc(t,customerId);}
 public Activity saveActivity(long t,Activity x){x.setTenantId(t);if(x.getCustomerId()!=null)customer(t,x.getCustomerId());return activities.save(x);}
}
