CREATE TABLE tenants (id BIGINT PRIMARY KEY AUTO_INCREMENT, code VARCHAR(50) NOT NULL UNIQUE, name VARCHAR(200) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);
CREATE TABLE customers (id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, code VARCHAR(50) NOT NULL, name VARCHAR(200) NOT NULL, email VARCHAR(255), phone VARCHAR(40), status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, UNIQUE KEY uk_customer_tenant_code (tenant_id, code), FOREIGN KEY (tenant_id) REFERENCES tenants(id));
CREATE TABLE products (id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, sku VARCHAR(80) NOT NULL, name VARCHAR(200) NOT NULL, description TEXT, unit VARCHAR(30) NOT NULL DEFAULT 'EA', cost_price DECIMAL(18,4) NOT NULL DEFAULT 0, selling_price DECIMAL(18,4) NOT NULL DEFAULT 0, reorder_level DECIMAL(18,4) NOT NULL DEFAULT 0, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, UNIQUE KEY uk_product_tenant_sku (tenant_id, sku), FOREIGN KEY (tenant_id) REFERENCES tenants(id));
CREATE TABLE warehouses (id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, code VARCHAR(50) NOT NULL, name VARCHAR(200) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', UNIQUE KEY uk_warehouse_tenant_code (tenant_id, code), FOREIGN KEY (tenant_id) REFERENCES tenants(id));
CREATE TABLE inventory_transactions (id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, warehouse_id BIGINT NOT NULL, product_id BIGINT NOT NULL, transaction_type VARCHAR(40) NOT NULL, quantity DECIMAL(18,4) NOT NULL, reference_type VARCHAR(40), reference_id BIGINT, correlation_id VARCHAR(100), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX ix_inventory_product (tenant_id, warehouse_id, product_id), FOREIGN KEY (tenant_id) REFERENCES tenants(id), FOREIGN KEY (warehouse_id) REFERENCES warehouses(id), FOREIGN KEY (product_id) REFERENCES products(id));
CREATE TABLE inventory_dispatches (id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, dispatch_number VARCHAR(50) NOT NULL, customer_id BIGINT NOT NULL, reference_number VARCHAR(80), dispatch_date TIMESTAMP NOT NULL, delivery_address VARCHAR(500), notes VARCHAR(1000), status VARCHAR(20) NOT NULL DEFAULT 'DISPATCHED', UNIQUE KEY uk_dispatch_tenant_number (tenant_id,dispatch_number), INDEX ix_dispatch_tenant_date (tenant_id,dispatch_date), FOREIGN KEY (tenant_id) REFERENCES tenants(id), FOREIGN KEY (customer_id) REFERENCES customers(id));
CREATE TABLE inventory_dispatch_items (id BIGINT PRIMARY KEY AUTO_INCREMENT, dispatch_id BIGINT NOT NULL, product_id BIGINT NOT NULL, warehouse_id BIGINT NOT NULL, quantity DECIMAL(18,4) NOT NULL, FOREIGN KEY (dispatch_id) REFERENCES inventory_dispatches(id), FOREIGN KEY (product_id) REFERENCES products(id), FOREIGN KEY (warehouse_id) REFERENCES warehouses(id));
CREATE TABLE sales_orders (id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, order_number VARCHAR(50) NOT NULL, customer_id BIGINT NOT NULL, status VARCHAR(40) NOT NULL DEFAULT 'DRAFT', subtotal DECIMAL(18,4) NOT NULL DEFAULT 0, tax_amount DECIMAL(18,4) NOT NULL DEFAULT 0, total_amount DECIMAL(18,4) NOT NULL DEFAULT 0, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, UNIQUE KEY uk_order_tenant_number (tenant_id, order_number), FOREIGN KEY (tenant_id) REFERENCES tenants(id), FOREIGN KEY (customer_id) REFERENCES customers(id));
CREATE TABLE sales_order_items (id BIGINT PRIMARY KEY AUTO_INCREMENT, order_id BIGINT NOT NULL, product_id BIGINT NOT NULL, quantity DECIMAL(18,4) NOT NULL, unit_price DECIMAL(18,4) NOT NULL, tax_amount DECIMAL(18,4) NOT NULL DEFAULT 0, line_total DECIMAL(18,4) NOT NULL, FOREIGN KEY (order_id) REFERENCES sales_orders(id), FOREIGN KEY (product_id) REFERENCES products(id));
CREATE TABLE audit_events (id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT, actor VARCHAR(200), action VARCHAR(100) NOT NULL, entity_type VARCHAR(100) NOT NULL, entity_id VARCHAR(100), old_value JSON, new_value JSON, correlation_id VARCHAR(100), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX ix_audit_tenant_time (tenant_id, created_at));
CREATE TABLE crm_contacts (id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, customer_id BIGINT NOT NULL, first_name VARCHAR(100) NOT NULL, last_name VARCHAR(100), job_title VARCHAR(120), email VARCHAR(255), phone VARCHAR(40), mobile VARCHAR(40), primary_contact BOOLEAN NOT NULL DEFAULT FALSE, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, INDEX ix_contact_customer (tenant_id,customer_id), FOREIGN KEY (tenant_id) REFERENCES tenants(id));
CREATE TABLE crm_leads (id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, lead_number VARCHAR(40) NOT NULL, name VARCHAR(200) NOT NULL, email VARCHAR(255), phone VARCHAR(40), company_name VARCHAR(200), source VARCHAR(40) NOT NULL DEFAULT 'OTHER', status VARCHAR(40) NOT NULL DEFAULT 'NEW', rating VARCHAR(40) NOT NULL DEFAULT 'WARM', estimated_value DECIMAL(18,4), assigned_to VARCHAR(200), expected_close_date DATE, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, UNIQUE KEY uk_lead_tenant_number (tenant_id,lead_number), INDEX ix_lead_tenant_status (tenant_id,status), FOREIGN KEY (tenant_id) REFERENCES tenants(id));
CREATE TABLE crm_opportunities (id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, opportunity_number VARCHAR(40) NOT NULL, name VARCHAR(200) NOT NULL, customer_id BIGINT, lead_id BIGINT, stage VARCHAR(40) NOT NULL DEFAULT 'QUALIFICATION', amount DECIMAL(18,4), probability DECIMAL(5,2) DEFAULT 0, expected_close_date DATE, assigned_to VARCHAR(200), description VARCHAR(1000), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, UNIQUE KEY uk_opp_tenant_number (tenant_id,opportunity_number), INDEX ix_opp_tenant_stage (tenant_id,stage), FOREIGN KEY (tenant_id) REFERENCES tenants(id), FOREIGN KEY (lead_id) REFERENCES crm_leads(id));
CREATE TABLE crm_activities (id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, customer_id BIGINT, lead_id BIGINT, opportunity_id BIGINT, type VARCHAR(30) NOT NULL DEFAULT 'NOTE', subject VARCHAR(200) NOT NULL, description VARCHAR(2000), due_at TIMESTAMP NULL, completed_at TIMESTAMP NULL, assigned_to VARCHAR(200), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX ix_activity_customer (tenant_id,customer_id,created_at), FOREIGN KEY (tenant_id) REFERENCES tenants(id), FOREIGN KEY (customer_id) REFERENCES customers(id), FOREIGN KEY (lead_id) REFERENCES crm_leads(id), FOREIGN KEY (opportunity_id) REFERENCES crm_opportunities(id));
INSERT INTO tenants(code,name,status) VALUES ('DEMO','Sivion Demo Tenant','ACTIVE') ON DUPLICATE KEY UPDATE name=VALUES(name);
INSERT INTO warehouses(tenant_id,code,name,status) SELECT id,'MAIN','Main Warehouse','ACTIVE' FROM tenants WHERE code='DEMO' AND NOT EXISTS (SELECT 1 FROM warehouses WHERE code='MAIN');
INSERT INTO customers(tenant_id,code,name,email,phone,status) SELECT id,'ACME-001','Acme Industries','sales@acme.example','+91 90000 00001','ACTIVE' FROM tenants WHERE code='DEMO' AND NOT EXISTS (SELECT 1 FROM customers WHERE code='ACME-001');
INSERT INTO customers(tenant_id,code,name,email,phone,status) SELECT id,'GLOBEX-001','Globex Corporation','contact@globex.example','+91 90000 00002','ACTIVE' FROM tenants WHERE code='DEMO' AND NOT EXISTS (SELECT 1 FROM customers WHERE code='GLOBEX-001');

-- HR staff accounts, organization and payroll
CREATE TABLE IF NOT EXISTS hr_departments (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, code VARCHAR(40) NOT NULL, name VARCHAR(160) NOT NULL,
 parent_id BIGINT NULL, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_hr_dept_tenant_code (tenant_id,code), FOREIGN KEY (tenant_id) REFERENCES tenants(id),
 FOREIGN KEY (parent_id) REFERENCES hr_departments(id)
);
CREATE TABLE IF NOT EXISTS hr_designations (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, code VARCHAR(40) NOT NULL, name VARCHAR(160) NOT NULL,
 description VARCHAR(500), department_id BIGINT NULL, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_hr_designation_tenant_code (tenant_id,code), FOREIGN KEY (tenant_id) REFERENCES tenants(id),
 FOREIGN KEY (department_id) REFERENCES hr_departments(id)
);
CREATE TABLE IF NOT EXISTS hr_employees (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, employee_number VARCHAR(40) NOT NULL,
 first_name VARCHAR(100) NOT NULL, last_name VARCHAR(100) NOT NULL, email VARCHAR(180) NOT NULL, phone VARCHAR(80),
 department_id BIGINT NULL, designation_id BIGINT NULL, designation VARCHAR(120), manager_id BIGINT NULL, joining_date DATE NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', keycloak_user_id VARCHAR(80), username VARCHAR(80),
 role VARCHAR(40) NOT NULL DEFAULT 'EMPLOYEE', account_enabled BOOLEAN NOT NULL DEFAULT TRUE,
 UNIQUE KEY uk_hr_employee_tenant_number (tenant_id,employee_number), FOREIGN KEY (tenant_id) REFERENCES tenants(id),
 FOREIGN KEY (department_id) REFERENCES hr_departments(id), FOREIGN KEY (designation_id) REFERENCES hr_designations(id)
);
CREATE TABLE IF NOT EXISTS hr_attendance (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, employee_id BIGINT NOT NULL, attendance_date DATE NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'PRESENT', check_in DATETIME NULL, check_out DATETIME NULL,
 UNIQUE KEY uk_hr_attendance_employee_day (tenant_id,employee_id,attendance_date),
 FOREIGN KEY (tenant_id) REFERENCES tenants(id), FOREIGN KEY (employee_id) REFERENCES hr_employees(id)
);
CREATE TABLE IF NOT EXISTS hr_leave_requests (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, employee_id BIGINT NOT NULL, leave_type VARCHAR(40) NOT NULL,
 start_date DATE NOT NULL, end_date DATE NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'PENDING', reason VARCHAR(500), approved_by BIGINT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (tenant_id) REFERENCES tenants(id),
 FOREIGN KEY (employee_id) REFERENCES hr_employees(id)
);
CREATE TABLE IF NOT EXISTS hr_goals (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, employee_id BIGINT NOT NULL, title VARCHAR(200) NOT NULL,
 description VARCHAR(1000), target_value DOUBLE NULL, current_value DOUBLE NOT NULL DEFAULT 0, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
 due_date DATE NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (tenant_id) REFERENCES tenants(id),
 FOREIGN KEY (employee_id) REFERENCES hr_employees(id)
);
CREATE TABLE IF NOT EXISTS hr_payslips (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, tenant_id BIGINT NOT NULL, employee_id BIGINT NOT NULL, period_start DATE NOT NULL,
 period_end DATE NOT NULL, gross_pay DECIMAL(18,2) NOT NULL DEFAULT 0, deductions DECIMAL(18,2) NOT NULL DEFAULT 0,
 net_pay DECIMAL(18,2) NOT NULL DEFAULT 0, status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', notes VARCHAR(1000),
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 UNIQUE KEY uk_hr_payslip_employee_period (tenant_id,employee_id,period_start,period_end),
 FOREIGN KEY (tenant_id) REFERENCES tenants(id), FOREIGN KEY (employee_id) REFERENCES hr_employees(id)
);
