package com.sivion.api.crm.web;

import com.sivion.api.crm.domain.*; import com.sivion.api.crm.service.CrmService; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*; import java.util.*;

@RestController @RequestMapping("/api/v1/crm")
public class CrmController {
 private final CrmService service; public CrmController(CrmService s){service=s;}
 @GetMapping("/customers") public List<Customer> customers(@RequestHeader(value="X-Tenant-ID",defaultValue="1") long t,@RequestParam(required=false) String q){return service.customers(t,q);}
 @GetMapping("/customers/{id}") public Customer customer(@RequestHeader(value="X-Tenant-ID",defaultValue="1") long t,@PathVariable long id){return service.customer(t,id);}
 @PostMapping("/customers") @ResponseStatus(HttpStatus.CREATED) public Customer createCustomer(@RequestHeader(value="X-Tenant-ID",defaultValue="1") long t,@RequestBody Customer x){return service.saveCustomer(t,x);}
 @PutMapping("/customers/{id}") public Customer updateCustomer(@RequestHeader(value="X-Tenant-ID",defaultValue="1") long t,@PathVariable long id,@RequestBody Customer x){Customer old=service.customer(t,id); old.setCode(x.getCode()==null?old.getCode():x.getCode()); old.setName(x.getName()); old.setEmail(x.getEmail()); old.setPhone(x.getPhone()); old.setStatus(x.getStatus()); return service.saveCustomer(t,old);}
 @GetMapping("/contacts") public List<Contact> contacts(@RequestHeader(value="X-Tenant-ID",defaultValue="1") long t,@RequestParam(required=false) Long customerId){return service.contacts(t,customerId);}
 @PostMapping("/contacts") @ResponseStatus(HttpStatus.CREATED) public Contact createContact(@RequestHeader(value="X-Tenant-ID",defaultValue="1") long t,@RequestBody Contact x){return service.saveContact(t,x);}
 @GetMapping("/leads") public List<Lead> leads(@RequestHeader(value="X-Tenant-ID",defaultValue="1") long t,@RequestParam(required=false) String status){return service.leads(t,status);}
 @PostMapping("/leads") @ResponseStatus(HttpStatus.CREATED) public Lead createLead(@RequestHeader(value="X-Tenant-ID",defaultValue="1") long t,@RequestBody Lead x){return service.saveLead(t,x);}
 @GetMapping("/opportunities") public List<Opportunity> opportunities(@RequestHeader(value="X-Tenant-ID",defaultValue="1") long t,@RequestParam(required=false) String stage){return service.opportunities(t,stage);}
 @PostMapping("/opportunities") @ResponseStatus(HttpStatus.CREATED) public Opportunity createOpportunity(@RequestHeader(value="X-Tenant-ID",defaultValue="1") long t,@RequestBody Opportunity x){return service.saveOpportunity(t,x);}
 @GetMapping("/activities") public List<Activity> activities(@RequestHeader(value="X-Tenant-ID",defaultValue="1") long t,@RequestParam(required=false) Long customerId){return service.activities(t,customerId);}
 @PostMapping("/activities") @ResponseStatus(HttpStatus.CREATED) public Activity createActivity(@RequestHeader(value="X-Tenant-ID",defaultValue="1") long t,@RequestBody Activity x){return service.saveActivity(t,x);}
}
