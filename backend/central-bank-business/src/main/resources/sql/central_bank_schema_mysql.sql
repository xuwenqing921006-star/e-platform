create table if not exists cb_content (
    id bigint not null primary key,
    title varchar(200) not null,
    category varchar(32) not null,
    scope varchar(32) not null,
    county_code varchar(32),
    office_code varchar(64) not null,
    office_name varchar(100) not null,
    rich_text_html text not null,
    published_at datetime not null,
    created_at datetime not null,
    updated_at datetime not null,
    key idx_cb_content_category_scope (category, scope, county_code),
    key idx_cb_content_published_at (published_at)
);

create table if not exists cb_attachment (
    id bigint not null primary key,
    content_id bigint,
    file_name varchar(200) not null,
    file_type varchar(16) not null,
    file_size bigint not null,
    storage_path varchar(500) not null,
    created_at datetime not null,
    key idx_cb_attachment_content_id (content_id)
);

create table if not exists cb_financial_product (
    id bigint not null primary key,
    bank_code varchar(64) not null,
    bank_name varchar(100) not null,
    product_name varchar(150) not null,
    product_type varchar(32) not null,
    admission_conditions text not null,
    product_intro text not null,
    business_manager varchar(80) not null,
    contact_info varchar(80) not null,
    created_at datetime not null,
    updated_at datetime not null,
    key idx_cb_product_bank_type (bank_code, product_type)
);

create table if not exists cb_account_extension (
    id bigint not null primary key,
    user_id bigint not null,
    role varchar(32) not null,
    office_code varchar(64),
    office_name varchar(100),
    enabled tinyint(1) not null default 1,
    created_at datetime not null,
    updated_at datetime not null,
    unique key uk_cb_account_extension_user_id (user_id)
);
