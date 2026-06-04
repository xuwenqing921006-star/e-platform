SET NAMES utf8mb4;

insert into cb_content (
    id, title, category, scope, county_code, office_code, office_name, rich_text_html,
    published_at, created_at, updated_at
) values (
    101, '[Seed] 肇州县金融服务便民联系指南', 'SERVICE_GUIDE', 'RURAL', 'ZHAOZHOU',
    'ZHAOZHOU', '肇州县', '<h2>办理说明</h2><p>县域服务指引用于验证基础数据层。</p>',
    '2026-05-29 09:30:00', current_timestamp, current_timestamp
) on duplicate key update title = values(title);

update cb_content
set title = '肇州县金融服务便民联系指南',
    rich_text_html = '<h2>办理说明</h2><p>县域服务指引展示县域服务网点、咨询渠道和办理提示。</p>'
where id = 101;

insert into cb_content (
    id, title, category, scope, county_code, office_code, office_name, rich_text_html,
    published_at, created_at, updated_at
) values (
    102, '大庆市征信代理查询网点地址及电话', 'SERVICE_GUIDE', 'FINANCIAL', null,
    'CREDIT_REPORT', '征信管理科', '<h2>办理说明</h2><p>申请人可携带有效身份证件前往就近服务网点办理。</p>',
    '2026-05-30 09:30:00', current_timestamp, current_timestamp
) on duplicate key update title = values(title), rich_text_html = values(rich_text_html);

insert into cb_content (
    id, title, category, scope, county_code, office_code, office_name, rich_text_html,
    published_at, created_at, updated_at
) values (
    103, '大庆市金融支持小微企业政策提示', 'POLICY_PROMOTION', 'FINANCIAL', null,
    'MONETARY_CREDIT', '货币信贷政策管理科', '<h2>政策要点</h2><p>金融机构持续优化小微企业融资服务，提升政策直达效率。</p>',
    '2026-05-31 09:30:00', current_timestamp, current_timestamp
) on duplicate key update title = values(title), rich_text_html = values(rich_text_html);

insert into cb_content (
    id, title, category, scope, county_code, office_code, office_name, rich_text_html,
    published_at, created_at, updated_at
) values (
    104, '肇州县涉农金融服务办理提示', 'SERVICE_GUIDE', 'RURAL', 'ZHAOZHOU',
    'ZHAOZHOU', '肇州县', '<h2>服务提示</h2><p>涉农主体可联系县域服务队获取金融产品和政策咨询。</p>',
    '2026-06-01 09:30:00', current_timestamp, current_timestamp
) on duplicate key update title = values(title), rich_text_html = values(rich_text_html);

insert into cb_attachment (
    id, content_id, file_name, file_type, file_size, storage_path, created_at
) values (
    9001, 101, '县域征信服务网点信息表.xlsx', 'EXCEL', 20480, 'seed/attachments/9001.xlsx', current_timestamp
) on duplicate key update file_name = values(file_name);

insert into cb_attachment (
    id, content_id, file_name, file_type, file_size, storage_path, created_at
) values (
    9002, 101, '个人信用报告查询指引.pdf', 'PDF', 51200, 'seed/attachments/9002.pdf', current_timestamp
) on duplicate key update file_name = values(file_name);

insert into cb_attachment (
    id, content_id, file_name, file_type, file_size, storage_path, created_at
) values (
    9003, 101, '征信业务申请材料清单.docx', 'WORD', 36864, 'seed/attachments/9003.docx', current_timestamp
) on duplicate key update file_name = values(file_name);

insert into cb_attachment (
    id, content_id, file_name, file_type, file_size, storage_path, created_at
) values (
    9004, 101, '补充办理说明.pdf', 'PDF', 10240, 'seed/attachments/9004.pdf', current_timestamp
) on duplicate key update file_name = values(file_name);

insert into cb_financial_product (
    id, bank_code, bank_name, product_name, product_type, admission_conditions,
    product_intro, business_manager, contact_info, created_at, updated_at
) values (
    2001, 'CCB', '建设银行', '善营贷', 'AGRICULTURAL',
    '营业执照满1年、年龄18-65周岁、有地补', '线上申请，最高300万元，期限最长12个月，随借随还到期一次性还本付息，无抵押。', '李孟', '0459-2792057',
    current_timestamp, current_timestamp
) on duplicate key update bank_code = values(bank_code), bank_name = values(bank_name), product_name = values(product_name), product_type = values(product_type), admission_conditions = values(admission_conditions), product_intro = values(product_intro), business_manager = values(business_manager), contact_info = values(contact_info), updated_at = values(updated_at);

insert into cb_financial_product (
    id, bank_code, bank_name, product_name, product_type, admission_conditions,
    product_intro, business_manager, contact_info, created_at, updated_at
) values (
    2002, 'SUNSHINE_AGRICULTURE', '阳光惠农贷', '阳光惠农贷', 'AGRICULTURAL',
    '年龄要求：22周岁以上，贷款到期不超60周岁 经验要求：具备两年以上农业劳动经营 场所要求：拥有真实的农业劳作场所 范围：种植、养殖、农资/农贸销售等涉农项目担保要求：黑龙江省农业融资担保公司提供担保', '身份证、户口、结婚证、银行流水、微信/支付宝消费记录、村级及以上机构确认的土地承包证明、土地承包（流转）合同、农业生产服务合同、近一年度的种植补贴、粮食补贴、土地补贴相关证明、购货记录、售粮记录（不限于银行对账单、补贴证明等）、种植保险单等', '张大勇/孙岩鹏', '13936858177/15604599289',
    current_timestamp, current_timestamp
) on duplicate key update bank_code = values(bank_code), bank_name = values(bank_name), product_name = values(product_name), product_type = values(product_type), admission_conditions = values(admission_conditions), product_intro = values(product_intro), business_manager = values(business_manager), contact_info = values(contact_info), updated_at = values(updated_at);

insert into cb_financial_product (
    id, bank_code, bank_name, product_name, product_type, admission_conditions,
    product_intro, business_manager, contact_info, created_at, updated_at
) values (
    2003, 'CITIC', '中信银行', '粮农贷', 'AGRICULTURAL',
    '北大荒垦区农户，连续经营2年，符合我行线上审批规则', '面向北大荒垦区粮农的纯信用类经营贷，线上申请、自动审批，适配春耕/秋收周期 。', '吕文博', '13091667910',
    current_timestamp, current_timestamp
) on duplicate key update bank_code = values(bank_code), bank_name = values(bank_name), product_name = values(product_name), product_type = values(product_type), admission_conditions = values(admission_conditions), product_intro = values(product_intro), business_manager = values(business_manager), contact_info = values(contact_info), updated_at = values(updated_at);

insert into cb_financial_product (
    id, bank_code, bank_name, product_name, product_type, admission_conditions,
    product_intro, business_manager, contact_info, created_at, updated_at
) values (
    2004, 'KUNLUN_BANK', '昆仑银行', '昆仑E贷', 'AGRICULTURAL',
    '1、借款企业生产经营正常，连续正常经营1年（含1年）以上生产经营稳定； 2、本产品通过大数据中的税务数据分析出企业的实际经营情况。经过我行风控模型的评估，系统会根据模型规则自动为企业匹配贷款额度，且贷款额度上限为人民币500万元（含）； 3、所从事的生产经营活动符合我行信贷投向指引，不属于我行黑名单、反洗钱名单等禁入名单内企业； 4、禁止类行业：房地产业、酒吧服务业、“两高一剩”等行业；', '昆仑E贷业务是我行依托大数据应用技术打造的泛场景化普惠小微类线上贷款产品。以企业工商、税务、司法、发票等行政及司法部门相关数据和我行大数据风险评分模型为主要依据，通过线上申请、人工尽调、自动审批、自助放款的模式向小微企业发放的短期流动资金类贷款业务，以满足其日常经营活动资金需求。线上申请，无担保，随借随还，最高额度300万', '王子慧', '0459-5958201',
    current_timestamp, current_timestamp
) on duplicate key update bank_code = values(bank_code), bank_name = values(bank_name), product_name = values(product_name), product_type = values(product_type), admission_conditions = values(admission_conditions), product_intro = values(product_intro), business_manager = values(business_manager), contact_info = values(contact_info), updated_at = values(updated_at);

insert into cb_financial_product (
    id, bank_code, bank_name, product_name, product_type, admission_conditions,
    product_intro, business_manager, contact_info, created_at, updated_at
) values (
    2005, 'HARBIN_BANK', '哈尔滨银行', '农兴贷', 'AGRICULTURAL',
    '1.夫妻双方的身份证，户口本，结婚证 2.房产证 3.土地承包合同 4.土地经营权证 5.村证明 6.卖粮流水', '年龄要求：18-55周岁，从事种植农户，借款人信用情况良好，当前无逾期，且无重大不良信用及录、未被列入失信被执行人名单；第一还款来源充足且稳定；从事种植的应当有适当规模的土地及相应农机具；贷款利率4%', '孙庆宇（杜蒙支行） 曲椿迪（肇源支行）', '13704805030（杜蒙支行） 13946961777（肇源支行）',
    current_timestamp, current_timestamp
) on duplicate key update bank_code = values(bank_code), bank_name = values(bank_name), product_name = values(product_name), product_type = values(product_type), admission_conditions = values(admission_conditions), product_intro = values(product_intro), business_manager = values(business_manager), contact_info = values(contact_info), updated_at = values(updated_at);

insert into cb_financial_product (
    id, bank_code, bank_name, product_name, product_type, admission_conditions,
    product_intro, business_manager, contact_info, created_at, updated_at
) values (
    2006, 'ICBC', '中国工商银行', '新一代经营快贷', 'SMALL_MICRO',
    '1.工商注册企业或个体户的法定代表人，工商注册正常，且成立1年以上 2.已注册为个人手机银行用户 3.小微企业评级不低于BBB级，个人评级不低于D级或评分满足条件 4.非房地产开发企业及其关系人等行业政策要求。', '开放获客，主动申请，额度共享，30万以下实时授信直接提款，线上办理，随借随还。 1.法定代表人通过个人手机银行发起业务申请，并完成征信、商户收单等多种数据授权，系统自动测算授信额度。 2.对于资质较好的客户，系统会直接生效部分小额额度，客户可直接提款。其余额度通过线下核实确认流程后可生效。 3.额度生效后，可由个人或企业分别通过个手或企手企网等渠道提款，共用授信额度。 最高额度500万元，贷款期限12个月，享受普惠贷款优惠利率。', '于天宝', '0459-6618234',
    current_timestamp, current_timestamp
) on duplicate key update bank_code = values(bank_code), bank_name = values(bank_name), product_name = values(product_name), product_type = values(product_type), admission_conditions = values(admission_conditions), product_intro = values(product_intro), business_manager = values(business_manager), contact_info = values(contact_info), updated_at = values(updated_at);

insert into cb_financial_product (
    id, bank_code, bank_name, product_name, product_type, admission_conditions,
    product_intro, business_manager, contact_info, created_at, updated_at
) values (
    2007, 'ABC', '农业银行', '微捷贷', 'SMALL_MICRO',
    '1、贷款对象。“微捷贷”业务采用共同借款人模式，即由企业及其法定代表人（个人独资企业为其投资人，以下统称法定代表人）作为共同借款人。企业主年满18周岁且不超过65周岁，法定代表人持股比例达到20%以上，非港、澳、台及外籍人士。 2、在我行开立结算账户； 3、生产经营2年以上； 4、最近一次纳税信用等级在B级（含）以上。 5、近两年诚信缴税，无税务部门认定的严重失信情形。 6、近12个月纳税总额在1万元（含）以上； 7、申请贷款时，不存在欠缴的税款； 8、办理业务时，企业在我行无有效客户评级及授信额度；对外负债不超过500万。 9、企业及企业主信用状况正常；近半年征信查询不超过6次。', '申请方式：企业法定代表人使用个人掌银/企业网银发起申请。额度：以全线上方式办理的，额度最高不超过300万元（含）；期限：贷款额度有效期最长不超过1年，单笔贷款期限最短为1天。利率：系统测算确定，年利率3%起。无需抵押担保。', '姜阳（高新区）', '04597665204',
    current_timestamp, current_timestamp
) on duplicate key update bank_code = values(bank_code), bank_name = values(bank_name), product_name = values(product_name), product_type = values(product_type), admission_conditions = values(admission_conditions), product_intro = values(product_intro), business_manager = values(business_manager), contact_info = values(contact_info), updated_at = values(updated_at);

insert into cb_financial_product (
    id, bank_code, bank_name, product_name, product_type, admission_conditions,
    product_intro, business_manager, contact_info, created_at, updated_at
) values (
    2008, 'BOC', '中国银行', '个人经营贷', 'SMALL_MICRO',
    '1.借款人年龄为 18 周岁至 65 周岁（贷款期限与年龄之和 不超过 65 周岁），借款人身体状况良好，具有完全民事行为能 力的自然人。 2.借款人户籍所在地、固定住所或固定经营场所在经办机 构服务辖区内(服务辖区不等同行政区划)。 3.借款人应有一定的从业经验，营业执照满一年。 4.原则上最近两年内无连续三次、累计六次及以上逾期记 录（客户提供非本人恶意拖欠逾期证明，可正常受理）。 5.借款人有稳定的经济收入且第一还款来源充足。 6.贷款用途明确、合法，贷款申请数额、期限和币种合理。 7.借款人符合我行反洗钱及制裁合规相关规定。', '线下申请，申请材料：（一）身份证明材料 a.身份证 b.户口簿 c.婚姻证明（二）经营资格证明材料，营业执照、上下游合同等（三）经营收支流水等。额度最高1000万，最长期限12个月，到期一次性还款，担保方式：信用、抵押、保证等方式', '纪海红', '0459-6386289',
    current_timestamp, current_timestamp
) on duplicate key update bank_code = values(bank_code), bank_name = values(bank_name), product_name = values(product_name), product_type = values(product_type), admission_conditions = values(admission_conditions), product_intro = values(product_intro), business_manager = values(business_manager), contact_info = values(contact_info), updated_at = values(updated_at);

insert into cb_financial_product (
    id, bank_code, bank_name, product_name, product_type, admission_conditions,
    product_intro, business_manager, contact_info, created_at, updated_at
) values (
    2009, 'CCB', '建设银行', '信用快贷', 'SMALL_MICRO',
    '企业主年满18周岁且不超过65周岁，非港、澳、台及外籍人士；信用状况良好；企业成立且实际经营满一年。', '全流程线上申请，纯信用贷款，最高500万元，期限最高三年，还款方式为每月还息到期一次性还本，随借随还或固定期限或按计划还款。', '王瑶', '0459-2792009',
    current_timestamp, current_timestamp
) on duplicate key update bank_code = values(bank_code), bank_name = values(bank_name), product_name = values(product_name), product_type = values(product_type), admission_conditions = values(admission_conditions), product_intro = values(product_intro), business_manager = values(business_manager), contact_info = values(contact_info), updated_at = values(updated_at);

insert into cb_financial_product (
    id, bank_code, bank_name, product_name, product_type, admission_conditions,
    product_intro, business_manager, contact_info, created_at, updated_at
) values (
    2010, 'HARBIN_BANK', '哈尔滨银行', '科新贷', 'SMALL_MICRO',
    '符合信贷政策要求的科技型中小企业、创新型中小企业、高新技术企业、专精特新企业以及其他满足条件的科创型小微企业客户，用于其经营周转、固定资产投资等用途的人民币贷款。', '以信用保证为主、额度大、审批快，我行网点线下申请，最高1000万，最长36个月，利率范围3.0%-4.0%，担保方式为组合类（抵押和信用）。', '郭超', '18945965353',
    current_timestamp, current_timestamp
) on duplicate key update bank_code = values(bank_code), bank_name = values(bank_name), product_name = values(product_name), product_type = values(product_type), admission_conditions = values(admission_conditions), product_intro = values(product_intro), business_manager = values(business_manager), contact_info = values(contact_info), updated_at = values(updated_at);

insert into cb_account_extension (
    id, user_id, role, office_code, office_name, enabled, created_at, updated_at
) values (
    1, 1, 'ADMIN', null, null, 1, current_timestamp, current_timestamp
) on duplicate key update role = values(role);

insert into cb_account_extension (
    id, user_id, role, office_code, office_name, enabled, created_at, updated_at
) values (
    2, 2, 'OFFICE_USER', 'MONETARY_CREDIT', '货币信贷政策管理科', 1, current_timestamp, current_timestamp
) on duplicate key update role = values(role), office_code = values(office_code), office_name = values(office_name);
