package com.pocas.repo;

import com.pocas.entity.Account;
import com.pocas.entity.DailyCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyCollectionRepository extends JpaRepository<DailyCollection, Long> {

    List<DailyCollection> findByAccount(Account account);

    List<DailyCollection> findByAccountAndMonthAndYear(Account account, Integer month, Integer year);

    List<DailyCollection> findByCollectionDateBetween(LocalDate start, LocalDate end);

    @Query(value = "SELECT SUM(d.collected_amount) " +
            "FROM daily_collections d " +
            "WHERE d.account_id = :accountId " +
            "AND d.month = :month " +
            "AND d.year = :year", nativeQuery = true)
    BigDecimal sumMonthlyCollection(@Param("accountId") Long accountId,
                               @Param("month") Integer month,
                               @Param("year") Integer year);
}