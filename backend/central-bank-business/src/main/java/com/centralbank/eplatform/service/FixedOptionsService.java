package com.centralbank.eplatform.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.centralbank.eplatform.dto.OptionItem;
import com.centralbank.eplatform.dto.OptionsResponse;

@Service
public class FixedOptionsService
{
    private static final List<OptionItem> CONTENT_CATEGORIES = List.of(
            OptionItem.of("SERVICE_GUIDE", "服务指引"),
            OptionItem.of("POLICY_PROMOTION", "政策宣传"));

    private static final List<OptionItem> PRODUCT_TYPES = List.of(
            OptionItem.of("AGRICULTURAL", "涉农产品"),
            OptionItem.of("SMALL_MICRO", "小微产品"));

    private static final List<OptionItem> OFFICES = List.of(
            OptionItem.of("MONETARY_CREDIT", "货币信贷政策管理科"),
            OptionItem.of("MACRO_PRUDENTIAL", "宏观审慎与金融市场管理科"),
            OptionItem.of("FINANCIAL_STABILITY", "金融稳定科"),
            OptionItem.of("STATISTICS_RESEARCH", "统计研究科"),
            OptionItem.of("PAYMENT_SETTLEMENT", "支付结算科"),
            OptionItem.of("CURRENCY_GOLD_SILVER", "货币金银科"),
            OptionItem.of("TREASURY", "国库科"),
            OptionItem.of("CREDIT_REPORT", "征信管理科"),
            OptionItem.of("ANTI_MONEY_LAUNDERING", "反洗钱科"),
            OptionItem.of("FOREIGN_EXCHANGE", "外汇管理科"),
            OptionItem.countyOffice("ZHAOZHOU", "肇州县", "ZHAOZHOU"),
            OptionItem.countyOffice("ZHAOYUAN", "肇源县", "ZHAOYUAN"),
            OptionItem.countyOffice("LINDIAN", "林甸县", "LINDIAN"),
            OptionItem.countyOffice("DUMENG", "杜蒙县", "DUMENG"));

    private static final List<OptionItem> BANKS = List.of(
            OptionItem.of("ADBC_DAQING", "农发行大庆市分行"),
            OptionItem.of("ICBC", "中国工商银行大庆分行"),
            OptionItem.of("ABC", "农业银行大庆分行"),
            OptionItem.of("BOC", "中国银行大庆分行"),
            OptionItem.of("CCB", "建设银行大庆分行"),
            OptionItem.of("BOCOM_DAQING", "交通银行大庆分行"),
            OptionItem.of("CGB", "广发银行大庆分行"),
            OptionItem.of("CIB", "兴业银行大庆分行"),
            OptionItem.of("CMB", "招商银行大庆分行"),
            OptionItem.of("SPDB_DAQING", "浦发银行大庆分行"),
            OptionItem.of("CMBC_DAQING", "中国民生银行大庆分行"),
            OptionItem.of("CITIC", "中信银行大庆分行"),
            OptionItem.of("HARBIN_BANK", "哈尔滨银行大庆分行"),
            OptionItem.of("KUNLUN_BANK", "昆仑银行大庆分行"),
            OptionItem.of("LONGJIANG_BANK", "龙江银行大庆分行"),
            OptionItem.of("CEB", "光大银行大庆分行"));

    public OptionsResponse getOptions()
    {
        return new OptionsResponse(CONTENT_CATEGORIES, PRODUCT_TYPES, OFFICES, BANKS);
    }

    public Optional<OptionItem> findOffice(String officeCode)
    {
        return OFFICES.stream()
                .filter(office -> office.value().equals(officeCode))
                .findFirst();
    }

    public Optional<OptionItem> findBank(String bankCode)
    {
        return BANKS.stream()
                .filter(bank -> bank.value().equals(bankCode))
                .findFirst();
    }

    public Optional<OptionItem> findBankByValueOrLabel(String bankValueOrLabel)
    {
        String normalized = normalize(bankValueOrLabel);
        return BANKS.stream()
                .filter(bank -> normalize(bank.value()).equals(normalized)
                        || normalize(bank.label()).equals(normalized)
                        || normalize(bank.label()).equals(normalized + "大庆分行")
                        || (normalized.startsWith("中国")
                                && (normalize(bank.label()).equals(normalized.substring("中国".length()))
                                        || normalize(bank.label()).equals(
                                                normalized.substring("中国".length()) + "大庆分行"))))
                .findFirst();
    }

    public Optional<OptionItem> findProductType(String productType)
    {
        return PRODUCT_TYPES.stream()
                .filter(type -> type.value().equals(productType))
                .findFirst();
    }

    public Optional<OptionItem> findProductTypeByValueOrLabel(String productTypeValueOrLabel)
    {
        String normalized = normalize(productTypeValueOrLabel);
        if ("涉农信贷".equals(normalized))
        {
            normalized = "涉农产品";
        }
        if ("小微信贷".equals(normalized))
        {
            normalized = "小微产品";
        }
        final String target = normalized;
        return PRODUCT_TYPES.stream()
                .filter(type -> normalize(type.value()).equals(target) || normalize(type.label()).equals(target))
                .findFirst();
    }

    public boolean isCountyOffice(String officeCode)
    {
        return findOffice(officeCode)
                .map(office -> office.countyCode() != null && !office.countyCode().isBlank())
                .orElse(false);
    }

    private String normalize(String value)
    {
        return value == null ? "" : value.trim();
    }
}
