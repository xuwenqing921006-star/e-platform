create table if not exists cb_content (
    id bigint not null primary key,
    title varchar(200) not null,
    category varchar(32) not null,
    scope varchar(32) not null,
    county_code varchar(32),
    office_code varchar(64) not null,
    office_name varchar(100) not null,
    rich_text_html clob not null,
    published_at timestamp not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create index if not exists idx_cb_content_category_scope
    on cb_content (category, scope, county_code);

create index if not exists idx_cb_content_published_at
    on cb_content (published_at);

create table if not exists cb_attachment (
    id bigint not null primary key,
    content_id bigint,
    file_name varchar(200) not null,
    file_type varchar(16) not null,
    file_size bigint not null,
    storage_path varchar(500) not null,
    created_at timestamp not null
);

create index if not exists idx_cb_attachment_content_id
    on cb_attachment (content_id);

create table if not exists cb_financial_product (
    id bigint not null primary key,
    bank_code varchar(64) not null,
    bank_name varchar(100) not null,
    product_name varchar(150) not null,
    product_type varchar(32) not null,
    admission_conditions clob not null,
    product_intro clob not null,
    business_manager varchar(500) not null,
    contact_info varchar(500) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create index if not exists idx_cb_product_bank_type
    on cb_financial_product (bank_code, product_type);

create table if not exists cb_account_extension (
    id bigint not null primary key,
    user_id bigint not null,
    role varchar(32) not null,
    office_code varchar(64),
    office_name varchar(100),
    enabled boolean not null default true,
    created_at timestamp not null,
    updated_at timestamp not null
);

create unique index if not exists uk_cb_account_extension_user_id
    on cb_account_extension (user_id);
