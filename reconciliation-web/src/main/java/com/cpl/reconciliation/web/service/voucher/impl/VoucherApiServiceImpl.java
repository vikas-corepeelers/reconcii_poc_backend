//package com.cpl.reconciliation.web.service.voucher.impl;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.core.api.util.AuthUtils;
//import com.cpl.reconciliation.core.enums.*;
//import com.cpl.reconciliation.core.request.*;
//import com.cpl.reconciliation.core.response.voucher.BankingVoucherResponse;
//import com.cpl.reconciliation.core.response.voucher.VoucherDashboardResponse;
//import com.cpl.reconciliation.core.response.voucher.VoucherDashboardTransactionWiseData;
//import com.cpl.reconciliation.domain.dao.BankStatementDao;
//import com.cpl.reconciliation.domain.dao.MPRDao;
//import com.cpl.reconciliation.domain.entity.BankingVoucher;
//import com.cpl.reconciliation.domain.entity.BankingVoucherEntries;
//import com.cpl.reconciliation.domain.repository.BankingVoucherEntriesRepository;
//import com.cpl.reconciliation.domain.repository.BankingVoucherRepository;
//import com.cpl.reconciliation.domain.util.ObjectMapperUtils;
//import com.cpl.reconciliation.tasks.utils.MailService;
//import com.cpl.reconciliation.web.service.util.VoucherCreationUtil;
//import com.cpl.reconciliation.web.service.voucher.VoucherApiService;
//import com.cpl.reconciliation.web.service.voucher.VoucherReportDownloadService;
//import com.cpl.reconciliation.web.service.voucher.impl.reports.ReportFactory;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.modelmapper.ModelMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.core.env.Environment;
//import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
//import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.CollectionUtils;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.LocalDate;
//import java.util.*;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.Executor;
//
//import static com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH;
//import static com.cpl.core.api.constant.Formatter.YYYYMMDD_HHMMSS_DASH;
//
//@Data
//@Slf4j
//@Service
//public class VoucherApiServiceImpl implements VoucherApiService {
//
//    private final ReportFactory reportFactory;
//
//    private final BankingVoucherRepository bankingVoucherRepository;
//    private final BankingVoucherEntriesRepository bankingVoucherEntriesRepository;
//    private final BankStatementDao bankStatementDao;
//    private final MPRDao mprDao;
//    private final NamedParameterJdbcTemplate jdbcTemplate;
//    private final VoucherCreationUtil voucherCreationUtil;
//    private final MailService mailService;
//    private final Environment env;
//    @Autowired
//    @Qualifier(value = "asyncExecutor")
//    protected Executor asyncExecutor;
//    private final ModelMapper modelMapper;
//
//
//    @Override
//    @Transactional
//    public BankingVoucher createVouchers(VoucherRequest request) {
//        List<BankingVoucherEntries> bankingVoucherEntries = new ArrayList<>();
//        if(VoucherType.FINAL.equals(request.getVoucherType())){
//            createBankVouchers(request, bankingVoucherEntries);
//            createIciciRefundVouchers(request, bankingVoucherEntries);
//            createMPRVouchers(request, bankingVoucherEntries);
//        }
//        if(VoucherType.TRANSFER.equals(request.getVoucherType())){
//            createTRFDebitVouchers(request, bankingVoucherEntries);
//            createTRFCreditVouchers(request, bankingVoucherEntries);
//        }
//
//        BankingVoucher bankingVoucher = new BankingVoucher();
//        LocalDate startDate = LocalDate.parse(request.getStartDate(), YYYYMMDD_HHMMSS_DASH);
//        LocalDate endDate = LocalDate.parse(request.getEndDate(), YYYYMMDD_HHMMSS_DASH);
//        bankingVoucher.setStartDate(startDate);
//        bankingVoucher.setEndDate(endDate);
//        bankingVoucher.setApprovalStage(VoucherApprovalStage.CREATED);
//        bankingVoucher.setBank(request.getBank());
//        bankingVoucher.setPaymentType(request.getPaymentType());
//        bankingVoucher.setVoucherType(request.getVoucherType());
//        bankingVoucher = bankingVoucherRepository.save(bankingVoucher);
//        BankingVoucher finalBankingVoucher = bankingVoucher;
//        bankingVoucherEntries.forEach(voucherEntries -> {
//            voucherEntries.setBankingVoucher(finalBankingVoucher);
//        });
//        bankingVoucherEntriesRepository.saveAll(bankingVoucherEntries);
//        return bankingVoucher;
//    }
//
//    @Override
//    public VoucherDashboardResponse getDashboardData(VoucherDashboardRequest request) {
//        VoucherDashboardResponse response = new VoucherDashboardResponse();
//        String sql = "SELECT \n" +
//                "    payment_type,approval_stage,voucher_type,booked,amount\n" +
//                "FROM\n" +
//                "    subway.banking_vouchers\n" +
//                "    where bank=:bank and (start_date between :startDate AND :endDate) OR (end_date between :startDate AND :endDate)\n" +
//                "    group by payment_type,approval_stage,booked";
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("startDate", request.getStartDate());
//        parameters.addValue("endDate", request.getEndDate());
//        parameters.addValue("bank", request.getType());
//        Map<PaymentType, VoucherDashboardTransactionWiseData> paymentTypeMap = new HashMap<>();
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//           PaymentType transactionType = PaymentType.getPaymentType(resultSet.getString("payment_type"));
//            VoucherApprovalStage voucherApprovalStage = VoucherApprovalStage.get(resultSet.getString("approval_stage"));
//            boolean booked = resultSet.getBoolean("booked");
//            double amount = resultSet.getDouble("amount");
//            VoucherDashboardTransactionWiseData data = paymentTypeMap.getOrDefault(transactionType, new VoucherDashboardTransactionWiseData());
//            data.setTotalAmount(data.getTotalAmount() + amount);
//            data.setTransactionType(transactionType);
//            response.setTotalAmount(response.getTotalAmount() + amount);
//            if (booked) {
//                data.setBooked(data.getBooked() + amount);
//                response.setBooked(response.getBooked() + amount);
//            }
//            if (voucherApprovalStage.equals(VoucherApprovalStage.PENDING)) {
//                data.setPending(data.getPending() + amount);
//                response.setPending(response.getPending() + amount);
//            }
//            if (voucherApprovalStage.equals(VoucherApprovalStage.APPROVED)) {
//                data.setApproved(data.getApproved() + amount);
//                response.setApproved(response.getApproved() + amount);
//            }
//            paymentTypeMap.put(transactionType, data);
//            return null;
//        });
//        response.setVoucherDashboardTransactionWiseData(paymentTypeMap.values().stream().toList());
//        return response;
//    }
//
//    @Override
//    public BankingVoucher sendForApproval(SendForApprovalRequest request) {
////        List<BankingVoucherEntries> vouchersEntries = getAllCreatedEntries(request.getRequest());
////        BankingVoucher bankingVoucher = null;
////        if (!CollectionUtils.isEmpty(vouchersEntries)) {
////            bankingVoucher = new BankingVoucher();
////            LocalDate startDate = LocalDate.parse(request.getRequest().getStartDate(), YYYYMMDD_DASH);
////            LocalDate endDate = LocalDate.parse(request.getRequest().getEndDate(), YYYYMMDD_DASH);
////            bankingVoucher.setStartDate(startDate);
////            bankingVoucher.setEndDate(endDate);
////            bankingVoucher.setApprovalStage(VoucherApprovalStage.PENDING);
////            bankingVoucher.setBank(vouchersEntries.get(0).getBank());
////            bankingVoucher.setPaymentType(vouchersEntries.get(0).getPaymentType());
////            double amount = vouchersEntries.stream().map(BankingVoucherEntries::getAmount).mapToDouble(f -> f).sum();
////            bankingVoucher = bankingVoucherRepository.save(bankingVoucher);
////            BankingVoucher finalBankingVoucher = bankingVoucher;
////            vouchersEntries.forEach(voucherEntries -> {
////                voucherEntries.setBankingVoucher(finalBankingVoucher);
////            });
////            bankingVoucherEntriesRepository.saveAll(vouchersEntries);
//////            bankingVoucherEntriesRepository.updateVoucherCreatedFlag(bankingVoucher, bankingVoucher.hashCode());
////        }
////        if (!CollectionUtils.isEmpty(vouchersEntries)) {
////            CompletableFuture.runAsync(() -> {
////                try {
////                    sendApprovalMail(vouchersEntries.get(0).getBank(), vouchersEntries.get(0).getPaymentType(), request.getEmails());
////                } catch (Exception e) {
////                    log.error("Failed to send approval mail notification", e);
////                }
////            },asyncExecutor);
////
////        }
////        return bankingVoucher;
//
//        return null;
//    }
//
//    @Override
//    public List<BankingVoucher> approve(VoucherApproveRequest request) {
//        List<VoucherStatus> voucherStatuses = request.getVoucherStatuses();
//        List<BankingVoucher> updatedBankingVoucher = new ArrayList<>();
//        if (voucherStatuses != null) {
//            voucherStatuses.forEach(voucherStatus -> {
//                Optional<BankingVoucher> bankingVoucherOptional = bankingVoucherRepository.findById(voucherStatus.getVoucherId());
//                if (bankingVoucherOptional.isPresent()) {
//                    BankingVoucher bankingVoucher = bankingVoucherOptional.get();
//                    if (voucherStatus.getVoucherApprovalStage().equals(VoucherStatusRequestEnum.APPROVED)) {
//                        bankingVoucher.setApprovalStage(VoucherApprovalStage.APPROVED);
//                        updatedBankingVoucher.add(bankingVoucher);
//                    } else if (voucherStatus.getVoucherApprovalStage().equals(VoucherStatusRequestEnum.REJECTED)) {
//                        bankingVoucher.setApprovalStage(VoucherApprovalStage.REJECTED);
//                        updatedBankingVoucher.add(bankingVoucher);
//                    }
//                }
//            });
//        }
//        List<BankingVoucher> response = bankingVoucherRepository.saveAll(updatedBankingVoucher);
//        List<BankingVoucher> rejected = response.stream().filter(res -> res.getApprovalStage().equals(VoucherApprovalStage.REJECTED)).toList();
//        List<BankingVoucher> approved = response.stream().filter(res -> res.getApprovalStage().equals(VoucherApprovalStage.APPROVED)).toList();
//        if (!CollectionUtils.isEmpty(approved)) {
//            CompletableFuture.runAsync(() -> {
//                try {
//                    sendApprovedMail(approved, true);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            },asyncExecutor);
//        }
//        if (!CollectionUtils.isEmpty(rejected)) {
//            CompletableFuture.runAsync(() -> {
//                try {
//                    sendApprovedMail(rejected, false);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            },asyncExecutor);
//        }
//
//        return response;
//    }
//
//    private void sendApprovedMail(List<BankingVoucher> bankingVouchers, boolean approved) {
//        String body = getApprovedMailBody(bankingVouchers, approved);
//        Properties props = new Properties();
//        props.put("mail.smtp.auth", env.getProperty("mail.smtp.auth"));
//        props.put("mail.smtp.starttls.enable", env.getProperty("mail.smtp.starttls.enable"));
//        props.put("mail.smtp.host", env.getProperty("mail.smtp.host"));
//        props.put("mail.smtp.port", env.getProperty("mail.smtp.port"));
//
//
//        final String username = env.getProperty("mail.smtp.username");
//        final String senderEmail = env.getProperty("mail.smtp.mail");
//        final String password = env.getProperty("mail.smtp.password");
//
//        String[] recipients = new String[]{"prateet.garg@corepeelers.com", "jatin.singh@corepeelers.com"};
//
//        for (String recipient : recipients) {
//            mailService.sendMail(recipient, approved ? "Voucher Approved" : "Voucher Rejected", senderEmail, username, password, props, body);
//        }
//    }
//
//    @Override
//    public List<BankingVoucherResponse> getVouchers(VoucherRequest request) {
//        LocalDate startDate = LocalDate.parse(request.getStartDate(), YYYYMMDD_HHMMSS_DASH);
//        LocalDate endDate = LocalDate.parse(request.getEndDate(), YYYYMMDD_HHMMSS_DASH);
//        Bank bank = request.getBank();
//        PaymentType paymentType = request.getPaymentType();
//        VoucherType voucherType = request.getVoucherType();
//        List<BankingVoucher> list = bankingVoucherRepository.findByDateBetweenAndBankAndPaymentTypeAndVoucherType(startDate, endDate, bank, paymentType, voucherType);
//        List<BankingVoucherResponse> bankVouchers  = ObjectMapperUtils.mapAll(list, BankingVoucherResponse.class);
//        return bankVouchers;
//    }
//
//    @Override
//    public void editVouchers(Long voucherId, MultipartFile file) {
//        VoucherReportDownloadService voucherService;
//        Optional<BankingVoucher> bankingVoucherOptional = bankingVoucherRepository.findById(voucherId);
//        BankingVoucher bankingVoucher;
//        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
//            if (bankingVoucherOptional.isPresent()){
//                bankingVoucher =  bankingVoucherOptional.get();
//            }
//            else throw new Exception("Voucher does not exists with this id");
//            voucherService = reportFactory.getReportService(bankingVoucher.getBank());
//            voucherService.editVouchers(voucherId,workbook,bankingVoucher);
//        } catch (Exception e) {
//            log.error("Error while while updating Voucher id: {} , Error: {}",voucherId, e);
//            throw new ApiException(e.getMessage());
//        }
//    }
//
//    public boolean rowStartCondition(int count , Row row){
//        if (count ==0 && row.getCell(0)!=null)
//            return true;
//        else return false;
//    }
//
//    private void createIciciRefundVouchers(VoucherRequest request, List<BankingVoucherEntries> bankingVoucherEntries) {
//        Bank bank = Bank.getBank(request.getBank().name());
//
//        if (bank != null && bank.equals(Bank.ICICI)) {
//            // TODO: TID STORE MAPPING PENDING
//        }
//    }
//
//    private void createBankVouchers(VoucherRequest request, List<BankingVoucherEntries> bankingVoucherEntries) {
//        String sql = "SELECT \n" +
//                "    id,value_date, bank, payment_type, deposit_amt as amount,narration,transaction_id \n" +
//                "FROM\n" +
//                "    bank_statements\n" +
//                "WHERE\n" +
//                " value_date between :startDate AND :endDate\n";
//        sql += "AND payment_type LIKE '" + "%" + request.getPaymentType().name() + "%'\n";
//
//        sql += "AND source_bank LIKE '" + "%" + request.getBank().name() + "%'\n";
//
//        //TODO: remove later
//        sql += "AND source_bank in ('ICICI','HDFC','SBI','AMEX')\n";
//
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("startDate", request.getStartDate());
//        parameters.addValue("endDate", request.getEndDate());
//
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//            BankingVoucherEntries bank = new BankingVoucherEntries();
////            VoucherEntryId voucherEntryId = new VoucherEntryId();
////            bank.setVoucherEntryId(voucherEntryId);
//            String payment_type = resultSet.getString("payment_type");
//            bank.setBank(Bank.valueOf(resultSet.getString("bank")));
//            bank.setAmount(resultSet.getDouble("amount"));
//            bank.setDate(LocalDate.parse(resultSet.getString("value_date"), YYYYMMDD_HHMMSS_DASH));
//            bank.setDc(DC.D);
//            bank.setLedger(Ledger.BANK_ACCOUNT);
//            bank.setEntryType(EntryType.BANK);
//            bank.setNarration(voucherCreationUtil.getNarration(bank.getBank(), payment_type, VoucherCreationUtil.NarrationType.BS, bank.getDate()));
//            bank.setStoreCode("D9999");
//            bank.setVersion(voucherCreationUtil.getVersion(bank.getBank(), payment_type, VoucherCreationUtil.NarrationType.BS));
//            bank.setChequeNo(getVoucherCreationUtil().getBankReference(bank.getBank(), payment_type, resultSet));
//            bank.setPaymentType(PaymentType.getPaymentType(payment_type));
//
//            bankingVoucherEntries.add(bank);
//            return null;
//        });
//    }
//
//    private void createTRFDebitVouchers(VoucherRequest request, List<BankingVoucherEntries> bankingVoucherEntries) {
//        String sql = "SELECT \n" +
//                "    id,value_date, bank, payment_type, deposit_amt as amount,withdrawal_amt, narration,transaction_id,account_number \n" +
//                "FROM\n" +
//                "    bank_statements\n" +
//                "WHERE\n" +
//                " deposit_amt=0 AND value_date between :startDate AND :endDate\n";
//        sql += "AND payment_type LIKE '" + "%" + request.getPaymentType().name() + "%'\n";
//
//        sql += "AND source_bank LIKE '" + "%" + request.getBank().name() + "%'\n";
//
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("startDate", request.getStartDate());
//        parameters.addValue("endDate", request.getEndDate());
//
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//            BankingVoucherEntries bank = new BankingVoucherEntries();
//            String payment_type = resultSet.getString("payment_type");
//            String account_number = resultSet.getString("account_number");
//            bank.setBank(Bank.valueOf(resultSet.getString("bank")));
//            bank.setAmount(resultSet.getDouble("withdrawal_amt"));
//            bank.setDate(LocalDate.parse(resultSet.getString("value_date"), YYYYMMDD_HHMMSS_DASH));
//            bank.setDc(DC.D);
//            bank.setLedger(Ledger.BANK_ACCOUNT);
//            bank.setEntryType(EntryType.TRANSFER);
//            bank.setNarration(voucherCreationUtil.getTransferNarration(bank.getBank(), payment_type, VoucherCreationUtil.NarrationType.DEBIT, bank.getDate(),account_number));
//            bank.setStoreCode("D9999");
//            bank.setVersion(voucherCreationUtil.getVersion(bank.getBank(), payment_type, VoucherCreationUtil.NarrationType.DEBIT));
//            bank.setPaymentType(PaymentType.getPaymentType(payment_type));
//
//            bankingVoucherEntries.add(bank);
//            return null;
//        });
//    }
//
//    private void createTRFCreditVouchers(VoucherRequest request, List<BankingVoucherEntries> bankingVoucherEntries) {
//        String sql = "SELECT \n" +
//                "    id,value_date, bank, payment_type, deposit_amt as amount,withdrawal_amt, narration,transaction_id,account_number \n" +
//                "FROM\n" +
//                "    bank_statements\n" +
//                "WHERE\n" +
//                " deposit_amt!=0 AND value_date between :startDate AND :endDate\n";
//        sql += "AND payment_type LIKE '" + "%" + request.getPaymentType().name() + "%'\n";
//
//        sql += "AND source_bank LIKE '" + "%" + request.getBank().name() + "%'\n";
//
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("startDate", request.getStartDate());
//        parameters.addValue("endDate", request.getEndDate());
//
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//            BankingVoucherEntries bank = new BankingVoucherEntries();
//            String payment_type = resultSet.getString("payment_type");
//            String account_number = resultSet.getString("account_number");
//            bank.setBank(Bank.valueOf(resultSet.getString("bank")));
//            bank.setAmount(resultSet.getDouble("amount"));
//            bank.setDate(LocalDate.parse(resultSet.getString("value_date"), YYYYMMDD_HHMMSS_DASH));
//            bank.setDc(DC.C);
//            bank.setLedger(Ledger.BANK_ACCOUNT);
//            bank.setEntryType(EntryType.TRANSFER);
//            bank.setNarration(voucherCreationUtil.getTransferNarration(bank.getBank(), payment_type, VoucherCreationUtil.NarrationType.CREDIT, bank.getDate(),account_number));
//            bank.setStoreCode("D9999");
//            bank.setVersion(voucherCreationUtil.getVersion(bank.getBank(), payment_type, VoucherCreationUtil.NarrationType.CREDIT));
//            bank.setPaymentType(PaymentType.getPaymentType(payment_type));
//            bankingVoucherEntries.add(bank);
//            return null;
//        });
//    }
//    private void createMPRVouchers(VoucherRequest request, List<BankingVoucherEntries> bankingVoucherEntries) {
//        String sql = "SELECT \n" +
//                "    bank,\n" +
//                "    payment_type,\n" +
//                "    date,\n" +
//                "    amount,\n" +
//                "    charges,\n" +
//                "    store_id\n" +
//                "FROM\n" +
//                "    (SELECT \n" +
//                "        mpr.bank,\n" +
//                "        DATE(mpr.settled_date) AS date,\n" +
//                "        mpr.payment_type,\n" +
//                "        mpr.store_id as store_id,\n" +
//                "        COALESCE(SUM(mpr_amount), 0) AS amount,\n" +
//                "        (COALESCE(SUM(commission), 0) + COALESCE(SUM(sb_cess), 0) + COALESCE(SUM(service_tax), 0) + COALESCE(SUM(sgst), 0) + COALESCE(SUM(utgst), 0) + COALESCE(SUM(igst), 0) + COALESCE(SUM(kk_cess), 0) + COALESCE(SUM(gst), 0) + COALESCE(SUM(cgst), 0)) AS charges\n" +
//                "    FROM\n" +
//                "        subway.mpr\n" +
//                "    WHERE\n" +
//                "        settled_date BETWEEN :startDate AND :endDate " +
//                "            AND mpr.tid IS NOT NULL\n" +
//                "            AND mpr.tid != ''\n" ;
//
//
//        sql += "AND payment_type LIKE '" + "%" + request.getPaymentType().name() + "%'\n";
//
//        sql += "AND bank LIKE '" + "%" + request.getBank().name() + "%'\n";
//
//        //TODO: remove later
//        sql += "AND bank in ('ICICI','HDFC','SBI','AMEX')\n";
//
//        sql +=  "    GROUP BY\n" +
//                "        mpr.bank,\n" +
//                "        DATE(mpr.settled_date),\n" +
//                "        mpr.payment_type,\n" +
//                "        mpr.store_id) AS subquery\n";
//
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("startDate", request.getStartDate());
//        parameters.addValue("endDate", request.getEndDate());
//
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//            String payment_type = resultSet.getString("payment_type");
//
//            BankingVoucherEntries mpr = new BankingVoucherEntries();
////            VoucherEntryId voucherEntryId = new VoucherEntryId();
////            mpr.setVoucherEntryId(voucherEntryId);
//            mpr.setBank(Bank.valueOf(resultSet.getString("bank")));
//            mpr.setAmount(resultSet.getDouble("amount"));
//            mpr.setDate(LocalDate.parse(resultSet.getString("date"), YYYYMMDD_DASH));
//            mpr.setDc(DC.C);
//            mpr.setEntryType(EntryType.SALES);
//            mpr.setLedger(Ledger.CUSTOMER);
//            mpr.setNarration(voucherCreationUtil.getNarration(mpr.getBank(), payment_type, VoucherCreationUtil.NarrationType.MPR, mpr.getDate()));
//            mpr.setStoreCode(resultSet.getString("store_id"));
//            mpr.setVersion(voucherCreationUtil.getVersion(mpr.getBank(), payment_type, VoucherCreationUtil.NarrationType.BS));
//            mpr.setPaymentType(PaymentType.getPaymentType(payment_type));
//            bankingVoucherEntries.add(mpr);
//
//
//            if (resultSet.getDouble("charges") != 0) {
//                BankingVoucherEntries charges = new BankingVoucherEntries();
////                VoucherEntryId voucherEntryId1 = new VoucherEntryId();
////                charges.setVoucherEntryId(voucherEntryId1);
//                charges.setBank(Bank.valueOf(resultSet.getString("bank")));
//                charges.setAmount(resultSet.getDouble("charges"));
//                charges.setDate(LocalDate.parse(resultSet.getString("date"), YYYYMMDD_DASH));
//                charges.setDc(DC.D);
//                charges.setEntryType(EntryType.COMMISSION);
//                charges.setLedger(Ledger.GL_ACCOUNT);
//                charges.setNarration(voucherCreationUtil.getNarration(charges.getBank(), payment_type, VoucherCreationUtil.NarrationType.CHARGES, charges.getDate()));
//                charges.setStoreCode(resultSet.getString("store_id"));
//                charges.setPaymentType(PaymentType.getPaymentType(payment_type));
//                charges.setVersion(voucherCreationUtil.getVersion(charges.getBank(), payment_type, VoucherCreationUtil.NarrationType.CHARGES));
//                bankingVoucherEntries.add(charges);
//            }
//            return null;
//        });
//    }
//
//    public XSSFWorkbook getWorkbook(Long voucherId) {
//        BankingVoucher voucher;
//        Bank bank = null;
//        PaymentType paymentType = null;
//        List<BankingVoucherEntries> entries = null;
//        VoucherType voucherType = null;
//        Optional<BankingVoucher> optionalBankingVoucherEntries = bankingVoucherRepository.findById(voucherId);
//        if (optionalBankingVoucherEntries.isPresent()) {
//            voucher = optionalBankingVoucherEntries.get();
//            entries = voucher.getBankingVoucherEntriesList();
//            paymentType = voucher.getPaymentType();
//            bank = voucher.getBank();
//            voucherType = voucher.getVoucherType();
//        }
//
//        VoucherReportDownloadService service = reportFactory.getReportService(bank);
//        if (service != null) return service.downloadReport(entries, paymentType, bank, voucherType);
//        return null;
//    }
//
//
//    private void sendApprovalMail(Bank bank, PaymentType paymentType, List<String> emails) {
//
//        Properties props = new Properties();
//        props.put("mail.smtp.auth", env.getProperty("mail.smtp.auth"));
//        props.put("mail.smtp.starttls.enable", env.getProperty("mail.smtp.starttls.enable"));
//        props.put("mail.smtp.host", env.getProperty("mail.smtp.host"));
//        props.put("mail.smtp.port", env.getProperty("mail.smtp.port"));
//
//
//        final String username = env.getProperty("mail.smtp.username");
//        final String senderEmail = env.getProperty("mail.smtp.mail");
//        final String password = env.getProperty("mail.smtp.password");
//
//        String[][] managers = new String[][]{{"Prateet Garg", "prateet.garg@corepeelers.com"}, {"Jatin Singh", "jatin.singh@corepeelers.com"}};
//
//        for (String[] manager : managers) {
//            String body = getVoucherMailBody(bank, paymentType, manager[0], AuthUtils.authentication().getName());
//            mailService.sendMail(manager[1], "Voucher Approval Request", senderEmail, username, password, props, body);
//        }
//        if (!CollectionUtils.isEmpty(emails)) {
//            for (String email : emails) {
//                String body = getVoucherMailBody(bank, paymentType, null, AuthUtils.authentication().getName());
//                mailService.sendMail(email, "Voucher Approval Request", senderEmail, username, password, props, body);
//            }
//        }
//
//
//    }
//
//    public String getApprovedMailBody(List<BankingVoucher> bankingVouchers, boolean approved) {
//        StringBuilder emailBody = new StringBuilder();
//        emailBody.append("<html><head>");
//        emailBody.append("<style>");
//        emailBody.append("table { border-collapse: collapse; width: 100%; }");
//        emailBody.append(" th, td { text-align: center; border: 1px solid black; padding: 8px;}");
//        emailBody.append("th { background-color: #f2f2f2; }");
//        emailBody.append("</style>");
//        emailBody.append("</head><body>");
//
//        emailBody.append("To Whom It May Concern,<br><br>");
//
//
//        emailBody.append("Finance have ").append(approved ? "approved" : "rejected").append(" following vouchers.").append("<br><br>");
//
//        emailBody.append("<table>");
//        emailBody.append("<tr><th>Bank</th><th>Type</th><th>Amount</th><th>Start Date</th><th>End Date</th></tr>");
//
//        for (BankingVoucher voucherEntry : bankingVouchers) {
//            emailBody.append("<tr>");
//            emailBody.append("<td>").append(voucherEntry.getBank().name()).append("</td>");
//            emailBody.append("<td>").append(voucherEntry.getPaymentType().name()).append("</td>");
//            emailBody.append("<td>").append(voucherEntry.getStartDate().format(Formatter.MMMddyyyy)).append("</td>");
//            emailBody.append("<td>").append(voucherEntry.getEndDate().format(Formatter.MMMddyyyy)).append("</td>");
//            emailBody.append("</tr>");
//        }
//        emailBody.append("</table>");
//
//
//        emailBody.append("<br>Best regards,<br>Sales Reconciliation Team<br><br>");
//
//        emailBody.append("*This is an automated email. Please do not reply to this message. If you have any questions or need assistance, please contact the Reconciliation Team or the associate directly. Thank you.");
//
//        emailBody.append("</body></html>");
//
//
//        return emailBody.toString();
//    }
//
//    public String getVoucherMailBody(Bank bank, PaymentType paymentType, String managerName, String associateName) {
//        StringBuilder emailBody = new StringBuilder();
//        emailBody.append("<html><head>");
//        emailBody.append("<style>");
//        emailBody.append("table { border-collapse: collapse; width: 100%; }");
//        emailBody.append(" th, td { text-align: center; border: 1px solid black; padding: 8px;}");
//        emailBody.append("th { background-color: #f2f2f2; }");
//        emailBody.append("</style>");
//        emailBody.append("</head><body>");
//
//        if (managerName != null) {
//            emailBody.append("Dear ").append(managerName).append(",<br><br>");
//        } else {
//            emailBody.append("To Whom It May Concern,<br><br>");
//        }
//
//        emailBody.append(associateName).append(" has submitted a voucher for ").append(bank.name()).append(" ").append(paymentType.name()).append(" that requires your approval. Kindly review the details and take action at your earliest convenience.<br><br>");
//
//        emailBody.append("<br>You can access and approve the vouchers by logging into the Subway at <a href=\"https://subway.corepeelers.com\">https://subway.corepeelers.com</a>.<br><br>");
//        emailBody.append("Thank you for your prompt attention to this request.<br><br>");
//        emailBody.append("Best regards,<br>Sales Reconciliation Team<br><br>");
//
//        emailBody.append("*This is an automated email. Please do not reply to this message. If you have any questions or need assistance, please contact the Reconciliation Team or the associate directly. Thank you.");
//
//        emailBody.append("</body></html>");
//
//
//        return emailBody.toString();
//    }
//
//}
