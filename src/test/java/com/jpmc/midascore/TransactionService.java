package com.jpmc.midascore;

import com.jpmc.midascore.entity.UserRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class TransactionService {

    private final UserRecordRepository userRecordRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    public TransactionService(UserRecordRepository userRecordRepository,
                              TransactionRecordRepository transactionRecordRepository) {
        this.userRecordRepository = userRecordRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @Transactional
    public void processTransaction(Transaction tx) {

        Long senderId = tx.getSenderId();
        Long recipientId = tx.getRecipientId();
        double amountDouble = tx.getAmount();  // adjust if your Transaction uses Float/BigDecimal

        if (amountDouble <= 0) {
            return; // invalid amount
        }

        Optional<UserRecord> senderOpt = userRecordRepository.findById(senderId);
        Optional<UserRecord> recipientOpt = userRecordRepository.findById(recipientId);

        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            return; // invalid user IDs
        }

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();

        // ⚠ Adjust these types if balance in UserRecord is Float/Double
        double senderBalance = sender.getBalance();
        if (senderBalance < amountDouble) {
            // not enough balance
            return;
        }

        // Update balances
        sender.setBalance(senderBalance - amountDouble);
        recipient.setBalance(recipient.getBalance() + amountDouble);

        // Save transaction record (store amount as BigDecimal with 2 decimals)
        BigDecimal amount = BigDecimal.valueOf(amountDouble);
        TransactionRecord record = new TransactionRecord(sender, recipient, amount);
        transactionRecordRepository.save(record);

        // Because of @Transactional and JPA dirty checking,
        // updated UserRecord balances will also be flushed to the DB.
    }
}
