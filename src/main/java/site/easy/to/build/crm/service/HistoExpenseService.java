package site.easy.to.build.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.repository.HistoExpenseRepository;

@Service
@RequiredArgsConstructor
public class HistoExpenseService {

    private final HistoExpenseRepository histoExpenseRepository;

    public HistoExpense save(HistoExpense histoExpense) {
        return histoExpenseRepository.save(histoExpense);
    }
}
